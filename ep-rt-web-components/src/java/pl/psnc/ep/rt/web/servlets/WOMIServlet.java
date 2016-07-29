package pl.psnc.ep.rt.web.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.content.PluginContentInfo;
import pl.psnc.dlibra.content.server.BasicRemoteOutputStream;
import pl.psnc.dlibra.content.server.DigestedElementOutputStream;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.EditionInfo;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.common.exceptions.PublicIdentityProviderException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.dlibra.web.fw.util.servlet.RequestWrapperFactory;
import pl.psnc.dlibra.web.fw.util.servlet.ServletRequestWrapper;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.util.MimetypesMapProvider;
import pl.psnc.ep.rt.util.ObjectMalformedException;
import pl.psnc.ep.rt.util.SimpleImageInfo;
import pl.psnc.ep.rt.util.WOMIXMLHandler;
import pl.psnc.ep.rt.web.Util;
import pl.psnc.ep.rt.web.womi.Downloadable;
import pl.psnc.ep.rt.web.womi.WomiPreprocessor;
import pl.psnc.util.IOUtils;

public class WOMIServlet extends HttpServlet {

    private static final String PLUGIN_IMAGEMAGICK = "imagick";

    private static final String PLUGIN_UNZIP = "unzip";

    private static final Logger LOGGER = Logger.getLogger(WOMIServlet.class);

    private static final Logger WOMI_LOGGER = Logger.getLogger("WOMIServlet");


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();

        if (WOMI_LOGGER.isDebugEnabled())
            WOMI_LOGGER.debug("Start request " + req.getPathInfo() + " service.");

        PluginContentInfo ci = buildContentInfo(req, resp);

        if (WOMI_LOGGER.isDebugEnabled())
            WOMI_LOGGER.debug("End preparing WOMI content info. Id: " + (ci != null ? ci.getEditionId().getId() : null)
                    + ". Time: " + (System.currentTimeMillis() - start));

        if (ci == null)
            return;

        try {
            long start2 = System.currentTimeMillis();
            if (WOMI_LOGGER.isDebugEnabled())
                WOMI_LOGGER.debug("Start reading WOMI content. Id: " + ci.getEditionId().getId());

            ContentServer cs = ServicesManager.getInstance().getContetServer();
            resp.setHeader("Content-Disposition", "inline");

            if (ci.getPluginId().equals(PLUGIN_IMAGEMAGICK)) {
                ByteArrayOutputStream arrayStream = new ByteArrayOutputStream(1024 * 256);
                DigestedElementOutputStream elementOutputStream = new DigestedElementOutputStream(
                        new BasicRemoteOutputStream(arrayStream, false));
                try {
                    cs.getContent(ci, elementOutputStream);
                } finally {
                    elementOutputStream.close();
                }
                byte[] bytes = arrayStream.toByteArray();
                SimpleImageInfo sii = new SimpleImageInfo(bytes);
                resp.setContentType(sii.getMimeType());

                try {
                    resp.getOutputStream().write(bytes);
                    resp.getOutputStream().flush();
                } finally {
                    resp.getOutputStream().close();
                }
            } else if (ci.getPluginId().equals(PLUGIN_UNZIP)) {
                Object extension = ci.getParameters().get(1);
                String mimeType = MimetypesMapProvider.get().getContentType("file." + extension);
                resp.setContentType(mimeType);

                DigestedElementOutputStream elementOutputStream = new DigestedElementOutputStream(
                        new BasicRemoteOutputStream(resp.getOutputStream(), false));

                cs.getContent(ci, elementOutputStream);
                resp.getOutputStream().flush();
                elementOutputStream.close();
            } else {
                throw new RuntimeException("Content type finding not implemented for plugin " + ci.getPluginId());
            }

            if (WOMI_LOGGER.isDebugEnabled())
                WOMI_LOGGER.debug("End reading WOMI content. Id: " + ci.getEditionId().getId() + ". Time: "
                        + (System.currentTimeMillis() - start2));
        } catch (RemoteException e) {
            Throwable detail = e.detail;
            while (detail instanceof RemoteException)
                detail = ((RemoteException) detail).detail;
            if ((detail instanceof IdNotFoundException) || (detail instanceof FileNotFoundException)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                LOGGER.error("Unexpected error occured while preparing womi " + ci.getEditionId(), e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            LOGGER.error("Unexpected error occured while preparing womi " + ci.getEditionId(), ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (WOMI_LOGGER.isDebugEnabled())
                WOMI_LOGGER.debug(
                    "Request " + req.getPathInfo() + " service time: " + (System.currentTimeMillis() - start));
        }

    }


    private PluginContentInfo buildContentInfo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        ServletRequestWrapper reqWr = null;
        try {
            reqWr = RequestWrapperFactory.getInstance(req, resp);
        } catch (PublicIdentityProviderException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        String pathInfo = reqWr.getPathInfo();
        if (StringUtils.isEmpty(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        String[] desc = pathInfo.split("/");
        if (desc.length < 4) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproperly formed request.");
            return null;
        }

        MediaFormat media;
        String mediaStr = desc[2];
        try {
            media = MediaFormat.valueOf(mediaStr.toUpperCase());
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Media: " + mediaStr + " not supported.");
            return null;
        }

        TargetFormat target;
        String targetStr = desc[3];
        try {
            target = TargetFormat.valueOf(targetStr.toUpperCase());
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Target: " + targetStr + " not supported.");
            return null;
        }

        EditionId eid;
        Map<WOMIFormat, String> filesMap;
        try {
            String idStr = desc[1];
            eid = new EditionId(Long.valueOf(idStr));
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
            EditionInfo einfo = (EditionInfo) ms.getPublicationManager()
                    .getObjects(new EditionFilter(eid), new OutputFilter(EditionInfo.class)).getResultInfo();
            if (einfo == null || einfo.getState() != Edition.PUBLISHED)
                throw new IdNotFoundException(eid);

            ContentServer cs = ServicesManager.getInstance().getContetServer();
            filesMap = Util.loadWOMIData(eid, ms, cs);
            boolean mediaExists = false;
            for (WOMIFormat format : filesMap.keySet()) {
                if (format.mediaFormat == media)
                    mediaExists = true;
            }
            if (!mediaExists) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
        } catch (ObjectMalformedException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "WOMI is malformed.");
            return null;
        } catch (NumberFormatException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Numeric ID value required.");
            return null;
        } catch (IdNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        } catch (DLibraException e) {
            LOGGER.error("Unexpected error during server communication", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        List<Object> params = new ArrayList<Object>();
        params.add(target.toString());
        switch (media) {
            case ICON:
                params.add(0);
                params.add(false);
                params.add(false);
                return new PluginContentInfo(eid, PLUGIN_IMAGEMAGICK, params);
            case IMAGE:
                int res = 0;
                if (reqWr.getRequestParameter("cover") != null) {
                    res = target.getCoverResolution();
                } else if (reqWr.getRequestParameter("miniature") != null) {
                    res = target.getMiniatureResolution();
                } else {
                    try {
                        String resStr = reqWr.getRequestParameter("res");
                        res = Integer.valueOf(resStr);
                        if (Arrays.binarySearch(target.getResolutions(), res) < 0) {
                            res = getDefaultResolution(target);
                        }
                    } catch (NumberFormatException ex) {
                        res = getDefaultResolution(target);
                    }
                }
                params.add(res);

                params.add(reqWr.getRequestParameter("3D") != null);
                params.add(reqWr.getRequestParameter("svg2png") != null);
                return new PluginContentInfo(eid, PLUGIN_IMAGEMAGICK, params);
            case PACKAGE:
                String fileTypeStr = reqWr.getRequestParameter("filetype");
                String pathStr = reqWr.getRequestParameter("path");
                if (reqWr.getRequestParameter("zip") != null) {
                    sendOriginalDirectly(resp, eid, filesMap);
                } else if (reqWr.getRequestParameter("list") != null) {
                    sendListing(resp, eid, filesMap);
                } else if (!StringUtils.isEmpty(pathStr)) {
                    extractDirectly(resp, eid, pathStr, filesMap);
                } else if (!StringUtils.isEmpty(fileTypeStr)) {
                    params.add(fileTypeStr);
                    return new PluginContentInfo(eid, PLUGIN_UNZIP, params);
                } else {
                    convertInteractiveDirectly(resp, eid, filesMap);
                }
                return null;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Format support not implemented: " + media);
                return null;
        }
    }


    private int getDefaultResolution(TargetFormat target) {
        int[] ress = target.getResolutions();
        return ress[ress.length - 1];
    }


    private void sendOriginalDirectly(HttpServletResponse resp, EditionId womiId, Map<WOMIFormat, String> filesMap)
            throws IOException {
        try {
            VersionInfo packageInfo;
            packageInfo = getPackageFile(womiId, filesMap);
            if (packageInfo == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            ContentServer cs = ServicesManager.getInstance().getContetServer();
            File packageFile = cs.getEditionFiles(Arrays.asList(packageInfo.getId())).get(0);
            FileInputStream input = new FileInputStream(packageFile);
            resp.setContentType("application/zip");
            if (packageInfo.getSize() <= Integer.MAX_VALUE)
                resp.setContentLength((int) packageInfo.getSize());
            String fileName = filesMap.get(new WOMIFormat(WOMIType.INTERACTIVE, MediaFormat.PACKAGE));
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            ServletOutputStream output = resp.getOutputStream();
            try {
                IOUtils.copyStream(input, output);
            } finally {
                input.close();
                output.close();
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error occured while processing package " + womiId, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void sendListing(HttpServletResponse resp, EditionId womiId, Map<WOMIFormat, String> filesMap)
            throws IOException {
        try {
            VersionInfo packageInfo = getPackageFile(womiId, filesMap);
            if (packageInfo == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            JSONObject json = new JSONObject();
            JSONArray listing = new JSONArray();
            json.put("files", listing);

            ContentServer cs = ServicesManager.getInstance().getContetServer();
            File packageFile = cs.getEditionFiles(Arrays.asList(packageInfo.getId())).get(0);
            ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(packageFile));
            try {
                ZipArchiveEntry entry;
                while ((entry = zis.getNextZipEntry()) != null) {
                    if (entry.isDirectory())
                        continue;
                    JSONObject file = new JSONObject();
                    file.put("path", entry.getName());
                    file.put("size", entry.getSize());
                    listing.put(file);
                }
            } finally {
                zis.close();
                cs.releaseElement(packageInfo.getId());
            }

            byte[] content = json.toString(1).getBytes("UTF-8");
            resp.setContentLength(content.length);
            resp.setContentType(Util.JSON_CONTENT_TYPE);
            resp.getOutputStream().write(content);
        } catch (Exception e) {
            LOGGER.error("Unexpected error occured while processing package " + womiId, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void convertInteractiveDirectly(HttpServletResponse resp, EditionId womiId,
            Map<WOMIFormat, String> filesMap)
                    throws IOException {
        long start = System.currentTimeMillis();

        try {
            VersionInfo packageInfo = getPackageFile(womiId, filesMap);
            if (packageInfo == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            ContentServer cs = ServicesManager.getInstance().getContetServer();
            File packageFile = cs.getEditionFiles(Arrays.asList(packageInfo.getId())).get(0);
            Downloadable downloadable;
            try {
                downloadable = new WomiPreprocessor().processWomiInteractivePackage(packageFile);
            } finally {
                cs.releaseElement(packageInfo.getId());
            }

            if (WOMI_LOGGER.isDebugEnabled())
                WOMI_LOGGER.debug("Process Womi Interactive Package time: " + (System.currentTimeMillis() - start));
            if (downloadable == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            resp.setContentType(downloadable.getContentType());
            resp.setContentLength((int) downloadable.getContentLength());
            resp.setHeader("Content-Disposition", "inline");

            InputStream in = downloadable.getInputStream();
            try {
                OutputStream out = resp.getOutputStream();
                try {
                    IOUtils.copyStream(in, out);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }

        } catch (Exception ex) {
            LOGGER.error("Unexpected error occured while processing package " + womiId, ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (WOMI_LOGGER.isDebugEnabled())
                WOMI_LOGGER.debug("Direct conversion time: " + (System.currentTimeMillis() - start));
        }

    }


    private void extractDirectly(HttpServletResponse resp, EditionId womiId, String path,
            Map<WOMIFormat, String> filesMap)
                    throws IOException {
        long start = System.currentTimeMillis();

        try {
            VersionInfo packageInfo = getPackageFile(womiId, filesMap);
            if (packageInfo == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            ContentServer cs = ServicesManager.getInstance().getContetServer();
            File packageFile = cs.getEditionFiles(Arrays.asList(packageInfo.getId())).get(0);

            ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(packageFile));
            try {
                ZipArchiveEntry entry;
                while ((entry = zis.getNextZipEntry()) != null) {
                    String entryPath = entry.getName().replace('\\', '/');
                    if (entryPath.equals(path)) {
                        if (entry.getSize() != -1 && entry.getSize() < Integer.MAX_VALUE)
                            resp.setContentLength((int) entry.getSize());
                        resp.setHeader("Content-Disposition", "inline");
                        ServletOutputStream output = resp.getOutputStream();
                        try {
                            IOUtils.copyStream(zis, output);
                        } finally {
                            output.close();
                        }
                        return;
                    }
                }
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                LOGGER.warn("It seems package file in womi " + womiId + " is corrupted", e);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } finally {
                zis.close();
                cs.releaseElement(packageInfo.getId());
            }
        } catch (DLibraException e) {
            LOGGER.error("Unexpected error occured while extracting " + path + " from package " + womiId, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (WOMI_LOGGER.isDebugEnabled())
                WOMI_LOGGER.debug("Direct extraction time: " + (System.currentTimeMillis() - start));
        }
    }


    private VersionInfo getPackageFile(EditionId womiId, Map<WOMIFormat, String> filesMap)
            throws RemoteException, DLibraException {
        MetadataServer ms = ServicesManager.getInstance().getMetadataServer();

        WOMIFormat format = new WOMIFormat(WOMIType.INTERACTIVE, MediaFormat.PACKAGE);
        String fileName = filesMap.get(format);
        if (fileName == null) {
            return null;
        }
        FileFilter fileFilter = new FileFilter(WOMIXMLHandler.toDLibraPath(format, fileName));
        fileFilter.setEditionId(womiId);
        VersionId versionId = (VersionId) ms.getFileManager().getObjects(fileFilter, new OutputFilter(VersionId.class))
                .getResultId();
        VersionInfo versionInfo = (VersionInfo) ms.getFileManager()
                .getObjects(new InputFilter(versionId), new OutputFilter(VersionInfo.class)).getResultInfo();
        return versionInfo;
    }
}