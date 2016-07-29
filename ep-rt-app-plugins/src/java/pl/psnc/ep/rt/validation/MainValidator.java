package pl.psnc.ep.rt.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.extension.Extension;
import pl.psnc.dlibra.app.extension.validator.PublicationData;
import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.dlibra.app.extension.validator.Validator;
import pl.psnc.dlibra.mgmt.AbstractServiceResolver;
import pl.psnc.dlibra.service.DLibraException;

public class MainValidator implements Validator {

    private final static String BUNDLE_PATH = "GUI";

    private final static Logger logger = Logger.getLogger(MainValidator.class);

    private ResourceBundle textsBundle;

    private BookValidator bookValidator;

    private ModuleValidator moduleValidator;

    private WOMIValidator womiValidator;


    @Override
    public void initialize(Map<String, Object> initPrefs) {
        textsBundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault());
        String configDir = initPrefs.get(Extension.EXTENSION_DIRECTORY).toString();
        moduleValidator = new ModuleValidator(textsBundle, configDir);
        bookValidator = new BookValidator(textsBundle, moduleValidator);
        womiValidator = new WOMIValidator(textsBundle);
    }


    @Override
    public List<ValidationResult> validate(PublicationData publicationData, AbstractServiceResolver serverInterface) {
        List<ValidationResult> results = new ArrayList<ValidationResult>();

        try {
            if (bookValidator.recognize(publicationData, serverInterface)) {
                results.addAll(bookValidator.validate(publicationData));
            } else if (moduleValidator.recognize(publicationData, serverInterface)) {
                results.add(new ValidationResult(ValidationResultType.NOTICE,
                        textsBundle.getString("validation.recognized.module"), null));
                results.addAll(moduleValidator.validate(publicationData));
            } else if (womiValidator.recognize(publicationData, serverInterface)) {
                results.add(new ValidationResult(ValidationResultType.NOTICE,
                        textsBundle.getString("validation.recognized.womi"), null));
                results.addAll(womiValidator.validate(publicationData));
            } else {
                results.add(new ValidationResult(ValidationResultType.WARNING,
                        textsBundle.getString("validation.recognized.none"), null));
            }
        } catch (IOException e) {
            logger.error("Could not validate files", e);
            results.add(new ValidationResult(ValidationResultType.ERROR, textsBundle.getString("validation.error.io"),
                    e.getMessage()));
        } catch (DLibraException e) {
            logger.error("Could not validate files", e);
            results.add(new ValidationResult(ValidationResultType.ERROR,
                    textsBundle.getString("validation.error.dlibra"), e.getClass() + ": " + e.getMessage()));
        }
        return results;
    }


    @Override
    public String toString() {
        return textsBundle.getString("validation.plugin.name");
    }

}
