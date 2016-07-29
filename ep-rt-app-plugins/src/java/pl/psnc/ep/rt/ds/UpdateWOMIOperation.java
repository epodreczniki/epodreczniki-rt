package pl.psnc.ep.rt.ds;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.eventserver.metadata.manager.EventPublicationManager;
import pl.psnc.dlibra.app.operationsqueue.AbstractOperation;
import pl.psnc.dlibra.app.operationsqueue.OperationInformation;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.Util;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.ds.gui.WOMIDefinitionPanel;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.util.MimetypesMapProvider;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class UpdateWOMIOperation extends AbstractOperation implements Serializable {

    private static final int BUF_SIZE = 262144;

    private static final Logger logger = Logger.getLogger(UpdateWOMIOperation.class);

    private final Map<WOMIFormat, java.io.File> sourceFiles;

    private final EditionId womiId;

    private transient Map<WOMIFormat, String> fileNames;

    private transient String womiName;


    public UpdateWOMIOperation(Map<WOMIFormat, java.io.File> sourceFiles, EditionId womiId,
            Map<WOMIFormat, String> fileNames) {
        this.sourceFiles = sourceFiles;
        this.womiId = womiId;
        this.fileNames = fileNames;
    }


    @Override
    public void toFile(java.io.File file)
            throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        try {
            out.writeObject(this);
        } finally {
            out.close();
        }
    }


    @Override
    public void run()
            throws Exception {
        if (sourceFiles == null || sourceFiles.isEmpty())
            return;

        if (fileNames == null)
            fileNames = Util.findFileNames(womiId);
        for (Map.Entry<WOMIFormat, String> entry : fileNames.entrySet()) {
            WOMIFormat format = entry.getKey();
            if (!sourceFiles.containsKey(format)) {
                sourceFiles.put(format, new java.io.File(entry.getValue()));
            } else if (sourceFiles.get(format) == null) {
                sourceFiles.remove(format);
            }
        }
        java.io.File mainFileTemp = java.io.File.createTempFile("womi", ".xml");
        OutputStream output = new FileOutputStream(mainFileTemp);
        boolean hasAlternatives = sourceFiles.containsKey(WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT);
        Map<WOMIFormat, String> paths;
        try {
            paths = WOMIXMLHandler.saveMultiFormatXML(sourceFiles, output, hasAlternatives);
        } finally {
            output.close();
        }

        int totalSteps = paths.size() + (hasAlternatives ? 1 : 0) + 3;
        int currentStep = 0;

        List<VersionId> versionIds = new ArrayList<VersionId>();
        for (Map.Entry<WOMIFormat, String> entry : paths.entrySet()) {
            fireOperationInProgress(new OperationInformation(currentStep++, "", totalSteps));
            WOMIFormat format = entry.getKey();
            String dlibraPath = entry.getValue();
            java.io.File file = sourceFiles.get(format);
            if (file.isAbsolute())
                versionIds.add(sendFileToServer(file, dlibraPath, format).getId());
            else
                versionIds.add(findCurrentFileVersion(dlibraPath));
        }

        if (hasAlternatives) {
            fireOperationInProgress(new OperationInformation(currentStep++, "", totalSteps));
            java.io.File file = sourceFiles.get(WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT);
            if (file.isAbsolute()) {
                Version v = sendFileToServer(file, WOMIDefinitionPanel.STATIC_ALTERNATIVE_PATH, null);
                versionIds.add(v.getId());
            } else {
                versionIds.add(findCurrentFileVersion(WOMIDefinitionPanel.STATIC_ALTERNATIVE_PATH));
            }
        }
        fireOperationInProgress(new OperationInformation(currentStep++, "", totalSteps));
        String mainFilePath = "/" + WOMIXMLHandler.MAIN_FILE_NAME;
        Version mainFileVersion = sendFileToServer(mainFileTemp, mainFilePath, null);
        versionIds.add(mainFileVersion.getId());
        mainFileTemp.delete();
        fireOperationInProgress(new OperationInformation(currentStep++, "", totalSteps));
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        PublicationManager pm = ms.getPublicationManager();
        PublicationId publicationId = (PublicationId) pm
                .getObjects(new EditionFilter(womiId), new OutputFilter(PublicationId.class)).getResultId();
        pm.setMainFile(publicationId, mainFileVersion.getFileId());
        Edition edition = (Edition) pm.getObjects(new EditionFilter(womiId), new OutputFilter(Edition.class))
                .getResult();
        boolean isPublished = edition.getState() == Edition.PUBLISHED;
        if (isPublished)
            pm.setEditionState(womiId, false);
        pm.updateEditionVersions(womiId, versionIds.toArray(new VersionId[versionIds.size()]));
        if (isPublished)
            pm.setEditionState(womiId, true);

        fireOperationInProgress(new OperationInformation(currentStep++, "", totalSteps));
        reportWOMIChange(womiId);
    }


    @Override
    public String toString() {
        if (womiName == null) {
            EventPublicationManager pm = ApplicationContext.getInstance().getEventMetadataServer()
                    .getPublicationManager();
            try {
                AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) pm
                        .getObjects(new EditionFilter(womiId), new OutputFilter(AbstractPublicationInfo.class))
                        .getResultInfo();
                womiName = pubInfo.getLabel();
            } catch (Exception e) {
                logger.error("Could not retrieve WOMI name ", e);
                return ">WOMI< " + womiId;
            }
        }
        return ">WOMI< " + womiName;
    }


    public static void reportWOMIChange(EditionId womiId)
            throws RemoteException, DLibraException {
        EPService ep = (EPService) ApplicationContext.getInstance().getUserServiceResolver()
                .getService(EPService.SERVICE_TYPE, null);
        ep.reportWOMIModified(womiId);
    }


    private Version sendFileToServer(java.io.File file, String dlibraPath, WOMIFormat format)
            throws DLibraException, IOException {
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        PublicationManager pm = ms.getPublicationManager();
        FileManager fm = ms.getFileManager();
        PublicationId publicationId = (PublicationId) pm
                .getObjects(new EditionFilter(womiId), new OutputFilter(PublicationId.class)).getResultId();

        String fileType = MimetypesMapProvider.get().getContentType(file);
        if (file.getName().endsWith(".mp4") && format != null && format.mediaFormat == MediaFormat.AUDIO)
            fileType = "audio/mp4";
        File dlibraFile = new File(fileType, publicationId, dlibraPath, null, null);
        Version version = fm.createVersion(dlibraFile, file.length(), new Date(), "");

        ContentServer cs = (ContentServer) ApplicationContext.getInstance().getUserServiceResolver()
                .getService(ContentServer.SERVICE_TYPE, null);
        OutputStream os = cs.getVersionOutputStream(version.getId());
        try {
            InputStream is = new FileInputStream(file);
            try {
                byte[] buffer = new byte[BUF_SIZE];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
        return version;
    }


    private VersionId findCurrentFileVersion(String dlibraPath)
            throws RemoteException, DLibraException {
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        VersionId versionId = (VersionId) ms.getFileManager()
                .getObjects(new FileFilter(dlibraPath).setEditionId(womiId), new OutputFilter(VersionId.class))
                .getResultId();
        return versionId;
    }
}
