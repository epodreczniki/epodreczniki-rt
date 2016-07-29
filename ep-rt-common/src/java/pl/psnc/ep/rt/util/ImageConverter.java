package pl.psnc.ep.rt.util;

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import pl.psnc.ep.rt.TargetFormat;

public class ImageConverter {

    private static final int BUFFER_SIZE = 1024;

    private static final String SVG_EXTENSION = ".svg";

    private static final String PNG_EXTENSION = ".png";

    private final static Logger logger = Logger.getLogger(ImageConverter.class);

    private final static Logger imLogger = Logger.getLogger("ImageMagick");

    private final static boolean isWindows;

    private final static String imagemagickPath;

    private final static String inkscapePath;


    static {
        isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        org.apache.log4j.BasicConfigurator.configure();
        String imPath = null;
        String isPath = null;
        try {
            Process which = runCommand(Arrays.asList(isWindows ? "where" : "which", "convert"), true);
            String[] paths = readAll(which.getInputStream()).split("\\r?\\n");
            for (String p : paths) {
                Process convert = runCommand(Arrays.asList(p, "-version"), true);
                String version = readAll(convert.getInputStream());
                if (version.startsWith("Version: ImageMagick 6")) {
                    imPath = p;
                    break;
                }
            }
            if (imPath == null) {
                logger.error("ImageMagick version 6 not found in " + Arrays.toString(paths));
            }

            Process inkscape = runCommand(Arrays.asList("inkscape", "-V", "-z"), true);
            String version = readAll(inkscape.getInputStream());
            if (version.startsWith("Inkscape"))
                isPath = "inkscape";
            else
                logger.error("Inkscape not found in system path");
        } catch (IOException e) {
            logger.error("Error while looking for image conversion tools", e);
        } finally {
            imagemagickPath = imPath;
            inkscapePath = isPath;
        }
    }


    public static boolean isEnabled() {
        return imagemagickPath != null;
    }


    public static String getOutputFormat(File file, TargetFormat targetFormat, int resolution, boolean svg2png) {
        String name = file.getName();
        String extension = name.substring(name.lastIndexOf("."));
        if (extension.equals(SVG_EXTENSION)) {
            if (targetFormat == TargetFormat.EBOOK || resolution == targetFormat.getMiniatureResolution() || svg2png) {
                return PNG_EXTENSION;
            }
        }
        return extension;
    }


    public static Image getImage(File source, TargetFormat targetFormat, int resolution)
            throws IOException {
        if (source.getName().endsWith(SVG_EXTENSION)) {
            throw new IllegalArgumentException("SVG images not supported");
        }
        if (!isEnabled())
            throw new IllegalStateException("ImageMagick could not be loaded");
        List<String> command = getMagickImageCommand(source, targetFormat, resolution, "-");
        imLogger.debug(command);
        Process process = runCommand(command, false);
        try {
            InputStream input = process.getInputStream();
            Image result = ImageIO.read(input);
            return result;
        } finally {
            waitForProcess(process);
        }
    }


    public static boolean saveImageFile(File source, TargetFormat targetFormat, int resolution, String fileName)
            throws IOException {
        if (source.getName().endsWith(SVG_EXTENSION) && fileName.endsWith(SVG_EXTENSION)) {
            copyFile(source, new File(fileName));
            return true;
        }
        if (!isEnabled())
            throw new IllegalStateException("ImageMagick could not be loaded");
        List<String> command = getMagickImageCommand(source, targetFormat, resolution, fileName);
        imLogger.debug(command);
        Process process = runCommand(command, false);
        boolean success = false;
        try {
            String stdOut = readAll(process.getInputStream());
            if (!stdOut.isEmpty()) {
                imLogger.debug(stdOut);
            }
        } finally {
            success = waitForProcess(process);
        }
        return success;
    }


    private static ArrayList<String> getMagickImageCommand(File source, TargetFormat targetFormat, int resolution,
            String targetFile) {
        resolution = checkResolution(targetFormat, resolution);
        ArrayList<String> command = new ArrayList<String>();

        boolean useInkscape = source.getName().endsWith(SVG_EXTENSION) && targetFile.endsWith(PNG_EXTENSION)
                && inkscapePath != null;
        if (useInkscape) {
            if (isWindows) {
                command.add("cmd");
                command.add("/c");
            } else {
                command.add("/bin/sh");
                command.add("-c");
            }
            command.add(inkscapePath);
            command.add("-z");
            command.add("-e");
            command.add(targetFile);
            command.add("-w");
            command.add(resolution + "");
            command.add(source.getAbsolutePath());
            command.add("&&");
            command.add('"' + imagemagickPath.replaceFirst("convert(\\.exe)?$", "mogrify$1") + '"');
        } else {
            command.add(imagemagickPath);

            if (resolution > 0) {
                command.add("-extract");
                command.add(resolution + "x" + (resolution * 10));
            }
            command.add(source.getAbsolutePath());
        }

        if (targetFormat != null) {
            switch (targetFormat) {
                case EBOOK:
                    if (resolution != TargetFormat.EBOOK.getCoverResolution()) {
                        command.add("-alpha");
                        command.add("Remove");
                        command.add("-colorspace");
                        command.add("Gray");
                        command.add("-colors");
                        command.add("16");
                    }
                    break;
                default:
                    command.add("-colorspace");
                    command.add("sRGB");
            }
        }
        command.add(targetFile);

        if (useInkscape) {
            StringBuilder joinedCommand = new StringBuilder();
            for (int i = 2; i < command.size(); i++)
                joinedCommand.append(command.get(i)).append(' ');
            command = new ArrayList<String>(command.subList(0, 2));
            command.add(joinedCommand.toString());
        }
        return command;
    }


    private static Process runCommand(List<String> command, boolean mergeError)
            throws IOException {
        Process process = new ProcessBuilder(command).redirectErrorStream(mergeError).start();
        if (mergeError)
            return process;

        final InputStream stderr = process.getErrorStream();
        new Thread("ImageMagick-stderrReader") {

            public void run() {
                try {
                    readAllAndLog(stderr);
                } catch (IOException e) {
                    logger.error("Error getting process output", e);
                }
            }


            private void readAllAndLog(InputStream in)
                    throws IOException {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read, offset = 0;
                while ((read = in.read(buffer, offset, BUFFER_SIZE - offset)) != -1) {
                    offset += read;
                    if (read < 0 || in.available() < 1 || offset >= BUFFER_SIZE) {
                        if (offset > 0) {
                            logger.warn(new String(buffer, 0, offset));
                        }
                        offset = 0;
                    }
                }
            }
        }.start();

        return process;
    }


    private static int checkResolution(TargetFormat targetFormat, int resolution) {
        if (targetFormat == null)
            return resolution;

        if (resolution == 0)
            return 0;

        int[] resolutions = targetFormat.getResolutions();
        boolean recognized = false;
        for (int res : resolutions)
            if (res == resolution)
                recognized = true;
        if (!recognized)
            recognized = resolution == targetFormat.getCoverResolution()
                    || resolution == targetFormat.getMiniatureResolution();
        if (!recognized)
            throw new IllegalArgumentException(
                    "Resolution " + resolution + " is not supported by format " + targetFormat);
        return resolution;
    }


    private static String readAll(InputStream in)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read;
        while ((read = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toString();
    }


    private static boolean waitForProcess(Process process) {
        try {
            int exitStatus = process.waitFor();
            if (exitStatus != 0) {
                imLogger.warn("Command exit status " + exitStatus);
            }
            return exitStatus == 0;
        } catch (InterruptedException e) {
            process.destroy();
            Thread.currentThread().interrupt();
            return false;
        }
    }


    private static void copyFile(File source, File target)
            throws IOException {
        FileInputStream is = new FileInputStream(source);
        try {
            FileOutputStream os = new FileOutputStream(target);
            byte[] buffer = new byte[BUFFER_SIZE];
            try {
                int read;
                while ((read = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    os.write(buffer, 0, read);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }
}
