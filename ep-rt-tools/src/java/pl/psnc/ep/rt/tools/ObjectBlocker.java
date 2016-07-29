package pl.psnc.ep.rt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.user.UserServer;
import pl.psnc.ep.rt.util.Versioning;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.LongStringParser;
import com.martiansoftware.jsap.stringparsers.StringStringParser;

public class ObjectBlocker {

    private static final String ARG_LOGIN = "login";

    private static final String ARG_PASSWORD = "password";

    private static final String ARG_WOMI = "womi";
    private static final String ARG_MODULE = "module";
    private static final String ARG_COLLECTION = "collection";

    private static ServerManager manager;


    public static void main(String[] args)
            throws Exception {
        SimpleJSAP argsParser = new SimpleJSAP("object-blocker", "Ustawia obiekty jako zablokowane (zamrozone)");
        argsParser.registerParameter(new UnflaggedOption(ARG_LOGIN, StringStringParser.getParser(), true, null));
        argsParser.registerParameter(new UnflaggedOption(ARG_PASSWORD, StringStringParser.getParser(), true, null));
        FlaggedOption objectOption = new FlaggedOption(ARG_WOMI, LongStringParser.getParser(), null, false, 'w',
                "id WOMI", "identyfikator WOMI w repozytorium");
        objectOption.setAllowMultipleDeclarations(true);
        argsParser.registerParameter(objectOption);
        objectOption = new FlaggedOption(ARG_MODULE, StringStringParser.getParser(), null, false, 'm', "id modulu",
                "identyfikator modulu w repozytorium");
        objectOption.setAllowMultipleDeclarations(true);
        argsParser.registerParameter(objectOption);
        objectOption = new FlaggedOption(ARG_COLLECTION, StringStringParser.getParser(), null, false, 'c',
                "id kolekcji", "identyfikator kolekcji w repozytorium");
        objectOption.setAllowMultipleDeclarations(true);
        argsParser.registerParameter(objectOption);

        JSAPResult parsedArgs = argsParser.parse(args);
        if (argsParser.messagePrinted())
            System.exit(1);

        Properties serverConfig = new Properties();
        FileInputStream configIS = new FileInputStream(new File("config.properties"));
        try {
            serverConfig.load(configIS);
        } finally {
            configIS.close();
        }
        serverConfig.put(ServerManager.LOGIN, parsedArgs.getString(ARG_LOGIN));
        serverConfig.put(ServerManager.PASSWORD, parsedArgs.getString(ARG_PASSWORD));
        manager = new ServerManager(serverConfig);
        DLToolkit.setIOConf(serverConfig);

        PublicationManager pm = manager.getMetadataServer().getPublicationManager();
        UserServer us = manager.getUserServer();
        for (long womiId : parsedArgs.getLongArray(ARG_WOMI)) {
            Versioning.block(new EditionId(womiId), pm, us);
        }
        for (String moduleId : parsedArgs.getStringArray(ARG_MODULE)) {
            EditionId editionId = (EditionId) pm
                    .getObjects(new EditionFilter().setExternalId(moduleId), new OutputFilter(EditionId.class))
                    .getResultId();
            Versioning.block(editionId, pm, us);
        }
        for (String collection : parsedArgs.getStringArray(ARG_COLLECTION)) {
            Matcher matcher = Pattern.compile("([0-9]+)/([0-9]+)").matcher(collection);
            if (matcher.matches()) {
                long id = Long.valueOf(matcher.group(1));
                int version = Integer.valueOf(matcher.group(2));
                PublicationId pubId = findVersion(new PublicationId(id), version);
                if (pubId != null) {
                    Versioning.block(pubId, pm, us);
                } else {
                    System.out.println("No such collection: " + id + "/" + version);
                }
            } else {
                Versioning.block(new PublicationId(Long.valueOf(collection)), pm, us);
            }
        }
        System.out.println("OK");
        System.exit(0);
    }


    public static PublicationId findVersion(PublicationId rootId, int version)
            throws RemoteException, DLibraException {
        if (version < 1)
            return null;

        MetadataServer ms = manager.getMetadataServer();
        AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(rootId,
            AttributeValue.AV_ASSOC_DIRECT);
        AttributeId rootAttributeId = Versioning.getAttributeId(Versioning.ROOT_ID_RDF_NAME, ms);
        List<AbstractAttributeValue> rootRootValues = avs.getAttributeValues(rootAttributeId, "pl",
            AttributeValueSet.Values.OnlyDirect);
        if (!rootRootValues.isEmpty())
            return null;

        if (version == 1)
            return rootId;

        AttributeValueManager avm = ms.getAttributeValueManager();
        AttributeValueId rootIdAttVal = (AttributeValueId) avm
                .getObjects(new AttributeValueFilter(rootAttributeId).setValue(rootId.toString(), true),
                    new OutputFilter(AttributeValueId.class))
                .getResultId();
        if (rootIdAttVal == null)
            return null;

        List<Publication> allVersions = Versioning.findAllVersions(rootIdAttVal, ms);
        if (allVersions.size() > version - 2)
            return allVersions.get(version - 2).getId();

        return null;
    }
}
