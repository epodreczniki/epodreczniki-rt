package pl.psnc.ep.rt.web;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.web.common.exceptions.PublicIdentityProviderException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.dlibra.web.fw.util.servlet.RequestWrapperFactory;
import pl.psnc.dlibra.web.fw.util.servlet.ServletRequestWrapper;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class Util {

    public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private static ConcurrentHashMap<String, AttributeId> attributesCache = new ConcurrentHashMap<String, AttributeId>();

    private static ConcurrentHashMap<AttributeId, String> attributeNamesCache = new ConcurrentHashMap<AttributeId, String>();


    public static AttributeId getAttributeId(String rdfName, MetadataServer ms)
            throws RemoteException, DLibraException {
        AttributeId attributeId = attributesCache.get(rdfName);
        if (attributeId == null) {
            AttributeManager am = ms.getAttributeManager();
            attributeId = (AttributeId) am
                    .getObjects(new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList(rdfName)),
                        new OutputFilter(AttributeId.class))
                    .getResultId();
            attributesCache.put(rdfName, attributeId);
        }
        return attributeId;
    }


    public static String getAttributeName(AttributeId id, MetadataServer ms)
            throws RemoteException, DLibraException {
        String name = attributeNamesCache.get(id);
        if (name == null) {
            AttributeManager am = ms.getAttributeManager();
            AttributeInfo attributeInfo = (AttributeInfo) am
                    .getObjects(new AttributeFilter(id), new OutputFilter(AttributeInfo.class)).getResultInfo();
            name = attributeInfo.getRDFName();
        }
        return name;
    }


    public static Map<WOMIFormat, String> loadWOMIData(EditionId womiId, MetadataServer ms, ContentServer cs)
            throws DLibraException, IOException {
        return loadWOMIData(womiId, ms, cs, false);
    }


    public static Map<WOMIFormat, String> loadWOMIData(EditionId womiId, MetadataServer ms, ContentServer cs,
            boolean includeStaticAlternative)
                    throws DLibraException, IOException {
        if (ms == null)
            ms = ServicesManager.getInstance().getMetadataServer();
        if (cs == null)
            cs = ServicesManager.getInstance().getContetServer();

        VersionId mainId = (VersionId) ms.getPublicationManager()
                .getObjects(new EditionFilter(womiId).setMainVersion(true), new OutputFilter(VersionId.class))
                .getResultId();
        InputStream fis = cs.getVersionInputStream(mainId);
        try {
            return WOMIXMLHandler.loadMultiFormatXML(fis, includeStaticAlternative);
        } finally {
            fis.close();
            cs.releaseElement(mainId);
        }
    }


    public static EditionId getRequestedId(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            ServletRequestWrapper reqWr = RequestWrapperFactory.getInstance(req, resp);
            String pathInfo = reqWr.getPathInfo();
            if (StringUtils.isEmpty(pathInfo)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            String[] desc = pathInfo.split("/");
            if (desc.length != 2) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproperly formed request.");
                return null;
            }
            try {
                Long id = Long.valueOf(desc[1]);
                return new EditionId(id);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproperly formed request.");
                return null;
            }
        } catch (PublicIdentityProviderException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return null;
        }
    }


    public static PublicationId findVersion(PublicationId rootId, int version)
            throws RemoteException, DLibraException {
        if (version < 1)
            return null;

        MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
        AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(rootId,
            AttributeValue.AV_ASSOC_DIRECT);
        AttributeId rootAttributeId = getAttributeId(Versioning.ROOT_ID_RDF_NAME, ms);
        List<AbstractAttributeValue> rootRootValues = getValues(avs, rootAttributeId);
        if (!rootRootValues.isEmpty())
            return null;

        if (version == 1)
            return rootId;

        AttributeValueManager avm = ms.getAttributeValueManager();
        AttributeValueId rootIdAttVal = (AttributeValueId) avm
                .getObjects(new AttributeValueFilter(rootAttributeId).setValue(rootId.toString(), true),
                    new OutputFilter(AttributeValueId.class))
                .getResultId();
        if (rootIdAttVal == null)
            return null;

        List<Publication> allVersions = Versioning.findAllVersions(rootIdAttVal, ms);
        if (allVersions.size() > version - 2)
            return allVersions.get(version - 2).getId();

        return null;
    }


    public static List<AbstractAttributeValue> getValues(AttributeValueSet avs, MetadataServer ms, String rdfName)
            throws RemoteException, DLibraException {
        AttributeId attributeId = getAttributeId(rdfName, ms);
        List<AbstractAttributeValue> titles = getValues(avs, attributeId);
        return titles;
    }


    public static List<AbstractAttributeValue> getValues(AttributeValueSet avs, AttributeId attributeId) {
        return avs.getAttributeValues(attributeId, "pl", AttributeValueSet.Values.OnlyDirect);
    }
}
