package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.util.IOUtils;

public class EmissionFormatContentServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(EmissionFormatContentServlet.class);

    private static final Logger EFC_LOGGER = Logger.getLogger("EmissionFormatContentServlet");

    private String contentServerURL = null;

    private HttpClient httpClient;

    private boolean initialized = false;


    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
        try {
            contentServerURL = config.getInitParameter("http.server.url");
            httpClient = new HttpClient();
            initialized = true;
        } catch (Exception ex) {
            Logger.getLogger(getClass()).error("An error occured while initializing servlet.", ex);
        }
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        if (EFC_LOGGER.isDebugEnabled())
            EFC_LOGGER.debug("Start request " + req.getPathInfo() + " service.");

        if (!initialized) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Servlet was not properly initialized and is unable to perform methods.");
            return;
        }
        Long id = null;
        try {
            id = Long.valueOf(req.getParameter("id"));
        } catch (NumberFormatException ex) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproper edition id parameter!");
            return;
        }

        String variant = req.getParameter("variant");
        if (StringUtils.isEmpty(variant)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Variant parameter was not send!");
            return;
        }

        String format = req.getParameter("format");
        if (StringUtils.isEmpty(format)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Format parameter was not send!");
            return;
        }

        String reqUrl;
        try {
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
            long[] contentIdAndVersion = Versioning.findContentIdAndVersion(new PublicationId(id), ms);
            reqUrl = contentServerURL + "/" + contentIdAndVersion[0] + "/" + contentIdAndVersion[1] + "/" + variant
                    + "/collection." + format;
        } catch (DLibraException e) {
            logger.error("Could not find contentId and version of collection " + id, e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        HttpMethod getMethod = new GetMethod(reqUrl);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            int statusCode = httpClient.executeMethod(getMethod);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method GET of " + reqUrl + " failed: " + getMethod.getStatusLine());
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "content.not.found");
                return;
            }

            if (format.equalsIgnoreCase("epub")) {
                res.setContentType("application/epub+zip");
            } else if (format.equalsIgnoreCase("pdf")) {
                res.setContentType("application/pdf");
            } else if (format.equalsIgnoreCase("html")) {
                res.setContentType("text/html");
            }

            InputStream is = getMethod.getResponseBodyAsStream();
            OutputStream os = res.getOutputStream();
            IOUtils.copyStream(is, os);

            os.flush();
            os.close();
            is.close();
        } catch (HttpException e) {
            Logger.getLogger(getClass()).error("Unexpected http error!", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected http error!");
        } catch (IOException e) {
            Logger.getLogger(getClass()).error("Fatal transport error!", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fatal transport error!");
        } catch (Exception e) {
            Logger.getLogger(getClass()).error("Unexpected error occured!", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occured!");
        } finally {
            getMethod.releaseConnection();

            if (EFC_LOGGER.isDebugEnabled())
                EFC_LOGGER.debug(
                    "Request " + req.getPathInfo() + " service time: " + (System.currentTimeMillis() - start));
        }

    }

}