package pl.psnc.ep.rt.util;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
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
import pl.psnc.dlibra.mgmt.AbstractServiceResolver;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.ActorId;
import pl.psnc.dlibra.user.DirectoryRightId;
import pl.psnc.dlibra.user.PublicationRightId;
import pl.psnc.dlibra.user.PublicationRightValues;
import pl.psnc.dlibra.user.RightId;
import pl.psnc.dlibra.user.RightOperation;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserId;
import pl.psnc.dlibra.user.UserServer;
import pl.psnc.ep.rt.server.EPService;

public class Versioning {

    public final static String ROOT_ID_RDF_NAME = "RootID";

    public final static String EDITOR_MODE_RDF_NAME = "TrybEdytora";

    public final static String EDITOR_MODE_ONLINE = "edition-online";

    public final static String EDITOR_MODE_EXT_PROGRESS = "external-in-progress";

    public final static String EDITOR_MODE_EXT_FINAL = "external-final";

    private final static Logger logger = Logger.getLogger(Versioning.class);

    private static ConcurrentHashMap<String, AttributeId> attributesCache = new ConcurrentHashMap<String, AttributeId>();

    private static List<ActorId> allUsers;


    public static AttributeId getAttributeId(String rdfName, MetadataServer ms)
            throws RemoteException, IdNotFoundException, DLibraException {
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


    public static long whichVersion(PublicationId publicationId, List<AbstractAttributeValue> rootId, MetadataServer ms)
            throws RemoteException, DLibraException {
        if (rootId.isEmpty())
            return 1;

        List<Publication> allVersions = findAllVersions((AttributeValueId) rootId.get(0).getId(), ms);
        for (int i = 0; i < allVersions.size(); i++) {
            if (allVersions.get(i).getId().equals(publicationId))
                return i + 2;
        }
        throw new IllegalArgumentException(
                publicationId + " is not among versions of collection " + rootId.get(0).getValue());
    }


    public static List<Publication> findAllVersions(AttributeValueId rootIdAttributeValue, MetadataServer ms)
            throws RemoteException, DLibraException {
        AttributeValueManager avm = ms.getAttributeValueManager();
        ArrayList<PublicationId> versionsIds = new ArrayList<PublicationId>();
        Collection<Info> allPubVersionsInfos = avm.getObjects(new AttributeValueFilter(null, rootIdAttributeValue),
            new OutputFilter(AbstractPublicationInfo.class)).getResultInfos();
        for (Info versionInfo : allPubVersionsInfos) {
            if (((AbstractPublicationInfo) versionInfo).getStatus() == Publication.PUB_GROUP_ROOT)
                versionsIds.add((PublicationId) versionInfo.getId());
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<EditionId> allEdiVersionsIds = (List) avm
                .getObjects(new AttributeValueFilter(null, rootIdAttributeValue), new OutputFilter(EditionId.class))
                .getResultIds();
        allPubVersionsInfos = ms.getPublicationManager()
                .getObjects(new EditionFilter(allEdiVersionsIds), new OutputFilter(AbstractPublicationInfo.class))
                .getResultInfos();
        for (Info versionInfo : allPubVersionsInfos) {
            if (((AbstractPublicationInfo) versionInfo).getStatus() == Publication.PUB_NORMAL)
                versionsIds.add((PublicationId) versionInfo.getId());
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<Publication> allVersions = (List) ms.getPublicationManager()
                .getObjects(new PublicationFilter(versionsIds), new OutputFilter(Publication.class)).getResults();
        Collections.sort(allVersions, new Comparator<Publication>() {

            @Override
            public int compare(Publication o1, Publication o2) {
                return o1.getModificationTime().compareTo(o2.getModificationTime());
            }
        });
        return allVersions;
    }


    public static long[] findContentIdAndVersion(PublicationId pubId, MetadataServer ms)
            throws DLibraException, RemoteException {
        List<AbstractAttributeValue> values = getAttributeValues(pubId, getAttributeId(ROOT_ID_RDF_NAME, ms), ms);
        if (values == null) {
            logger.error(
                "Could not determine version of collection " + pubId + ": invalid group state or editions count");
            return new long[] { pubId.getId(), 1 };
        }
        try {
            long contentId = values.isEmpty() ? pubId.getId() : Long.valueOf(values.get(0).getValue());
            long version = whichVersion(pubId, values, ms);
            return new long[] { contentId, version };
        } catch (NumberFormatException e) {
            logger.error("Could not determine version of collectin " + pubId + ": RootId is not a number ("
                    + values.get(0) + ")");
            return new long[] { pubId.getId(), 1 };
        }
    }


    private static List<Publication> findAllVersions(PublicationId pubId, MetadataServer ms)
            throws DLibraException, RemoteException {
        AttributeId attributeId = getAttributeId(ROOT_ID_RDF_NAME, ms);
        List<AbstractAttributeValue> values = getAttributeValues(pubId, attributeId, ms);
        if (values == null)
            return Collections.emptyList();

        AttributeValueId rootIdAttributeValue;
        if (!values.isEmpty()) {
            rootIdAttributeValue = (AttributeValueId) values.get(0).getId();
        } else {
            AttributeValueFilter avFilter = new AttributeValueFilter(attributeId);
            avFilter.setValue(pubId.getId().toString(), true);
            rootIdAttributeValue = (AttributeValueId) ms.getAttributeValueManager()
                    .getObjects(avFilter, new OutputFilter(AttributeValueId.class)).getResultId();
            if (rootIdAttributeValue == null)
                return Collections.emptyList();
        }

        List<Publication> allVersions = findAllVersions(rootIdAttributeValue, ms);
        return allVersions;
    }


    public static boolean hasNextVersion(PublicationId pubId, MetadataServer ms)
            throws DLibraException, RemoteException {
        List<Publication> allVersions = findAllVersions(pubId, ms);
        boolean isLastVersion = allVersions.isEmpty() || allVersions.get(allVersions.size() - 1).getId().equals(pubId);
        return !isLastVersion;
    }


    private static List<AbstractAttributeValue> getAttributeValues(PublicationId pubId, AttributeId attributeId,
            MetadataServer ms)
                    throws DLibraException, RemoteException {
        PublicationManager pm = ms.getPublicationManager();
        Publication pub = (Publication) pm.getObjects(new PublicationFilter(pubId), new OutputFilter(Publication.class))
                .getResult();
        ElementId metadataId = null;
        if (pub.getGroupStatus() == Publication.PUB_GROUP_ROOT)
            metadataId = pubId;
        else if (pub.getGroupStatus() == Publication.PUB_NORMAL)
            metadataId = (ElementId) pm
                    .getObjects(new PublicationFilter(null, pubId), new OutputFilter(EditionId.class)).getResultId();
        if (metadataId == null) {
            logger.debug(
                "Could not determine version of collection " + pubId + ": invalid group state or editions count");
            return null;
        }

        AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(metadataId,
            AttributeValue.AV_ASSOC_DIRECT);
        List<AbstractAttributeValue> values = avs.getAttributeValues(attributeId, "pl",
            AttributeValueSet.Values.OnlyDirect);
        return values;
    }


    public static List<PublicationId> findUnremovableHandbooks(DirectoryId directoryId, MetadataServer ms)
            throws DLibraException, RemoteException {
        DirectoryFilter df = new DirectoryFilter(null, directoryId);
        df.setGroupStatus((byte) (Publication.PUB_GROUP_ROOT | Publication.PUB_NORMAL));
        df.setState(Publication.PUB_STATE_ACTUAL);
        df.setRecursive(true);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<PublicationId> publicationIds = (Collection) ms.getDirectoryManager()
                .getObjects(df, new OutputFilter(PublicationId.class)).getResultIds();

        List<PublicationId> cantRemove = new ArrayList<PublicationId>();
        for (PublicationId pubId : publicationIds) {
            List<Publication> allVersions = findAllVersions(pubId, ms);
            int currentVersion = 0;
            for (int i = 0; i < allVersions.size(); i++) {
                if (allVersions.get(i).getId().equals(pubId)) {
                    currentVersion = i + 1;
                    break;
                }
            }
            for (int i = currentVersion; i < allVersions.size(); i++) {
                if (!publicationIds.contains(allVersions.get(i).getId())) {
                    cantRemove.add(pubId);
                    break;
                }
            }
        }
        return cantRemove;
    }


    public static String checkVersions(AttributeValueSet metadata, AttributeValueSet oldAVS,
            AbstractServiceResolver resolver, UserId currentUserId, Publication[] rootPublicationOut)
                    throws RemoteException, DLibraException {
        MetadataServer ms = (MetadataServer) resolver.getService(MetadataServer.SERVICE_TYPE, null);
        PublicationManager pm = ms.getPublicationManager();
        AttributeId attributeId = Versioning.getAttributeId(Versioning.ROOT_ID_RDF_NAME, ms);
        List<AbstractAttributeValue> oldValues = (List<AbstractAttributeValue>) oldAVS.getAttributeValues(attributeId);
        List<AbstractAttributeValue> newValues = (List<AbstractAttributeValue>) metadata
                .getAttributeValues(attributeId);
        if (oldValues.equals(newValues))
            return null;

        if (newValues.isEmpty())
            return null;

        if (metadata.getElementId() instanceof DirectoryId)
            return "Versions not allowed on directories";

        PublicationId versionPubId = getPublicationId(metadata.getElementId(), pm);
        Publication versionPub = (Publication) pm
                .getObjects(new PublicationFilter(versionPubId), new OutputFilter(Publication.class)).getResult();
        String errorMessage = checkIsTextbook(versionPub, null, null, ms);
        if (errorMessage != null) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Modified object is not a textbook: " + errorMessage;
        }

        PublicationId rootId;
        try {
            rootId = new PublicationId(Long.valueOf(newValues.get(0).getValue()));
        } catch (NumberFormatException e) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Inalid value: " + newValues.get(0);
        }
        if (rootId.equals(versionPubId)) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Self reference not allowed";
        }
        Publication rootPublication;
        try {
            rootPublication = (Publication) pm
                    .getObjects(new PublicationFilter(rootId), new OutputFilter(Publication.class)).getResult();
        } catch (IdNotFoundException e) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "No such textbook: " + rootId;
        }
        rootPublicationOut[0] = rootPublication;
        errorMessage = checkIsTextbook(rootPublication, null, null, ms);
        if (errorMessage != null) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return errorMessage;
        }

        ElementMetadataManager emm = ms.getElementMetadataManager();
        AttributeValueSet rootAVS = emm.getAttributeValueSet(rootId, AttributeValue.AV_ASSOC_DIRECT);
        if (!rootAVS.getAttributeValues(attributeId).isEmpty()) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Object is not the first version: " + rootId;
        }
        if (newValues.size() > 1) {
            logger.warn("Removing excess values of first version");
            newValues.subList(1, newValues.size()).clear();
            metadata.setDirectAttributeValues(attributeId, "pl", newValues);
        }
        UserServer us = (UserServer) resolver.getService(UserServer.SERVICE_TYPE, null);
        DirectoryId dirId = (DirectoryId) pm
                .getObjects(new PublicationFilter(rootId), new OutputFilter(DirectoryId.class)).getResultId();
        boolean hasRights = us.getRightManager().checkDirectoryRight(dirId, currentUserId,
            DirectoryRightId.PUBLICATION_CREATE);
        if (!hasRights) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Access to first version is denied!";
        }

        errorMessage = checkEditorMode(metadata, oldAVS, versionPub, null, null, false, ms);
        if (errorMessage != null) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Editor mode not compatible: " + errorMessage;
        }
        return "";
    }


    public static void checkVersionsFollowUp(AttributeValueSet metadata, AttributeValueSet oldAVS, boolean confirmed,
            AbstractServiceResolver resolver)
                    throws RemoteException, DLibraException {
        MetadataServer ms = (MetadataServer) resolver.getService(MetadataServer.SERVICE_TYPE, null);
        PublicationManager pm = ms.getPublicationManager();
        AttributeId attributeId = Versioning.getAttributeId(Versioning.ROOT_ID_RDF_NAME, ms);
        List<AbstractAttributeValue> oldValues = (List<AbstractAttributeValue>) oldAVS.getAttributeValues(attributeId);
        List<AbstractAttributeValue> newValues = (List<AbstractAttributeValue>) metadata
                .getAttributeValues(attributeId);
        if (oldValues.equals(newValues))
            return;

        if (!newValues.isEmpty()) {
            if (confirmed) {
                UserServer us = (UserServer) resolver.getService(UserServer.SERVICE_TYPE, null);
                PublicationId rootId = new PublicationId(Long.valueOf(newValues.get(0).getValue()));
                Versioning.block(rootId, pm, us);
                for (Publication version : Versioning.findAllVersions((AttributeValueId) newValues.get(0).getId(), ms))
                    Versioning.block(version.getId(), pm, us);
            } else {
                metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
                return;
            }
        }

        if (!oldValues.isEmpty()) {
            PublicationId publicationId = getPublicationId(oldAVS.getElementId(), pm);
            EPService ep = (EPService) resolver.getService(EPService.SERVICE_TYPE, null);
            ep.reportTextbookRemoved(publicationId);
        }
    }


    public static String checkEditorMode(AttributeValueSet metadata, AttributeValueSet oldAVS, Publication publication,
            Edition edition, String mainFilePath, boolean isUpdating, MetadataServer ms)
                    throws RemoteException, DLibraException {
        AttributeId attributeId = getAttributeId(Versioning.EDITOR_MODE_RDF_NAME, ms);
        List<AbstractAttributeValue> oldValues = (List<AbstractAttributeValue>) oldAVS.getAttributeValues(attributeId);
        List<AbstractAttributeValue> newValues = (List<AbstractAttributeValue>) metadata
                .getAttributeValues(attributeId);

        if (isUpdating) {
            List<String> oldValuesStr = new ArrayList<String>();
            for (AbstractAttributeValue v : oldValues)
                oldValuesStr.add(v.getValue());
            List<String> newValuesStr = new ArrayList<String>();
            for (AbstractAttributeValue v : newValues)
                newValuesStr.add(v.getValue());
            if (!oldValuesStr.equals(newValuesStr)) {
                metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
                return "Changing " + Versioning.EDITOR_MODE_RDF_NAME + " not allowed";
            }
        }

        if (!newValues.isEmpty() && metadata.getElementId() instanceof DirectoryId) {
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Editor mode on directory not allowed";
        }

        String errorMessage = checkIsTextbook(publication, edition, mainFilePath, ms);
        if (errorMessage != null) {
            if (newValues.isEmpty())
                return null;
            metadata.setDirectAttributeValues(attributeId, "pl", oldValues);
            return "Modified object is not a textbook: " + errorMessage;
        }

        if (newValues.size() > 1) {
            logger.warn("Removing excess values of editor mode");
            newValues.subList(1, newValues.size()).clear();
        }
        String newEditorMode = newValues.isEmpty() ? null : newValues.get(0).getValue();
        boolean isOnlineEditor = publication.getGroupStatus() == Publication.PUB_NORMAL;
        if (isOnlineEditor) {
            if (EDITOR_MODE_EXT_PROGRESS.equals(newEditorMode) || EDITOR_MODE_EXT_FINAL.equals(newEditorMode)) {
                return "External modes are reserved for groups of modules";
            }
        } else {
            if (EDITOR_MODE_ONLINE.equals(newEditorMode))
                return "Online mode is reserved for collection-XMLs";
        }

        if (isUpdating)
            return null;
        AttributeId versionAttrId = Versioning.getAttributeId(Versioning.ROOT_ID_RDF_NAME, ms);
        List<AbstractAttributeValue> versionValues = (List<AbstractAttributeValue>) metadata
                .getAttributeValues(versionAttrId);
        if (versionValues.isEmpty())
            return null;

        AttributeValueId valueId = (AttributeValueId) ms.getAttributeValueManager()
                .getObjects(new AttributeValueFilter(versionAttrId).setValue(versionValues.get(0).getValue(), true),
                    new OutputFilter(AttributeValueId.class))
                .getResultId();
        List<Publication> allVersions = findAllVersions(valueId, ms);
        Collections.reverse(allVersions);
        allVersions
                .add((Publication) ms.getPublicationManager()
                        .getObjects(
                            new PublicationFilter(new PublicationId(Long.valueOf(versionValues.get(0).getValue()))),
                            new OutputFilter(Publication.class))
                        .getResult());
        String previousVersionEditorMode = null;
        PublicationId previousVersionId = null;
        for (Publication pub : allVersions) {
            if (!pub.getId().equals(publication.getId())) {
                List<AbstractAttributeValue> values = getAttributeValues(pub.getId(), attributeId, ms);
                if (values != null && !values.isEmpty()) {
                    previousVersionEditorMode = values.get(0).getValue();
                    previousVersionId = pub.getId();
                }
                break;
            }
        }

        if (EDITOR_MODE_EXT_PROGRESS.equals(previousVersionEditorMode)) {
            if (newEditorMode == null)
                return "Previous version (" + previousVersionId + ") is " + previousVersionEditorMode
                        + ", new cannot be empty";
        } else if (EDITOR_MODE_EXT_FINAL.equals(previousVersionEditorMode)
                || EDITOR_MODE_ONLINE.equals(previousVersionEditorMode)) {
            if (!EDITOR_MODE_ONLINE.equals(newEditorMode))
                return "Previous version (" + previousVersionId + ") is " + previousVersionEditorMode + ", new must be "
                        + EDITOR_MODE_ONLINE;
        }

        return null;
    }


    private static String checkIsTextbook(Publication rootPublication, Edition edition, String mainFilePath,
            MetadataServer ms)
                    throws RemoteException, DLibraException {
        if (rootPublication == null || rootPublication.getState() != Publication.PUB_STATE_ACTUAL
                || (rootPublication.getGroupStatus() != Publication.PUB_GROUP_ROOT
                        && rootPublication.getGroupStatus() != Publication.PUB_NORMAL)) {
            return "No such textbook: " + (rootPublication == null ? null : rootPublication.getId());
        }

        PublicationId rootId = rootPublication.getId();
        if (rootPublication.getGroupStatus() == Publication.PUB_NORMAL) {
            if (mainFilePath == null) {
                FileId mainFileId = rootPublication.getMainFileId();
                if (mainFileId == null)
                    return null;
                File mainFile = (File) ms.getFileManager()
                        .getObjects(new FileFilter(mainFileId), new OutputFilter(File.class)).getResult();
                mainFilePath = mainFile.getPath();
            }
            if (mainFilePath.equals("/" + WOMIXMLHandler.MAIN_FILE_NAME)) {
                return "Object " + rootId + " is a WOMI";
            }
            if (edition == null) {
                edition = (Edition) ms.getPublicationManager()
                        .getObjects(new PublicationFilter(null, rootId), new OutputFilter(Edition.class)).getResult();
            }
            if (edition == null || edition.getExternalId() != null) {
                return "Object " + rootId + " is a module?";
            }
        }
        return null;
    }


    private static PublicationId getPublicationId(ElementId elementId, PublicationManager pm)
            throws IdNotFoundException, RemoteException, DLibraException {
        if (elementId instanceof PublicationId)
            return (PublicationId) elementId;

        if (elementId instanceof EditionId)
            return (PublicationId) pm
                    .getObjects(new EditionFilter((EditionId) elementId), new OutputFilter(PublicationId.class))
                    .getResultId();

        return null;
    }


    public static void block(ElementId elementId, PublicationManager pm, UserServer us)
            throws RemoteException, DLibraException {
        RightOperation rightOperation = new RightOperation(PublicationRightId.PUBLICATION_EDIT, RightOperation.REMOVE);
        ArrayDeque<PublicationId> queue = new ArrayDeque<PublicationId>();
        queue.add(getPublicationId(elementId, pm));
        while (!queue.isEmpty()) {
            PublicationId publicationId = queue.remove();
            try {
                us.getRightManager().setPublicationRights(publicationId, getAllUsers(us), rightOperation);
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Collection<PublicationId> childrenIds = (Collection) pm
                        .getObjects(new PublicationFilter(null, publicationId), new OutputFilter(PublicationId.class))
                        .getResultIds();
                queue.addAll(childrenIds);
            } catch (AccessDeniedException e) {
                logger.warn("Could not block object " + publicationId + ", already have no rights");
            }
        }
    }


    public static boolean isBlocked(EditionId editionId, PublicationManager pm, UserServer us)
            throws RemoteException, DLibraException {
        Edition edition = (Edition) pm.getObjects(new EditionFilter(editionId), new OutputFilter(Edition.class))
                .getResult();

        PublicationId pubId = (PublicationId) edition.getParentId();
        ActorId actId = edition.getCreatedBy().getId();
        PublicationRightValues publicationRights = us.getRightManager().getPublicationRights(pubId, actId);
        return !publicationRights.hasGranted(pubId, actId, PublicationRightId.PUBLICATION_EDIT);
    }


    public static boolean isBlocked(PublicationId publicationId, UserServer us)
            throws RemoteException, DLibraException {
        PublicationRightValues publicationRights = us.getRightManager().getPublicationRights(
            Arrays.asList(publicationId), getAllUsers(us),
            Arrays.asList((RightId) PublicationRightId.PUBLICATION_EDIT));
        for (ActorId userId : allUsers) {
            if (!User.isAdmin(userId)
                    && publicationRights.hasGranted(publicationId, userId, PublicationRightId.PUBLICATION_EDIT))
                return false;
        }
        return true;
    }


    private static synchronized List<ActorId> getAllUsers(UserServer us)
            throws RemoteException, DLibraException {
        if (allUsers == null) {
            allUsers = us.getUserManager().getUserIds(User.ALL_USERS + User.ALL_GROUPS);
        }
        return allUsers;
    }
}
