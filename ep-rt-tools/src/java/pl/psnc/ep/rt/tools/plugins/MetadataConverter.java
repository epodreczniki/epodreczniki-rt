package pl.psnc.ep.rt.tools.plugins;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pl.psnc.dlibra.app.extension.metadata.MetadataFinder;
import pl.psnc.dlibra.app.gui.base.AmbiguousAttributeValueException;
import pl.psnc.dlibra.common.DLObject;
import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet.Values;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;

public class MetadataConverter {

    private static final String CLEANUP_REGEX_ALLOWBREAKS = "[ \\t\\x0B\\f\\r]+";

    private static final String CLEANUP_REGEX_DISALLOWBREAKS = "\\s+";

    private static final int TAB_WIDTH = 2;


    public static List<Map<String, Map<String, List<String>>>> convertToMap(List<AttributeValueSet> avsSets,
            MetadataServer ms)
                    throws IdNotFoundException, RemoteException, DLibraException {
        if (avsSets == null) {
            return null;
        }

        List<Map<String, Map<String, List<String>>>> metadataSets = new ArrayList<Map<String, Map<String, List<String>>>>();
        for (AttributeValueSet avs : avsSets) {
            Map<String, Map<String, List<String>>> metadata = new HashMap<String, Map<String, List<String>>>();

            for (String l : avs.getAvailableLanguages(true)) {
                Map<String, List<String>> rdfToVals = metadata.get(l);
                if (rdfToVals == null) {
                    metadata.put(l, rdfToVals = new HashMap<String, List<String>>());
                }

                for (AttributeId id : avs.getAttributeIds()) {
                    AttributeInfo info = (AttributeInfo) ms.getAttributeManager()
                            .getObjects(new AttributeFilter(id), new OutputFilter(AttributeInfo.class)).getResultInfo();

                    rdfToVals.put(info.getRDFName(),
                        convertToStringList(avs.getAttributeValues(id, l, Values.AllNonUni)));
                }
            }
            metadataSets.add(metadata);
        }

        return metadataSets;
    }


    private static List<String> convertToStringList(Collection<AbstractAttributeValue> values) {
        List<String> result = new ArrayList<String>();
        for (AbstractAttributeValue v : values) {
            result.add(v.getValue());
        }

        return result;
    }


    public static List<AttributeValueSet> convertToAVS(List<Object> metadataIdentifiers,
            List<Map<String, Map<String, List<String>>>> metadataSets, String defaultLanguage, MetadataServer ms)
                    throws IdNotFoundException, RemoteException, DLibraException {
        if (metadataSets == null) {
            return null;
        }

        List<AttributeValueSet> sets = new ArrayList<AttributeValueSet>();

        int index = 0;
        for (Map<String, Map<String, List<String>>> metadata : metadataSets) {
            AttributeValueSet avs = convert(metadata, defaultLanguage, null, false, ms);
            if (metadataIdentifiers != null && metadataIdentifiers.size() > index) {
                Object id = metadataIdentifiers.get(index);
                if (id instanceof ElementId) {
                    avs.setElementId((ElementId) id);
                }
            }
            sets.add(avs);
            index++;
        }
        return sets;
    }


    public static List<AttributeValueSet> convertToAVS(List<Object> metadataIdentifiers,
            List<Map<String, Map<String, List<String>>>> metadataSets, String defaultLanguage,
            Map<String, AttributeId> attMap, boolean allowBreaks, MetadataServer ms)
                    throws IdNotFoundException, RemoteException, DLibraException {
        if (metadataSets == null) {
            return null;
        }

        List<AttributeValueSet> sets = new ArrayList<AttributeValueSet>();

        int index = 0;
        for (Map<String, Map<String, List<String>>> metadata : metadataSets) {
            AttributeValueSet avs = convert(metadata, defaultLanguage, attMap, allowBreaks, ms);
            if (metadataIdentifiers != null && metadataIdentifiers.size() > index) {
                Object id = metadataIdentifiers.get(index);
                if (id instanceof ElementId) {
                    avs.setElementId((ElementId) id);
                }
            }
            sets.add(avs);
            index++;
        }
        return sets;
    }


    private static AttributeValueSet convert(Map<String, Map<String, List<String>>> metadata, String defaultLanguage,
            Map<String, AttributeId> attMap, boolean allowLinebreaks, MetadataServer ms)
                    throws IdNotFoundException, RemoteException, DLibraException {
        AttributeValueSet result = new AttributeValueSet();
        Map<String, AttributeId> RDFToId = attMap == null ? getAttributesMap(ms) : attMap;
        for (Entry<String, Map<String, List<String>>> lang : metadata.entrySet()) {
            String language = MetadataFinder.DEFAULT_LANGUAGE.equals(lang.getKey()) ? defaultLanguage : lang.getKey();
            for (Entry<String, List<String>> rdfNames : lang.getValue().entrySet()) {
                AttributeId id = RDFToId.get(rdfNames.getKey());
                if (id != null) {
                    List<AbstractAttributeValue> attributeValues = convertToAAV(rdfNames.getValue(), id, language,
                        allowLinebreaks, ms);
                    attributeValues.addAll(result.getAttributeValues(id, language, Values.OnlyDirect));
                    result.setDirectAttributeValues(id, language, attributeValues);
                }
            }
        }

        return result;
    }


    public static String cleanValue(String rawValue, boolean allowLineBreaks) {
        String value = rawValue
                .replaceAll(allowLineBreaks ? CLEANUP_REGEX_ALLOWBREAKS : CLEANUP_REGEX_DISALLOWBREAKS, " ").trim();
        return value;
    }


    private static List<AbstractAttributeValue> convertToAAV(List<String> values, AttributeId id, String lang,
            boolean allowLineBreaks, MetadataServer ms) {
        LinkedHashSet<String> uniqueValues = new LinkedHashSet<String>(values);
        List<AbstractAttributeValue> result = new ArrayList<AbstractAttributeValue>();
        for (String value : uniqueValues) {
            value = cleanValue(value, allowLineBreaks);
            result.add(createDummyAttributeValue(value, id, lang));
        }
        return result;
    }


    private static AttributeValue createDummyAttributeValue(String value, AttributeId id, String lang) {
        AttributeValue aValue = new AttributeValue(null, id, null);
        aValue.setValue(value);
        aValue.setLanguageName(lang);
        return aValue;
    }


    private static Map<String, AttributeId> getAttributesMap(MetadataServer ms)
            throws RemoteException, IdNotFoundException, DLibraException {
        Map<String, AttributeId> attMap = new HashMap<String, AttributeId>();
        Collection<Info> infos = ms.getAttributeManager()
                .getObjects((AttributeFilter) new AttributeFilter(null, null).setRecursive(true),
                    new OutputFilter(AttributeInfo.class))
                .getResultInfos();
        for (Info info : infos) {
            attMap.put(((AttributeInfo) info).getRDFName(), ((AttributeInfo) info).getId());
        }
        return attMap;
    }


    public static String convertToHTML(AttributeValueSet set, String language, boolean includeInherited,
            MetadataServer ms)
                    throws UnsupportedOperationException, IdNotFoundException, RemoteException, DLibraException {
        boolean hasValues = false;

        for (AbstractAttributeValue aav : set.getValues()) {
            if (language.equalsIgnoreCase(aav.getLanguageName())) {
                hasValues = true;
                break;
            }
        }

        if (!hasValues) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<HTML><HEAD>" + "<style type=\"text/css\"> " + "\n th {font-family: courier}"
                + "\n td {font-family: courier}" + "\n caption {font-family: courier} </style>"
                + "</HEAD><TABLE border=1>");
        buffer.append("<TR><TH>" + "Attribute name" + "</TH><TH>" + "Attribute value" + "</TH></TR>");
        buildTableBody(buffer, 0, ms.getAttributeManager()
                .getObjects(new AttributeFilter(null, null), new OutputFilter(AttributeInfo.class)).getResultInfos(),
            set, language, includeInherited, ms);
        buffer.append("</TABLE></HTML>");

        return buffer.toString();
    }


    private static boolean buildTableBody(StringBuffer buffer, int i, Collection<Info> resultInfos,
            AttributeValueSet avs, String contentLanguage, boolean includeInh, MetadataServer ms)
                    throws UnsupportedOperationException, IdNotFoundException, RemoteException, DLibraException {
        boolean hasAnyValues = false;

        for (Info info : resultInfos) {
            AttributeInfo aInfo = (AttributeInfo) info;
            aInfo.setLanguage("pl");

            StringBuffer childBuffer = new StringBuffer();
            boolean childHasValues = buildTableBody(childBuffer, i + TAB_WIDTH,
                ms.getAttributeManager()
                        .getObjects(new AttributeFilter(null, aInfo.getId()), new OutputFilter(AttributeInfo.class))
                        .getResultInfos(),
                avs, contentLanguage, includeInh, ms);

            List<AbstractAttributeValue> attributeValues = avs.getAttributeValues(aInfo.getId(), contentLanguage,
                includeInh ? Values.AllNonUni : Values.OnlyDirect);

            if (attributeValues.size() > 0) {
                hasAnyValues = true;
            } else if (!childHasValues) {
                continue;
            }

            if (attributeValues.size() > 1) {
                buffer.append("<TR><TD valign=\"top\" rowspan=\"" + (attributeValues.size() + 1) + "\"><PRE>");
                addSpaces(buffer, i);
                buffer.append("* " + aInfo.getLabel() + "</PRE>");
                buffer.append("</TD></TR>");

                for (AbstractAttributeValue v : attributeValues) {
                    v.setLanguageName(contentLanguage);
                    buffer.append("<TR><TD>");
                    buffer.append(v.getValue());
                    buffer.append("</TD></TR>");
                }
            } else {
                buffer.append("<TR><TD valign=\"top\"><PRE>");
                addSpaces(buffer, i);
                buffer.append("* " + aInfo.getLabel() + "</PRE>");
                buffer.append("</TD><TD>");
                for (AbstractAttributeValue v : attributeValues) {
                    v.setLanguageName(contentLanguage);
                    buffer.append(v.getValue());
                }

                buffer.append("</TD></TR>");
            }

            buffer.append(childBuffer);
        }

        return hasAnyValues;
    }


    private static void addSpaces(StringBuffer buffer, int i) {
        for (int j = 0; j < i; j++) {
            buffer.append("&nbsp;");
        }
    }


    public static AttributeValueSet updateAVS(AttributeValueSet attValueSet, boolean silent, MetadataServer ms)
            throws RemoteException, IdNotFoundException, DuplicatedValueException, AccessDeniedException,
            AmbiguousAttributeValueException, DLibraException {
        if (attValueSet == null) {
            return null;
        }
        List<String> langs = attValueSet.getAvailableLanguages(true);

        for (String language : langs) {
            for (AttributeId currentAttributeId : attValueSet.getAttributeIds()) {
                Collection<AbstractAttributeValue> oldValues = attValueSet.getAttributeValues(currentAttributeId,
                    language, Values.OnlyDirect);
                List<AbstractAttributeValue> newValues = new ArrayList<AbstractAttributeValue>();
                if (oldValues != null && oldValues.size() > 0) {
                    for (AbstractAttributeValue element : oldValues) {
                        if (element.getId() == null) {
                            try {
                                newValues.add(addAttributeValue(currentAttributeId, element, language, ms));
                            } catch (RemoteException e) {
                                if (!silent) {
                                    throw e;
                                }
                            } catch (AmbiguousAttributeValueException e) {
                                if (!silent) {
                                    throw e;
                                }
                            }
                        } else {
                            newValues.add(element);
                        }
                    }
                    attValueSet.setDirectAttributeValues(currentAttributeId, language, newValues);
                }
            }
        }
        return attValueSet;
    }


    private static AttributeValue addAttributeValue(AttributeId currentAttributeId, AbstractAttributeValue value,
            String currentLanguageName, MetadataServer ms)
                    throws AmbiguousAttributeValueException, RemoteException, IdNotFoundException,
                    DuplicatedValueException, AccessDeniedException, DLibraException {
        AttributeValueManager attributeValueManager = ms.getAttributeValueManager();
        List<DLObject> groupsWithValue = new ArrayList<DLObject>(
                attributeValueManager
                        .getObjects(new AttributeValueFilter(currentAttributeId).setValue(value.getValue(), true)
                                .setLanguageName(currentLanguageName),
                            new OutputFilter(AttributeValue.class))
                        .getResults());
        if (groupsWithValue.isEmpty()) {
            AttributeValue newVal = new AttributeValue(null, currentAttributeId, null);
            newVal.setLanguageName(currentLanguageName);
            newVal.setValue(value.getValue());
            AttributeValueId newId = attributeValueManager.addAttributeValue(newVal);
            newVal.setId(newId);
            return newVal;
        } else if (groupsWithValue.size() == 1) {
            Object group = groupsWithValue.get(0);
            if (group != null) {
                Collection<DLObject> groupValues = attributeValueManager
                        .getObjects(new AttributeValueFilter(null, ((AttributeValue) group).getId()),
                            new OutputFilter(AttributeValue.class))
                        .getResults();
                AttributeValue foundValue = null;
                for (Iterator<DLObject> iter = groupValues.iterator(); iter.hasNext();) {
                    AttributeValue element = (AttributeValue) iter.next();
                    element.setLanguageName(currentLanguageName);
                    if (element.getValue().equals(value.getValue())) {
                        foundValue = element;
                        break;
                    }
                }
                if (foundValue != null) {
                    return foundValue;
                }
                throw new IdNotFoundException(null);
            }
            return null;
        } else {
            @SuppressWarnings("unchecked")
            List<AttributeValue> unchecked = (List<AttributeValue>) (Object) groupsWithValue;
            throw new AmbiguousAttributeValueException(value.getValue(), unchecked);
        }
    }
}
