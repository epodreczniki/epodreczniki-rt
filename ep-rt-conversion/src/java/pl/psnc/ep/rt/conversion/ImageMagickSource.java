package pl.psnc.ep.rt.conversion;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.content.PluginContentInfo;
import pl.psnc.dlibra.content.conversion.ImageContentConverter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.mgmt.ServiceResolver;
import pl.psnc.dlibra.plugin.ImageContentConverterFactory;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.util.ImageConverter;
import pl.psnc.ep.rt.util.WOMIXMLHandler;
import pl.psnc.util.filecache.FileSource;

public class ImageMagickSource implements FileSource<PluginContentInfo> {

    public static final int SVG2PNG_POS = 3;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ImageMagickSource.class);

    private final ServiceResolver sr;


    public ImageMagickSource(ServiceResolver sr)
            throws IOException {
        this.sr = sr;
    }


    public File getFile(PluginContentInfo fileId, File parentDir)
            throws IOException {
        java.io.File converted = null;
        java.io.File tempFile = null;

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            String targetFormat = (String) fileId.getParameters().get(PluginContentInfo.TARGET_FORMAT_POS);
            parameters.put(ImageMagickConverter.TARGET_FORMAT, targetFormat);
            int resolution = (Integer) fileId.getParameters().get(PluginContentInfo.RESOLUTION_POS);
            parameters.put(ImageMagickConverter.RESOLUTION, resolution);
            parameters.put(ImageMagickConverter.IS_3D, fileId.getParameters().get(PluginContentInfo.IS_3D_POS));
            Boolean svg2png = (Boolean) fileId.getParameters().get(SVG2PNG_POS);
            tempFile = prepareFile(fileId.getEditionId(), parameters);

            if (tempFile == null || !tempFile.exists())
                return null;
            String outputFormat = ImageConverter.getOutputFormat(tempFile, TargetFormat.valueOf(targetFormat),
                resolution, svg2png);
            String outputName = tempFile.getName();
            outputName = outputName.substring(0, outputName.lastIndexOf(".")) + outputFormat;
            converted = new File(parentDir, outputName);
            ImageContentConverter icc = ImageContentConverterFactory.getConverter(fileId.getPluginId());
            if (icc.convertImageFile(tempFile, parameters, converted.getAbsolutePath()) && converted.exists())
                return converted;
            return null;
        } catch (DLibraException e) {
            throw new RemoteException("Error while preparing image", e);
        } finally {
            if (tempFile != null)
                tempFile.delete();
        }

    }


    private String getFileNameToConvert(EditionId id, Map<String, Object> parameters)
            throws DLibraException, IOException {
        Map<WOMIFormat, String> filesMap = Util.retrieveWOMIFilesMap(id, sr);

        WOMIType womiType = filesMap.keySet().iterator().next().womiType;
        MediaFormat mediaFormat = womiType == WOMIType.ICON ? MediaFormat.ICON : MediaFormat.IMAGE;
        WOMIFormat wf = new WOMIFormat(womiType, mediaFormat,
                TargetFormat.valueOf((String) parameters.get(ImageMagickConverter.TARGET_FORMAT)),
                ((Boolean) parameters.get(ImageMagickConverter.IS_3D)).booleanValue());

        String fileName = filesMap.get(wf);
        if (fileName == null) {
            wf = new WOMIFormat(womiType, mediaFormat, TargetFormat.CLASSIC,
                    ((Boolean) parameters.get(ImageMagickConverter.IS_3D)).booleanValue());
            fileName = filesMap.get(wf);
        }

        if (fileName == null)
            return null;

        return WOMIXMLHandler.toDLibraPath(wf, fileName);
    }


    private java.io.File prepareFile(EditionId id, Map<String, Object> parameters)
            throws DLibraException, IOException {

        String fileName = getFileNameToConvert(id, parameters);

        if (fileName == null)
            throw new IdNotFoundException(id);
        File result = File.createTempFile("edi" + id.toString() + "_", fileName.substring(fileName.lastIndexOf(".")));

        Util.copyFileFromServer(id, fileName, result, sr);

        return result;
    }
}
