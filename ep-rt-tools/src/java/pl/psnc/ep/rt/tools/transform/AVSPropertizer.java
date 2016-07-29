package pl.psnc.ep.rt.tools.transform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import pl.psnc.dlibra.app.common.metadata.MetadataConverter;
import pl.psnc.dlibra.app.gui.common.propertizer.Propertizer;
import pl.psnc.dlibra.app.gui.editor.publicationbrowser.PublicationBrowser;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.multipubuploader.transformer.ExistingPublicationToPropertiesTransformer;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationException;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationProblem;
import pl.psnc.ep.rt.tools.plugins.MetadataManager;

public class AVSPropertizer implements Propertizer<AttributeValueSet> {

    private final File parentFolder;

    private final File metadataFile;

    private final boolean lookupParent;

    public static final String DEFAULT_METADATA_FILE = "publication.rdf";

    private MetadataServer metadataServer;


    public AVSPropertizer(File metadataFile, MetadataServer ms) {
        this(null, metadataFile, false, ms);
    }


    public AVSPropertizer(File parentFolder, File metadataFile, boolean lookupParent, MetadataServer ms) {
        this.parentFolder = parentFolder;
        this.metadataFile = metadataFile;
        this.lookupParent = lookupParent;
        this.metadataServer = ms;
    }


    public AttributeValueSet load(Properties p)
            throws TransformationException {
        String metadataFileName = p.getProperty(ExistingPublicationToPropertiesTransformer.PUBLICATION_METADATAFILE);
        boolean ignoreExtension = false;
        if (metadataFileName == null || metadataFileName.isEmpty()) {
            metadataFileName = p.getProperty(ExistingPublicationToPropertiesTransformer.PUBLICATION_MAINFILE);
            ignoreExtension = true;
        }

        AttributeValueSet avs = null;
        if (metadataFileName != null) {
            avs = readMetadata(avs, parentFolder, metadataFileName, ignoreExtension, metadataServer);
            checkNotNull(avs);
        }
        if (lookupParent) {
            avs = readMetadata(avs, parentFolder.getParentFile(), DEFAULT_METADATA_FILE, false, metadataServer);
        }
        if (avs == null) {
            avs = new AttributeValueSet();
        }

        return avs;
    }


    private AttributeValueSet readMetadata(AttributeValueSet avs, File parent, String metadataFileName,
            boolean ignoreExtension, MetadataServer ms) {
        File metadatafile = new File(parent, metadataFileName);
        try {
            List<AttributeValueSet> avss = MetadataManager.getInstance().findMetadata(metadatafile, ignoreExtension,
                metadataServer);
            if (avs == null) {
                avs = avss.get(0);
            } else {
                avss.get(0).addAll(avs);
                avs = avss.get(0);
            }
        } catch (Exception e) {
            return avs;
        }
        return avs;
    }


    private void checkNotNull(Object o)
            throws TransformationException {
        if (o == null) {
            throw new TransformationException("Could not load metadata file.");
        }
    }


    public Properties propertize(AttributeValueSet object) {
        Properties p = new Properties();

        if (object == null || metadataFile == null) {
            return p;
        }

        try {
            List<AttributeValueSet> avss = new ArrayList<AttributeValueSet>();
            avss.add(object);

            MetadataManager.getInstance().getStoreRDFMetadataFinder().storeMetadata(PublicationBrowser.getInstance(),
                null, MetadataConverter.convertToMap(avss), metadataFile);

            p.setProperty(ExistingPublicationToPropertiesTransformer.PUBLICATION_METADATAFILE, metadataFile.getName());
        } catch (Exception e) {
        }

        return p;
    }


    @Override
    public List<TransformationProblem> getTransformationProblems() {
        return new ArrayList<TransformationProblem>();
    }

}
