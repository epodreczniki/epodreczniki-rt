package pl.psnc.ep.rt.conversion;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import pl.psnc.dlibra.content.conversion.ImageContentConverter;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.util.ImageConverter;

public class ImageMagickConverter implements ImageContentConverter {

    public static final String TARGET_FORMAT = "targetFormat";

    public static final String RESOLUTION = "resolution";

    public static final String IS_3D = "is3d";

    public static final String SVG2PNG = "svg2png";


    public boolean convertImageFile(File source, Map<String, Object> parameters, String outputFilename)
            throws IOException {
        if (ImageConverter.isEnabled())
            return ImageConverter.saveImageFile(source, TargetFormat.valueOf((String) parameters.get(TARGET_FORMAT)),
                (Integer) parameters.get(RESOLUTION), outputFilename);

        return false;
    }
}
