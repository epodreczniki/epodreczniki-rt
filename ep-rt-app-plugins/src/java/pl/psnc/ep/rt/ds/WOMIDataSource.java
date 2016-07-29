package pl.psnc.ep.rt.ds;

import java.awt.Cursor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.extension.datasource.DataSource2;
import pl.psnc.dlibra.app.extension.datasource.FileInfo;
import pl.psnc.dlibra.app.extension.datasource.FileSelectionListener;
import pl.psnc.dlibra.app.extension.datasource.LocalResourceUnavailableException;
import pl.psnc.dlibra.app.extension.datasource.SimpleFileInfo;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.ds.gui.WOMIDefinitionPanel;
import pl.psnc.ep.rt.ds.gui.WOMIDefinitionPanel.ChangesListener;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

abstract public class WOMIDataSource implements DataSource2, ChangesListener {

    private final static String BUNDLE_PATH = "GUI";

    private final static Logger logger = Logger.getLogger(WOMIDataSource.class);

    protected ResourceBundle textsBundle;

    protected WOMIDefinitionPanel panel;

    protected FileSelectionListener fileSelectionListener;

    protected File tempDir;


    @Override
    public void initialize(Map<String, Object> initPrefs) {
        tempDir = new File((String) initPrefs.get(TEMP_DIRECTORY));
        textsBundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault());
    }


    @Override
    public JComponent getPanel(FileSelectionListener listener) {
        if (panel == null) {
            panel = new WOMIDefinitionPanel(getWOMIType(), textsBundle);
            panel.setChangesListener(this);
        }
        fileSelectionListener = listener;
        panel.clear();
        return panel;
    }


    abstract protected WOMIType getWOMIType();


    @Override
    public void filesChanged(final Map<WOMIFormat, File> sourceFiles) {
        if (sourceFiles == null) {
            clearFileInfos();
            return;
        }

        SwingWorker<List<FileInfo>, Object> worker = new SwingWorker<List<FileInfo>, Object>() {

            @Override
            protected List<FileInfo> doInBackground()
                    throws Exception {
                Map<WOMIFormat, String> desiredPaths;
                File mainFile = File.createTempFile("ep-rt-ds-", ".xml", tempDir);
                OutputStream outputStream = new FileOutputStream(mainFile);
                try {
                    boolean hasAlternatives = sourceFiles.containsKey(WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT);
                    desiredPaths = WOMIXMLHandler.saveMultiFormatXML(sourceFiles, outputStream, hasAlternatives);
                } finally {
                    outputStream.close();
                }

                List<FileInfo> fileInfos = new ArrayList<FileInfo>();
                SimpleFileInfo mainFileInfo = new SimpleFileInfo(mainFile.toURI().toURL(), true,
                        "/" + WOMIXMLHandler.MAIN_FILE_NAME);
                fileInfos.add(mainFileInfo);
                for (Map.Entry<WOMIFormat, File> entry : sourceFiles.entrySet()) {
                    WOMIFormat format = entry.getKey();
                    File file = entry.getValue();
                    if (file == null)
                        continue;
                    URL url = file.toURI().toURL();
                    String path;
                    if (format == WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT)
                        path = WOMIDefinitionPanel.STATIC_ALTERNATIVE_PATH;
                    else
                        path = desiredPaths.get(format);
                    fileInfos.add(new SimpleFileInfo(url, true, path));
                }
                return fileInfos;
            }


            @Override
            protected void done() {
                try {
                    List<FileInfo> fileInfos = get();
                    fileSelectionListener.filesSelected(fileInfos);
                } catch (LocalResourceUnavailableException e) {
                    logger.error("One of selected files disappeared?", e);
                    JOptionPane.showMessageDialog(panel, e.getMessage(), textsBundle.getString("datasoure.error"),
                        JOptionPane.ERROR_MESSAGE);
                    clearFileInfos();
                } catch (ExecutionException e) {
                    logger.error("Unexpected error on updating publication files list", e);
                    JOptionPane.showMessageDialog(panel, e.getCause().getMessage(),
                        textsBundle.getString("datasoure.error"), JOptionPane.ERROR_MESSAGE);
                    clearFileInfos();
                } catch (InterruptedException e) {
                    throw new RuntimeException("This operation should not be interrupted");
                } finally {
                    panel.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }


    private void clearFileInfos() {
        try {
            fileSelectionListener.filesSelected(null);
            return;
        } catch (LocalResourceUnavailableException e) {
            throw new RuntimeException("Shouldn't happen: files list is empty");
        }
    }


    @Override
    public void setFileInfos(List<FileInfo> files) {
        try {
            InputStream is = files.get(0).getURL().openStream();
            try {
                Map<WOMIFormat, String> womiFormats = WOMIXMLHandler.loadMultiFormatXML(is);
                Map<WOMIFormat, File> womiFiles = new HashMap<WOMIFormat, File>();
                for (Map.Entry<WOMIFormat, String> entry : womiFormats.entrySet()) {
                    WOMIFormat format = entry.getKey();
                    String fileName = entry.getValue();
                    String dLibraPath = WOMIXMLHandler.toDLibraPath(format, fileName);
                    for (FileInfo file : files) {
                        if (file.getDLibraPath().equals(dLibraPath)) {
                            womiFiles.put(format, new File(file.getURL().toURI()));
                            break;
                        }
                    }
                    if (!womiFiles.containsKey(format)) {
                        logger.warn("Files list does not contain for format " + format);
                    }
                }

                for (FileInfo file : files) {
                    if (file.getDLibraPath().equals(WOMIDefinitionPanel.STATIC_ALTERNATIVE_PATH)) {
                        womiFiles.put(WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT, new File(file.getURL().toURI()));
                    }
                }

                panel.setFiles(womiFiles);
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
