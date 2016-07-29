package pl.psnc.ep.rt;

import java.util.Arrays;

public enum MediaFormat {
    IMAGE("JPEG", "JPG", "PNG", "SVG"),
    VIDEO("MP4"),
    AUDIO("WAV", "MP3", "MP4", "M4A"),
    PACKAGE("ZIP", "GGB"),
    SUBTITLES("VTT"),
    CAPTIONS("VTT"),
    CHAPTERS("TXT"),
    DESCRIPTION("TXT"),
    ICON("JPEG", "JPG", "PNG", "SVG");

    private String[] extensions;


    private MediaFormat(String... extensions) {
        this.extensions = extensions;
    }


    public boolean allows3D() {
        return this == IMAGE || this == VIDEO;
    }


    public boolean isMultiFormatSource() {
        return this == IMAGE || this == ICON;
    }


    public String[] getFileExtensions() {
        return Arrays.copyOf(extensions, extensions.length);
    }


    public boolean isRequired() {
        return this == IMAGE || this == VIDEO || this == AUDIO || this == PACKAGE || this == ICON;
    }
}
