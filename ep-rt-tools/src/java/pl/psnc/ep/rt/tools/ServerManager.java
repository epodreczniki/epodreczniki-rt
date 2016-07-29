package pl.psnc.ep.rt.tools;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import pl.psnc.dlibra.app.ApplicationRmiSocketFactory;
import pl.psnc.dlibra.app.ApplicationRmiSocketFactory.ConnectionMode;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.service.AuthorizationToken;
import pl.psnc.dlibra.service.Service;
import pl.psnc.dlibra.service.ServiceType;
import pl.psnc.dlibra.service.ServiceUrl;
import pl.psnc.dlibra.system.UserInterface;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserServer;
import pl.psnc.ep.rt.server.EPService;

public class ServerManager {

    public static final String DEFAULT_CONFIG_FILENAME = "config.properties";

    public static final String LOGIN = "login";

    public static final String PASSWORD = "password";

    private static final String SERVER_NAME = "server";

    private static final String PROXY_URL = "proxy";

    private static final String CONNECTION = "connection";

    private static final String TUNNEL_URL = "tunnelURL";
    private static final String DEFAULT_PORT = "10051";

    private static final String PORT_NAME = "port";
    private String login;
    private String password;
    private String serverIP;
    private int port;

    private String proxy;

    private static ApplicationRmiSocketFactory socketFactory;

    private Map<ServiceType, Service> servicesCache = new HashMap<ServiceType, Service>();

    private UserServiceResolver userServiceResolver;

    private User loggedUser;


    public ServerManager(Properties config) {
        login = config.getProperty(LOGIN, LOGIN);
        password = config.getProperty(PASSWORD, PASSWORD);
        serverIP = config.getProperty(SERVER_NAME, "localhost");
        try {
            port = Integer.parseInt(config.getProperty(PORT_NAME, DEFAULT_PORT));
        } catch (NumberFormatException e) {
            System.out.println("Port invalid! Using default value (10051)");
            port = Integer.valueOf(DEFAULT_PORT);
        }
        proxy = config.getProperty(PROXY_URL, null);
        if (proxy.isEmpty())
            proxy = null;

        try {
            socketFactory = new ApplicationRmiSocketFactory();
        } catch (IOException e) {
            System.err.println("Could not create socket factory");
            e.printStackTrace();
        }
        socketFactory.setProxy(proxy);

        ConnectionMode connectionMode = ConnectionMode.HTTPS;
        try {
            connectionMode = ConnectionMode.valueOf(config.getProperty(CONNECTION));
        } catch (IllegalArgumentException e) {
        }
        socketFactory.setConnectionMode(connectionMode);
        socketFactory.setTunnelingURL(config.getProperty(TUNNEL_URL));

        AuthorizationToken at = new AuthorizationToken(login, password);

        try {
            userServiceResolver = new UserServiceResolver(
                    new ServiceUrl(InetAddress.getByName(serverIP), UserInterface.SERVICE_TYPE, port), at);

            loggedUser = getUserServer().getUserManager().getUserData(login);
        } catch (Exception e) {
            System.err.println("Could not log in. Please check user, password, server IP or server port.");
            e.printStackTrace();
            System.exit(-3);
        }
    }


    public UserServiceResolver getUserServiceResolver() {
        return userServiceResolver;
    }


    public MetadataServer getMetadataServer() {
        return (MetadataServer) getServiceFromCache(MetadataServer.SERVICE_TYPE);
    }


    public UserServer getUserServer() {
        return (UserServer) getServiceFromCache(UserServer.SERVICE_TYPE);
    }


    public ContentServer getContentServer() {
        return (ContentServer) getServiceFromCache(ContentServer.SERVICE_TYPE);
    }


    private Service getServiceFromCache(ServiceType type) {
        Service service = servicesCache.get(type);
        if (service == null) {
            try {
                service = userServiceResolver.getService(type, null);
                servicesCache.put(type, service);
            } catch (Exception e) {
                System.err.println("Could not get service " + type + ".");
                e.printStackTrace();
                System.exit(-5);
            }
        }
        return service;
    }


    public EPService getEPService() {
        return (EPService) getServiceFromCache(EPService.SERVICE_TYPE);
    }


    public User getLoggedUser() {
        return loggedUser;
    }
}
