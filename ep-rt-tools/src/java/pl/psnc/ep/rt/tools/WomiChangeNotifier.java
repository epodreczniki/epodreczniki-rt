package pl.psnc.ep.rt.tools;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.LongStringParser;
import com.martiansoftware.jsap.stringparsers.StringStringParser;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.ep.rt.server.EPService;

public class WomiChangeNotifier {

    private static final String ARG_LOGIN = "login";
    private static final String ARG_PASSWORD = "password";
    private static final String ARG_CONF = "conf";
    private static final String ARG_ID = "id";

    private static final Logger logger = Logger.getLogger(WomiChangeNotifier.class);

    public static void main(String[] args) throws Exception {
        SimpleJSAP argsParser = new SimpleJSAP("notify-womi", "Wymusza przetworzenie WOMI jak po wprowadzeniu zmiany");
        argsParser.registerParameter(new UnflaggedOption(ARG_LOGIN, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_PASSWORD, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new FlaggedOption(ARG_CONF, FileStringParser.getParser(), "config.properties",
                false, 'c', "config", "plik konfiguracyjny"));
        UnflaggedOption idOption = new UnflaggedOption(ARG_ID, LongStringParser.getParser(), null, true, false,
                "identyfikatory WOMI, lista rozdzielana przecinkami");
        idOption.setList(true);
        idOption.setListSeparator(',');
        argsParser.registerParameter(idOption);

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
        ServerManager manager = new ServerManager(serverConfig);
        DLToolkit.setIOConf(serverConfig);

        long[] ids = parsedArgs.getLongArray(ARG_ID);
        EPService ep = manager.getEPService();
        for (long id : ids) {
            ep.reportWOMIModified(new EditionId(id));
        }
        logger.info("Udalo sie zglosic zmiany w " + ids.length + " obiektach");

        System.exit(0);
    }
}
