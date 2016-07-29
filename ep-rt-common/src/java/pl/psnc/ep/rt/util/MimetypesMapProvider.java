package pl.psnc.ep.rt.util;

import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

public class MimetypesMapProvider {

    private static final String FILE_TYPES_PATH = "/file-types.properties";

    private final static MimetypesFileTypeMap typeMap;


    static {
        try {
            InputStream inputStream = MimetypesMapProvider.class.getResourceAsStream(FILE_TYPES_PATH);
            try {
                typeMap = new MimetypesFileTypeMap(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load types map: " + FILE_TYPES_PATH, e);
        }
    }


    public static MimetypesFileTypeMap get() {
        return typeMap;
    }
}
