package pl.psnc.ep.rt.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import pl.psnc.dlibra.content.PluginContentInfo;
import pl.psnc.dlibra.content.conversion.ImageContentConverter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.mgmt.ServiceResolver;
import pl.psnc.dlibra.plugin.ImageContentConverterFactory;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.util.WOMIXMLHandler;
import pl.psnc.util.filecache.FileSource;

public class UnzipSource implements FileSource<PluginContentInfo> {

    private final ServiceResolver sr;


    public UnzipSource(ServiceResolver sr) {
        this.sr = sr;
    }


    @Override
    public File getFile(PluginContentInfo contentInfo, File parentDir)
            throws IOException {
        HashMap<String, Object> params = new HashMap<String, Object>();
        String fileType = (String) contentInfo.getParameters().get(1);
        params.put(UnzipConverter.FILE_TYPE, fileType);

        File tempFile = null;
        try {
            tempFile = prepareFileToConvert(contentInfo.getEditionId());
            File converted = new File(parentDir, tempFile.getName() + "." + fileType);
            ImageContentConverter icc = ImageContentConverterFactory.getConverter(contentInfo.getPluginId());
            boolean ok = icc.convertImageFile(tempFile, params, converted.getAbsolutePath());
            if (!ok)
                throw new FileNotFoundException("No " + fileType + " file in archive " + contentInfo.getEditionId());
            return converted;
        } catch (DLibraException e) {
            throw new RemoteException("Error while preparing file to unzip", e);
        } finally {
            if (tempFile != null)
                tempFile.delete();
        }
    }


    private File prepareFileToConvert(EditionId editionId)
            throws DLibraException, IOException {
        Map<WOMIFormat, String> filesMap = Util.retrieveWOMIFilesMap(editionId, sr);
        WOMIFormat zipFormat = new WOMIFormat(WOMIType.INTERACTIVE, MediaFormat.PACKAGE);
        String zipFileName = filesMap.get(zipFormat);
        String zipDLibraPath = WOMIXMLHandler.toDLibraPath(zipFormat, zipFileName);

        File tempFile = File.createTempFile(editionId.toString(), ".zip");
        Util.copyFileFromServer(editionId, zipDLibraPath, tempFile, sr);
        return tempFile;
    }

}
