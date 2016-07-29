package pl.psnc.ep.rt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TargetFormat {
    CLASSIC,
    MOBILE,
    PDF,
    EBOOK;

    public static final TargetFormat DEFAULT = CLASSIC;

    public static List<TargetFormat> ALL_FORMATS = Collections.unmodifiableList(Arrays.asList(TargetFormat.values()));


    public boolean allows3D() {
        return this != EBOOK;
    }


    public int[] getResolutions() {
        switch (this) {
            case CLASSIC:
            case MOBILE:
                return new int[] { 120, 480, 980, 1440, 1920 };
            case EBOOK:
                return new int[] { 800 };
            case PDF:
                return new int[] { 980 };
            default:
                throw new RuntimeException("No resolutions defined for target format " + this);
        }
    }


    public int getCoverResolution() {
        switch (this) {
            case CLASSIC:
            case MOBILE:
                return 0;
            case EBOOK:
                return 530;
            case PDF:
                return 1240;
            default:
                throw new RuntimeException("No cover resulution defined for target format " + this);
        }
    }


    public int getMiniatureResolution() {
        return 200;
    }
}
