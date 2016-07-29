package pl.psnc.ep.rt.validation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import pl.psnc.dlibra.app.extension.datasource.FileInfo;
import pl.psnc.dlibra.app.extension.validator.PublicationData;
import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.dlibra.mgmt.AbstractServiceResolver;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.util.ObjectMalformedException;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class WOMIValidator extends InternalValidator {

    public WOMIValidator(ResourceBundle textsBundle) {
        super(textsBundle);
    }


    @Override
    public boolean recognize(PublicationData publicationData, AbstractServiceResolver serviceResolver) {
        List<FileInfo> files = publicationData.getFiles();
        if (files.isEmpty())
            return false;

        FileInfo mainFile = files.get(0);
        String path = mainFile.getDLibraPath().toLowerCase();
        if (!path.equalsIgnoreCase("/" + WOMIXMLHandler.MAIN_FILE_NAME))
            return false;

        return true;
    }


    @Override
    public List<ValidationResult> validate(PublicationData publicationData)
            throws IOException, DLibraException {
        ArrayList<ValidationResult> results = new ArrayList<ValidationResult>();

        checkRequiredAttributes(publicationData.getAttributeValueSet(), WOMI_LIMITATIONS, results);

        FileInfo mainFile = publicationData.getFiles().get(0);
        InputStream inputStream = mainFile.getURL().openStream();
        Map<WOMIFormat, String> womiFiles;
        try {
            womiFiles = WOMIXMLHandler.loadMultiFormatXML(inputStream);
        } catch (ObjectMalformedException e) {
            results.add(new ValidationResult(ValidationResultType.ERROR,
                    textsBundle.getString("validation.error.xmlparse"), e.getMessage()));
            return results;
        } finally {
            inputStream.close();
        }

        validateFiles(womiFiles, publicationData.getFiles(), results);

        return results;
    }


    private void validateFiles(Map<WOMIFormat, String> womiFiles, List<FileInfo> publicationFiles,
            List<ValidationResult> results) {
        for (Map.Entry<WOMIFormat, String> entry : womiFiles.entrySet()) {
            WOMIFormat format = entry.getKey();
            String path = entry.getValue();

            FileInfo fileInfo = findFileInfo(publicationFiles, format, path);
            if (fileInfo == null) {
                results.add(new ValidationResult(ValidationResultType.ERROR,
                        String.format(textsBundle.getString("validation.error.file.missing"), path), null));
                continue;
            }

            WOMIFormat defalutFormat = format.toDefaultTarget();
            if (!defalutFormat.equals(format)) {
                FileInfo defaultFileInfo = findFileInfo(publicationFiles, defalutFormat, path);
                if (defaultFileInfo != null) {
                    String fileURL = fileInfo.getURL().toExternalForm();
                    String defaultURL = defaultFileInfo.getURL().toExternalForm();
                    if (fileURL.equals(defaultURL)) {
                        results.add(new ValidationResult(ValidationResultType.ERROR,
                                String.format(textsBundle.getString("validation.error.file.duplicate"), path),
                                textsBundle.getString("format." + format.id)));
                        continue;
                    }
                }
            }

            validateFile(fileInfo.getURL(), format, results);
        }
        if (womiFiles.containsKey(new WOMIFormat(WOMIType.INTERACTIVE, MediaFormat.PACKAGE))
                && !womiFiles.containsKey(new WOMIFormat(WOMIType.INTERACTIVE, MediaFormat.IMAGE))) {
            results.add(new ValidationResult(ValidationResultType.WARNING,
                    textsBundle.getString("validation.warning.interactiveImage.summary"),
                    textsBundle.getString("validation.warning.interactiveImage.description")));
        }
    }


    private FileInfo findFileInfo(List<FileInfo> publicationFiles, WOMIFormat format, String path) {
        String dLibraPath = WOMIXMLHandler.toDLibraPath(format, path);
        for (FileInfo fi : publicationFiles) {
            if (fi.getDLibraPath().equals(dLibraPath)) {
                return fi;
            }
        }
        return null;
    }


    private void validateFile(URL url, WOMIFormat format, List<ValidationResult> results) {
        ValidationResult vr = null;
        try {
            File file = new File(url.toURI());
            vr = FileValidator.checkFile(file, format.mediaFormat, textsBundle);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Should never happen: could not change URL to URI: " + url);
        }

        if (vr != null) {
            results.add(vr);
        }
    }

}
