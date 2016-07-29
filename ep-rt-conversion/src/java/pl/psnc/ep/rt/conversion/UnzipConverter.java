package pl.psnc.ep.rt.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pl.psnc.dlibra.content.conversion.ImageContentConverter;
import pl.psnc.util.IOUtils;

public class UnzipConverter implements ImageContentConverter {

    public static final String FILE_TYPE = "filetype";


    @Override
    public boolean convertImageFile(File source, Map<String, Object> parameters, String outputFilename)
            throws IOException {
        String extension = "." + parameters.get(FILE_TYPE);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(source));
        try {
            boolean found = false;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().endsWith(extension))
                    continue;

                if (found)
                    return false;
                found = true;

                FileOutputStream output = new FileOutputStream(outputFilename);
                try {
                    IOUtils.copyStream(zis, output);
                } finally {
                    output.close();
                }
            }
            return found;
        } finally {
            zis.close();
        }
    }

}
