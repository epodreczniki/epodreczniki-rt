package pl.psnc.ep.rt.listeners;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.swing.FocusManager;
import javax.swing.JOptionPane;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet.Values;
import pl.psnc.dlibra.metadata.attributes.ElementMetadataManager;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.ep.rt.Util;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;

public class PanoramaEventListener extends AbstractMetadataListener {

    private static final String PANORAMA_ATTR_RDFNAME = "PanoramaWTle";


    @Override
    public String toString() {
        return textsBundle.getString("panorama.pluginName");
    }


    @Override
    protected void validateObservedValue(ElementId elementId, MetadataServer ms, ElementMetadataManager emm)
            throws Exception {
        if (observedAttribute == null && !init(PANORAMA_ATTR_RDFNAME))
            return;

        AttributeValueSet avs = emm.getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
        List<AbstractAttributeValue> panoramaAVs = avs.getAttributeValues(observedAttribute, defaultLanguage,
            Values.OnlyDirect);
        if (panoramaAVs == null || panoramaAVs.isEmpty())
            return;

        EditionId panoramaId;
        try {
            panoramaId = checkPanoramaId(elementId, panoramaAVs.get(0).getValue(), true, ms);
        } catch (MetadataException e) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString(e.getMessage()), e.detail),
                textsBundle.getString("panorama.error.title"), JOptionPane.ERROR_MESSAGE);
            removeObservedValues(false, elementId, emm);
            return;
        }

        if (!panoramaId.toString().equals(panoramaAVs.get(0).getValue())) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString("panorama.warning.wrongIdType"), panoramaAVs.get(0), panoramaId),
                textsBundle.getString("panorama.warning.title"), JOptionPane.WARNING_MESSAGE);
            resetObservedValue(avs, panoramaId, ms, emm);
        } else if (panoramaAVs.size() > 1) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                textsBundle.getString("panorama.warning.tooMany"), textsBundle.getString("panorama.warning.title"),
                JOptionPane.WARNING_MESSAGE);
            removeObservedValues(true, elementId, emm);
        }
    }


    private EditionId checkPanoramaId(ElementId elementId, String panorama, boolean tryPublicationId, MetadataServer ms)
            throws MetadataException, RemoteException, DLibraException {
        if (!(elementId instanceof PublicationId))
            throw new MetadataException("panorama.error.notChapter", panorama);
        PublicationId bookId = (PublicationId) elementId;
        PublicationManager pm = ms.getPublicationManager();
        AbstractPublicationInfo nvInfo = (AbstractPublicationInfo) pm
                .getObjects(new PublicationFilter(bookId), new OutputFilter(AbstractPublicationInfo.class))
                .getResultInfo();
        if (nvInfo.getStatus() != Publication.PUB_GROUP_ROOT && nvInfo.getStatus() != Publication.PUB_GROUP_MID)
            throw new MetadataException("panorama.error.notChapter", panorama);

        EditionId panoramaId;
        try {
            panoramaId = new EditionId(Long.valueOf(panorama));
        } catch (NumberFormatException e) {
            throw new MetadataException("womi.error.invalidId", panorama);
        }

        try {
            validatePanorama(panoramaId, ms);
        } catch (MetadataException e1) {
            if (tryPublicationId) {
                PublicationId pubId = new PublicationId(Long.valueOf(panorama));
                panoramaId = (EditionId) pm
                        .getObjects(new PublicationFilter(null, pubId), new OutputFilter(EditionId.class))
                        .getResultId();
                if (panoramaId == null)
                    throw e1;
                try {
                    validatePanorama(panoramaId, ms);
                } catch (MetadataException e2) {
                    throw e1;
                }
            } else {
                throw e1;
            }
        }

        return panoramaId;
    }


    private void validatePanorama(EditionId panoramaId, MetadataServer ms)
            throws MetadataException, RemoteException, DLibraException {
        try {
            Map<WOMIFormat, String> filesMap = Util.findFileNames(panoramaId);
            for (Map.Entry<WOMIFormat, String> entry : filesMap.entrySet()) {
                WOMIFormat format = entry.getKey();
                if (format.womiType != WOMIType.INTERACTIVE)
                    throw new MetadataException("panorama.error.notInteractive", panoramaId.toString());
            }
        } catch (IdNotFoundException e) {
            throw new MetadataException("womi.error.notWOMI", panoramaId.toString());
        } catch (RemoteException e) {
            throw e;
        } catch (IOException e) {
            throw new MetadataException("womi.error.notWOMI", panoramaId.toString());
        }
    }
}
