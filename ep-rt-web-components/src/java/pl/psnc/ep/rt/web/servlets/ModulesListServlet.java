package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.web.Util;

public class ModulesListServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ModulesListServlet.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            EPService eps = (EPService) ServicesManager.getInstance().getServiceResolver()
                    .getService(EPService.SERVICE_TYPE, null);
            JSONArray resultArray = new JSONArray();
            List<String> modulesIds = eps.getAllModulesIds();
            for (String id : modulesIds) {
                resultArray.put(id);
            }

            byte[] content = resultArray.toString(1).getBytes("UTF-8");
            resp.setContentLength(content.length);
            resp.setContentType(Util.JSON_CONTENT_TYPE);
            resp.getOutputStream().write(content);
        } catch (Exception e) {
            logger.error("Unexpected error occured while accessing server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }
}
