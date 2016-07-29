package pl.psnc.ep.rt.listeners;

import java.util.List;

import javax.swing.FocusManager;
import javax.swing.JOptionPane;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet.Values;
import pl.psnc.dlibra.metadata.attributes.ElementMetadataManager;
import pl.psnc.ep.rt.util.ManualAuthorsHelper;

public class AuthorsEventListener extends AbstractMetadataListener {

    private static final String AUTHOR_ATTR_RDFNAME = "Autor";

    private ManualAuthorsHelper manualAuthorsHelper = new ManualAuthorsHelper();


    @Override
    protected void validateObservedValue(ElementId elementId, MetadataServer ms, ElementMetadataManager emm)
            throws Exception {
        if (observedAttribute == null && !init(AUTHOR_ATTR_RDFNAME))
            return;

        AttributeValueSet avs = emm.getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
        List<AbstractAttributeValue> authorAVs = avs.getAttributeValues(observedAttribute, defaultLanguage,
            Values.OnlyDirect);
        if (authorAVs == null || authorAVs.isEmpty())
            return;

        if (!(elementId instanceof PublicationId))
            return;
        AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) ms.getPublicationManager()
                .getObjects(new PublicationFilter((PublicationId) elementId),
                    new OutputFilter(AbstractPublicationInfo.class))
                .getResultInfo();
        boolean isBook = pubInfo.getStatus() == Publication.PUB_GROUP_ROOT;
        if (!isBook) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString("authorValidation.error.notABook"), authorAVs.get(0)),
                textsBundle.getString("authorValidation.error.title"), JOptionPane.ERROR_MESSAGE);
            removeObservedValues(false, elementId, emm);
            return;
        }

        String message = manualAuthorsHelper.validateAuthorValue(authorAVs.get(0).getValue());
        if (message != null) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString("authorValidation.error.invalid"), message),
                textsBundle.getString("authorValidation.error.title"), JOptionPane.ERROR_MESSAGE);
            removeObservedValues(false, elementId, emm);
            return;
        }

        if (authorAVs.size() > 1) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                textsBundle.getString("authorValidation.warning.tooMany"),
                textsBundle.getString("authorValidation.warning.title"), JOptionPane.WARNING_MESSAGE);
            removeObservedValues(true, elementId, emm);
        }
    }
}
