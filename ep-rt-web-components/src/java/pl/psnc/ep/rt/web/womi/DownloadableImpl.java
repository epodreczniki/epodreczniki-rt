package pl.psnc.ep.rt.web.womi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

class DownloadableImpl implements Downloadable {

    private final String content;

    private final String contentType;


    public DownloadableImpl(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }


    @Override
    public String getContentType() {
        return contentType;
    }


    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes());
    }


    @Override
    public long getContentLength() {
        return content.getBytes().length;
    }

}
