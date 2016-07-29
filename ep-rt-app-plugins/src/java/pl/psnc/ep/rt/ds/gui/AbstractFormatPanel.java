package pl.psnc.ep.rt.ds.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.swingx.JXTitledSeparator;

import pl.psnc.dlibra.app.extension.validator.ValidationResult;
import pl.psnc.dlibra.app.extension.validator.ValidationResult.ValidationResultType;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.validation.FileValidator;

public abstract class AbstractFormatPanel extends AbstractFilePanel {

    protected final WOMIFormat womiFormat;

    protected JXTitledSeparator separator;

    protected JButton sourcePreviewButton;

    protected JButton sourceClearButton;

    protected JPanel emissivePreviewPanel;

    protected JButton emissivePreviewButton;

    protected String remoteFileName;


    public AbstractFormatPanel(ResourceBundle textsBundle, WOMIFormat womiFormat) {
        super(textsBundle, new FileNameExtensionFilter(textsBundle.getString("fileFilter." + womiFormat.mediaFormat),
                womiFormat.mediaFormat.getFileExtensions()));
        this.womiFormat = womiFormat;

        layoutComponents();
        initGUITexts();
        addListeners();
    }


    protected void layoutComponents() {
        super.layoutComponents();

        if (womiFormat.mediaFormat.isRequired())
            sourcePathTextField.setBorder(BorderFactory.createLineBorder(Color.decode("#707070")));

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 5;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(separator = new JXTitledSeparator(), c);

        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 0;
        add(sourcePreviewButton = new JButton(), c);

        sourceClearButton = new JButton();
        if (!womiFormat.mediaFormat.isRequired()) {
            c.gridx++;
            add(sourceClearButton, c);
        }

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        emissivePreviewPanel = new JPanel();
        add(emissivePreviewPanel, c);

        emissivePreviewPanel.setLayout(new BoxLayout(emissivePreviewPanel, BoxLayout.LINE_AXIS));
        emissivePreviewPanel.add(emissivePreviewButton = new JButton());
    }


    protected void initGUITexts() {
        super.initGUITexts();
        sourcePreviewButton.setText(textsBundle.getString("file.source.preview"));
        sourceClearButton.setText(textsBundle.getString("file.source.clear"));
        emissivePreviewButton.setText(textsBundle.getString("file.emissive.preview"));
        separator.setTitle(textsBundle.getString("format." + womiFormat.id));
    }


    protected void addListeners() {
        super.addListeners();
        sourcePreviewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                changesListener.previewRequested(womiFormat, false);
            }
        });
        emissivePreviewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                changesListener.previewRequested(womiFormat, true);
            }
        });
        sourceClearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setSourceFile(null);
                setRemoteFileName(null);
                changesListener.sourceFilesChanged();
            }
        });
    }


    @Override
    protected void sourcePathTypedIn() {
        String newPath = sourcePathTextField.getText();
        if (sourceFileToText(sourceFile).equals(newPath)) {
            return;
        }
        if (newPath.equals(sourceFileToText(null))) {
            setSourceFile(null);
            return;
        }
        super.sourcePathTypedIn();
    }


    protected void refreshPanel() {
        sourcePathTextField.setText(sourceFileToText(sourceFile));
        sourceClearButton.setEnabled(sourceFile != null || remoteFileName != null);
    }


    private String sourceFileToText(File sourceFile) {
        if (sourceFile != null)
            return sourceFile.getAbsolutePath();
        else if (remoteFileName != null)
            return remoteFileName + textsBundle.getString("formatPanel.remoteFileSuffix");
        else
            return "";
    }


    public void setPreviewVisible(boolean emissiveVisible, boolean sourceVisible) {
        emissivePreviewButton.setVisible(emissiveVisible);
        sourcePreviewButton.setVisible(sourceVisible);
    }


    public void setPreviewEnabled(boolean emissiveEnabled, boolean sourceEnabled) {
        emissivePreviewButton.setEnabled(emissiveEnabled);
        sourcePreviewButton.setEnabled(sourceEnabled);
    }


    public void setModificationAllowed(boolean allowed) {
        super.setModificationAllowed(allowed);
        sourceClearButton.setEnabled(allowed);
    }


    protected boolean validateFile(File file) {
        if (!super.validateFile(file))
            return false;
        ValidationResult checkResult = FileValidator.checkFile(file, womiFormat.mediaFormat, textsBundle);
        if (checkResult != null && checkResult.getType() == ValidationResultType.ERROR) {
            JOptionPane.showMessageDialog(this, checkResult.getDescription(),
                textsBundle.getString("formatPanel.errorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }


    public void setRemoteFileName(String name) {
        remoteFileName = name;
        refreshPanel();
    }


    public String getRemoteFileName() {
        return remoteFileName;
    }


    public abstract boolean isFormatSelected();
}
