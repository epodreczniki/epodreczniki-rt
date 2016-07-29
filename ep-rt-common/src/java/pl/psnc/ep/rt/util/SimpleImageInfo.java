package pl.psnc.ep.rt.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("all")
public class SimpleImageInfo {

    private int height;

    private int width;

    private String mimeType;


    private SimpleImageInfo() {

    }


    public SimpleImageInfo(URL url)
            throws IOException {
        InputStream is = url.openStream();
        try {
            processStream(is);
        } finally {
            is.close();
        }
    }


    public SimpleImageInfo(File file)
            throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            processStream(is);
        } finally {
            is.close();
        }
    }


    public SimpleImageInfo(InputStream is)
            throws IOException {
        processStream(is);
    }


    public SimpleImageInfo(byte[] bytes)
            throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            processStream(is);
        } finally {
            is.close();
        }
    }


    private void processStream(InputStream input)
            throws IOException {
        BufferedInputStream is = new BufferedInputStream(input);
        is.mark(10);
        int c1 = is.read();
        int c2 = is.read();
        int c3 = is.read();

        mimeType = null;
        width = height = -1;
        if (c1 == 0xFF && c2 == 0xD8) {
            while (c3 == 255) {
                int marker = is.read();
                int len = readInt(is, 2, true);
                if (marker == 192 || marker == 193 || marker == 194) {
                    skip(is, 1);
                    height = readInt(is, 2, true);
                    width = readInt(is, 2, true);
                    mimeType = "image/jpeg";
                    break;
                }
                skip(is, len - 2);
                c3 = is.read();
            }
        } else if (c1 == 137 && c2 == 80 && c3 == 78) {
            skip(is, 15);
            width = readInt(is, 2, true);
            skip(is, 2);
            height = readInt(is, 2, true);
            mimeType = "image/png";
        } else if ((c1 == 'M' && c2 == 'M' && c3 == 0) || (c1 == 'I' && c2 == 'I' && c3 == 42)) {
            int c4 = is.read();
            if ((c1 == 'M' && c2 == 'M' && c3 == 0 && c4 == 42) || (c1 == 'I' && c2 == 'I' && c3 == 42 && c4 == 0)) {
                boolean bigEndian = c1 == 'M';
                int ifd = 0;
                int entries;
                ifd = readInt(is, 4, bigEndian);
                skip(is, ifd - 8);
                entries = readInt(is, 2, bigEndian);
                for (int i = 1; i <= entries; i++) {
                    int tag = readInt(is, 2, bigEndian);
                    int fieldType = readInt(is, 2, bigEndian);
                    long count = readInt(is, 4, bigEndian);
                    int valOffset;
                    if ((fieldType == 3 || fieldType == 8)) {
                        valOffset = readInt(is, 2, bigEndian);
                        skip(is, 2);
                    } else {
                        valOffset = readInt(is, 4, bigEndian);
                    }
                    if (tag == 256) {
                        width = valOffset;
                    } else if (tag == 257) {
                        height = valOffset;
                    }
                    if (width != -1 && height != -1) {
                        mimeType = "image/tiff";
                        break;
                    }
                }
            }
        } else {
            is.reset();
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                SAXParser parser = factory.newSAXParser();
                DefaultHandler handler = new DefaultHandler() {

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes)
                            throws SAXException {
                        if (localName.equals("svg")) {
                            mimeType = "image/svg+xml";
                            try {
                                Pattern pattern = Pattern.compile("[0-9]+");
                                Matcher matcher = pattern.matcher(attributes.getValue("width"));
                                width = matcher.find() ? Integer.valueOf(matcher.group()) : Integer.MAX_VALUE;
                                matcher = pattern.matcher(attributes.getValue("height"));
                                height = matcher.find() ? Integer.valueOf(matcher.group()) : Integer.MAX_VALUE;
                            } catch (Exception e) {
                                width = height = Integer.MAX_VALUE;
                            }
                        }
                        throw new SAXException("First element analyzed, stop parsing");
                    }
                };
                parser.parse(is, handler);
            } catch (SAXException e) {
            } catch (ParserConfigurationException e) {
            }
        }
        if (mimeType == null) {
            throw new IOException("Unsupported image type");
        }
    }


    private int readInt(InputStream is, int noOfBytes, boolean bigEndian)
            throws IOException {
        int ret = 0;
        int sv = bigEndian ? ((noOfBytes - 1) * 8) : 0;
        int cnt = bigEndian ? -8 : 8;
        for (int i = 0; i < noOfBytes; i++) {
            ret |= is.read() << sv;
            sv += cnt;
        }
        return ret;
    }


    private void skip(InputStream is, int n)
            throws IOException {
        while (n > 0) {
            long skipped = is.skip(n);
            if (skipped == 0)
                throw new EOFException();
            n -= skipped;
        }
    }


    public int getHeight() {
        return height;
    }


    public void setHeight(int height) {
        this.height = height;
    }


    public int getWidth() {
        return width;
    }


    public void setWidth(int width) {
        this.width = width;
    }


    public String getMimeType() {
        return mimeType;
    }


    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    @Override
    public String toString() {
        return "MIME Type : " + mimeType + "\t Width : " + width + "\t Height : " + height;
    }
}