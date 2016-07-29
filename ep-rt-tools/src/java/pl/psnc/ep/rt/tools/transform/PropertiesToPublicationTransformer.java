package pl.psnc.ep.rt.tools.transform;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;

import pl.psnc.dlibra.app.common.files.EditionFiles;
import pl.psnc.dlibra.app.gui.common.propertizer.EditionPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.HashMapRightsPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.ImagePropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.LibCollectionsPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.PublicationPropertizer;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationException;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationProblem;
import pl.psnc.util.EncodableProperties;

public class PropertiesToPublicationTransformer extends AbstractPropertiesToPublicationTransformer {

    private final static Collection<String> INHERITED_PROPERTIES = Collections
            .unmodifiableList(Arrays.asList(new String[] { PublicationPropertizer.PUBLICATION_NAME,
                    PublicationPropertizer.PUBLICATION_NOTES, PublicationPropertizer.PUBLICATION_SECURED,
                    EditionPropertizer.PUBLICATION_PUBLISHED, PUBLICATION_METADATAFILE, PUBLICATION_MAINFILE,
                    ImagePropertizer.IMAGE_CONTENT, ImagePropertizer.IMAGE_TYPE, }));

    private final static Collection<String> RECOGNIZED_PROPERTIES;


    static {
        HashSet<String> recognizedProperties = new HashSet<String>(INHERITED_PROPERTIES);
        recognizedProperties
                .addAll(Arrays.asList(new String[] { PublicationPropertizer.PUBLICATION_DESTINATION_DIRECTORYID,
                        PublicationPropertizer.PUBLICATION_DESTINATION_PARENTPUBLICATIONID,
                        PublicationPropertizer.PUBLICATION_PUBLISHING_DATE, PublicationPropertizer.PUBLICATION_ID,
                        EditionPropertizer.EDITION_ID, EditionPropertizer.EDITION_EXTERNAL_ID,
                        LibCollectionsPropertizer.PUBLICATION_COLLECTIONS, ImagePropertizer.EDITION_IMAGE_CONTENT,
                        ImagePropertizer.EDITION_IMAGE_TYPE, }));
        RECOGNIZED_PROPERTIES = Collections.unmodifiableSet(recognizedProperties);
    }


    private final static Collection<String> RECOGNIZED_PROPERTY_PREFIXES = Collections
            .unmodifiableList(Arrays.asList(new String[] { HashMapRightsPropertizer.PUBLICATION_ACTORSRIGHTS_ }));


    @Override
    public void transform(boolean transformChildren)
            throws TransformationException {
        super.transform(transformChildren);
        reportUnrecognizedProperties();
    }


    private void reportUnrecognizedProperties() {
        mainLoop: for (Object property : properties.keySet()) {
            if (RECOGNIZED_PROPERTIES.contains(property)) {
                continue mainLoop;
            }
            for (String prefix : RECOGNIZED_PROPERTY_PREFIXES) {
                if (((String) property).startsWith(prefix)) {
                    continue mainLoop;
                }
            }

            transformationProblems
                    .add(new TransformationProblem(false, "Unrecognized property: " + property, propsFile));
        }
    }


    public PropertiesToPublicationTransformer(File propsFile, MetadataServer ms) {
        super(propsFile, ms);
    }


    @Override
    protected EditionFiles getEditionFiles(Properties p, MetadataServer ms)
            throws TransformationException {
        String mainFile = p.getProperty(PUBLICATION_MAINFILE);
        if (mainFile == null || "".equals(mainFile)) {
            return null;
        }

        File main = new File(propsFile.getParent(), mainFile);
        if (checkContentFiles && (!main.exists() || !main.isFile())) {
            throw new TransformationException("Specified main file does not exist: " + main.getPath());
        }

        EditionFilesFactory sff = EditionFilesFactory.getInstance();
        sff.setMetadataServer(ms);

        try {
            return sff.createFiles(main.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("can't convert file to url", e);
        }
    }


    @Override
    protected AttributeValueSet loadAVS(EncodableProperties props)
            throws TransformationException {
        return new AVSPropertizer(propsFile.getParentFile(), null, parentTransformer == null, metadataServer)
                .load(props);
    }


    @Override
    protected void transformChildPublications() {
        File parent = propsFile.getParentFile();
        if (!parent.exists()) {
            return;
        }
        childTransformers = new ArrayList<AbstractPropertiesToPublicationTransformer>();
        File[] childFiles = parent.listFiles();
        Arrays.sort(childFiles, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (File child : childFiles) {
            if (child.isDirectory()) {
                File childPropsFile = new File(child, propsFile.getName());
                if (!childPropsFile.exists()) {
                    continue;
                }
                AbstractPropertiesToPublicationTransformer childTransformer = createChildTransformer(childPropsFile);
                try {
                    childTransformer.transform(true);
                    childTransformers.add(childTransformer);
                } catch (TransformationException e) {
                    e.setPropsFile(childPropsFile);
                    transformationProblems.add(new TransformationProblem(e));
                }
            }
        }
    }


    @Override
    protected EncodableProperties getPropertiesToInherit() {
        EncodableProperties propertiesToInherit = new EncodableProperties();
        for (String property : INHERITED_PROPERTIES) {
            if (properties.get(property) != null) {
                propertiesToInherit.put(property, properties.get(property));
            }
        }
        return propertiesToInherit;
    }


    @Override
    protected boolean isPlanned() {
        File directory = this.propsFile.getParentFile();
        File[] files = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        return files.length == 0;
    }
}
