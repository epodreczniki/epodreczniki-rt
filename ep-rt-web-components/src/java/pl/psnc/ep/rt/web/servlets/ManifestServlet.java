package pl.psnc.ep.rt.web.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.util.ObjectMalformedException;
import pl.psnc.ep.rt.util.SimpleImageInfo;
import pl.psnc.ep.rt.util.WOMIXMLHandler;
import pl.psnc.ep.rt.web.Util;

public class ManifestServlet extends HttpServlet {

    private static final String JS_PARAMETERS = "parameters";

    private static final String JS_MAIN_FILE = "mainFile";

    private static final String JS_VERSION = "version";

    private static final String JS_ENGINE = "engine";

    private static final String JS_HEIGHT_RATIO = "heightRatio";

    private static final String JS_MIME_TYPE = "mimeType";

    private static final String JS_OBJECT = "object";

    private static final String JS_STATIC_ALTERNATIVE = "advancedStaticAlternative";

    private static final Logger logger = Logger.getLogger(ManifestServlet.class);

    private static final Pattern SWIFFY_VERSION_PATTERN = Pattern
            .compile("<script.+src=\"https://www.gstatic.com/swiffy/v([^/]+)/");

    private static final Pattern SWIFFY_RESOLUTION_PATTERN = Pattern
            .compile("<div\\s+id=\"swiffycontainer\"\\s+style=\"width:\\s*([0-9]+)px;\\s*height:\\s*([0-9]+)px\\s*\">");


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        EditionId womiId = Util.getRequestedId(req, resp);
        if (womiId == null)
            return;

        try {
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
            ContentServer cs = ServicesManager.getInstance().getContetServer();
            AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) ms.getPublicationManager()
                    .getObjects(new EditionFilter(womiId), new OutputFilter(AbstractPublicationInfo.class))
                    .getResultInfo();
            if (pubInfo == null || pubInfo.getState() != Publication.PUB_STATE_ACTUAL
                    || pubInfo.getStatus() != Publication.PUB_NORMAL) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Map<WOMIFormat, String> womiData = Util.loadWOMIData(womiId, ms, cs, true);
            boolean hasStaticAlternative = womiData.remove(WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT) != null;
            WOMIType womiType = womiData.keySet().iterator().next().womiType;
            JSONObject manifest = new JSONObject();
            JSONObject parameters = new JSONObject();
            manifest.put(JS_PARAMETERS, parameters);
            switchToBreak: switch (womiType) {
                case INTERACTIVE:
                    WOMIFormat packageFormat = new WOMIFormat(womiType, MediaFormat.PACKAGE);
                    FileFilter fileFilter = new FileFilter(
                            WOMIXMLHandler.toDLibraPath(packageFormat, womiData.get(packageFormat)));
                    fileFilter.setEditionId(womiId);
                    VersionId packageId = (VersionId) ms.getFileManager()
                            .getObjects(fileFilter, new OutputFilter(VersionId.class)).getResultId();
                    File packageFile = cs.getEditionFiles(Arrays.asList(packageId)).get(0);
                    ZipFile zip = null;
                    try {
                        zip = new ZipFile(packageFile);
                        ZipArchiveEntry entry = zip.getEntry("manifest.json");
                        if (entry != null) {
                            InputStream is = zip.getInputStream(entry);
                            String content = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
                            is.close();
                            try {
                                manifest = new JSONObject(content);
                                if (manifest.has(JS_PARAMETERS))
                                    parameters = manifest.getJSONObject(JS_PARAMETERS);
                                else
                                    manifest.put(JS_PARAMETERS, parameters);
                            } catch (JSONException e) {
                                logger.warn("Invalid manifest.json in WOMI " + womiId);
                            }
                            break switchToBreak;
                        }

                        int count = 0;
                        for (Enumeration<? extends ZipArchiveEntry> it = zip.getEntries(); it.hasMoreElements();) {
                            count++;
                            entry = it.nextElement();
                            if (entry.getName().toLowerCase().endsWith(".html") && count == 1
                                    && !it.hasMoreElements()) {
                                processSwiffy(zip, entry, manifest);
                                break switchToBreak;
                            }
                            if (entry.getName().toLowerCase().endsWith(".ggb") && count == 1 && !it.hasMoreElements()) {
                                processGeogebra(zip, entry, manifest);
                                break switchToBreak;
                            }
                        }
                    } catch (IOException e) {
                        logger.warn("It seems package file in womi " + womiId + " is corrupted", e);
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    } finally {
                        if (zip != null)
                            zip.close();
                        cs.releaseElement(packageId);
                    }

                    break;
                case MOVIE:
                    EPService eps = (EPService) ServicesManager.getInstance().getServiceResolver()
                            .getService(EPService.SERVICE_TYPE, null);
                    Double aspectRatio = eps.getVideoAspectRatio(womiId);
                    if (aspectRatio != null && aspectRatio != 0) {
                        JSONObject ratioObject = new JSONObject();
                        ratioObject.put(JS_HEIGHT_RATIO, 1.0 / aspectRatio);
                        parameters.put(JS_OBJECT, ratioObject);
                    }
                    manifest.put(JS_ENGINE, womiType.toExternalForm().toLowerCase());
                    break;
                default:
                    manifest.put(JS_ENGINE, womiType.toExternalForm().toLowerCase());
                    break;
            }

            processImage(womiData, womiId, parameters, ms, cs);

            if (hasStaticAlternative) {
                JSONObject alternativeObject = new JSONObject();
                alternativeObject.put(JS_MIME_TYPE, "application/zip");
                parameters.put(JS_STATIC_ALTERNATIVE, alternativeObject);
            }

            byte[] content = manifest.toString(1).getBytes("UTF-8");
            resp.setContentLength(content.length);
            resp.setContentType(Util.JSON_CONTENT_TYPE);
            resp.getOutputStream().write(content);
        } catch (ObjectMalformedException e) {
            logger.error("Unexpected error occured while accessing WOMI " + womiId, e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IdNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (DLibraException e) {
            logger.error("Unexpected error occured while accessing server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void processGeogebra(final ZipFile zip, final ZipArchiveEntry entry, final JSONObject manifest)
            throws IOException {
        manifest.put(JS_ENGINE, "geogebra");
        manifest.put(JS_MAIN_FILE, "geogebra.html");

        DefaultHandler handler = new DefaultHandler() {

            boolean foundVersion, foundRatio;


            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
                if (localName.equals("geogebra")) {
                    manifest.put(JS_VERSION, attributes.getValue("version"));
                    foundVersion = true;
                }
                if (localName.equals("window")) {
                    try {
                        int width = Integer.valueOf(attributes.getValue("width"));
                        int height = Integer.valueOf(attributes.getValue("height"));
                        float heightRatio = 1.0f * height / width;
                        JSONObject params = manifest.getJSONObject(JS_PARAMETERS);
                        JSONObject ratioObject = new JSONObject();
                        ratioObject.put(JS_HEIGHT_RATIO, heightRatio);
                        params.put(JS_OBJECT, ratioObject);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid window size in " + zip + ":" + entry.getName() + " ("
                                + attributes.getValue("width") + ", " + attributes.getValue("height") + ")");
                    }
                    foundRatio = true;
                }
                if (foundVersion && foundRatio)
                    throw new SAXException("First element analyzed, stop parsing");
            }
        };
        ZipArchiveInputStream geogebraZip = new ZipArchiveInputStream(zip.getInputStream(entry));
        try {
            ZipArchiveEntry geogebraEntry = geogebraZip.getNextZipEntry();
            while (geogebraEntry != null) {
                if (geogebraEntry.getName().equals("geogebra.xml")) {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    SAXParser parser = factory.newSAXParser();
                    parser.parse(geogebraZip, handler);
                    break;
                }
                geogebraEntry = geogebraZip.getNextZipEntry();
            }
        } catch (SAXException e) {
        } catch (ParserConfigurationException e) {
            logger.warn("Could not parse xml in file " + zip + ":" + entry.getName(), e);
        } finally {
            geogebraZip.close();
        }
    }


    private void processSwiffy(ZipFile zip, ZipArchiveEntry entry, JSONObject manifest)
            throws IOException {
        manifest.put(JS_ENGINE, "swiffy");
        manifest.put(JS_MAIN_FILE, "swiffy.html");

        InputStream is = zip.getInputStream(entry);
        try {
            String content = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            Matcher matcher = SWIFFY_VERSION_PATTERN.matcher(content);
            manifest.put(JS_VERSION, matcher.find() ? matcher.group(1) : "5.2");

            matcher = SWIFFY_RESOLUTION_PATTERN.matcher(content);
            if (matcher.find()) {
                int width = Integer.valueOf(matcher.group(1));
                int height = Integer.valueOf(matcher.group(2));
                float heightRatio = 1.0f * height / width;
                JSONObject params = manifest.getJSONObject(JS_PARAMETERS);
                JSONObject ratioObject = new JSONObject();
                ratioObject.put(JS_HEIGHT_RATIO, heightRatio);
                params.put(JS_OBJECT, ratioObject);
            }
        } finally {
            is.close();
        }
    }


    private void processImage(Map<WOMIFormat, String> womiData, EditionId womiId, JSONObject parameters,
            MetadataServer ms, ContentServer cs)
                    throws DLibraException, IOException {
        WOMIFormat defaultFormat = null;
        SimpleImageInfo defaultImage = null;
        for (TargetFormat targetFormat : TargetFormat.ALL_FORMATS) {
            WOMIFormat imageFormat = getImageFormat(womiData, targetFormat);
            if (imageFormat == null)
                return;
            SimpleImageInfo image;
            if (targetFormat == TargetFormat.DEFAULT) {
                defaultFormat = imageFormat;
                defaultImage = image = getImageInfo(womiData, womiId, imageFormat, ms, cs);
            } else if (imageFormat.equals(defaultFormat))
                image = defaultImage;
            else
                image = getImageInfo(womiData, womiId, imageFormat, ms, cs);

            JSONObject imageInformation = new JSONObject();
            JSONArray resolutions = new JSONArray();
            for (int i : targetFormat.getResolutions()) {
                resolutions.put(i);
            }
            imageInformation.put("resolution", resolutions);
            String mimeType = image.getMimeType();
            if (mimeType.equals("image/svg+xml") && targetFormat == TargetFormat.EBOOK)
                mimeType = "image/png";
            imageInformation.put(JS_MIME_TYPE, mimeType);
            imageInformation.put(JS_HEIGHT_RATIO, 1.0f * image.getHeight() / image.getWidth());
            parameters.put(targetFormat.name().toLowerCase(), imageInformation);
        }
    }


    private WOMIFormat getImageFormat(Map<WOMIFormat, String> womiData, TargetFormat targetFormat) {
        WOMIType womiType = womiData.keySet().iterator().next().womiType;
        for (MediaFormat mediaFormat : new MediaFormat[] { MediaFormat.IMAGE, MediaFormat.ICON }) {
            WOMIFormat imageFormat = new WOMIFormat(womiType, mediaFormat, targetFormat, false);
            if (womiData.containsKey(imageFormat))
                return imageFormat;
            imageFormat = new WOMIFormat(womiType, mediaFormat, TargetFormat.DEFAULT, false);
            if (womiData.containsKey(imageFormat))
                return imageFormat;
        }
        return null;
    }


    private SimpleImageInfo getImageInfo(Map<WOMIFormat, String> womiData, EditionId womiId, WOMIFormat imageFormat,
            MetadataServer ms, ContentServer cs)
                    throws DLibraException, IOException {
        FileFilter fileFilter = new FileFilter(WOMIXMLHandler.toDLibraPath(imageFormat, womiData.get(imageFormat)));
        fileFilter.setEditionId(womiId);
        VersionId imageId = (VersionId) ms.getFileManager().getObjects(fileFilter, new OutputFilter(VersionId.class))
                .getResultId();
        File imageFile = cs.getEditionFiles(Arrays.asList(imageId)).get(0);
        try {
            SimpleImageInfo imageInfo = new SimpleImageInfo(imageFile);
            return imageInfo;
        } finally {
            cs.releaseElement(imageId);
        }
    }
}
