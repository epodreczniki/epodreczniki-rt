package pl.psnc.ep.rt.ds;

import pl.psnc.ep.rt.WOMIType;

public class MovieDataSource extends WOMIDataSource {

    @Override
    public String getName() {
        return textsBundle.getString("datasource.movie.name");
    }


    @Override
    protected WOMIType getWOMIType() {
        return WOMIType.MOVIE;
    }
}
