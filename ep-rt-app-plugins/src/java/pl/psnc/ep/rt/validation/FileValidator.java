package pl.psnc.ep.rt.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.util.SimpleImageInfo;

public class FileValidator {

    private final static int MIN_IMAGE_SIZE = 1920;

    private final static int MAX_ICON_SIZE = 300;

    private final static int RECOMMENDED_IMAGE_SIZE = 2880;


    public static ValidationResult checkFile(File file, MediaFormat mediaFormat, ResourceBundle textsBundle) {
        switch (mediaFormat) {
            case IMAGE:
                return checkImage(file, textsBundle, false);
            case ICON:
                return checkImage(file, textsBundle, true);
            case PACKAGE:
                return checkPackage(file, textsBundle);
            case CAPTIONS:
            case SUBTITLES:
                return checkSubtitles(file, textsBundle);
            default:
                return null;
        }
    }


    private static ValidationResult checkImage(File file, ResourceBundle textsBundle, boolean isIcon) {
        SimpleImageInfo imageInfo;
        try {
            imageInfo = new SimpleImageInfo(file);
        } catch (FileNotFoundException e) {
            return new ValidationResult(ValidationResultType.ERROR,
                    String.format(textsBundle.getString("validation.error.badfile"), file),
                    textsBundle.getString("file.error.filenotfound"));
        } catch (IOException e) {
            return new ValidationResult(ValidationResultType.ERROR,
                    String.format(textsBundle.getString("validation.error.badfile"), file),
                    textsBundle.getString("file.error.ioexception"));
        }

        if (isIcon) {
            if (imageInfo.getWidth() > MAX_ICON_SIZE && imageInfo.getHeight() > MAX_ICON_SIZE)
                return new ValidationResult(ValidationResultType.ERROR,
                        String.format(textsBundle.getString("validation.error.badfile"), file),
                        String.format(textsBundle.getString("icon.error.toobig"), imageInfo.getWidth(),
                            imageInfo.getHeight(), MAX_ICON_SIZE));
            return null;
        }

        if (imageInfo.getMimeType().equals("image/svg+xml"))
            return null;

        if (imageInfo.getWidth() < MIN_IMAGE_SIZE && imageInfo.getHeight() < MIN_IMAGE_SIZE)
            return new ValidationResult(ValidationResultType.ERROR,
                    String.format(textsBundle.getString("validation.error.badfile"), file),
                    String.format(textsBundle.getString("image.error.toosmall"), imageInfo.getWidth(),
                        imageInfo.getHeight(), MIN_IMAGE_SIZE));

        if (imageInfo.getWidth() < RECOMMENDED_IMAGE_SIZE && imageInfo.getHeight() < RECOMMENDED_IMAGE_SIZE)
            return new ValidationResult(ValidationResultType.WARNING,
                    String.format(textsBundle.getString("validation.error.badfile"), file),
                    String.format(textsBundle.getString("image.warning.small"), imageInfo.getWidth(),
                        imageInfo.getHeight(), RECOMMENDED_IMAGE_SIZE));

        return null;
    }


    private static ValidationResult checkPackage(File file, ResourceBundle textsBundle) {
        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            String commonRoot = null;
            try {
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory())
                        continue;
                    String name = entry.getName();
                    if (zipFile.size() == 1 && name.matches("^.*\\.((?i)ggb|html?)$"))
                        return null;
                    if (name.equals("manifest.json"))
                        return null;

                    String[] path = name.split("/");
                    if (path.length > 1 && (commonRoot == null || commonRoot.equals(path[0])))
                        commonRoot = path[0];
                    else
                        commonRoot = "";
                }
            } finally {
                zipFile.close();
            }
            if (commonRoot != null && !commonRoot.isEmpty())
                return new ValidationResult(ValidationResultType.ERROR,
                        textsBundle.getString("interactive.error.singleFolder"),
                        textsBundle.getString("interactive.error.singleFolder.description"));
            return new ValidationResult(ValidationResultType.ERROR, textsBundle.getString("interactive.error.invalid"),
                    textsBundle.getString("interactive.error.invalid.details"));
        } catch (Exception e) {
            String description = e.getLocalizedMessage();
            if (e instanceof IllegalArgumentException && "MALFORMED".equals(e.getMessage())) {
                description = textsBundle.getString("interactive.error.diacritics.description");
            }
            return new ValidationResult(ValidationResultType.ERROR, textsBundle.getString("file.error.ioexception"),
                    description);
        }
    }


    private static ValidationResult checkSubtitles(File file, ResourceBundle textsBundle) {
        try {
            FileInputStream is = new FileInputStream(file);
            Scanner scanner = new Scanner(is, "UTF-8");
            try {
                String s = scanner.next();
                if (s.charAt(0) == 0xFEFF)
                    s = s.substring(1);
                if (!"WEBVTT".equals(s)) {
                    throw new IOException("Not a valid VTT file");
                }
            } finally {
                scanner.close();
                is.close();
            }
        } catch (FileNotFoundException e) {
            return new ValidationResult(ValidationResultType.ERROR,
                    String.format(textsBundle.getString("validation.error.badfile"), file),
                    String.format(textsBundle.getString("file.error.filenotfound")));

        } catch (Exception e) {
            return new ValidationResult(ValidationResultType.ERROR,
                    String.format(textsBundle.getString("validation.error.badfile"), file),
                    String.format(textsBundle.getString("file.error.ioexception")));
        }

        return null;
    }
}
