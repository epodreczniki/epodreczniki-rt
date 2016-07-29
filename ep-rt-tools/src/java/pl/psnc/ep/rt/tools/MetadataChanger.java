package pl.psnc.ep.rt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.ep.rt.tools.plugins.MetadataConverter;
import pl.psnc.ep.rt.tools.plugins.MetadataManager;
import pl.psnc.ep.rt.util.Versioning;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.LongStringParser;
import com.martiansoftware.jsap.stringparsers.StringStringParser;

public class MetadataChanger {

    private static final String ARG_LOGIN = "login";

    private static final String ARG_PASSWORD = "password";

    private static final String ARG_OBJECT = "object";

    private static final String ARG_WOMI = "womi";

    private static final String ARG_FILE = "metadata-file";

    private static final String ARG_CONF = "conf";

    private static final Logger logger = Logger.getLogger(MetadataChanger.class);


    public static void main(String[] args)
            throws Exception {
        SimpleJSAP argsParser = new SimpleJSAP("change-metadata", "Ustawia metadane obiektow");
        argsParser.registerParameter(new UnflaggedOption(ARG_LOGIN, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_PASSWORD, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_FILE, FileStringParser.getParser(), true, null));
        argsParser.registerParameter(new FlaggedOption(ARG_OBJECT, LongStringParser.getParser(), null, false, 'o',
                "object", "identyfikator obiektu w repozytorium"));
        argsParser.registerParameter(new FlaggedOption(ARG_WOMI, LongStringParser.getParser(), null, false, 'w', "womi",
                "identyfikator womi w repozytorium"));

        argsParser.registerParameter(new FlaggedOption(ARG_CONF, FileStringParser.getParser(), "config.properties",
                false, 'c', "config", "plik konfiguracyjny"));

        JSAPResult parsedArgs = argsParser.parse(args);
        if (argsParser.messagePrinted())
            System.exit(1);

        File metadataFile = parsedArgs.getFile(ARG_FILE);
        if (!metadataFile.isFile()) {
            logger.error("Invalid metadata file: " + metadataFile);
        }

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

        ElementId objectId = null;
        Publication publication = null;
        boolean isObject = parsedArgs.userSpecified(ARG_OBJECT);
        boolean isWOMI = parsedArgs.userSpecified(ARG_WOMI);
        MetadataServer metadataServer = manager.getMetadataServer();
        PublicationManager pubManager = metadataServer.getPublicationManager();
        if (isObject && isWOMI) {
            System.err.println("Mozna podac tylko jeden identyfikator: womi lub obiektu, nie oba");
            System.exit(-1);
        } else if (isObject) {
            PublicationId pubId = new PublicationId(parsedArgs.getLong(ARG_OBJECT));
            if (Versioning.isBlocked(pubId, manager.getUserServer())) {
                System.err.println("Obiekt jest zablokowany, istnieje juz jego kolejna wersja");
                System.exit(-1);
            }
            publication = (Publication) pubManager
                    .getObjects(new PublicationFilter(pubId), new OutputFilter(Publication.class)).getResult();
            if (publication.getMainFileId() == null) {
                objectId = pubId;
            } else {
                objectId = (ElementId) pubManager
                        .getObjects(new PublicationFilter(null, pubId), new OutputFilter(EditionId.class))
                        .getResultId();
            }
        } else if (isWOMI) {
            EditionId ediId = new EditionId(parsedArgs.getLong(ARG_WOMI));
            objectId = ediId;
            if (Versioning.isBlocked(ediId, pubManager, manager.getUserServer())) {
                System.err.println("Obiekt jest zablokowany, istnieje juz jego kolejna wersja");
                System.exit(-1);
            }
            publication = (Publication) pubManager
                    .getObjects(new EditionFilter(ediId), new OutputFilter(Publication.class)).getResult();
        }
        if (objectId == null) {
            System.err.println("Nie znaleziono identyfikatora: womi (-w) lub obiektu (-o)");
            System.exit(-1);
        }

        List<AttributeValueSet> avss = MetadataManager.getInstance().findMetadata(metadataFile, false, metadataServer);
        if (avss == null || avss.isEmpty()) {
            System.err.println("Blad wczytywania metadanych z pliku " + metadataFile);
            System.exit(-2);
        }
        if (avss.size() > 1)
            System.err.println("Znaleziono " + avss.size() + " zestawow metadanych, wykorzystany zostanie pierwszy.");
        AttributeValueSet avs = avss.get(0);
        avs.setElementId(objectId);

        AttributeValueSet oldAVS = metadataServer.getElementMetadataManager().getAttributeValueSet(objectId,
            AttributeValue.AV_ASSOC_DIRECT);
        String editorModeError = Versioning.checkEditorMode(avs, oldAVS, publication, null, null, true, metadataServer);
        if (editorModeError != null) {
            System.err.println("Nieporawna wartosc dla Trybu edytora: " + editorModeError);
            System.exit(-1);
        }

        avs = MetadataConverter.updateAVS(avs, true, metadataServer);
        metadataServer.getElementMetadataManager().setAttributeValueSet(avs);

        System.out.println("OK!");
        System.exit(0);
    }
}
