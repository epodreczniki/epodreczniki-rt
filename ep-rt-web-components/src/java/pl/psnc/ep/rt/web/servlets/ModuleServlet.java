package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.io.InputStream;

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
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.util.IOUtils;

public class ModuleServlet extends HttpServlet {

    static final String PATH = "/module/";

    private static final Logger logger = Logger.getLogger(ModuleServlet.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String requestURL = req.getRequestURL().toString();
        int baseLength = requestURL.indexOf(PATH);
        String[] pathParts = requestURL.substring(baseLength + 8).split("/");
        if (pathParts.length < 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproperly formed request.");
            return;
        }
        String externalId = pathParts[0];
        EditionId editionId;
        try {
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
            PublicationManager pm = ms.getPublicationManager();
            editionId = (EditionId) pm
                    .getObjects(new EditionFilter().setExternalId(externalId), new OutputFilter(EditionId.class))
                    .getResultId();

            if (editionId == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            VersionId mainVersionId = (VersionId) pm
                    .getObjects(new EditionFilter(editionId).setMainVersion(true), new OutputFilter(VersionId.class))
                    .getResultId();

            VersionInfo mainVersionInfo = (VersionInfo) ms.getFileManager()
                    .getObjects(new InputFilter(mainVersionId), new OutputFilter(VersionInfo.class)).getResultInfo();
            resp.setContentLength((int) mainVersionInfo.getSize());

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
        } catch (Exception e) {
            logger.error("Unexpected error occured while accessing server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }
}
