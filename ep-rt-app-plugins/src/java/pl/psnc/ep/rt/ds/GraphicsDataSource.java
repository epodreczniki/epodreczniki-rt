package pl.psnc.ep.rt.ds;

import org.apache.log4j.Logger;

import pl.psnc.ep.rt.WOMIType;

public class GraphicsDataSource extends WOMIDataSource {

    private final static Logger logger = Logger.getLogger(GraphicsDataSource.class);


    @Override
    public String getName() {
        return textsBundle.getString("datasource.graphics.name");
    }


    @Override
    protected WOMIType getWOMIType() {
        return WOMIType.GRAPHICS;
    }
}
