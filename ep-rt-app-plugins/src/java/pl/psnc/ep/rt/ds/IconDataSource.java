package pl.psnc.ep.rt.ds;

import pl.psnc.ep.rt.WOMIType;

public class IconDataSource extends WOMIDataSource {

    @Override
    public String getName() {
        return textsBundle.getString("datasource.icon.name");
    }


    @Override
    protected WOMIType getWOMIType() {
        return WOMIType.ICON;
    }

}
