package pl.psnc.ep.rt.web.womi;

import java.io.InputStream;

public interface Downloadable {

    String getContentType();


    InputStream getInputStream();


    long getContentLength();

}
