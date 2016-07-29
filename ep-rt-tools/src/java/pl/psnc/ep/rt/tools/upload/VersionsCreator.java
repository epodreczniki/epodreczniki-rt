package pl.psnc.ep.rt.tools.upload;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pl.psnc.activation.ExtensionFileTypeRecognizer;
import pl.psnc.dlibra.app.common.files.EditionFile;
import pl.psnc.dlibra.app.gui.editor.wizard.common.upload.OperationCancelledException;
import pl.psnc.dlibra.app.gui.editor.wizard.common.upload.version.VersionInformation;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

public class VersionsCreator {

    private PublicationId publicationId;

    private List<VersionInformation> versions = new ArrayList<VersionInformation>();

    private static final int STEP = 10;

    private final FileManager fileManager;


    public VersionsCreator(PublicationId publicationId, FileManager fileManager) {
        this.publicationId = publicationId;
        this.fileManager = fileManager;
    }


    public void addVersion(EditionFile pubFile) {
        File file = pubFile.getDLibraFile();
        if (file == null) {
            String mimeType = pubFile.getMimeType() != null ? pubFile.getMimeType()
                    : ExtensionFileTypeRecognizer.getMimeType(pubFile.getSourceFile()).toString();
            file = new File(mimeType, publicationId, pubFile.getRelativePath(), null, null);
            pubFile.setDLibraFile(file);
        }

        versions.add(new VersionInformation(file, new Date(pubFile.getSourceFile().lastModified()), ""));
    }


    public void addBasedOn(File file, String pathToContent) {
        java.io.File fileContent = new java.io.File(pathToContent);
        Date lastModified = new Date(fileContent.lastModified());
        lastModified.setTime(lastModified.getTime() - (lastModified.getTime() % 1000));

        versions.add(new VersionInformation(file, lastModified, ""));
    }


    public List<pl.psnc.dlibra.metadata.Version> createVersions()
            throws IdNotFoundException, RemoteException, DLibraException {
        List<pl.psnc.dlibra.metadata.Version> result = new ArrayList<pl.psnc.dlibra.metadata.Version>();

        if (versions.size() == 0) {
            return result;
        }

        int bunches = versions.size() / STEP;

        for (int i = 0; i <= bunches; i++) {
            List<VersionInformation> bunch = versions.subList(i * STEP,
                (i + 1) * STEP > versions.size() ? versions.size() : ((i + 1) * STEP));
            result.addAll(Arrays.asList(sendBunch(bunch)));
        }

        versions.clear();

        return result;
    }


    private pl.psnc.dlibra.metadata.Version[] sendBunch(List<VersionInformation> bunch)
            throws AccessDeniedException, IdNotFoundException, RemoteException, DLibraException {
        File[] files = new File[bunch.size()];
        Date[] dates = new Date[bunch.size()];
        String[] descs = new String[bunch.size()];

        int index = 0;
        for (VersionInformation v : bunch) {
            files[index] = v.getFile();
            dates[index] = v.getLastModification();
            descs[index] = v.getDescription();
            index++;
        }

        return fileManager.createVersions(files, dates, descs);
    }


    public List<Version> createVersions(List<EditionFile> filesToUpload)
            throws IdNotFoundException, RemoteException, DLibraException {
        for (EditionFile file : filesToUpload) {
            addVersion(file);
        }

        List<Version> createdVersions = createVersions();

        for (int j = 0; j < filesToUpload.size(); j++) {
            filesToUpload.get(j).setVersion(createdVersions.get(j));

            File dLFile = filesToUpload.get(j).getDLibraFile();
            File newdLFile = new File(dLFile.getType(), dLFile.getPublicationId(), dLFile.getPath(),
                    createdVersions.get(j).getFileId(), dLFile.getVersionIds());
            filesToUpload.get(j).setDLibraFile(newdLFile);
        }

        return createdVersions;
    }
}
