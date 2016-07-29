package pl.psnc.ep.rt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.app.common.exception.OperationFailedException;
import pl.psnc.dlibra.app.common.files.EditionFile;
import pl.psnc.dlibra.app.gui.base.AmbiguousAttributeValueException;
import pl.psnc.dlibra.app.gui.common.propertizer.PublicationPropertizer;
import pl.psnc.dlibra.app.parser.html.ParseException;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet.Values;
import pl.psnc.dlibra.metadata.attributes.ElementMetadataManager;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationException;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationProblem;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.ActorId;
import pl.psnc.dlibra.user.RightManager;
import pl.psnc.dlibra.user.RightOperation;
import pl.psnc.dlibra.user.UserManager;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.tools.plugins.MetadataConverter;
import pl.psnc.ep.rt.tools.transform.AbstractPropertiesToPublicationTransformer;
import pl.psnc.ep.rt.tools.transform.PropertiesToPublicationTransformer;
import pl.psnc.ep.rt.tools.upload.FilesUploader;
import pl.psnc.ep.rt.tools.upload.VersionsCreator;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class PublicationUploader {

    static final String NO_NOTIFICATION = "noNotification";

    private static final String PUBLICATION_PROPERTIES_FILENAME = "publication.properties";

    public static final int THREAD_COUNT = 4;

    private static final Object STOP = new Object();

    private static final Logger log = Logger.getLogger(PublicationUploader.class);

    private static ServerManager manager = null;

    private static final HashSet<AbstractPropertiesToPublicationTransformer> replacing = new HashSet<AbstractPropertiesToPublicationTransformer>();

    private static Map<String, String> attrValues = new HashMap<String, String>();

    private static boolean skipContent = false;

    private static int waitingUploaderCount = 0;

    private static LinkedBlockingQueue<Object> uploadQueue = new LinkedBlockingQueue<Object>();

    private static List<PublicationId> createdPubsIds = Collections.synchronizedList(new ArrayList<PublicationId>());


    public static void main(String[] args)
            throws RemoteException, DLibraException {
        if (args.length < 3) {
            System.out.println("Usage: \nuploader username password directory [rdfname=\"value\"] [-N] [configfile]\n"
                    + "username = login uzytkownika w Repozytorium Tresci\n" + "password = haslo uzytkownika\n"
                    + "directory = sciezka do katalogu ze struktura obiektu do zaladowania\n"
                    + "rdfname='value' = rdfname - nazwa rdf atrybutu w systemie, dla ktorego ustawiamy wartosc, value - wartosc, mozna podac wiele takich par\n"
                    + "-N = nie przesylaj plikow dla aktualizowanych obiektow (podmieniaj tylko metadane)\n"
                    + "configfile = sciezka do konfiguracji polaczenia z Repozytorium Tresci, domyslnie plik config.properties");
            System.exit(-100);
        }

        Properties serverConfig = null;
        try {
            serverConfig = readConfiguration(args);
        } catch (FileNotFoundException e1) {
            System.err.println("Configuration file not found.");
            e1.printStackTrace();
            System.exit(-1);
        } catch (IOException e1) {
            System.err.println("IO problem with Configuration file.");
            e1.printStackTrace();
            System.exit(-2);
        }

        connect(serverConfig);
        DLToolkit.setIOConf(serverConfig);

        ArrayList<AbstractPropertiesToPublicationTransformer> transformers = analyzeDirectory(args[2]);
        PublicationId pubId = uploadPublications(transformers);
        if (pubId == null) {
            System.err.println("No publications were created.");
            System.exit(-3);
        }

        System.out.println(pubId);
        System.out.close();
        System.exit(0);
    }


    private static void connect(Properties serverConfig) {
        manager = new ServerManager(serverConfig);
    }


    private static Properties readConfiguration(String[] args)
            throws IOException, FileNotFoundException {
        String configFilename = null;
        for (int i = 3; i < args.length; i++) {
            String param = args[i];
            int eqIndex = param.indexOf("=");
            if (eqIndex != -1) {
                String rdfname = param.substring(0, eqIndex);
                String value = param.substring(eqIndex + 1);
                attrValues.put(rdfname, value);
            } else if (param.equals("-N")) {
                skipContent = true;
            } else if (configFilename == null) {
                configFilename = param;
            } else {
                System.err.println("Unrecognized parameter: " + param);
                System.exit(-1);
            }
        }
        if (configFilename == null)
            configFilename = ServerManager.DEFAULT_CONFIG_FILENAME;

        File serverConfFile = new File(configFilename);
        Properties serverConfig = new Properties();
        FileInputStream inputStream = new FileInputStream(serverConfFile);
        try {
            serverConfig.load(inputStream);
        } finally {
            inputStream.close();
        }

        serverConfig.put(ServerManager.LOGIN, args[0]);
        serverConfig.put(ServerManager.PASSWORD, args[1]);
        return serverConfig;
    }


    private static ArrayList<AbstractPropertiesToPublicationTransformer> analyzeDirectory(String pubDir)
            throws RemoteException, DLibraException {
        File directory = new File(pubDir);
        PublicationPropertizer.clearCache();
        ArrayList<AbstractPropertiesToPublicationTransformer> transformers = new ArrayList<AbstractPropertiesToPublicationTransformer>();
        ArrayList<TransformationProblem> problems = new ArrayList<TransformationProblem>();
        File[] dirContent = directory.listFiles();
        Arrays.sort(dirContent, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (int i = 0; i < dirContent.length; i++) {
            if (dirContent[i].isDirectory()) {
                File propsFile = new File(dirContent[i], PUBLICATION_PROPERTIES_FILENAME);
                System.out.println("Analyzing file: " + propsFile.getAbsolutePath());
                if (propsFile.exists()) {
                    AbstractPropertiesToPublicationTransformer propertiesToPublicationTransformer = new PropertiesToPublicationTransformer(
                            propsFile, manager.getMetadataServer());
                    propertiesToPublicationTransformer.setCheckContentFiles(!skipContent);
                    try {
                        propertiesToPublicationTransformer.transform(true);
                        transformers.add(propertiesToPublicationTransformer);
                        problems.addAll(propertiesToPublicationTransformer.getTransformationProblems());
                        checkExistingEditions(propertiesToPublicationTransformer, problems);
                    } catch (TransformationException e) {
                        e.setPropsFile(propsFile);
                        problems.add(new TransformationProblem(e));
                    }
                }
            }
        }
        if (problems.size() > 0) {
            System.out.println("Problems detected: " + problems.size());
            displayProblems(problems);
            System.exit(-200);
        }
        return transformers;
    }


    private static boolean checkExistingEditions(AbstractPropertiesToPublicationTransformer transformer,
            ArrayList<TransformationProblem> problems)
                    throws RemoteException, DLibraException {
        PublicationManager pm = manager.getMetadataServer().getPublicationManager();
        ArrayDeque<AbstractPropertiesToPublicationTransformer> queue = new ArrayDeque<AbstractPropertiesToPublicationTransformer>();
        queue.add(transformer);
        while (!queue.isEmpty()) {
            AbstractPropertiesToPublicationTransformer t = queue.remove();
            if (t.getEdition() != null) {
                if (!synchronizeWithExisting(t, problems, pm)) {
                    return false;
                }
            } else {
                List<AbstractPropertiesToPublicationTransformer> children = t.getChildTransformers();
                if (children != null)
                    queue.addAll(children);
            }
        }
        return true;
    }


    private static boolean synchronizeWithExisting(AbstractPropertiesToPublicationTransformer t,
            ArrayList<TransformationProblem> problems, PublicationManager pm)
                    throws IdNotFoundException, RemoteException, DLibraException {
        Edition tEdition = t.getEdition();
        Edition dlEdition = null;
        Publication tPublication = t.getPublication();
        if (tEdition.getId() != null && tEdition.getId().getId() != 0) {
            dlEdition = (Edition) pm
                    .getObjects(new EditionFilter((EditionId) tEdition.getId()), new OutputFilter(Edition.class))
                    .getResult();
        } else if (tEdition.getExternalId() != null) {
            dlEdition = (Edition) pm.getObjects(new EditionFilter().setExternalId(tEdition.getExternalId()),
                new OutputFilter(Edition.class)).getResult();
        } else if (tPublication.getId() != null && tPublication.getId().getId() != 0) {
            dlEdition = (Edition) pm
                    .getObjects(new PublicationFilter(null, tPublication.getId()), new OutputFilter(Edition.class))
                    .getResult();
        }

        if (dlEdition == null)
            return true;

        tEdition.setId(dlEdition.getId());
        tEdition.setParentId(dlEdition.getParentId());
        t.getPublication().setId(dlEdition.getParentId());
        AbstractPropertiesToPublicationTransformer parentTransformer = t;
        Publication parentPublication = (Publication) pm
                .getObjects(new PublicationFilter(t.getPublication().getId()), new OutputFilter(Publication.class))
                .getResult();
        while (true) {
            boolean transformerRoot = parentTransformer.getParentTransformer() == null;
            boolean publicationRoot = parentPublication.getParentPublicationId() == null;
            if (transformerRoot != publicationRoot) {
                problems.add(new TransformationProblem(true,
                        "Cannot replace existing object: hierarchy violated for " + t.getPublication().getName(),
                        t.getPropsFile()));
                return false;
            }
            if (transformerRoot) {
                break;
            }
            parentTransformer = parentTransformer.getParentTransformer();
            parentPublication = (Publication) pm
                    .getObjects(new PublicationFilter(parentPublication.getParentPublicationId()),
                        new OutputFilter(Publication.class))
                    .getResult();
            Publication transformerPublication = parentTransformer.getPublication();
            if (transformerPublication.getId() != null && transformerPublication.getId().getId() != 0) {
                if (!transformerPublication.getId().equals(parentPublication.getId())) {
                    problems.add(new TransformationProblem(true,
                            "Cannot replace existing object: hierarchy violated for " + t.getPublication().getName(),
                            t.getPropsFile()));
                    return false;
                }
            } else {
                transformerPublication.setId(parentPublication.getId());
                transformerPublication.setParentPublicationId(parentPublication.getParentPublicationId());
            }
            replacing.add(parentTransformer);
        }
        replacing.add(t);
        return true;
    }


    private static void displayProblems(ArrayList<TransformationProblem> problems) {
        for (TransformationProblem p : problems)
            System.out.println(p.getDescription() + ": " + p.getSummary());
    }


    private static PublicationId uploadPublications(List<AbstractPropertiesToPublicationTransformer> transformers) {
        EPService epService = manager.getEPService();
        for (AbstractPropertiesToPublicationTransformer t : transformers) {
            Publication publication = t.getPublication();
            if (publication.getGroupStatus() == Publication.PUB_GROUP_ROOT && publication.getId() != null) {
                try {
                    epService.textbookModificationStart(publication.getId());
                } catch (Exception e) {
                    log.error("Could not start textbook modification phase", e);
                }
            }
        }

        uploadQueue.addAll(transformers);

        for (int i = 1; i < THREAD_COUNT; i++) {
            new Thread("Uploader-" + i) {

                @Override
                public void run() {
                    uploaderLoop();
                };
            }.start();
        }
        uploaderLoop();

        for (AbstractPropertiesToPublicationTransformer t : transformers) {
            Publication publication = t.getPublication();
            if (publication.getGroupStatus() == Publication.PUB_GROUP_ROOT) {
                try {
                    epService.textbookModificationEnd(publication.getId(), !replacing.contains(t));
                } catch (Exception e) {
                    log.error("Could not end textbook modification phase", e);
                }
            }

            if (publication.getGroupStatus() == Publication.PUB_NORMAL && !skipContent && t.getEdition().getId() != null
                    && t.getMainPublicationFile().getDLibraFile() != null && t.getMainPublicationFile().getDLibraFile()
                            .getPath().equals("/" + WOMIXMLHandler.MAIN_FILE_NAME)) {
                try {
                    epService.reportWOMIModified((EditionId) t.getEdition().getId());
                } catch (Exception e) {
                    log.error("Modification reporting failed", e);
                }
            }
        }

        return createdPubsIds.isEmpty() ? null : createdPubsIds.get(0);
    }


    private static void uploaderLoop() {
        while (true) {
            Object next = null;
            try {
                next = uploadQueue.poll(1, TimeUnit.SECONDS);
                if (next == null) {
                    synchronized (PublicationUploader.class) {
                        waitingUploaderCount++;
                        if (waitingUploaderCount == THREAD_COUNT) {
                            for (int i = 0; i < THREAD_COUNT; i++)
                                uploadQueue.add(STOP);
                        }
                    }
                    next = uploadQueue.take();
                    synchronized (PublicationUploader.class) {
                        waitingUploaderCount--;
                    }
                }
                if (next == STOP)
                    break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (next instanceof AbstractPropertiesToPublicationTransformer) {
                upload((AbstractPropertiesToPublicationTransformer) next);
            } else if (next instanceof List) {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                List<AbstractPropertiesToPublicationTransformer> list = (List) next;
                for (AbstractPropertiesToPublicationTransformer t : list) {
                    upload(t);
                }
                updatePublicationsOrder(list.get(0).getParentTransformer());
            }
        }
    }


    private static void upload(AbstractPropertiesToPublicationTransformer transformer) {
        PublicationId pubId = null;
        try {
            if (transformer.getParentTransformer() != null) {
                Publication parentPublication = transformer.getParentTransformer().getPublication();
                transformer.getPublication().setParentPublicationId(parentPublication.getId());
            }
            pubId = transformer.getPublication().getId();
            final boolean replaceExisting = pubId != null && pubId.getId() != 0;
            List<EditionFile> pubFilesToUpload = transformer.getPublicationFiles();
            MetadataServer ms = manager.getMetadataServer();

            AttributeValueSet avs = transformer.getAvs();
            AttributeValueSet oldAVS = new AttributeValueSet();
            if (replaceExisting)
                oldAVS = ms.getElementMetadataManager().getAttributeValueSet(
                    pubFilesToUpload != null ? transformer.getEdition().getId() : transformer.getPublication().getId(),
                    AttributeValue.AV_ASSOC_DIRECT);
            String mainFilePath = pubFilesToUpload == null ? null : pubFilesToUpload.get(0).getRelativePath();
            String editorError = Versioning.checkEditorMode(avs, oldAVS, transformer.getPublication(),
                transformer.getEdition(), mainFilePath, replaceExisting, ms);
            if (editorError != null) {
                log.error(transformer.getPublication().getName() + ": Invalid editor mode value: " + editorError);
                return;
            }

            PublicationManager pm = ms.getPublicationManager();
            if (!replaceExisting) {
                pubId = pm.createPublication(transformer.getPublication());
                transformer.getPublication().setId(pubId);
            } else if (Versioning.isBlocked(pubId, manager.getUserServer())) {
                log.error("Object " + pubId + " is blocked due to a later version existing");
                return;
            }

            setPublicationRights(transformer.getActorsToOperations(), pubId);

            addPublicationToCollections(transformer.getCollections(), pubId);
            if (replaceExisting && pubFilesToUpload != null && skipContent) {
                updatePublicationData(transformer.getPublication(), pubId, null);
                Edition edition = transformer.getEdition();
                pm.setEditionData(edition);
                setAVS(edition.getId(), transformer);
            } else if (pubFilesToUpload != null) {
                List<Version> versions = createVersions(pubId, pubFilesToUpload);

                updatePublicationData(transformer.getPublication(), pubId,
                    transformer.getMainPublicationFile().getDLibraFile().getId());

                uploadFiles(pubFilesToUpload, versions);

                Edition edition = transformer.getEdition();
                if (replaceExisting) {
                    updateEditionFiles(pubFilesToUpload, edition);
                } else {
                    edition.setId(null);
                    edition.setParentId(pubId);
                    edition.setName(transformer.getPublication().getName());
                    edition = createEdition(pubFilesToUpload, edition);
                }

                setAVS(edition.getId(), transformer);

                edition.setPublished(true);
                pm.setEditionData(edition);
            } else {
                if (replaceExisting) {
                    updatePublicationData(transformer.getPublication(), pubId, null);
                }
                setAVS(pubId, transformer);
            }

            createdPubsIds.add(pubId);
            List<AbstractPropertiesToPublicationTransformer> childTransformers = transformer.getChildTransformers();
            if (childTransformers != null && !childTransformers.isEmpty()) {
                uploadQueue.add(childTransformers);
            }
        } catch (Exception e) {
            System.out.println("Upload failed for " + transformer.getPropsFile().getParent());
            System.err.println("Error when uploading publication " + transformer.getPropsFile().getParent());
            e.printStackTrace();
        }
    }


    private static void updateEditionFiles(List<EditionFile> pubFilesToUpload, Edition edition)
            throws DLibraException, RemoteException, AccessDeniedException, IdNotFoundException {
        PublicationManager pm = manager.getMetadataServer().getPublicationManager();
        EditionId editionId = (EditionId) edition.getId();
        VersionId[] versionIds = new VersionId[pubFilesToUpload.size()];
        for (int i = 0; i < versionIds.length; i++) {
            versionIds[i] = pubFilesToUpload.get(i).getVersion().getId();
        }
        boolean isPublished = edition.getState() == Edition.PUBLISHED;
        if (isPublished)
            pm.setEditionState(editionId, false);
        pm.updateEditionVersions(editionId, versionIds);
        if (isPublished)
            pm.setEditionState(editionId, true);
    }


    private static void setAVS(ElementId elementId, AbstractPropertiesToPublicationTransformer transformer)
            throws RemoteException, AmbiguousAttributeValueException, DLibraException {
        AttributeValueSet avs = transformer.getAvs();

        if (avs != null) {
            addExtraAttributeValues(avs);

            MetadataServer ms = manager.getMetadataServer();
            avs = MetadataConverter.updateAVS(avs, true, ms);
            avs.setElementId(elementId);

            ElementMetadataManager emm = ms.getElementMetadataManager();
            AttributeValueSet oldAVS = emm.getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
            String message = Versioning.checkVersions(avs, oldAVS, manager.getUserServiceResolver(),
                manager.getLoggedUser().getId(), new Publication[1]);
            if (message != null && !message.isEmpty()) {
                log.error("Assigning RootID failed: " + message);
            }

            Versioning.checkVersionsFollowUp(avs, oldAVS, true, manager.getUserServiceResolver());
            emm.setAttributeValueSet(avs);
        }
    }


    private static void addExtraAttributeValues(AttributeValueSet avs)
            throws RemoteException, DLibraException {
        for (Map.Entry<String, String> entry : attrValues.entrySet()) {
            String rdfname = entry.getKey();
            AttributeId attrId = (AttributeId) manager.getMetadataServer().getAttributeManager()
                    .getObjects(
                        new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList(new String[] { rdfname })),
                        new OutputFilter(AttributeId.class))
                    .getResultId();
            if (attrId != null) {
                AttributeValue value = new AttributeValue(null, attrId, null);
                value.setValue(entry.getValue());
                value.setLanguageName(manager.getMetadataServer().getLanguageManager().getMostFrequentLanguage());

                List<AbstractAttributeValue> values = new ArrayList<AbstractAttributeValue>();
                values.add(value);
                values.addAll(avs.getAttributeValues(attrId, value.getLanguageName(), Values.OnlyDirect));
                avs.setDirectAttributeValues(attrId,
                    manager.getMetadataServer().getLanguageManager().getMostFrequentLanguage(), values);
            }
        }
    }


    private static Edition createEdition(List<EditionFile> pubFilesToUpload, Edition newEdition)
            throws DLibraException, AccessDeniedException, IdNotFoundException, RemoteException {
        VersionId[] versionIds = new VersionId[pubFilesToUpload.size()];

        for (int i = 0; i < versionIds.length; i++) {
            versionIds[i] = pubFilesToUpload.get(i).getVersion().getId();
        }

        EditionId newEditionId = manager.getMetadataServer().getPublicationManager().createEdition(newEdition,
            versionIds);

        newEdition = (Edition) manager.getMetadataServer().getPublicationManager()
                .getObjects(new EditionFilter(newEditionId), new OutputFilter(Edition.class)).getResult();

        return newEdition;
    }


    private static void uploadFiles(List<EditionFile> pubFilesToUpload, List<Version> versions)
            throws DLibraException, IOException, ParseException, OperationFailedException {
        FilesUploader uploader = new FilesUploader(manager.getContentServer());
        uploader.uploadFiles(pubFilesToUpload, versions);
    }


    private static void updatePublicationData(Publication publication, PublicationId pubId, FileId fileId)
            throws IdNotFoundException, RemoteException, DLibraException {
        publication.setMainFileId(fileId);
        publication.setId(pubId);
        manager.getMetadataServer().getPublicationManager().setPublicationData(publication);
    }


    private static void updatePublicationsOrder(AbstractPropertiesToPublicationTransformer transformer) {
        if (!replacing.contains(transformer))
            return;
        List<PublicationId> positions = new ArrayList<PublicationId>();
        for (AbstractPropertiesToPublicationTransformer child : transformer.getChildTransformers()) {
            positions.add(child.getPublication().getId());
        }
        try {
            manager.getMetadataServer().getPublicationManager().setPositions(transformer.getPublication().getId(),
                positions);
        } catch (Exception e) {
            log.error("Could not set objects order", e);
        }
    }


    private static List<Version> createVersions(PublicationId pubId, List<EditionFile> pubFilesToUpload)
            throws RemoteException, IdNotFoundException, DLibraException {
        VersionsCreator creator = new VersionsCreator(pubId, manager.getMetadataServer().getFileManager());
        return creator.createVersions(pubFilesToUpload);
    }


    private static void addPublicationToCollections(Set<LibCollectionId> libCollections, PublicationId pubId)
            throws IdNotFoundException, RemoteException, AccessDeniedException, DLibraException {
        if (libCollections.size() > 0) {
            Collection<ElementId> elemIds = new ArrayList<ElementId>();
            elemIds.add(pubId);
            manager.getMetadataServer().getLibCollectionManager().addToCollections(libCollections, elemIds, false);
        }
    }


    private static void setPublicationRights(Map<String, ArrayList<RightOperation>> map, PublicationId pubId)
            throws RemoteException, IdNotFoundException, AccessDeniedException, DLibraException {
        RightManager rightManager = manager.getUserServer().getRightManager();
        UserManager userManager = manager.getUserServer().getUserManager();
        for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
            String stringActorId = iter.next();
            ArrayList<RightOperation> operations = map.get(stringActorId);
            ArrayList<ActorId> actors = new ArrayList<ActorId>();
            ActorId actorId = userManager.getActorId(stringActorId);
            if (actorId == null) {
                throw new IdNotFoundException(stringActorId);
            }
            actors.add(actorId);
            for (RightOperation operation : operations) {
                rightManager.setPublicationRights(pubId, actors, operation);
            }
        }
    }
}
