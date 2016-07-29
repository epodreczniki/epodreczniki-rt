package pl.psnc.ep.rt.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.extension.validator.PublicationData;
import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.mgmt.AbstractServiceResolver;
import pl.psnc.dlibra.service.DLibraException;

public abstract class InternalValidator {

    private static enum Limitation {
        REQUIRED,
        MAX_ONE
    }


    private static final Logger logger = Logger.getLogger(InternalValidator.class);

    static final String PL = "pl";

    static final HashMap<String, EnumSet<Limitation>> WOMI_LIMITATIONS = new HashMap<String, EnumSet<Limitation>>();

    static final HashMap<String, EnumSet<Limitation>> MODULE_LIMITATIONS = new HashMap<String, EnumSet<Limitation>>();

    static final HashMap<String, EnumSet<Limitation>> BOOK_LIMITATIONS = new HashMap<String, EnumSet<Limitation>>();

    HashMap<AttributeInfo, ArrayList<String>> duplicateValues;


    static {
        final String[] REQUIRED_ATTRIBUTES_WOMI = { "Tytul", "Autor", "TekstAlternatywny", "Licencja" };
        for (String attribute : REQUIRED_ATTRIBUTES_WOMI) {
            WOMI_LIMITATIONS.put(attribute, EnumSet.of(Limitation.REQUIRED));
        }

        final String[] REQUIRED_ATTRIBUTES_MODULE = { "Tytul", "Autor" };
        for (String attribute : REQUIRED_ATTRIBUTES_MODULE) {
            MODULE_LIMITATIONS.put(attribute, EnumSet.of(Limitation.REQUIRED));
        }

        final String[] REQUIRED_ATTRIBUTES_BOOK = { "Tytul", "Autor", "EtapEdukacyjny", "Klasa", "Odbiorca",
                "StatusTresci", "Abstrakt", "Okladka" };
        for (String attribute : REQUIRED_ATTRIBUTES_BOOK) {
            BOOK_LIMITATIONS.put(attribute, EnumSet.of(Limitation.REQUIRED));
        }
        BOOK_LIMITATIONS.get("EtapEdukacyjny").add(Limitation.MAX_ONE);
        BOOK_LIMITATIONS.get("Klasa").add(Limitation.MAX_ONE);
        BOOK_LIMITATIONS.get("Okladka").add(Limitation.MAX_ONE);
    }


    protected final ResourceBundle textsBundle;


    public InternalValidator(ResourceBundle textsBundle) {
        this.textsBundle = textsBundle;
    }


    public abstract boolean recognize(PublicationData publicationData, AbstractServiceResolver serviceResolver);


    public abstract List<ValidationResult> validate(PublicationData publicationData)
            throws IOException, DLibraException;


    void checkRequiredAttributes(AttributeValueSet avs, HashMap<String, EnumSet<Limitation>> limitations,
            ArrayList<ValidationResult> validationResults)
                    throws IOException, DLibraException {
        ArrayList<String> missing = new ArrayList<String>();
        ArrayList<String> tooMany = new ArrayList<String>();
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        for (Entry<String, EnumSet<Limitation>> entry : limitations.entrySet()) {
            EnumSet<Limitation> limitation = entry.getValue();
            if (!limitation.contains(Limitation.REQUIRED) && !limitation.contains(Limitation.MAX_ONE))
                continue;
            String attributeName = entry.getKey();
            AttributeInfo attributeInfo = (AttributeInfo) ms.getAttributeManager()
                    .getObjects(new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList(attributeName)),
                        new OutputFilter(AttributeInfo.class))
                    .getResultInfo();
            if (attributeInfo == null) {
                logger.warn("Could not find required attribute: " + attributeName);
                continue;
            }
            List<AbstractAttributeValue> values = avs.getAttributeValues(attributeInfo.getId(), PL,
                AttributeValueSet.Values.OnlyDirect);
            if (limitation.contains(Limitation.REQUIRED) && values.isEmpty()) {
                attributeInfo.setLanguage(PL);
                missing.add(attributeInfo.getLabel());
            }
            if (limitation.contains(Limitation.MAX_ONE) && values.size() > 1) {
                attributeInfo.setLanguage(PL);
                tooMany.add(attributeInfo.getLabel());
            }
        }

        if (!missing.isEmpty()) {
            validationResults.add(
                new ValidationResult(ValidationResultType.WARNING, textsBundle.getString("validation.metadata.missing"),
                        String.format(textsBundle.getString("validation.metadata.missing.list"), missing)));
        }
        if (!tooMany.isEmpty()) {
            validationResults.add(
                new ValidationResult(ValidationResultType.WARNING, textsBundle.getString("validation.metadata.tooMany"),
                        String.format(textsBundle.getString("validation.metadata.tooMany.list"), tooMany)));
        }
    }
}
