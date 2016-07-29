package pl.psnc.ep.rt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.ep.rt.ds.gui.WOMIDefinitionPanel;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class Util {

    public static final LibCollectionId COLLXML_COLLECTION = new LibCollectionId(21L);


    public static Map<WOMIFormat, String> findFileNames(EditionId editionId)
            throws DLibraException, IOException {
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        VersionId mainVersionId = (VersionId) ms.getPublicationManager()
                .getObjects(new EditionFilter(editionId).setMainVersion(true), new OutputFilter(VersionId.class))
                .getResultId();
        ContentServer cs = (ContentServer) ApplicationContext.getInstance().getUserServiceResolver()
                .getService(ContentServer.SERVICE_TYPE, null);
        InputStream mainVersionStream = cs.getVersionInputStream(mainVersionId);
        Map<WOMIFormat, String> formats;
        try {
            formats = WOMIXMLHandler.loadMultiFormatXML(mainVersionStream);
        } finally {
            mainVersionStream.close();
            cs.releaseElement(mainVersionId);
        }

        try {
            Collection<Id> alternative = ms.getFileManager()
                    .getObjects(new FileFilter(WOMIDefinitionPanel.STATIC_ALTERNATIVE_PATH).setEditionId(editionId),
                        new OutputFilter(VersionId.class))
                    .getResultIds();
            if (alternative != null && !alternative.isEmpty()) {
                formats.put(WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT, WOMIDefinitionPanel.STATIC_ALTERNATIVE_PATH);
            }
        } catch (IdNotFoundException e) {
        }
        return formats;
    }

}
