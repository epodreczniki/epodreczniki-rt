package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.common.exceptions.PublicIdentityProviderException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.dlibra.web.fw.util.servlet.RequestWrapperFactory;
import pl.psnc.dlibra.web.fw.util.servlet.ServletRequestWrapper;
import pl.psnc.ep.rt.web.Util;

public class SystemInfoServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SystemInfoServlet.class);

    private static final String TYPE_WOMI = "womi";
    private static final String TYPE_MODULE = "module";
    private static final String TYPE_COLLECTION = "collection";

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            return df;
        }
    };


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String objectType;
        String objectId;
        String objectVersion = null;
        try {
            ServletRequestWrapper reqWr = RequestWrapperFactory.getInstance(req, resp);
            String[] path = reqWr.getPathInfo().split("/");
            if (path.length < 3 || path.length > 4) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproperly formed request.");
                return;
            }
            objectType = path[1];
            objectId = path[2];
            if (path.length == 4)
                objectVersion = path[3];
        } catch (PublicIdentityProviderException e) {
            logger.error("Request wrapper failed", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        Date modificationDate;
        DirectoryId directoryId;
        try {
            PublicationManager pm = ServicesManager.getInstance().getMetadataServer().getPublicationManager();
            Edition edition = null;
            if (objectType.equals(TYPE_WOMI)) {
                EditionId editionId = new EditionId(Long.parseLong(objectId));
                edition = (Edition) pm.getObjects(new EditionFilter(editionId), new OutputFilter(Edition.class))
                        .getResult();
            } else if (objectType.equals(TYPE_MODULE)) {
                edition = (Edition) pm
                        .getObjects(new EditionFilter().setExternalId(objectId), new OutputFilter(Edition.class))
                        .getResult();
            }

            Publication publication;
            if (edition != null) {
                modificationDate = edition.getModificationTime();
                publication = (Publication) pm
                        .getObjects(new EditionFilter((EditionId) edition.getId()), new OutputFilter(Publication.class))
                        .getResult();
            } else if (objectType.equals(TYPE_COLLECTION)) {
                PublicationId rootId = new PublicationId(Long.parseLong(objectId));
                int version = objectVersion != null ? Integer.parseInt(objectVersion) : 1;
                PublicationId publicationId = Util.findVersion(rootId, version);
                if (publicationId == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                publication = (Publication) pm
                        .getObjects(new PublicationFilter(publicationId), new OutputFilter(Publication.class))
                        .getResult();
                if (publication == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                modificationDate = publication.getModificationTime();
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            directoryId = (DirectoryId) publication.getParentId();
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (IdNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (DLibraException e) {
            logger.error("Unexpected error occured while accessing server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        JSONObject systemInfo = new JSONObject();
        systemInfo.put("lastModified", DATE_FORMAT.get().format(modificationDate));
        systemInfo.put("directoryId", directoryId);
        byte[] content = systemInfo.toString(1).getBytes("UTF-8");
        resp.setContentLength(content.length);
        resp.setContentType(Util.JSON_CONTENT_TYPE);
        resp.getOutputStream().write(content);
    }
}
