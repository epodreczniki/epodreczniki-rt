package pl.psnc.ep.rt.web.womi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class WomiPreprocessor {

    private static final Logger logger = Logger.getLogger(WomiPreprocessor.class);

    private static final CharSequence SWIFFY_MARKUP = "swiffyobject";


    public Downloadable processWomiInteractivePackage(File womiZipFile)
            throws IOException {
        ZipFile zip = new ZipFile(womiZipFile);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                try {
                    ZipEntry zipEntry = entries.nextElement();
                    Downloadable d = processZipEntry(zip, zipEntry);
                    if (d != null) {
                        return d;
                    }
                } catch (IllegalArgumentException e) {
                    if ("MALFORMED".equals(e.getMessage())) {
                        logger.error("when processing next entry in " + zip, e);
                    } else {
                        throw e;
                    }
                }
            }
        } finally {
            zip.close();
        }
        return null;
    }


    private Downloadable processZipEntry(ZipFile zip, ZipEntry zipEntry)
            throws IOException {
        logger.debug("analyzing " + zipEntry.getName() + " in " + zip.getName());

        if (getExtension(zipEntry.getName()).equals("ggb")) {
            logger.debug("assuming it is a geogebra object (contains .ggb)");
            InputStream is = zip.getInputStream(zipEntry);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(is, baos);

            GgbToBase64Converter conv = new GgbToBase64Converter(baos.toByteArray());
            return new DownloadableImpl(conv.getHtml(), "text/html");
        }

        if (getExtension(zipEntry.getName()).equals("html")) {
            logger.debug("assuming it is a swiffy object (contains .html)");
            InputStream is = zip.getInputStream(zipEntry);

            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer);
            String content = writer.toString();

            if (!content.contains(SWIFFY_MARKUP)) {
                logger.debug("but it is not a swiffy object (wrong .html content)");
                return null;
            }

            JsonExtractor ext = new JsonExtractor(content);
            content = ext.buildSwiffyFromTemplate();
            return new DownloadableImpl(content, "text/html");
        }

        return null;
    }


    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(i + 1).toLowerCase();
        } else {
            return "";
        }
    }

}
