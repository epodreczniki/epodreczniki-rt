package pl.psnc.ep.rt.ds.gui;

import static pl.psnc.ep.rt.util.WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.util.ImageConverter;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class WOMIDefinitionPanel extends JPanel {

    public static interface ChangesListener {

        public void filesChanged(Map<WOMIFormat, File> sourceFiles);
    }


    public static final String STATIC_ALTERNATIVE_PATH = "/" + StaticAlternativePanel.ALTERNATIVES_FILE;

    private static final Logger log = Logger.getLogger(WOMIDefinitionPanel.class);

    private final ResourceBundle textsBundle;

    private final WOMIType womiType;

    private final Map<WOMIFormat, AbstractFormatPanel> formatToPanel = new HashMap<WOMIFormat, AbstractFormatPanel>();

    private StaticAlternativePanel staticAlternativePanel;

    private JCheckBox add3DCheckbox;

    private JFileChooser fileChooser;

    private ChangesListener changesListener;

    private ImagePreview imagePreview;

    private AudioVideoPreview audioVideoPreview;

    private JLabel errorLabel;

    private EditionId editionId;


    public WOMIDefinitionPanel(WOMIType womiType, ResourceBundle textsBundle) {
        this.textsBundle = textsBundle;
        this.womiType = womiType;

        fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);

        layoutComponents();
        initGUITexts();
        addListeners();
    }


    public WOMIType getWomiType() {
        return womiType;
    }


    public void clear() {
        for (WOMIFormat format : womiType.womiFormats) {
            AbstractFormatPanel panel = formatToPanel.get(format);
            panel.setSourceFile(null);
            panel.setRemoteFileName(null);
        }
        set3DVersionVisible(false);
        staticAlternativePanel.setSourceFile(null);
        staticAlternativePanel.setRemoteFile(false);
        updatePreviewButtons();
    }


    public void setChangesListener(ChangesListener changesListener) {
        this.changesListener = changesListener;
    }


    private void layoutComponents() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new BoxLayout(internalPanel, BoxLayout.PAGE_AXIS));
        internalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = c.weighty = 1;
        this.add(new JScrollPane(internalPanel), c);

        errorLabel = new JLabel();
        errorLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        errorLabel.setVisible(false);
        c.weighty = 0;
        this.add(errorLabel, c);

        ArrayList<JPanel> for3d = new ArrayList<JPanel>();
        for (WOMIFormat womiFormat : womiType.womiFormats) {
            AbstractFormatPanel formatPanel;
            if (womiFormat.targetFormat == TargetFormat.DEFAULT) {
                formatPanel = new DefaultFormatPanel(textsBundle, womiFormat);
            } else {
                formatPanel = new AlternativeFormatPanel(textsBundle, womiFormat);
            }
            formatPanel.setFileChooser(fileChooser);
            formatPanel.setChangesListener(this);
            boolean previewVisible = womiFormat.mediaFormat == MediaFormat.IMAGE;
            formatPanel.setPreviewVisible(previewVisible, previewVisible);
            formatToPanel.put(womiFormat, formatPanel);
            if (womiFormat.is3D) {
                for3d.add(formatPanel);
            } else {
                internalPanel.add(formatPanel);
                internalPanel.add(Box.createVerticalStrut(5));
            }
        }

        if (womiType.allows3D()) {
            internalPanel.add(Box.createVerticalStrut(15));
            internalPanel.add(add3DCheckbox = new JCheckBox());
            for (JPanel panel : for3d) {
                internalPanel.add(panel);
                internalPanel.add(Box.createVerticalStrut(5));
            }
        }

        internalPanel.add(Box.createVerticalStrut(15));
        internalPanel.add(staticAlternativePanel = new StaticAlternativePanel(textsBundle));
        staticAlternativePanel.setFileChooser(fileChooser);
        staticAlternativePanel.setChangesListener(this);
        for (int i = 0; i < internalPanel.getComponentCount(); i++)
            ((JComponent) internalPanel.getComponent(i)).setAlignmentX(Component.LEFT_ALIGNMENT);
    }


    private void initGUITexts() {
        if (add3DCheckbox != null)
            add3DCheckbox.setText(textsBundle.getString("datasource.add3d"));
    }


    private void addListeners() {
        if (add3DCheckbox != null) {
            add3DCheckbox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    set3DVersionVisible(add3DCheckbox.isSelected());
                    sourceFilesChanged();
                }
            });
        }
    }


    private void set3DVersionVisible(boolean visible) {
        for (WOMIFormat format : womiType.womiFormats) {
            if (format.is3D)
                formatToPanel.get(format).setVisible(visible);
        }
        if (add3DCheckbox != null)
            add3DCheckbox.setSelected(visible);
    }


    void sourceFilesChanged() {
        updatePreviewButtons();
        if (changesListener == null)
            return;

        Map<WOMIFormat, File> sourceFiles = new LinkedHashMap<WOMIFormat, File>();
        for (WOMIFormat womiFormat : womiType.womiFormats) {
            AbstractFormatPanel panel = formatToPanel.get(womiFormat);

            if (womiFormat.is3D && !is3DSelected()) {
                if (panel.getRemoteFileName() != null) {
                    sourceFiles.put(womiFormat, null);
                }
                continue;
            }

            File sourceFile = panel.getSourceFile();
            if (sourceFile != null && panel.isFormatSelected()) {
                sourceFiles.put(womiFormat, sourceFile);
            } else if (panel.getRemoteFileName() != null) {
                if (!panel.isFormatSelected())
                    sourceFiles.put(womiFormat, null);
            } else {
                boolean required = womiFormat.targetFormat == TargetFormat.DEFAULT
                        && womiFormat.mediaFormat.isRequired();
                if (womiFormat.womiType == WOMIType.INTERACTIVE && womiFormat.mediaFormat == MediaFormat.IMAGE)
                    required = false;
                if (required) {
                    changesListener.filesChanged(null);
                    return;
                }
                sourceFiles.put(womiFormat, null);
            }
        }
        File sourceFile = staticAlternativePanel.getSourceFile();
        if (sourceFile != null && staticAlternativePanel.isSelected())
            sourceFiles.put(STATIC_ALTERNATIVE_FORMAT, staticAlternativePanel.getSourceFile());
        else if (staticAlternativePanel.isRemoteFileSet() && !staticAlternativePanel.isSelected())
            sourceFiles.put(STATIC_ALTERNATIVE_FORMAT, null);

        if (!checkForDuplicates(sourceFiles))
            changesListener.filesChanged(null);
        else
            changesListener.filesChanged(sourceFiles);
    }


    private boolean checkForDuplicates(Map<WOMIFormat, File> sourceFiles) {
        for (Map.Entry<WOMIFormat, File> entry : sourceFiles.entrySet()) {
            WOMIFormat format = entry.getKey();
            File file = entry.getValue();
            if (file == null)
                continue;
            WOMIFormat defalutFormat = format.toDefaultTarget();
            if (!defalutFormat.equals(format) && file.equals(sourceFiles.get(defalutFormat))) {
                showError(String.format(textsBundle.getString("validation.error.file.duplicate"), file.toString()));
                return false;
            }
        }
        showError(null);
        return true;
    }


    private void showError(String error) {
        errorLabel.setVisible(error != null);
        if (error != null)
            errorLabel.setText(error);
    }


    private void updatePreviewButtons() {
        for (WOMIFormat womiFormat : womiType.womiFormats) {
            AbstractFormatPanel panel = formatToPanel.get(womiFormat);

            if (panel.getSourceFile() != null && panel.isFormatSelected()) {
                panel.setPreviewEnabled(ImageConverter.isEnabled(), true);
            } else if (panel.getRemoteFileName() != null) {
                panel.setPreviewEnabled(panel.isFormatSelected(), true);
            } else {
                WOMIFormat defaultFormat = womiFormat.toDefaultTarget();
                AbstractFormatPanel defaultPanel = formatToPanel.get(defaultFormat);
                panel.setPreviewEnabled(defaultPanel.getSourceFile() != null ? ImageConverter.isEnabled()
                        : defaultPanel.getRemoteFileName() != null,
                    false);
            }
        }
    }


    void previewRequested(WOMIFormat format, boolean emissive) {
        AbstractFormatPanel panel = formatToPanel.get(format);
        if (panel.isFormatSelected()) {
            showPreview(panel, format, emissive);
        } else {
            WOMIFormat defaultFormat = format.toDefaultTarget();
            AbstractFormatPanel defaultPanel = formatToPanel.get(defaultFormat);
            showPreview(defaultPanel, format, emissive);
        }
    }


    private void showPreview(AbstractFormatPanel panel, WOMIFormat format, boolean emissive) {
        if (format.mediaFormat == MediaFormat.IMAGE) {
            if (imagePreview == null)
                imagePreview = new ImagePreview(SwingUtilities.getWindowAncestor(this), textsBundle);
            if (panel.getSourceFile() != null) {
                imagePreview.showLocal(panel.getSourceFile(), emissive ? format.targetFormat : null);
            } else if (panel.getRemoteFileName() != null) {
                if (emissive) {
                    imagePreview.showEmissive(editionId, format);
                } else {
                    imagePreview.showSource(editionId, WOMIXMLHandler.toDLibraPath(format, panel.getRemoteFileName()));
                }
            }
        } else if (format.mediaFormat == MediaFormat.AUDIO || format.mediaFormat == MediaFormat.VIDEO) {
            if (audioVideoPreview == null)
                audioVideoPreview = new AudioVideoPreview(textsBundle);
            audioVideoPreview.showPreview(editionId);
        }
    }


    private boolean is3DSelected() {
        return add3DCheckbox != null && add3DCheckbox.isSelected();
    }


    public void setRemoteFileNames(Map<WOMIFormat, String> fileNames) {
        clear();
        for (Entry<WOMIFormat, String> entry : fileNames.entrySet()) {
            WOMIFormat format = entry.getKey();
            if (format == STATIC_ALTERNATIVE_FORMAT) {
                staticAlternativePanel.setRemoteFile(true);
                continue;
            }
            AbstractFormatPanel formatPanel = formatToPanel.get(format);
            if (formatPanel == null) {
                log.warn("Unrecognized format inside WOMI: " + format);
                continue;
            }
            formatPanel.setRemoteFileName(entry.getValue());
            if (format.is3D && !is3DSelected()) {
                set3DVersionVisible(true);
            }
        }
        updatePreviewButtons();
    }


    public void setFiles(Map<WOMIFormat, File> files) {
        clear();
        for (Entry<WOMIFormat, File> entry : files.entrySet()) {
            WOMIFormat format = entry.getKey();
            if (format == STATIC_ALTERNATIVE_FORMAT) {
                staticAlternativePanel.setSourceFile(entry.getValue());
                continue;
            }
            AbstractFilePanel formatPanel = formatToPanel.get(format);
            formatPanel.setSourceFile(entry.getValue());
            if (format.is3D && !is3DSelected()) {
                set3DVersionVisible(true);
            }
        }
        updatePreviewButtons();
    }


    public void setModificationAllowed(boolean allowed) {
        showError(allowed ? null : textsBundle.getString("womiDefinitionPanel.noRights"));
        if (add3DCheckbox != null)
            add3DCheckbox.setEnabled(allowed);
        for (AbstractFormatPanel panel : formatToPanel.values()) {
            panel.setModificationAllowed(allowed);
        }
        staticAlternativePanel.setModificationAllowed(allowed);
    }


    public void setPreviewVisible(WOMIFormat format, boolean isEmissiveVisible, boolean isSourceVisible) {
        AbstractFormatPanel panel = formatToPanel.get(format);
        if (panel != null)
            panel.setPreviewVisible(isEmissiveVisible, isSourceVisible);
    }


    public void setEditionId(EditionId editionId) {
        this.editionId = editionId;
    }
}
