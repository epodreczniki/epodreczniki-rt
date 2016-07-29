package pl.psnc.ep.rt;

import java.io.Serializable;

public class WOMIFormat implements Serializable {

    public final String id;

    public final WOMIType womiType;

    public final MediaFormat mediaFormat;

    public final TargetFormat targetFormat;

    public final boolean is3D;


    public WOMIFormat(WOMIType womiType, MediaFormat mediaFormat) {
        this(womiType, mediaFormat, TargetFormat.CLASSIC, false);
    }


    public WOMIFormat(WOMIType womiType, MediaFormat mediaFormat, TargetFormat targetFormat, boolean is3D) {
        this.womiType = womiType;
        this.mediaFormat = mediaFormat;
        this.targetFormat = targetFormat;
        this.is3D = is3D;
        this.id = getId();
    }


    @Override
    public String toString() {
        return id;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (is3D ? 1231 : 1237);
        result = prime * result + ((mediaFormat == null) ? 0 : mediaFormat.hashCode());
        result = prime * result + ((targetFormat == null) ? 0 : targetFormat.hashCode());
        result = prime * result + ((womiType == null) ? 0 : womiType.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WOMIFormat other = (WOMIFormat) obj;
        if (is3D != other.is3D)
            return false;
        if (mediaFormat != other.mediaFormat)
            return false;
        if (targetFormat != other.targetFormat)
            return false;
        if (womiType != other.womiType)
            return false;
        return true;
    }


    private String getId() {
        return womiType + "_" + mediaFormat + "_" + targetFormat + (is3D ? "_3D" : "");
    }


    public static WOMIFormat fromId(String id)
            throws IllegalArgumentException {
        String[] idParts = id.split("_");
        if (idParts.length < 3 || idParts.length > 4)
            throw new IllegalArgumentException("Wrong number of parts in id: " + idParts.length);
        WOMIType womiType = WOMIType.valueOf(idParts[0]);
        MediaFormat mediaFormat = MediaFormat.valueOf(idParts[1]);
        TargetFormat targetFormat = TargetFormat.valueOf(idParts[2]);
        boolean is3D = false;
        if (idParts.length == 4) {
            if (!"3D".equals(idParts[3]))
                throw new IllegalArgumentException("Last part must be '3D' or none");
            is3D = true;
        }
        return new WOMIFormat(womiType, mediaFormat, targetFormat, is3D);
    }


    public WOMIFormat toDefaultTarget() {
        return new WOMIFormat(womiType, mediaFormat, TargetFormat.DEFAULT, is3D);
    }
}
