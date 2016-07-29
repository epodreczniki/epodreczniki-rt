package pl.psnc.ep.rt.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.ep.rt.util.Versioning;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.LongStringParser;
import com.martiansoftware.jsap.stringparsers.StringStringParser;

public class PublicationDestroyer {

    private static final String ARG_OBJECT = "object";

    private static final String ARG_WOMI = "womi";

    private static final String ARG_DIR = "dir";

    private static final String ARG_CONF = "conf";

    private static final String ARG_LOGIN = "login";

    private static final String ARG_PASSWORD = "password";

    private static final Logger logger = Logger.getLogger(PublicationDestroyer.class);

    private static ServerManager manager = null;


    public static void main(String[] args)
            throws IOException, JSAPException {
        SimpleJSAP argsParser = new SimpleJSAP("downloader",
                "Usuwa wybrane obiekty z Repozytorium Tresci. UWAGA, OPERACJA NIEODWRACALNA!");
        argsParser.registerParameter(new UnflaggedOption(ARG_LOGIN, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_PASSWORD, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new FlaggedOption(ARG_CONF, FileStringParser.getParser(), "config.properties",
                false, 'c', "config", "plik konfiguracyjny"));
        argsParser.registerParameter(new FlaggedOption(ARG_DIR, LongStringParser.getParser(), null, false, 'd', "dir",
                "identyfikator katalogu do usuniecia z repozytorium"));
        FlaggedOption elementOption = new FlaggedOption(ARG_OBJECT, LongStringParser.getParser(), null, false, 'o',
                "object", "identyfikator obiektu usuniecia z repozytorium");
        elementOption.setAllowMultipleDeclarations(true);
        argsParser.registerParameter(elementOption);
        elementOption = new FlaggedOption(ARG_WOMI, LongStringParser.getParser(), null, false, 'w', "womi",
                "identyfikator womi do usuniecia z repozytorium");
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

        int objectCount = 0;

        if (parsedArgs.userSpecified(ARG_OBJECT)) {
            MetadataServer ms = manager.getMetadataServer();
            PublicationManager pm = ms.getPublicationManager();
            for (long pubId : parsedArgs.getLongArray(ARG_OBJECT)) {
                try {
                    PublicationId publicationId = new PublicationId(pubId);
                    if (Versioning.isBlocked(publicationId, manager.getUserServer())) {
                        logger.error("Object " + pubId + " is not the last version so it cannot be removed.");
                        continue;
                    }
                    pm.removePublication(publicationId, true, "eprt-tools");
                    objectCount++;
                } catch (Exception e) {
                    logger.error("Error while removing object " + pubId, e);
                }
            }
        }

        if (parsedArgs.userSpecified(ARG_WOMI)) {
            PublicationManager pm = manager.getMetadataServer().getPublicationManager();
            for (long ediId : parsedArgs.getLongArray(ARG_WOMI)) {
                try {
                    AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) pm
                            .getObjects(new EditionFilter(new EditionId(ediId)),
                                new OutputFilter(AbstractPublicationInfo.class))
                            .getResultInfo();
                    if (Versioning.isBlocked(pubInfo.getId(), manager.getUserServer())) {
                        logger.error("Object " + pubInfo.getId() + " is not the last version so it cannot be removed.");
                        continue;
                    }
                    pm.removePublication(pubInfo.getId(), true, "eprt-tools");
                    objectCount++;
                } catch (Exception e) {
                    logger.error("Error while removing womi " + ediId, e);
                }
            }
        }

        logger.info("Removed " + objectCount + " objects.");
        System.exit(0);
    }
}
