package pl.psnc.ep.rt.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.app.gui.common.propertizer.EditionPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.LibCollectionsPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.PublicationPropertizer;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryInfo;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.Element;
import pl.psnc.dlibra.metadata.ElementInfo;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileInfo;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.ElementMetadataManager;
import pl.psnc.dlibra.multipubuploader.transformer.ExistingPublicationToPropertiesTransformer;
import pl.psnc.dlibra.multipubuploader.wizard.MultiPubUploadManager;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.util.IOUtils;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.LongStringParser;
import com.martiansoftware.jsap.stringparsers.StringStringParser;

public class PublicationDownloader {

    private static final String ARG_OBJECT = "object";

    private static final String ARG_WOMI = "womi";

    private static final String ARG_DIR = "dir";

    private static final String ARG_DIR_ALL = "dir-all";

    private static final String ARG_CONF = "conf";

    private static final String ARG_FOLDER = "folder";

    private static final String ARG_LOGIN = "login";

    private static final String ARG_PASSWORD = "password";

    private static final String ARG_NO_FILES = "no-files";

    private static final String METADATA_FILE = "metadata.xml";

    private static final Logger logger = Logger.getLogger(PublicationDownloader.class);

    private static ServerManager manager = null;

    private static File targetFolder;

    private static boolean skipContent;

    private static Map<AttributeId, String> attributeToRDFName;

    private static int failures = 0;


    public static void main(String[] args)
            throws IOException, DLibraException, JSAPException {
        SimpleJSAP argsParser = new SimpleJSAP("downloader", "pobiera wybrane obiekty z Repozytorium Tresci");
        argsParser.registerParameter(new UnflaggedOption(ARG_LOGIN, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_PASSWORD, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_FOLDER, FileStringParser.getParser(), true,
                "sciezka do katalgou, w ktorym ma byc zapisany wynik"));
        argsParser.registerParameter(new FlaggedOption(ARG_CONF, FileStringParser.getParser(), "config.properties",
                false, 'c', "config", "plik konfiguracyjny"));
        argsParser.registerParameter(new FlaggedOption(ARG_DIR, LongStringParser.getParser(), null, false, 'd', "dir",
                "identyfikator katalogu w repozytorium"));
        argsParser.registerParameter(new FlaggedOption(ARG_DIR_ALL, LongStringParser.getParser(), null, false, 'D',
                "dir-all", "identyfikator katalogu w repozytorium + pobranie podkatalogow"));
        FlaggedOption elementOption = new FlaggedOption(ARG_OBJECT, LongStringParser.getParser(), null, false, 'o',
                "object", "identyfikator obiektu w repozytorium");
        elementOption.setAllowMultipleDeclarations(true);
        argsParser.registerParameter(elementOption);
        elementOption = new FlaggedOption(ARG_WOMI, LongStringParser.getParser(), null, false, 'w', "womi",
                "identyfikator womi w repozytorium");
        elementOption.setAllowMultipleDeclarations(true);
        argsParser.registerParameter(elementOption);
        argsParser.registerParameter(
            new Switch(ARG_NO_FILES, 'N', "no-files", "Nie pobieraj plikow zawartych w obiektach"));
        JSAPResult parsedArgs = argsParser.parse(args);
        if (argsParser.messagePrinted())
            System.exit(1);

        targetFolder = parsedArgs.getFile(ARG_FOLDER);
        if (!targetFolder.isDirectory() && !targetFolder.mkdirs()) {
            logger.error("Invalid target folder: " + targetFolder);
        }

        Properties serverConfig = new Properties();
        FileInputStream configIS = new FileInputStream(parsedArgs.getFile(ARG_CONF));
        try {
            serverConfig.load(configIS);
        } finally {
            configIS.close();
        }
        serverConfig.put(ServerManager.LOGIN, parsedArgs.getString(ARG_LOGIN));
        serverConfig.put(ServerManager.PASSWORD, parsedArgs.getString(ARG_PASSWORD));
        manager = new ServerManager(serverConfig);
        DLToolkit.setIOConf(serverConfig);
        skipContent = parsedArgs.getBoolean(ARG_NO_FILES);

        if (parsedArgs.userSpecified(ARG_DIR))
            downloadDirContents(new DirectoryId(parsedArgs.getLong(ARG_DIR)), null);
        if (parsedArgs.userSpecified(ARG_DIR_ALL))
            downloadDirContents(new DirectoryId(parsedArgs.getLong(ARG_DIR_ALL)), "");

        if (parsedArgs.userSpecified(ARG_OBJECT)) {
            PublicationManager pm = manager.getMetadataServer().getPublicationManager();
            for (long pubId : parsedArgs.getLongArray(ARG_OBJECT)) {
                try {
                    AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) pm
                            .getObjects(new PublicationFilter(new PublicationId(pubId)),
                                new OutputFilter(AbstractPublicationInfo.class))
                            .getResultInfo();
                    downloadPublication(pubInfo, null, pm);
                } catch (Exception e) {
                    logger.error("Error while downloading object " + pubId, e);
                    failures++;
                }
            }
        }

        if (parsedArgs.userSpecified(ARG_WOMI)) {
            PublicationManager pm = manager.getMetadataServer().getPublicationManager();
            for (long ediId : parsedArgs.getLongArray(ARG_WOMI)) {
                try {
                    AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) pm
                            .getObjects(new EditionFilter(new EditionId(ediId)),
                                new OutputFilter(AbstractPublicationInfo.class))
                            .getResultInfo();
                    downloadPublication(pubInfo, null, pm);
                } catch (Exception e) {
                    logger.error("Error while downloading womi " + ediId, e);
                    failures++;
                }
            }
        }

        if (failures == 0) {
            logger.info("Success!");
            System.exit(0);
        } else {
            logger.error(failures + " operation(s) failed.");
            System.exit(1);
        }
    }


    private static void downloadDirContents(DirectoryId dirId, String folderNamePrefix)
            throws RemoteException, DLibraException {
        DirectoryManager dm = manager.getMetadataServer().getDirectoryManager();
        PublicationManager pm = manager.getMetadataServer().getPublicationManager();
        try {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<ElementInfo> elements = (List) dm
                    .getObjects(
                        new DirectoryFilter(null, dirId)
                                .setGroupStatus((byte) (Publication.PUB_NORMAL + Publication.PUB_GROUP_ROOT))
                                .setState(Publication.PUB_STATE_ACTUAL),
                new OutputFilter(ElementInfo.class, List.class)).getResultInfos();

            for (ElementInfo element : elements) {
                try {
                    if (element instanceof DirectoryInfo) {
                        boolean recursive = folderNamePrefix != null;
                        if (recursive) {
                            String subfolderNamePrefix = folderNamePrefix + "_"
                                    + element.getLabel().replaceAll("[^a-zA-Z0-9]+", "-");
                            downloadDirContents((DirectoryId) element.getId(), subfolderNamePrefix);
                        } else {
                            logger.info("Directory " + element.getId() + " will be skipped");
                        }
                    } else if (element instanceof AbstractPublicationInfo) {
                        AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) element;
                        downloadPublication(pubInfo, folderNamePrefix, pm);
                    }
                } catch (Exception e) {
                    logger.error("Error downloading object " + element.getId(), e);
                    failures++;
                }
            }
        } catch (IdNotFoundException e) {
            logger.error("Directory " + e.getNotFoundId() + " does not exist or is empty", e);
            System.exit(-4);
        }
    }


    private static void downloadPublication(AbstractPublicationInfo pubInfo, String folderNamePrefix,
            PublicationManager pm)
                    throws IdNotFoundException, RemoteException, DLibraException {
        if (pubInfo.getStatus() == Publication.PUB_GROUP_ROOT) {
            logger.info("Downloading handbook " + pubInfo.getId() + ": " + pubInfo.getLabel());
            Publication publication = (Publication) pm
                    .getObjects(new PublicationFilter(pubInfo.getId()), new OutputFilter(Publication.class))
                    .getResult();
            downloadGroup(publication, folderNamePrefix, targetFolder);
        } else if (pubInfo.getStatus() == Publication.PUB_NORMAL) {
            logger.info("Downloading womi " + pubInfo.getId() + ": " + pubInfo.getLabel());
            downloadSinglePublication(pubInfo.getId(), folderNamePrefix, targetFolder);
        } else {
            logger.error("Invalid state of object " + pubInfo.getId() + " (publication)");
            failures++;
        }
    }


    private static void downloadGroup(Publication publication, String folderNamePrefix, File targetFolder)
            throws RemoteException, DLibraException {
        File pubFolder = new File(targetFolder, getFoldername(publication, folderNamePrefix));
        pubFolder.mkdirs();

        Properties properties = new Properties();
        PublicationPropertizer publicationPropertizer = new PublicationPropertizer();
        properties.putAll(publicationPropertizer.propertize(publication));
        properties.put(ExistingPublicationToPropertiesTransformer.PUBLICATION_METADATAFILE, METADATA_FILE);
        if ((publication.getGroupStatus() & (Publication.PUB_GROUP_MID | Publication.PUB_GROUP_LEAF)) > 0) {
            properties.remove(PublicationPropertizer.PUBLICATION_DESTINATION_DIRECTORYID);
            properties.remove(PublicationPropertizer.PUBLICATION_DESTINATION_PARENTPUBLICATIONID);
        }

        MetadataServer ms = manager.getMetadataServer();
        LibCollectionsPropertizer collectionsPropertizer = new LibCollectionsPropertizer();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<LibCollectionId> collections = (Collection) ms.getPublicationManager()
                .getObjects(new PublicationFilter(null, publication.getId()).setAssocType(Publication.PUB_ASSOC_DIRECT),
                    new OutputFilter(LibCollectionId.class))
                .getResultIds();
        properties.putAll(collectionsPropertizer.propertize(new HashSet<LibCollectionId>(collections)));

        storeProperties(properties, pubFolder);

        storeMetadata(publication, pubFolder, ms);

        PublicationManager pm = ms.getPublicationManager();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<Publication> subPublications = (List) pm.getObjects(
            new PublicationFilter(null, publication.getId()).setPublicationState(Publication.PUB_STATE_ACTUAL),
            new OutputFilter(Publication.class)).getResults();
        for (Publication subPublication : subPublications) {
            if (subPublication.getGroupStatus() == Publication.PUB_GROUP_LEAF) {
                downloadSinglePublication((PublicationId) subPublication.getInfo().getId(), null, pubFolder);
            } else {
                downloadGroup(subPublication, null, pubFolder);
            }
        }
    }


    private static void downloadSinglePublication(PublicationId pubId, String folderNamePrefix, File targetFolder)
            throws RemoteException, DLibraException {
        MetadataServer ms = manager.getMetadataServer();
        PublicationManager pm = ms.getPublicationManager();
        Edition edition = (Edition) pm.getObjects(new PublicationFilter(null, pubId), new OutputFilter(Edition.class))
                .getResult();
        File pubFolder = new File(targetFolder, getFoldername(edition, folderNamePrefix));
        pubFolder.mkdirs();

        if (!skipContent)
            downloadFiles(edition, pubFolder, ms);

        storeProperties(edition, pubId, pubFolder, ms);

        storeMetadata(edition, pubFolder, ms);
    }


    private static void downloadFiles(Edition edition, File pubFolder, MetadataServer ms)
            throws RemoteException, IdNotFoundException, DLibraException {
        FileManager fm = ms.getFileManager();
        PublicationManager pm = ms.getPublicationManager();
        ContentServer cs = manager.getContentServer();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<VersionId> versionIds = (List) pm
                .getObjects(new EditionFilter((EditionId) edition.getId()), new OutputFilter(VersionId.class))
                .getResultIds();
        for (VersionId versionId : versionIds) {
            FileInfo fileInfo = (FileInfo) fm.getObjects(new InputFilter(versionId), new OutputFilter(FileInfo.class))
                    .getResultInfo();
            File fileStorage = new File(pubFolder, fileInfo.getFullPath());
            fileStorage.getParentFile().mkdirs();

            try {
                downloadFile(versionId, fileStorage, cs);
            } catch (IOException e) {
                logger.error("Error downloading file " + fileInfo.getFullPath(), e);
                failures++;
            }
        }
    }


    private static void downloadFile(VersionId versionId, File fileStorage, ContentServer cs)
            throws DLibraException, IOException {
        InputStream is = cs.getVersionInputStream(versionId);
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(fileStorage));
            try {
                IOUtils.copyStream(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
            cs.releaseElement(versionId);
        }
    }


    private static void storeProperties(Edition edition, PublicationId pubId, File pubFolder, MetadataServer ms)
            throws IdNotFoundException, RemoteException, DLibraException {
        EditionPropertizer editionPropertizer = new EditionPropertizer(pubFolder);
        Properties properties = editionPropertizer.propertize(edition);
        properties.remove(EditionPropertizer.EDITION_NAME);

        PublicationManager pm = ms.getPublicationManager();
        Publication publication = (Publication) pm
                .getObjects(new PublicationFilter(pubId), new OutputFilter(Publication.class)).getResult();
        PublicationPropertizer publicationPropertizer = new PublicationPropertizer();
        properties.putAll(publicationPropertizer.propertize(publication));
        if ((publication.getGroupStatus() & (Publication.PUB_GROUP_MID | Publication.PUB_GROUP_LEAF)) > 0) {
            properties.remove(PublicationPropertizer.PUBLICATION_DESTINATION_DIRECTORYID);
            properties.remove(PublicationPropertizer.PUBLICATION_DESTINATION_PARENTPUBLICATIONID);
        }

        LibCollectionsPropertizer collectionsPropertizer = new LibCollectionsPropertizer();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<LibCollectionId> collections = (Collection) pm
                .getObjects(new PublicationFilter(null, pubId).setAssocType(Publication.PUB_ASSOC_DIRECT),
                    new OutputFilter(LibCollectionId.class))
                .getResultIds();
        properties.putAll(collectionsPropertizer.propertize(new HashSet<LibCollectionId>(collections)));

        properties.setProperty(ExistingPublicationToPropertiesTransformer.PUBLICATION_METADATAFILE, METADATA_FILE);

        FileManager fm = ms.getFileManager();
        FileInfo fileInfo = (FileInfo) fm
                .getObjects(new FileFilter(publication.getMainFileId()), new OutputFilter(FileInfo.class))
                .getResultInfo();
        String filePath = fileInfo.getFullPath().replaceFirst("^/", "");
        properties.setProperty(ExistingPublicationToPropertiesTransformer.PUBLICATION_MAINFILE, filePath);

        storeProperties(properties, pubFolder);
    }


    private static void storeProperties(Properties properties, File pubFolder) {
        File propertiesFile = new File(pubFolder, MultiPubUploadManager.PUBLICATION_PROPERTIES_FILENAME);
        try {
            FileOutputStream fos = new FileOutputStream(propertiesFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            try {
                properties.store(writer, null);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            logger.error("Could not save " + propertiesFile, e);
            failures++;
        }
    }


    private static void storeMetadata(Element element, File pubFolder, MetadataServer ms)
            throws RemoteException, DLibraException {
        if (attributeToRDFName == null) {
            attributeToRDFName = new HashMap<AttributeId, String>();
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<AttributeInfo> infos = (List) ms.getAttributeManager()
                    .getObjects((AttributeFilter) new AttributeFilter(null, null).setRecursive(true),
                        new OutputFilter(AttributeInfo.class))
                    .getResultInfos();
            for (AttributeInfo info : infos) {
                attributeToRDFName.put(info.getId(), info.getRDFName());
            }
        }

        ElementMetadataManager emm = ms.getElementMetadataManager();
        AttributeValueSet avs = emm.getAttributeValueSet(element.getId(), AttributeValue.AV_ASSOC_DIRECT);
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            doc = factory.newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);
            org.w3c.dom.Element root = doc.createElement("metadata");
            doc.appendChild(root);

            for (AttributeId attributeId : avs.getAttributeIds()) {
                Collection<AbstractAttributeValue> attributeValues = avs.getAttributeValues(attributeId);
                for (AbstractAttributeValue av : attributeValues) {
                    org.w3c.dom.Element item = doc.createElement(attributeToRDFName.get(attributeId));
                    item.setTextContent(av.getValue());
                    root.appendChild(item);
                }
            }
        } catch (ParserConfigurationException e) {
            logger.error("Could not create metadata xml", e);
            failures++;
            return;
        }

        File metadataFile = new File(pubFolder, METADATA_FILE);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            OutputStream os = new FileOutputStream(metadataFile);
            try {
                StreamResult result = new StreamResult(os);
                transformer.transform(source, result);
            } finally {
                os.close();
            }
        } catch (IOException e) {
            logger.error("Could not save " + metadataFile, e);
            failures++;
        } catch (TransformerException e) {
            logger.error("Could not save " + metadataFile, e);
            failures++;
        }
    }


    private static String getFoldername(Element elementInfo, String folderNamePrefix) {
        String label = elementInfo.getName();
        label = label.replaceAll("[^a-zA-Z0-9]+", "-");
        if (label.length() > 20)
            label = label.substring(0, 20);
        if (folderNamePrefix != null && !folderNamePrefix.isEmpty()) {
            return folderNamePrefix + "." + elementInfo.getId() + "_" + label;
        }
        return elementInfo.getId() + "_" + label;
    }
}
