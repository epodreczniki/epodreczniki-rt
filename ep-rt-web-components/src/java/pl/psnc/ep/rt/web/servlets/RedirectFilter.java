package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;

public class RedirectFilter implements Filter {

    private static final Logger logger = Logger.getLogger(RedirectFilter.class);


    private static class RedirectResponseWrapper extends HttpServletResponseWrapper {

        private final String host;
        private final int port;


        public RedirectResponseWrapper(HttpServletResponse response, String host, int port) {
            super(response);
            this.host = host;
            this.port = port;
        }


        @Override
        public void sendRedirect(String location)
                throws IOException {
            URI uri = URI.create(location);
            if (!uri.getHost().equals(host) || uri.getPort() != port) {
                try {
                    URI newURI = new URI(uri.getScheme(), uri.getUserInfo(), host, port, uri.getPath(), uri.getQuery(),
                            uri.getFragment());
                    super.sendRedirect(newURI.toString());
                    return;
                } catch (URISyntaxException e) {
                    logger.error("Could not change redirect URI", e);
                }
            }
            super.sendRedirect(location);
        }
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        RedirectResponseWrapper responseWrapper = new RedirectResponseWrapper((HttpServletResponse) resp,
                req.getServerName(), req.getServerPort());
        chain.doFilter(req, responseWrapper);
    }


    @Override
    public void init(FilterConfig arg0)
            throws ServletException {
    }


    @Override
    public void destroy() {
    }

}
