package pl.psnc.ep.rt.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.ServiceResolver;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.util.ObjectMalformedException;
import pl.psnc.ep.rt.util.WOMIXMLHandler;
import pl.psnc.util.IOUtils;

class Util {

    static Map<WOMIFormat, String> retrieveWOMIFilesMap(EditionId editionId, ServiceResolver sr)
            throws RemoteException, DLibraException, IOException {
        MetadataServer ms = DLStaticServiceResolver.getMetadataServer(sr, null);
        VersionId mainId = (VersionId) ms.getPublicationManager()
                .getObjects(new EditionFilter(editionId).setMainVersion(true), new OutputFilter(VersionId.class))
                .getResultId();

        ContentServer cs = DLStaticServiceResolver.getContentServer(sr, null);
        List<File> editionFiles = cs.getEditionFiles(Arrays.asList(mainId));
        FileInputStream fis = new FileInputStream(editionFiles.get(0));
        try {
            Map<WOMIFormat, String> filesMap = WOMIXMLHandler.loadMultiFormatXML(fis);
            return filesMap;
        } catch (ObjectMalformedException e) {
            throw new IOException("invalid main file in edition " + editionId, e);
        } finally {
            fis.close();
            cs.releaseElement(mainId);
        }
    }


    static void copyFileFromServer(EditionId editionId, String dLibraPath, File targetFile, ServiceResolver sr)
            throws RemoteException, DLibraException, IOException {
        MetadataServer ms = DLStaticServiceResolver.getMetadataServer(sr, null);
        VersionId verId = (VersionId) ms.getFileManager()
                .getObjects(new FileFilter(dLibraPath).setEditionId(editionId), new OutputFilter(VersionId.class))
                .getResultId();

        ContentServer cs = DLStaticServiceResolver.getContentServer(sr, null);
        List<File> files = cs.getEditionFiles(Arrays.asList(verId));
        FileInputStream input = new FileInputStream(files.get(0));
        try {
            FileOutputStream output = new FileOutputStream(targetFile);
            try {
                IOUtils.copyStream(input, output);
            } finally {
                output.close();
            }
        } finally {
            input.close();
            cs.releaseElement(verId);
        }
    }
}
