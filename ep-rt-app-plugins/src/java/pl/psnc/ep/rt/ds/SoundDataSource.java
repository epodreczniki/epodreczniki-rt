package pl.psnc.ep.rt.ds;

import pl.psnc.ep.rt.WOMIType;

public class SoundDataSource extends WOMIDataSource {

    @Override
    public String getName() {
        return textsBundle.getString("datasource.sound.name");
    }


    @Override
    protected WOMIType getWOMIType() {
        return WOMIType.SOUND;
    }

}
