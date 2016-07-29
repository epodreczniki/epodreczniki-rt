package pl.psnc.ep.rt.listeners;

import java.awt.Window;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.extension.eventlistener.AppEvent;
import pl.psnc.dlibra.app.extension.eventlistener.AppEventType;
import pl.psnc.dlibra.app.extension.eventlistener.EventListener;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.Language;
import pl.psnc.dlibra.metadata.LanguageId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.ElementMetadataManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet.Values;
import pl.psnc.dlibra.mgmt.AbstractServiceResolver;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;

public abstract class AbstractMetadataListener implements EventListener {

    public static class MetadataException extends Exception {

        public final String detail;


        public MetadataException(String message, String rootId) {
            super(message);
            this.detail = rootId;
        }
    }


    private final static String BUNDLE_PATH = "GUI";

    protected ResourceBundle textsBundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault());;

    protected String defaultLanguage;

    protected AttributeId observedAttribute;

    protected boolean isFixingInProgress = false;


    @Override
    public boolean isConfigurable() {
        return false;
    }


    @Override
    public void showConfiguration(Window parentWindow) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void initialize(Map<String, Object> initPrefs) {
    }


    @Override
    public void eventPushed(AppEvent event, AbstractServiceResolver serverInterface) {
        if (isFixingInProgress)
            return;
        if (event.getEventType() != AppEventType.EDITION_METADATA_SET
                && event.getEventType() != AppEventType.PUBLICATION_METADATA_SET
                && event.getEventType() != AppEventType.DIRECTORY_METADATA_SET)
            return;
        ElementId elementId = (ElementId) event.getObjectId();
        try {
            MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
            ElementMetadataManager emm = ms.getElementMetadataManager();
            validateObservedValue(elementId, ms, emm);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }


    protected abstract void validateObservedValue(ElementId elementId, MetadataServer ms, ElementMetadataManager emm)
            throws Exception;


    protected boolean init(String attributeName) {
        try {
            MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
            Map<String, LanguageId> languageIds = ms.getLanguageManager().getLanguageIds(Language.LAN_DEFAULT_METADATA);
            defaultLanguage = languageIds.keySet().iterator().next();

            AttributeManager am = ms.getAttributeManager();
            observedAttribute = (AttributeId) am
                    .getObjects(new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList(attributeName)),
                        new OutputFilter(AttributeId.class))
                    .getResultId();
            if (observedAttribute == null) {
                Logger.getLogger(getClass()).warn(
                    "Could not find attribute with RDF name " + attributeName + ", versioning feature will not work");
                return false;
            }
        } catch (RemoteException e) {
            throw new RuntimeException("Unexpected server error", e);
        } catch (DLibraException e) {
            throw new RuntimeException("Unexpected server error", e);
        }
        return true;
    }


    protected void removeObservedValues(boolean leaveFirstValue, ElementId elementId, ElementMetadataManager emm)
            throws RemoteException, DLibraException {
        AttributeValueSet avs = emm.getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
        AttributeValueSet newavs = new AttributeValueSet(elementId);
        Collection<AttributeId> attributeIds = avs.getAttributeIds();
        List<String> languages = avs.getAvailableLanguages(true);
        for (AttributeId attributeId : attributeIds) {
            if (attributeId.equals(observedAttribute) && !leaveFirstValue)
                continue;

            for (String lang : languages) {
                List<AbstractAttributeValue> values = avs.getAttributeValues(attributeId, lang, Values.OnlyDirect);
                if (attributeId.equals(observedAttribute))
                    values = lang.equals(defaultLanguage) ? Arrays.asList(values.get(0)) : null;
                newavs.setDirectAttributeValues(attributeId, lang, values);
            }
        }
        isFixingInProgress = true;
        emm.setAttributeValueSet(newavs);
        isFixingInProgress = false;
    }


    protected void resetObservedValue(AttributeValueSet avs, EditionId womiId, MetadataServer ms,
            ElementMetadataManager emm)
                    throws RemoteException, IdNotFoundException, DLibraException, DuplicatedValueException,
                    AccessDeniedException {
        AttributeValueManager avm = ms.getAttributeValueManager();
        AttributeValueFilter avFilter = new AttributeValueFilter().setValue(womiId.toString(), true)
                .setLanguageName(defaultLanguage).setAttributeIds(Arrays.asList(observedAttribute));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<AbstractAttributeValue> avValues = (List) avm.getObjects(avFilter, new OutputFilter(AttributeValue.class))
                .getResults();
        if (avValues == null || avValues.isEmpty()) {
            AttributeValue av = new AttributeValue(null);
            av.setAttributeId(observedAttribute);
            av.setLanguageName(defaultLanguage);
            av.setValue(womiId.toString());
            AttributeValueId avId = avm.addAttributeValue(av);
            av.setId(avId);
            avValues = Arrays.asList((AbstractAttributeValue) av);
        }
        avs.setDirectAttributeValues(observedAttribute, defaultLanguage, avValues);
        isFixingInProgress = true;
        emm.setAttributeValueSet(avs);
        isFixingInProgress = false;
    }
}
