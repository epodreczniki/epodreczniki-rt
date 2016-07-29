package pl.psnc.ep.rt.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.metadata.Directory;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryManager;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.LongStringParser;
import com.martiansoftware.jsap.stringparsers.StringStringParser;

public class DirectoryCreator {

    private static final String ARG_LOGIN = "login";

    private static final String ARG_PASSWORD = "password";

    private static final String ARG_PARENT = "parent-dir";

    private static final String ARG_DIR_NAME = "dir-name";

    private static final String ARG_CONF = "conf";

    private static ServerManager manager = null;


    public static void main(String[] args)
            throws JSAPException, IOException {
        SimpleJSAP argsParser = new SimpleJSAP("createdir", "Tworzy nowe katalogi w Repozytorium Tresci");
        argsParser.registerParameter(new UnflaggedOption(ARG_LOGIN, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_PASSWORD, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(
            new UnflaggedOption(ARG_PARENT, LongStringParser.getParser(), true, "identyfikator katalogu nadrzednego"));
        FlaggedOption elementOption = new FlaggedOption(ARG_DIR_NAME, StringStringParser.getParser(), null, false, 'n',
                "dir-name", "nazwa nowego katalogu");
        argsParser.registerParameter(new FlaggedOption(ARG_CONF, FileStringParser.getParser(), "config.properties",
                false, 'c', "config", "plik konfiguracyjny"));
        elementOption.setAllowMultipleDeclarations(true);
        argsParser.registerParameter(elementOption);

        JSAPResult parsedArgs = argsParser.parse(args);
        if (argsParser.messagePrinted())
            System.exit(1);

        Properties serverConfig = new Properties();
        FileInputStream configIS = new FileInputStream(parsedArgs.getFile(ARG_CONF));
        try {
            serverConfig.load(configIS);
        } finally {
            configIS.close();
        }
        serverConfig.put(ServerManager.LOGIN, parsedArgs.getString(ARG_LOGIN));
        serverConfig.put(ServerManager.PASSWORD, parsedArgs.getString(ARG_PASSWORD));
        manager = new ServerManager(serverConfig);
        DLToolkit.setIOConf(serverConfig);

        DirectoryId parentId = new DirectoryId(parsedArgs.getLong(ARG_PARENT));

        if (parsedArgs.userSpecified(ARG_DIR_NAME)) {
            DirectoryManager dm = manager.getMetadataServer().getDirectoryManager();
            for (String dirName : parsedArgs.getStringArray(ARG_DIR_NAME)) {
                try {
                    Directory directory = new Directory(null, parentId);
                    directory.setLanguageName("pl");
                    directory.setName(dirName);
                    DirectoryId dirId = dm.createDirectory(directory);
                    System.out.println(dirId);
                } catch (Exception e) {
                    System.out.println("Error while creating directory " + dirName);
                    e.printStackTrace();
                }
            }
        }
    }
}
