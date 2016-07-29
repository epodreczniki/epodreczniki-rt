package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryInfo;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.web.Util;

public class FoldersServlet extends HttpServlet {

    private static final String DIRECTORY_SUBFOLDERS = "subfolders";

    private static final String DIRECTORY_NAME = "name";

    private static final String DIRECTORY_ID = "id";

    private static final String WOMI_COUNT = "womi-count";

    private static final Logger logger = Logger.getLogger(FoldersServlet.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        JSONObject rootNode;
        try {
            final boolean womiOnly = req.getParameter("womi-only") != null;

            DirectoryManager dm = ServicesManager.getInstance().getMetadataServer().getDirectoryManager();
            DirectoryInfo rootDir = (DirectoryInfo) dm
                    .getObjects(new DirectoryFilter(null, null), new OutputFilter(DirectoryInfo.class)).getResultInfo();

            rootNode = createJSONDirectory(rootDir, dm, womiOnly);
            if (rootNode == null) {
                rootNode = createJSONDirectorry(rootDir);
                rootNode.put(WOMI_COUNT, 0);
            }
        } catch (Exception e) {
            logger.error("Unexpected error occured while accessing server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        byte[] content = rootNode.toString(1).getBytes("UTF-8");
        resp.setContentLength(content.length);
        resp.setContentType(Util.JSON_CONTENT_TYPE);
        resp.getOutputStream().write(content);
    }


    private JSONObject createJSONDirectory(DirectoryInfo dir, DirectoryManager dm, boolean womiOnly)
            throws RemoteException, DLibraException {
        JSONObject dirObject = createJSONDirectorry(dir);
        if (womiOnly) {
            DirectoryFilter df = new DirectoryFilter(null, dir.getId());
            df.setGroupStatus(Publication.PUB_NORMAL).setState(Publication.PUB_STATE_ACTUAL).setRecursive(true);
            int womiCount = dm.getObjects(df, new OutputFilter(Integer.class)).getResultsCount();
            if (womiCount == 0)
                return null;
            dirObject.put(WOMI_COUNT, womiCount);
        }

        Collection<Info> subdirInfos = dm
                .getObjects(new DirectoryFilter(null, dir.getId()), new OutputFilter(DirectoryInfo.class))
                .getResultInfos();
        JSONArray subdirObjects = new JSONArray();
        for (Info subdirInfo : subdirInfos) {
            JSONObject subdirObject = createJSONDirectory((DirectoryInfo) subdirInfo, dm, womiOnly);
            if (subdirObject != null)
                subdirObjects.put(subdirObject);
        }
        dirObject.put(DIRECTORY_SUBFOLDERS, subdirObjects);
        return dirObject;
    }


    private JSONObject createJSONDirectorry(DirectoryInfo dir) {
        JSONObject dirObject = new JSONObject();
        dirObject.put(DIRECTORY_ID, dir.getId().getId().longValue());
        dirObject.put(DIRECTORY_NAME, dir.getLabel());
        return dirObject;
    }
}