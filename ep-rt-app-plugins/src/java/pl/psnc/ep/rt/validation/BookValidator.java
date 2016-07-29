package pl.psnc.ep.rt.validation;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.eventserver.metadata.EventMetadataServer;
import pl.psnc.dlibra.app.extension.datasource.FileInfo;
import pl.psnc.dlibra.app.extension.validator.PublicationData;
import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.mgmt.AbstractServiceResolver;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class BookValidator extends InternalValidator {

    static final String TITLE_ATTR = "Tytul";

    private static final Logger logger = Logger.getLogger(BookValidator.class);

    private final ModuleValidator moduleValidator;


    public BookValidator(ResourceBundle textsBundle, ModuleValidator moduleValidator) {
        super(textsBundle);
        this.moduleValidator = moduleValidator;
    }


    @Override
    public boolean recognize(PublicationData publicationData, AbstractServiceResolver serviceResolver) {
        if (publicationData.getPublication().getGroupStatus() == Publication.PUB_GROUP_ROOT)
            return true;

        Edition edition = publicationData.getEdition();
        if (edition == null)
            return false;
        if (edition.getExternalId() == null)
            return false;

        List<FileInfo> files = publicationData.getFiles();
        if (files.isEmpty())
            return false;
        if (files.get(0).getDLibraPath().equalsIgnoreCase("/" + WOMIXMLHandler.MAIN_FILE_NAME))
            return false;

        return true;
    }


    @Override
    public List<ValidationResult> validate(PublicationData publicationData)
            throws IOException, DLibraException {
        ArrayList<ValidationResult> results = new ArrayList<ValidationResult>();

        EventMetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        PublicationId publicationId = publicationData.getPublication().getId();
        boolean isUpdating = publicationId != null && publicationId.getId() != 0;
        AttributeValueSet avs = publicationData.getAttributeValueSet();
        AttributeValueSet oldValues;
        if (isUpdating) {
            if (Versioning.isBlocked(publicationId, ApplicationContext.getInstance().getEventUserServer())) {
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        textsBundle.getString("validation.error.bookBlocked"), null));
                return results;
            }
            ElementId elementId = publicationData.getFiles().isEmpty() ? publicationId
                    : publicationData.getEdition().getId();
            oldValues = ms.getElementMetadataManager().getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
        } else {
            oldValues = new AttributeValueSet();
        }

        String mainFilePath = publicationData.getFiles().isEmpty() ? null
                : publicationData.getFiles().get(0).getDLibraPath();
        String editorError = Versioning.checkEditorMode(avs, oldValues, publicationData.getPublication(),
            publicationData.getEdition(), mainFilePath, isUpdating, ms);
        if (editorError != null) {
            results.add(new ValidationResult(ValidationResultType.ERROR,
                    textsBundle.getString("validation.error.editorMode"), editorError));
            return results;
        }

        checkRequiredAttributes(avs, BOOK_LIMITATIONS, results);

        List<PublicationData> children = publicationData.getChildren();
        if (children == null || children.isEmpty())
            results.add(new ValidationResult(ValidationResultType.ERROR,
                    textsBundle.getString("validation.error.bookEmpty"), null));

        ArrayDeque<PublicationData> queue = new ArrayDeque<PublicationData>(children);
        while (!queue.isEmpty()) {
            PublicationData data = queue.poll();
            Publication publication = data.getPublication();
            if (publication.getState() != Publication.PUB_STATE_ACTUAL) {
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        String.format(textsBundle.getString("validation.error.invalidState"), publication.getId()),
                        textsBundle.getString("validation.suggestion.fromScratch")));
            } else if (publication.getGroupStatus() == Publication.PUB_GROUP_MID) {
                children = data.getChildren();
                if (children != null && !children.isEmpty()) {
                    queue.addAll(data.getChildren());
                } else {
                    results.add(new ValidationResult(ValidationResultType.ERROR,
                            publication.getName() + textsBundle.getString("validation.error.chapterEmpty"), null));
                }
            } else if (publication.getGroupStatus() == Publication.PUB_GROUP_LEAF) {
                if (moduleValidator.recognize(data, null)) {
                    List<ValidationResult> childValidation = moduleValidator.validate(data);
                    for (ValidationResult vr : childValidation) {
                        String bookName = publication.getName();
                        if (bookName.length() > 30)
                            bookName = bookName.substring(0, 30) + '\u2026';
                        results.add(
                            new ValidationResult(vr.getType(), bookName + ": " + vr.getSummary(), vr.getDescription()));
                    }
                } else {
                    results.add(new ValidationResult(ValidationResultType.ERROR,
                            textsBundle.getString("validation.error.leafNotModule") + publication.getName(), null));
                }
            }
        }

        return results;
    }
}
