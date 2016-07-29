package pl.psnc.ep.rt.ds;

import pl.psnc.ep.rt.WOMIType;

public class InteractiveDataSource extends WOMIDataSource {

    @Override
    public String getName() {
        return textsBundle.getString("datasource.interactive.name");
    }


    @Override
    protected WOMIType getWOMIType() {
        return WOMIType.INTERACTIVE;
    }
}
