package pl.psnc.ep.rt.tools.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.app.common.files.EditionFile;
import pl.psnc.dlibra.app.common.files.EditionFiles;
import pl.psnc.dlibra.app.common.metadata.MetadataConverter;
import pl.psnc.dlibra.app.gui.base.AmbiguousAttributeValueException;
import pl.psnc.dlibra.app.gui.common.propertizer.EditionPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.ImagePropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.LibCollectionsPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.OperationRightsPropertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.Propertizer;
import pl.psnc.dlibra.app.gui.common.propertizer.PublicationPropertizer;
import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.Element;
import pl.psnc.dlibra.metadata.ElementImage;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationImage;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet.Values;
import pl.psnc.dlibra.metadata.attributes.UTF8BOMIndependentFileInputStream;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationException;
import pl.psnc.dlibra.multipubuploader.transformer.TransformationProblem;
import pl.psnc.dlibra.multipubuploader.wizard.MultiPublicationUploadSelectPage;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.RightOperation;
import pl.psnc.util.EncodableProperties;

public abstract class AbstractPropertiesToPublicationTransformer implements Cloneable {

    protected static final String PUBLICATION_MAINFILE = "publication.mainFile";

    protected static final String PUBLICATION_METADATAFILE = "publication.metadataFile";

    protected static final String PUBLICATION_PUBLISHED = "publication.published";

    private static final String PROPERTIES_ENCODING = "UTF8";

    private static final int MAX_VALUE_LENGTH = 16000;

    protected File propsFile;

    protected EncodableProperties properties;

    private List<EditionFile> publicationFiles;

    private Set<LibCollectionId> collections;

    protected EditionFile mainPublicationFile;

    private String metadataFilename;

    private Publication publication;

    private static Logger logger = Logger.getLogger(AbstractPropertiesToPublicationTransformer.class.getName());

    protected List<AbstractPropertiesToPublicationTransformer> childTransformers;

    protected final List<TransformationProblem> transformationProblems = new ArrayList<TransformationProblem>();

    protected AbstractPropertiesToPublicationTransformer parentTransformer;

    private boolean published;

    private Map<String, ArrayList<RightOperation>> actorsToOperations;

    private AttributeValueSet avs;

    private boolean isGroupPublication;

    private Edition edition;

    protected MetadataServer metadataServer;

    protected boolean checkContentFiles;


    public AbstractPropertiesToPublicationTransformer(File propsFile, MetadataServer ms) {
        this.propsFile = propsFile;
        this.metadataServer = ms;
    }


    public void transform(boolean transformChildren)
            throws TransformationException {
        properties = parentTransformer != null ? parentTransformer.getPropertiesToInherit() : new EncodableProperties();
        try {
            load(properties, propsFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + propsFile.getAbsolutePath());
        } catch (IOException e) {
            throw new TransformationException("Could not read properties file" + propsFile.getAbsolutePath());
        }
        extractInformation(properties);

        avs = loadAVS(properties);
        if (publication.getName() == null) {
            evaluatePublicationName();
        }

        checkConstraints(propsFile == null);

        if (transformChildren && isGroupPublication) {
            transformChildPublications();
        }
    }


    protected abstract void transformChildPublications();


    protected abstract EncodableProperties getPropertiesToInherit();


    protected AbstractPropertiesToPublicationTransformer createChildTransformer(File propsFile2) {
        AbstractPropertiesToPublicationTransformer childTransformer;
        try {
            childTransformer = (AbstractPropertiesToPublicationTransformer) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported: " + this.getClass());
        }
        childTransformer.propsFile = propsFile2;
        childTransformer.parentTransformer = this;
        childTransformer.childTransformers = null;
        return childTransformer;
    }


    private void checkConstraints(boolean quiet) {
        if (publication.getName() != null && publication.getName().length() > Element.MAX_NAME_LENGTH) {
            System.err.println("Publication name is too long. It will be cut to fit maximum name length which is "
                    + Element.MAX_NAME_LENGTH + ".");
            publication.setName(publication.getName().substring(0, Element.MAX_NAME_LENGTH));
        }
        if (publication.getNotes() != null && publication.getNotes().length() > Element.MAX_NOTES_LENGTH) {
            System.err.println(
                "Publication notes are too long. The notes will be cut to fit maximum notes length which is "
                        + Element.MAX_NOTES_LENGTH + ".");
            publication.setNotes(publication.getNotes().substring(0, Element.MAX_NOTES_LENGTH));
        }
        if (avs != null) {
            boolean tooLong = false;
            for (AttributeId id : avs.getAttributeIds()) {
                for (AbstractAttributeValue value : avs.getAttributeValues(id)) {
                    if (value.getValue().length() > MAX_VALUE_LENGTH) {
                        System.err.println(
                            "Some attribute values are too long. They will be cut to fit maximum length which is "
                                    + MAX_VALUE_LENGTH + ".");
                        tooLong = true;
                        break;
                    }
                }
                if (tooLong) {
                    break;
                }
            }
            if (tooLong) {
                for (AttributeId id : avs.getAttributeIds()) {
                    for (String language : avs.getAvailableLanguages(true)) {
                        ArrayList<AbstractAttributeValue> newValues = new ArrayList<AbstractAttributeValue>();
                        constraintValues(id, language, newValues, Values.OnlyDirect);
                        constraintValues(id, language, newValues, Values.OnlyInherited);
                    }
                }
            }
        }
    }


    private void constraintValues(AttributeId id, String language, ArrayList<AbstractAttributeValue> newValues,
            Values valuesType) {
        for (AbstractAttributeValue value : avs.getAttributeValues(id, language, valuesType)) {
            if (value.getValue().length() > MAX_VALUE_LENGTH) {
                value.setValue(value.getValue().substring(0, MAX_VALUE_LENGTH));
            }
            newValues.add(value);
        }
        if (Values.OnlyDirect.equals(valuesType)) {
            avs.setDirectAttributeValues(id, language, newValues);
        } else if (Values.OnlyInherited.equals(valuesType)) {
            avs.setInheritedAttributeValues(id, language, newValues);
        }
    }


    protected abstract AttributeValueSet loadAVS(EncodableProperties props)
            throws TransformationException;


    private void evaluatePublicationName()
            throws TransformationException {
        if (avs == null) {
            throw new TransformationException("Publication name has to be specified.");
        }
        String name = null;
        if ((name = getDefaultPublicationName(avs)).length() == 0) {
            throw new TransformationException("Publication name has to be specified.");
        }
        publication.setName(name);
    }


    private String getDefaultPublicationName(AttributeValueSet attrValueSet) {
        String publicationName = "";
        try {
            String template = DLToolkit.DEFAUL_TITLE_TEMPLATE;

            VelocityEngine ve = new VelocityEngine();
            ve.setProperty("runtime.log", DLToolkit.getVelocityLogFile());
            ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, DLToolkit.class.getName());
            ve.setProperty("runtime.log.logsystem.log4j.category", logger.getName());
            ve.init();
            VelocityContext vc = new VelocityContext();

            AttributeManager attributeManager = metadataServer.getAttributeManager();
            Collection<Info> attributesInfos = attributeManager
                    .getObjects((AttributeFilter) new AttributeFilter((AttributeId) null).setRecursive(true),
                        new OutputFilter(AttributeInfo.class))
                    .getResultInfos();

            for (Info i : attributesInfos) {
                AttributeInfo ai = (AttributeInfo) i;
                List<AbstractAttributeValue> values = attrValueSet.getAttributeValues(ai.getId(), "pl", Values.All);
                if (values != null && values.size() > 0) {
                    vc.put(ai.getRDFName(), values.iterator().next().toString());
                }
            }

            StringWriter pubName = new StringWriter();
            ve.evaluate(vc, pubName, "DLToolkit.getDefaultPublicationName", template);
            publicationName = pubName.toString();

            if (publicationName.length() > Element.MAX_NAME_LENGTH) {
                publicationName = publicationName.substring(0, Element.MAX_NAME_LENGTH);
            }
        } catch (NullPointerException e) {
        } catch (IdNotFoundException e) {
            System.err.println("Identifier not found\n" + e.getMessage());
        } catch (Exception e) {
            System.err.println("Other problem when preparing publication name.\n" + e.getMessage());
        }
        return publicationName;
    }


    protected void load(EncodableProperties props, File propertiesFile)
            throws FileNotFoundException, IOException {
        ArrayList<File> propsFilesList = new ArrayList<File>();
        if (propertiesFile.exists()) {
            propsFilesList.add(propertiesFile);
        }

        final boolean lookupParent = parentTransformer == null;
        if (lookupParent) {
            File parentFolder = propertiesFile.getParentFile();
            if (parentFolder != null) {
                parentFolder = parentFolder.getParentFile();
            }
            if (parentFolder != null) {
                String candidate = parentFolder.getAbsolutePath() + File.separator
                        + MultiPublicationUploadSelectPage.PUBLICATION_PROPERTIES_FILENAME;
                File currentFile = new File(candidate);
                if (currentFile.exists()) {
                    propsFilesList.add(currentFile);
                }
            }
        }

        Collections.reverse(propsFilesList);
        for (File file : propsFilesList) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new UTF8BOMIndependentFileInputStream(file);
                props.load(fileInputStream, PROPERTIES_ENCODING);
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
        }
    }


    private void extractInformation(EncodableProperties props)
            throws TransformationException {
        extractPublished(props);
        final PublicationPropertizer publicationPropertizer = new PublicationPropertizer();
        if (parentTransformer != null) {
            publicationPropertizer.setDefaultParentDirId((DirectoryId) parentTransformer.publication.getParentId());
        }
        publicationPropertizer.setMetadataServer(metadataServer);
        publication = publicationPropertizer.load(props);
        copyProblems(publicationPropertizer);

        OperationRightsPropertizer rightsPropertizer = new OperationRightsPropertizer();
        actorsToOperations = rightsPropertizer.load(props);
        copyProblems(rightsPropertizer);

        LibCollectionsPropertizer collectionsPropertizer = new LibCollectionsPropertizer();
        collections = collectionsPropertizer.load(props);
        copyProblems(collectionsPropertizer);

        final boolean belongsToGroup = parentTransformer != null || publication.getParentPublicationId() != null;

        boolean hasContent = extractFiles(props);
        if (!hasContent) {
            if (!isPlanned()) {
                isGroupPublication = true;
                ImagePropertizer imagePropertizer = new ImagePropertizer(propsFile.getParentFile(),
                        PublicationImage.class);
                ElementImage image = imagePropertizer.load(props);
                copyProblems(imagePropertizer);
                if (image != null) {
                    publication.setImage(image);
                }
                publication.setGroupStatus(belongsToGroup ? Publication.PUB_GROUP_MID : Publication.PUB_GROUP_ROOT);
            } else {
                publication.setState(Publication.PUB_STATE_PLANNED);
                publication.setGroupStatus(belongsToGroup ? Publication.PUB_GROUP_LEAF : Publication.PUB_NORMAL);
            }
        } else {
            try {
                EditionPropertizer editionPropertizer = new EditionPropertizer(
                        propsFile == null ? null : propsFile.getParentFile());
                edition = editionPropertizer.load(props);
                copyProblems(editionPropertizer);
            } catch (Exception e) {
                throw new TransformationException("Error occurred while creating edition. Exception:" + e.getMessage());
            }
            publication.setGroupStatus(belongsToGroup ? Publication.PUB_GROUP_LEAF : Publication.PUB_NORMAL);
        }
        extractMetadataFileName(props);
    }


    protected abstract boolean isPlanned();


    private void copyProblems(Propertizer<?> propertizer) {
        for (TransformationProblem problem : propertizer.getTransformationProblems()) {
            problem.setPropsFile(propsFile);
            transformationProblems.add(problem);
        }
    }


    private void extractMetadataFileName(EncodableProperties props) {
        String metadataFile = props.getProperty(PUBLICATION_METADATAFILE);
        if (metadataFile != null) {
            this.metadataFilename = metadataFile;
        }
    }


    protected abstract EditionFiles getEditionFiles(Properties p, MetadataServer metadataServer)
            throws TransformationException;


    private boolean extractFiles(EncodableProperties props)
            throws TransformationException {
        EditionFiles files = getEditionFiles(props, metadataServer);
        if (files == null) {
            return false;
        }

        this.mainPublicationFile = files.getMainFile();

        publicationFiles = files.getFiles();

        ListIterator<EditionFile> lit = publicationFiles.listIterator();
        while (lit.hasNext()) {
            EditionFile eF = lit.next();
            if (MultiPublicationUploadSelectPage.PUBLICATION_PROPERTIES_FILENAME.equals(eF.getSourceFile().getName())
                    || eF.isMissing()) {
                lit.remove();
            }
        }
        return true;
    }


    private void extractPublished(EncodableProperties props) {
        this.published = false;
        String publish = props.getProperty(PUBLICATION_PUBLISHED);
        if (publish != null) {
            Boolean publishedBool = Boolean.valueOf(publish);
            this.published = publishedBool.booleanValue();
        }
    }


    public Publication getPublication() {
        return publication;
    }


    public Map<String, ArrayList<RightOperation>> getActorsToOperations() {
        return actorsToOperations;
    }


    public Set<LibCollectionId> getCollections() {
        return collections;
    }


    public List<EditionFile> getPublicationFiles() {
        return publicationFiles;
    }


    public AbstractPropertiesToPublicationTransformer getParentTransformer() {
        return parentTransformer;
    }


    public List<TransformationProblem> getTransformationProblems() {
        return transformationProblems;
    }


    public List<AbstractPropertiesToPublicationTransformer> getChildTransformers() {
        return childTransformers;
    }


    public EditionFile getMainPublicationFile() {
        return mainPublicationFile;
    }


    public String getMetadataFilename() {
        return metadataFilename;
    }


    public File getPropsFile() {
        return propsFile;
    }


    public AttributeValueSet getAvs() {
        return avs;
    }


    @Override
    public String toString() {
        return propsFile.getParentFile().getName() + ": " + publication.getName();
    }


    public boolean isPublished() {
        return published;
    }


    public Edition getEdition() {
        return edition;
    }


    public void updateAVS()
            throws RemoteException, IdNotFoundException, DuplicatedValueException, AccessDeniedException,
            AmbiguousAttributeValueException, DLibraException {
        MetadataConverter.updateAVS(getAvs(), false);
        if (childTransformers != null) {
            for (AbstractPropertiesToPublicationTransformer childTransformer : childTransformers) {
                childTransformer.updateAVS();
            }
        }
    }


    public void setMetadataServer(MetadataServer metadataServer) {
        this.metadataServer = metadataServer;
    }


    public void setCheckContentFiles(boolean checkContentFiles) {
        this.checkContentFiles = checkContentFiles;
    }
}
