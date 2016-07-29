package pl.psnc.ep.rt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;

import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.ep.rt.server.EPService;

public class DirectoryRetriever {

    private static final String DEFAULT_CONFIG_FILENAME = "config.properties";


    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: \nretriever username password [configfile]\n"
                    + "username = user name in e-podreczniki\n" + "password = user's password\n"
                    + "configfile = path to configuration file with server connection details, defaults to config.properties");
            System.exit(-100);
        }

        ServerManager manager;
        try {
            Properties serverConfig = readConfiguration(args);
            manager = connect(serverConfig);
        } catch (FileNotFoundException e1) {
            System.err.println("Configuration file not found.");
            e1.printStackTrace();
            System.exit(-1);
            return;
        } catch (IOException e1) {
            System.err.println("IO problem with Configuration file.");
            e1.printStackTrace();
            System.exit(-2);
            return;
        }

        try {
            EPService ep = (EPService) manager.getUserServiceResolver().getService(EPService.SERVICE_TYPE, null);
            String folderStructure = ep.getFolderStructure(args[0]);
            PrintWriter out;
            try {
                out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This should never happen", e);
            }
            out.println(folderStructure);
            out.close();
            System.exit(0);
        } catch (RemoteException e) {
            System.err.println("Problem with RMI call to server.");
            e.printStackTrace();
            System.exit(-7);
        } catch (DLibraException e) {
            System.err.println("dLibra error.");
            e.printStackTrace();
            System.exit(-8);
        }
    }


    private static ServerManager connect(Properties serverConfig) {
        ServerManager manager = new ServerManager(serverConfig);
        return manager;
    }


    private static Properties readConfiguration(String[] args)
            throws IOException, FileNotFoundException {
        String configFilename = args.length == 3 ? args[2] : DEFAULT_CONFIG_FILENAME;
        File serverConfFile = new File(configFilename);
        Properties serverConfig = new Properties();
        FileInputStream fis = new FileInputStream(serverConfFile);
        try {
            serverConfig.load(fis);
        } finally {
            fis.close();
        }
        serverConfig.put(ServerManager.LOGIN, args[0]);
        serverConfig.put(ServerManager.PASSWORD, args[1]);
        return serverConfig;
    }
}
