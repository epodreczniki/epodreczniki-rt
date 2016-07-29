package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileInfo;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.web.Util;
import pl.psnc.util.IOUtils;

public class MainFileServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(MetadataServlet.class);

    private static Map<EditionId, String> contentTypes = new ConcurrentHashMap<EditionId, String>();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        EditionId womiId = Util.getRequestedId(req, resp);
        if (womiId == null)
            return;

        try {
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
            VersionId mainVersionId = (VersionId) ms.getPublicationManager()
                    .getObjects(new EditionFilter(womiId).setMainVersion(true), new OutputFilter(VersionId.class))
                    .getResultId();

            resp.setContentType("text/xml");
            ContentServer cs = ServicesManager.getInstance().getContetServer();
            InputStream fis = cs.getVersionInputStream(mainVersionId);
            try {
                IOUtils.copyStream(fis, resp.getOutputStream());
            } finally {
                fis.close();
                cs.releaseElement(mainVersionId);
            }
            resp.getOutputStream().close();
        } catch (IdNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (DLibraException e) {
            logger.error("Unexpected error occured while accessing server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
