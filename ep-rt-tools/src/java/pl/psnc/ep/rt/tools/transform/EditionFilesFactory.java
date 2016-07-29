package pl.psnc.ep.rt.tools.transform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.psnc.activation.ExtensionFileTypeRecognizer;
import pl.psnc.dlibra.app.common.files.DataSourceEditionFile;
import pl.psnc.dlibra.app.common.files.EditionAppFile;
import pl.psnc.dlibra.app.common.files.EditionAppFiles;
import pl.psnc.dlibra.app.common.files.EditionFile;
import pl.psnc.dlibra.app.common.files.EditionFiles;
import pl.psnc.dlibra.app.eventserver.metadata.EventMetadataServer;
import pl.psnc.dlibra.app.extension.Extension;
import pl.psnc.dlibra.app.extension.datasource.FileInfo;
import pl.psnc.dlibra.app.extension.datasource.LocalResourceUnavailableException;
import pl.psnc.dlibra.app.extension.fileparser.FileParser;
import pl.psnc.dlibra.app.parser.html.HtmlEncodingFinder;
import pl.psnc.dlibra.app.parser.html.ParseException;
import pl.psnc.dlibra.app.plugins.ApplicationExtension;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.ep.rt.tools.plugins.MetadataManager;

public class EditionFilesFactory {

    private java.util.List<FileParser> parsers = new ArrayList<FileParser>();

    private Map<String, EditionFile> pubFiles;

    private static EditionFilesFactory instance;

    private MetadataServer metadataServer = null;


    public static EditionFilesFactory getInstance() {
        if (instance == null) {
            instance = new EditionFilesFactory();
        }

        return instance;
    }


    private EditionFilesFactory() {
        Collection<Extension> fileParsers = ApplicationExtension.getInstance()
                .getExtensionsMap(ApplicationExtension.ExtensionType.FILES).keySet();

        for (Extension fp : fileParsers) {
            registerParser((FileParser) fp);
        }
    }


    public void setMetadataServer(MetadataServer ms) {
        this.metadataServer = ms;
    }


    public EditionFiles createFiles(URL mainFileURL) {
        if (mainFileURL == null) {
            return null;
        }

        pubFiles = new HashMap<String, EditionFile>();

        EditionAppFile eMainFile = null;

        File mainFile;
        try {
            mainFile = new File(mainFileURL.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't get File from path", e);
        }
        eMainFile = new EditionAppFile(mainFile, mainFile.getParent() == null ? "" : mainFile.getParent());

        eMainFile.setMainFile(true);

        try {
            if (!eMainFile.getSourceFile().isDirectory()) {
                pubFiles.put(eMainFile.getSourceFile().getCanonicalPath(), eMainFile);
            }
        } catch (IOException e1) {
            showReadError(mainFile);
            return null;
        }

        EditionFiles eFiles = new EditionAppFiles(pubFiles, eMainFile);

        if (Thread.currentThread().isInterrupted()) {
            return eFiles;
        }

        try {
            findRelated(eMainFile);
        } catch (InterruptedException e1) {
            return eFiles;
        }

        if (Thread.currentThread().isInterrupted()) {
            return eFiles;
        }

        try {
            findNotRelated(eMainFile.getSourceFile().isDirectory() ? eMainFile.getSourceFile()
                    : eMainFile.getSourceFile().getParentFile(),
                eMainFile.getMainDirectoryPath());
        } catch (InterruptedException e1) {
            return eFiles;
        }

        if (Thread.currentThread().isInterrupted()) {
            return eFiles;
        }

        try {
            loadAttribtueValueSet(eMainFile, eFiles);
        } catch (Exception e) {
            System.err.println("Reading metadata file failed. " + "No metadata will be automatically loaded.");
        }

        if (Thread.currentThread().isInterrupted()) {
            return eFiles;
        }

        updateFilesWithEncoding(eFiles);
        if (eFiles.getMainFile() != null && eFiles.getMainFile().getSourceFile() != null
                && eFiles.getMainFile().getSourceFile().isDirectory()) {
            return new EditionAppFiles(pubFiles, null);
        }
        return eFiles;
    }


    private void updateFilesWithEncoding(EditionFiles htmlFiles) {
        HtmlEncodingFinder encodingFinder = new HtmlEncodingFinder();

        for (EditionFile file : htmlFiles.getFiles()) {

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            try {
                file.setEncodable(ExtensionFileTypeRecognizer.getMimeType(file.getSourceFile()).toString()
                        .equalsIgnoreCase("text/html"));
                if (file.isEncodable()) {
                    file.setEncoding(encodingFinder.getEncoding(file.getSourceFile()));
                }
            } catch (Throwable e) {
                file.setEncoding(null);
            }
        }
    }


    private void showReadError(File file) {
        System.err.println("A problem with reading #1 file occurred, please make sure that this file is correct."
                .replace("#1", file != null ? file.getAbsolutePath() : ""));
    }


    private void loadAttribtueValueSet(EditionAppFile eMainFile, EditionFiles editionFiles)
            throws IOException, IdNotFoundException, DLibraException {
        MetadataManager metadataManager = MetadataManager.getInstance();

        List<AttributeValueSet> sets = metadataManager.findMetadata(eMainFile.getSourceFile(), true, metadataServer);
        File mFile = metadataManager.findMetadataFile(eMainFile.getSourceFile());

        if (sets != null && sets.size() != 0 && mFile != null) {
            AttributeValueSet set = sets.get(0);

            editionFiles.setMetadata(set);
            editionFiles.setMetadataFile(mFile);
        }
    }


    private void findNotRelated(File file, String mDirPath)
            throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files == null) {
                return;
            }
            for (File f : files) {
                findNotRelated(f, mDirPath);
            }
        } else {
            String path;
            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                path = file.getAbsolutePath();
            }
            if (pubFiles.get(path) == null) {
                pubFiles.put(path, new EditionAppFile(file, mDirPath));
            }
        }
    }


    private void findRelated(EditionFile eFile)
            throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        if (eFile.isMissing()) {
            return;
        }

        if (tryParsers(eFile)) {
            return;
        }

        if (eFile.getSourceFile().isDirectory()) {
            File[] files = eFile.getSourceFile().listFiles();
            for (File file : files) {
                findRelated(new EditionAppFile(file, eFile.getMainDirectoryPath()));
            }
            return;
        }
    }


    public List<URL> findRelated(URL mainURL)
            throws FileNotFoundException, Exception {
        for (FileParser parser : parsers) {
            if (parser.supports(mainURL)) {
                return parser.parseFile(mainURL);
            }
        }
        return null;
    }


    private boolean tryParsers(EditionFile eFile) {
        List<URL> related;
        try {
            URL url = eFile.getSourceFile().toURI().toURL();
            related = findRelated(url);
        } catch (FileNotFoundException e) {
            return false;
        } catch (ParseException e) {
            eFile.setInvalid(true);
            return false;
        } catch (IOException e) {
            eFile.setInvalid(true);
            return false;
        } catch (Exception e) {
            System.err.println("Other problem when parsing edition file.\n" + e.getMessage());
            return false;
        }

        if (related == null)
            return false;

        for (URL url : related) {
            File file = null;
            try {
                file = new File(url.toURI());
                if (file.isDirectory())
                    continue;
                EditionFile ediFile = new EditionAppFile(file, eFile.getMainDirectoryPath());
                ediFile.addLinkedFrom(eFile);
                eFile.addLinkedTo(ediFile);
                String canonicalPath = ediFile.getSourceFile().getCanonicalPath();
                if (!pubFiles.containsKey(canonicalPath)) {
                    pubFiles.put(canonicalPath, ediFile);
                    findRelated(ediFile);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException("Can't get url from file");
            } catch (InterruptedException e) {
                return false;
            } catch (IOException e) {
                showReadError(file);
                return false;
            }
        }

        return true;
    }


    public void registerParser(FileParser parser) {
        parsers.add(parser);
    }


    private Version findVersion(pl.psnc.dlibra.metadata.File f, List<Version> versions) {
        for (Version version : versions) {
            if (version.getFileId().equals(f.getId())) {
                return version;
            }
        }
        return null;
    }


    public EditionFiles updateFiles(EditionFiles newestFiles, String sourceDirectory) {
        if (sourceDirectory == null) {
            return newestFiles;
        }

        newestFiles.update(sourceDirectory);

        if (newestFiles.getMainFile() != null && newestFiles.getMainFile().getSourceFile() != null
                && newestFiles.getMainFile().getSourceFile().exists()) {

            pubFiles = new HashMap<String, EditionFile>();
            EditionFiles eFiles = new EditionAppFiles(pubFiles, newestFiles.getMainFile());

            try {
                findRelated(newestFiles.getMainFile());
            } catch (InterruptedException e1) {
                return eFiles;
            }
            try {
                findNotRelated(newestFiles.getMainFile().getSourceFile().getParentFile(), sourceDirectory);
            } catch (InterruptedException e1) {
                return eFiles;
            }

            updateFilesWithEncoding(eFiles);

            for (EditionFile ef : eFiles.getFiles()) {
                if (!ef.isMissing()) {
                    for (EditionFile nf : newestFiles.getFiles()) {
                        if (!nf.isMissing()) {
                            try {
                                if (ef.getSourceFile().getCanonicalPath()
                                        .equals(nf.getSourceFile().getCanonicalPath())) {
                                    ef.setDLibraFile(nf.getDLibraFile());
                                    ef.setVersion(nf.getVersion());
                                }
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }

            return eFiles;
        }

        return null;
    }


    public EditionFiles createFiles(List<pl.psnc.dlibra.metadata.File> files, List<Version> versions,
            FileId mainFileId) {
        pubFiles = new HashMap<String, EditionFile>();

        EditionAppFile mf = null;
        for (pl.psnc.dlibra.metadata.File f : files) {
            EditionAppFile ef = new EditionAppFile(f.getPath());
            ef.setDLibraFile(f);
            ef.setVersion(findVersion(f, versions));

            if (f.getId().equals(mainFileId)) {
                mf = ef;
            }
            pubFiles.put(f.getPath().replace('/', File.separatorChar), ef);
        }

        return new EditionAppFiles(pubFiles, mf);
    }


    public EditionFiles createFiles(EventMetadataServer server, List<VersionInfo> versionsInfos)
            throws IdNotFoundException, RemoteException, AccessDeniedException, DLibraException {
        pubFiles = new HashMap<String, EditionFile>();

        List<FileId> filesIds = new ArrayList<FileId>();
        for (VersionInfo info : versionsInfos) {
            filesIds.add(info.getFileId());
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<pl.psnc.dlibra.metadata.File> files = new ArrayList(server.getFileManager()
                .getObjects(new FileFilter(filesIds), new OutputFilter(pl.psnc.dlibra.metadata.File.class))
                .getResults());

        FileId fileId = null;

        if (files.size() > 0) {
            fileId = ((Publication) server.getPublicationManager()
                    .getObjects(new PublicationFilter(files.get(0).getPublicationId()),
                        new OutputFilter(Publication.class))
                    .getResult()).getMainFileId();
        }

        EditionAppFile mf = null;

        for (pl.psnc.dlibra.metadata.File f : files) {

            EditionAppFile ef = new EditionAppFile(f.getPath());

            if (f.getId().equals(fileId)) {
                ef.setMainFile(true);
                mf = ef;
            }

            ef.setDLibraFile(f);
            List<Version> versions = getVersions(server, f);
            ef.setVersions(versions);
            ef.setVersion(getVersion(versionsInfos, versions));
            pubFiles.put(f.getPath().replace('/', File.separatorChar), ef);
        }

        return new EditionAppFiles(pubFiles, mf);
    }


    public EditionFiles createFiles(EventMetadataServer eventMetadataServer, ElementId elementId)
            throws AccessDeniedException, IdNotFoundException, RemoteException, DLibraException {
        pubFiles = new HashMap<String, EditionFile>();

        List<VersionInfo> versionsInfos = getVersionsInfos(eventMetadataServer, elementId);

        List<FileId> filesIds = new ArrayList<FileId>();

        for (VersionInfo info : versionsInfos) {
            filesIds.add(info.getFileId());
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<pl.psnc.dlibra.metadata.File> files = new ArrayList(eventMetadataServer.getFileManager()
                .getObjects(new FileFilter(filesIds), new OutputFilter(pl.psnc.dlibra.metadata.File.class))
                .getResults());

        FileId fileId = null;

        if (files.size() > 0) {
            fileId = ((Publication) eventMetadataServer.getPublicationManager()
                    .getObjects(new PublicationFilter(files.get(0).getPublicationId()),
                        new OutputFilter(Publication.class))
                    .getResult()).getMainFileId();
        }

        EditionAppFile mf = null;
        for (pl.psnc.dlibra.metadata.File f : files) {
            EditionAppFile ef = new EditionAppFile(f.getPath());
            if (f.getId().equals(fileId)) {
                ef.setMainFile(true);
                mf = ef;
            }
            ef.setDLibraFile(f);
            List<Version> versions = getVersions(eventMetadataServer, f);
            ef.setVersions(versions);
            ef.setVersion(getVersion(versionsInfos, versions));
            pubFiles.put(f.getPath().replace('/', File.separatorChar), ef);
        }

        return new EditionAppFiles(pubFiles, mf);
    }


    public EditionFiles createDataSourceFiles(List<FileInfo> files)
            throws LocalResourceUnavailableException {
        Map<String, EditionFile> editionFilesMap = new HashMap<String, EditionFile>();
        EditionFile mainFile = null;
        for (FileInfo info : files) {
            if (editionFilesMap.containsKey(info.getDLibraPath())) {
                throw new IllegalArgumentException("Duplicate path: " + info.getDLibraPath());
            }
            EditionFile editionFile = new DataSourceEditionFile(info);
            editionFilesMap.put(info.getDLibraPath(), editionFile);
            if (mainFile == null) {
                editionFile.setMainFile(true);
                mainFile = editionFile;
            }
        }
        return new EditionAppFiles(editionFilesMap, mainFile);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<VersionInfo> getVersionsInfos(EventMetadataServer eventMetadataServer, ElementId elementId)
            throws DLibraException, AccessDeniedException, IdNotFoundException, RemoteException {
        List<Id> versIds = null;

        if (elementId instanceof PublicationId) {
            versIds = new ArrayList<Id>(eventMetadataServer.getPublicationManager()
                    .getObjects(new PublicationFilter(null, (PublicationId) elementId),
                        new OutputFilter(VersionId.class))
                    .getResultIds());
        } else if (elementId instanceof EditionId) {
            versIds = new ArrayList<Id>(eventMetadataServer.getPublicationManager()
                    .getObjects(new EditionFilter((EditionId) elementId), new OutputFilter(VersionId.class))
                    .getResultIds());
        } else {
            return new ArrayList<VersionInfo>();
        }

        return new ArrayList(eventMetadataServer.getFileManager()
                .getObjects(new InputFilter(versIds), new OutputFilter(VersionInfo.class)).getResultInfos());
    }


    private Version getVersion(List<VersionInfo> versionsInfos, List<Version> versions) {
        for (Version version : versions) {
            for (VersionInfo i : versionsInfos) {
                if (i.getId().equals(version.getId())) {
                    return version;
                }
            }
        }
        return null;
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<Version> getVersions(EventMetadataServer eventMetadataServer, pl.psnc.dlibra.metadata.File f)
            throws RemoteException, IdNotFoundException, AccessDeniedException, DLibraException {
        return new ArrayList(eventMetadataServer.getFileManager()
                .getObjects(new InputFilter(f.getVersionIds()), new OutputFilter(Version.class)).getResults());
    }
}
