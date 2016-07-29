package pl.psnc.ep.rt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum WOMIType {

    GRAPHICS(true, "IMAGE", MediaFormat.IMAGE),
    ICON(false, "ICON", MediaFormat.ICON),
    MOVIE(true, "VIDEO", MediaFormat.VIDEO, MediaFormat.SUBTITLES, MediaFormat.CAPTIONS, MediaFormat.IMAGE),
    SOUND(false, "AUDIO", MediaFormat.AUDIO, MediaFormat.DESCRIPTION),
    INTERACTIVE(false, "OINT", MediaFormat.PACKAGE, MediaFormat.IMAGE);

    public final List<WOMIFormat> womiFormats;

    private final boolean allows3D;

    private final String externalForm;


    private WOMIType(boolean add3D, String externalForm, MediaFormat... mediaFormats) {
        this.allows3D = add3D;
        this.externalForm = externalForm;
        ArrayList<WOMIFormat> womiFormats = new ArrayList<WOMIFormat>();
        for (MediaFormat mediaFormat : mediaFormats) {
            if (mediaFormat.isMultiFormatSource()) {
                for (TargetFormat targetFormat : TargetFormat.ALL_FORMATS) {
                    addFormat(womiFormats, mediaFormat, targetFormat, add3D);
                }
            } else {
                addFormat(womiFormats, mediaFormat, TargetFormat.DEFAULT, add3D);
            }
        }
        this.womiFormats = Collections.unmodifiableList(womiFormats);
    }


    private void addFormat(ArrayList<WOMIFormat> womiFormats, MediaFormat mf, TargetFormat tf, boolean add3D) {
        womiFormats.add(new WOMIFormat(this, mf, tf, false));
        if (add3D && mf.allows3D() && tf.allows3D()) {
            womiFormats.add(new WOMIFormat(this, mf, tf, true));
        }
    }


    public boolean allows3D() {
        return allows3D;
    }


    public String toExternalForm() {
        return externalForm;
    }
}
