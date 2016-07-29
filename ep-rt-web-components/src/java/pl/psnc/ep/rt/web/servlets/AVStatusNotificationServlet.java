package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.server.EPService;

public class AVStatusNotificationServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AVStatusNotificationServlet.class);


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String transactionId = req.getParameter("transactionId");
        String status = req.getParameter("status");

        logger.info("Report from repoAV: transactionId: " + transactionId + ", status: " + status);

        if (!"Recoded".equals(status)) {
            return;
        }

        Long womiId;
        try {
            womiId = Long.valueOf(transactionId);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "transactionId is not a number");
            return;
        }

        try {
            EPService eps = (EPService) ServicesManager.getInstance().getServiceResolver()
                    .getService(EPService.SERVICE_TYPE, null);
            eps.reportAVMaterialUpdate(new EditionId(womiId));
        } catch (DLibraException e) {
            logger.error("Could not pass video status to server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
