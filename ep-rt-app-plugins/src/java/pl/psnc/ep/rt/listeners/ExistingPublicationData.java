package pl.psnc.ep.rt.listeners;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.extension.datasource.FileInfo;
import pl.psnc.dlibra.app.extension.validator.PublicationData;
import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.dlibra.common.DLObject;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.RightId;

public class ExistingPublicationData implements PublicationData {

    private static final String BUNDLE_PATH = "GUI";

    private final Publication publication;

    private final List<ValidationResult> validationResults;

    private final ResourceBundle textsBundle;

    private EditionId editionId;

    private AttributeValueSet avs;


    public ExistingPublicationData(Publication publication, List<ValidationResult> validationResults) {
        this.publication = publication;
        this.validationResults = validationResults;
        textsBundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault());
    }


    @Override
    public Publication getPublication() {
        return publication;
    }


    @Override
    public Edition getEdition() {
        return null;
    }


    @Override
    public Set<LibCollectionId> getCollections() {
        return null;
    }


    @Override
    public AttributeValueSet getAttributeValueSet() {
        if (avs != null)
            return avs;
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        try {
            ElementId elementId = publication.getId();
            if (publication.getGroupStatus() == Publication.PUB_GROUP_LEAF
                    || publication.getGroupStatus() == Publication.PUB_NORMAL)
                elementId = getEditionId(ms);
            if (elementId == null)
                return null;
            avs = ms.getElementMetadataManager().getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
            return avs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private EditionId getEditionId(MetadataServer ms)
            throws IdNotFoundException, RemoteException, DLibraException {
        if (editionId != null)
            return editionId;
        Collection<Id> editionIds = ms.getPublicationManager()
                .getObjects(new PublicationFilter(null, publication.getId()), new OutputFilter(EditionId.class))
                .getResultIds();
        if (editionIds.size() != 1) {
            validationResults.add(new ValidationResult(ValidationResultType.ERROR,
                    String.format(textsBundle.getString("validation.error.invalidState"), editionIds),
                    textsBundle.getString("validation.suggestion.fromScratch")));
            return null;
        }
        editionId = (EditionId) editionIds.iterator().next();
        return editionId;
    }


    @Override
    public Map<String, Set<RightId>> getActorRights() {
        return null;
    }


    @Override
    public List<FileInfo> getFiles() {
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        try {
            Collection<DLObject> files = ms.getPublicationManager()
                    .getObjects(new PublicationFilter(null, publication.getId()), new OutputFilter(File.class))
                    .getResults();
            ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
            getEditionId(ms);
            for (DLObject file : files) {
                boolean isMain = publication.getMainFileId().equals(file.getId());
                fileInfos.add(isMain ? 0 : fileInfos.size(), new DLibraFileInfo((File) file));
            }
            return fileInfos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<PublicationData> getChildren() {
        if (publication.getGroupStatus() == Publication.PUB_GROUP_LEAF)
            return null;
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        try {
            Collection<DLObject> publications = ms.getPublicationManager()
                    .getObjects(new PublicationFilter(null, publication.getId()), new OutputFilter(Publication.class))
                    .getResults();
            ArrayList<PublicationData> pubDatas = new ArrayList<PublicationData>();
            for (DLObject pub : publications) {
                pubDatas.add(new ExistingPublicationData((Publication) pub, validationResults));
            }
            return pubDatas;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private class DLibraFileInfo implements FileInfo {

        private final File file;


        public DLibraFileInfo(File file) {
            this.file = file;
        }


        @Override
        public URL getURL() {
            URI readerURI = URI.create(ApplicationContext.getInstance().getReaderURL());
            URI contentURI = readerURI.resolve("Content/" + editionId + getDLibraPath());
            try {
                return contentURI.toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public boolean isLocal() {
            return false;
        }


        @Override
        public String getDLibraPath() {
            return file.getPath();
        }
    }
}
