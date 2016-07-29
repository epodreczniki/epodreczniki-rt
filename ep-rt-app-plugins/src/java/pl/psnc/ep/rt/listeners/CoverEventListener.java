package pl.psnc.ep.rt.listeners;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.FocusManager;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.VersionId;
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
import pl.psnc.ep.rt.util.SimpleImageInfo;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class CoverEventListener extends AbstractMetadataListener {

    private static final String COVER_ATTRIBUTE_RDFNAME = "Okladka";

    private static final double DESIRED_ASPECT_RATIO_MIN = 70d / 100d;

    private static final double DESIRED_ASPECT_RATIO_MAX = 75d / 100d;

    private static CoverEventListener instance;


    public CoverEventListener() {
        if (instance != null)
            instance = this;
    }


    public static CoverEventListener getInstance() {
        if (instance == null)
            instance = new CoverEventListener();
        return instance;
    }


    @Override
    public String toString() {
        return textsBundle.getString("cover.pluginName");
    }


    @Override
    protected void validateObservedValue(ElementId elementId, MetadataServer ms, ElementMetadataManager emm)
            throws Exception {
        if (observedAttribute == null && !init(COVER_ATTRIBUTE_RDFNAME))
            return;
        AttributeValueSet avs = emm.getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
        List<AbstractAttributeValue> coverAVs = avs.getAttributeValues(observedAttribute, defaultLanguage,
            Values.OnlyDirect);
        if (coverAVs == null || coverAVs.isEmpty())
            return;

        EditionId coverId;
        try {
            coverId = checkCoverId(elementId, coverAVs.get(0).getValue(), true, ms);
        } catch (MetadataException e) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString(e.getMessage()), e.detail),
                textsBundle.getString("cover.error.title"), JOptionPane.ERROR_MESSAGE);
            removeObservedValues(false, elementId, emm);
            return;
        }

        if (!coverId.toString().equals(coverAVs.get(0).getValue())) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString("cover.warning.wrongIdType"), coverAVs.get(0), coverId),
                textsBundle.getString("cover.warning.title"), JOptionPane.WARNING_MESSAGE);

            resetObservedValue(avs, coverId, ms, emm);
        } else if (coverAVs.size() > 1) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                textsBundle.getString("cover.warning.tooManyCovers"), textsBundle.getString("cover.warning.title"),
                JOptionPane.WARNING_MESSAGE);
            removeObservedValues(true, elementId, emm);
        }
    }


    private EditionId checkCoverId(ElementId elementId, String cover, boolean tryPublicationId, MetadataServer ms)
            throws MetadataException, RemoteException, DLibraException {
        PublicationManager pm = ms.getPublicationManager();
        if (elementId instanceof PublicationId) {
            PublicationId bookId = (PublicationId) elementId;
            AbstractPublicationInfo nvInfo = (AbstractPublicationInfo) pm
                    .getObjects(new PublicationFilter(bookId), new OutputFilter(AbstractPublicationInfo.class))
                    .getResultInfo();
            if (nvInfo.getStatus() == Publication.PUB_GROUP_LEAF) {
                boolean isCollXML = !ms.getLibCollectionManager()
                        .checkCollectionMembers(Util.COLLXML_COLLECTION, Arrays.asList(bookId)).isEmpty();
                if (!isCollXML)
                    throw new MetadataException("cover.error.notBook", cover);
            } else if (nvInfo.getStatus() != Publication.PUB_GROUP_ROOT) {
                throw new MetadataException("cover.error.notBook", cover);
            }
        } else if (elementId instanceof DirectoryId) {
        } else {
            throw new MetadataException("cover.error.notBook", cover);
        }

        EditionId coverId;
        try {
            coverId = new EditionId(Long.valueOf(cover));
        } catch (NumberFormatException e) {
            throw new MetadataException("womi.error.invalidId", cover);
        }

        try {
            validateCover(coverId, ms);
        } catch (MetadataException e1) {
            if (tryPublicationId) {
                PublicationId pubId = new PublicationId(Long.valueOf(cover));
                coverId = (EditionId) pm
                        .getObjects(new PublicationFilter(null, pubId), new OutputFilter(EditionId.class))
                        .getResultId();
                if (coverId == null)
                    throw e1;
                try {
                    validateCover(coverId, ms);
                } catch (MetadataException e2) {
                    throw e1;
                }
            } else {
                throw e1;
            }
        }

        return coverId;
    }


    private void validateCover(EditionId coverId, MetadataServer ms)
            throws MetadataException, RemoteException, DLibraException {
        try {
            Map<WOMIFormat, String> filesMap = Util.findFileNames(coverId);
            ContentServer cs = (ContentServer) ApplicationContext.getInstance().getUserServiceResolver()
                    .getService(ContentServer.SERVICE_TYPE, null);
            for (Map.Entry<WOMIFormat, String> entry : filesMap.entrySet()) {
                WOMIFormat format = entry.getKey();
                if (format.womiType != WOMIType.GRAPHICS)
                    throw new MetadataException("cover.error.notGraphics", coverId.toString());
                if (!checkImageRatio(coverId, format, entry.getValue(), ms, cs))
                    throw new MetadataException("cover.error.wrongRatio", coverId.toString());
            }
        } catch (IdNotFoundException e) {
            throw new MetadataException("cover.error.corrupted", coverId.toString());
        } catch (RemoteException e) {
            throw e;
        } catch (IOException e) {
            throw new MetadataException("womi.error.notWOMI", coverId.toString());
        }
    }


    private boolean checkImageRatio(EditionId coverId, WOMIFormat format, String fileName, MetadataServer ms,
            ContentServer cs)
                    throws MetadataException, RemoteException, DLibraException {
        String filePath = WOMIXMLHandler.toDLibraPath(format, fileName);
        try {
            VersionId versionId = (VersionId) ms.getFileManager()
                    .getObjects(new FileFilter(filePath).setEditionId(coverId), new OutputFilter(VersionId.class))
                    .getResultId();
            InputStream vis = cs.getVersionInputStream(versionId);
            try {
                SimpleImageInfo imageInfo = new SimpleImageInfo(vis);
                final double ratio = (double) imageInfo.getWidth() / (double) imageInfo.getHeight();
                return (ratio >= DESIRED_ASPECT_RATIO_MIN) && (ratio <= DESIRED_ASPECT_RATIO_MAX);
            } finally {
                vis.close();
                cs.releaseElement(versionId);
            }
        } catch (IdNotFoundException e) {
            throw new MetadataException("cover.error.corrupted", coverId.toString());
        } catch (IOException e) {
            Logger.getLogger(getClass()).error("Could not check image ratio (" + coverId + ":" + filePath + ")", e);
            throw new MetadataException("cover.error.corrupted", coverId.toString());
        }
    }


    public EditionId getCoverId(PublicationId bookId, MetadataServer ms, ArrayList<ValidationResult> validationResults)
            throws RemoteException, DLibraException {
        if (observedAttribute == null && !init(COVER_ATTRIBUTE_RDFNAME))
            return null;
        ElementMetadataManager emm = ms.getElementMetadataManager();
        AttributeValueSet avs = emm.getAttributeValueSet(bookId, AttributeValue.AV_ASSOC_DIRECT);
        List<AbstractAttributeValue> coverAVs = avs.getAttributeValues(observedAttribute, defaultLanguage,
            Values.OnlyDirect);
        if (coverAVs == null || coverAVs.isEmpty()) {
            validationResults.add(
                new ValidationResult(ValidationResultType.ERROR, textsBundle.getString("cover.error.missing"), null));
            return null;
        }

        try {
            return checkCoverId(bookId, coverAVs.get(0).getValue(), true, ms);
        } catch (MetadataException e) {
            validationResults
                    .add(new ValidationResult(ValidationResultType.ERROR, textsBundle.getString("cover.error.title"),
                            String.format(textsBundle.getString(e.getMessage()), e.detail)));
            return null;
        }
    }

}
