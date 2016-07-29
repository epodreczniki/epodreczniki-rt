package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;

public class PublicationAliasServlet extends HttpServlet {

    private static final String PATH = "/publication-alias/";

    private static final Logger logger = Logger.getLogger(PublicationAliasServlet.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        URI reqURI = URI.create(req.getRequestURL().toString());
        String requestPath = reqURI.getPath();
        int baseLength = requestPath.indexOf(PATH);
        String dirPart = requestPath.substring(baseLength + PATH.length());
        DirectoryId dirId = getDirectoryId(dirPart, res);
        if (dirId == null)
            return;

        List<AbstractPublicationInfo> elements = getElements(dirId, res);
        if (elements == null)
            return;

        if (elements.isEmpty()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "No handbooks in directory: " + dirId);
            return;
        }

        long maxId = 0;
        for (AbstractPublicationInfo info : elements) {
            Long id = info.getId().getId();
            if (id > maxId)
                maxId = id;
        }

        String redirectPath = requestPath.substring(0, baseLength) + "/publication/" + maxId;
        URI redirectURI;
        try {
            redirectURI = new URI(reqURI.getScheme(), reqURI.getUserInfo(), reqURI.getHost(), reqURI.getPort(),
                    redirectPath, req.getQueryString(), reqURI.getFragment());
            res.sendRedirect(redirectURI.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    private List<AbstractPublicationInfo> getElements(DirectoryId dirId, HttpServletResponse res)
            throws IOException {
        try {
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer(User.ADMIN_ID);
            DirectoryManager dm = ms.getDirectoryManager();
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<AbstractPublicationInfo> elements = (List) dm.getObjects(new DirectoryFilter(null, dirId)
                    .setGroupStatus((byte) (Publication.PUB_GROUP_ROOT)).setState(Publication.PUB_STATE_ACTUAL),
                new OutputFilter(AbstractPublicationInfo.class, List.class)).getResultInfos();
            return elements;
        } catch (IdNotFoundException e) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "No such directory: " + dirId);
            return null;
        } catch (DLibraException e) {
            logger.error("Could not contents of directory " + dirId, e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

    }


    private DirectoryId getDirectoryId(String dirPart, HttpServletResponse res)
            throws IOException {
        DirectoryId dirId;
        try {
            dirId = new DirectoryId(Long.valueOf(dirPart));
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid dir id: " + dirPart);
            return null;
        }
        return dirId;
    }
}
