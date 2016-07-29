package pl.psnc.ep.rt.validation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.extension.datasource.FileInfo;
import pl.psnc.dlibra.app.extension.validator.PublicationData;
import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.EditionInfo;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.mgmt.AbstractServiceResolver;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class ModuleValidator extends InternalValidator {

    private static final String SCHEMA_PATH = "../../pl.psnc.ep.rt.womi/metadane/epxml.rng";

    private static final String MODULE_FILE_SUFFIX = ".xml";

    private static final String[] MODULE_FILE_EXCLUDES = { "/" + WOMIXMLHandler.MAIN_FILE_NAME.toLowerCase() };

    private static final int MAX_FILE_SZIE = 50 * 1024 * 1024;

    private final static String REFERENCE_TAG = "ep:reference";

    private final static String ID_TTRIBUTE = "ep:id";

    private final URL schemaLocation;

    private Schema schema;


    public ModuleValidator(ResourceBundle textsBundle, String configDir) {
        super(textsBundle);
        try {
            schemaLocation = new java.io.File(configDir, SCHEMA_PATH).getCanonicalFile().toURI().toURL();
        } catch (IOException e) {
            throw new RuntimeException("Could not determine schema definition location: " + configDir + SCHEMA_PATH);
        }
    }


    @Override
    public boolean recognize(PublicationData publicationData, AbstractServiceResolver serviceResolver) {
        List<FileInfo> files = publicationData.getFiles();
        if (files.isEmpty())
            return false;

        FileInfo mainFile = files.get(0);
        String path = mainFile.getDLibraPath().toLowerCase();
        if (!path.endsWith(MODULE_FILE_SUFFIX))
            return false;
        for (String exclude : MODULE_FILE_EXCLUDES) {
            if (path.equalsIgnoreCase(exclude))
                return false;
        }
        return true;
    }


    @Override
    public List<ValidationResult> validate(PublicationData publicationData)
            throws IOException, DLibraException {
        ArrayList<ValidationResult> results = new ArrayList<ValidationResult>();

        PublicationId publicationId = publicationData.getPublication().getId();
        boolean isUpdating = publicationId != null && publicationId.getId() != 0;
        if (isUpdating) {
            if (Versioning.isBlocked(publicationId, ApplicationContext.getInstance().getEventUserServer()))
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        textsBundle.getString("validation.error.bookBlocked"), null));
            return results;
        }

        checkRequiredAttributes(publicationData.getAttributeValueSet(), MODULE_LIMITATIONS, results);
        if (true)
            return results;

        FileInfo mainFile = publicationData.getFiles().get(0);
        String mainFileContent = getContent(mainFile.getURL());
        boolean isSchemaOK = validateSchema(mainFileContent, results);
        if (isSchemaOK)
            validateReferences(new ByteArrayInputStream(mainFileContent.getBytes("UTF-8")), results);
        return results;
    }


    private String getContent(URL url)
            throws IOException {
        URLConnection connection = url.openConnection();
        connection.connect();
        int contentLength = connection.getContentLength();
        if (contentLength > MAX_FILE_SZIE)
            throw new IOException("File too big (" + contentLength + " bytes): " + url);
        if (contentLength < 0)
            throw new IOException("File not available: " + url);

        byte[] content = new byte[contentLength];
        InputStream stream = connection.getInputStream();
        try {
            int bytesRead = 0;
            while (bytesRead < contentLength) {
                bytesRead += stream.read(content, bytesRead, contentLength - bytesRead);
            }
        } finally {
            stream.close();
        }
        String contentEncoding = connection.getContentEncoding();
        if (contentEncoding == null)
            contentEncoding = "UTF-8";
        return new String(content, Charset.forName(contentEncoding));
    }


    private boolean validateSchema(String content, List<ValidationResult> results)
            throws IOException {
        try {
            Source source = new StreamSource(new StringReader(content));
            Validator validator = getSchema().newValidator();
            validator.validate(source);
            return true;
        } catch (SAXException e) {
            results.add(new ValidationResult(ValidationResultType.ERROR,
                    textsBundle.getString("validation.error.schema"), e.getMessage()));
            return false;
        }
    }


    private Schema getSchema() {
        if (schema != null)
            return schema;
        try {
            System.setProperty(SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI,
                "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory");
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
            schema = factory.newSchema(schemaLocation);
            return schema;
        } catch (SAXException e) {
            throw new RuntimeException("Invalid schema resource: " + schemaLocation, e);
        }
    }


    private void validateReferences(InputStream contentStream, List<ValidationResult> results)
            throws RemoteException, DLibraException {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(contentStream);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error on xml parsing", e);
        }
        NodeList list = document.getElementsByTagName(REFERENCE_TAG);
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        PublicationManager publicationManager = ms.getPublicationManager();
        FileManager fileManager = ms.getFileManager();

        for (int i = 0; i < list.getLength(); i++) {
            NamedNodeMap attributes = list.item(i).getAttributes();

            String idGroup = attributes.getNamedItem(ID_TTRIBUTE).getTextContent();
            long id;
            try {
                id = Long.valueOf(idGroup);
            } catch (NumberFormatException e) {
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        textsBundle.getString("validation.error.reference.numberFormat.message") + idGroup,
                        textsBundle.getString("validation.error.reference.numberFormat.description")));
                continue;
            }
            EditionId editionId = new EditionId(id);
            EditionInfo editionInfo = (EditionInfo) publicationManager
                    .getObjects(new EditionFilter(editionId), new OutputFilter(EditionInfo.class)).getResultInfo();
            if (editionInfo == null || editionInfo.getState() != Edition.PUBLISHED) {
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        textsBundle.getString("validation.error.reference.missing.message") + idGroup, null));
                continue;
            }
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<LibCollectionId> collectionIds = (List) publicationManager
                    .getObjects(new EditionFilter(editionId), new OutputFilter(LibCollectionId.class)).getResultIds();
            if (collectionIds.isEmpty()) {
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        textsBundle.getString("validation.error.reference.noCollections.message") + idGroup, null));
                continue;
            }
            Publication publication = (Publication) publicationManager
                    .getObjects(new EditionFilter(editionId), new OutputFilter(Publication.class)).getResult();
            FileId mainFileId = publication.getMainFileId();
            File mainFile = (File) fileManager.getObjects(new FileFilter(mainFileId), new OutputFilter(File.class))
                    .getResult();
            boolean isWOMI = mainFile.getPath().equals("/" + WOMIXMLHandler.MAIN_FILE_NAME);
            if (!isWOMI) {
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        textsBundle.getString("validation.error.reference.notWomi.message") + idGroup, null));
                continue;
            }
        }
    }
}
