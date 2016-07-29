package pl.psnc.ep.rt.ds.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StaticAlternativePanel extends AbstractFilePanel {

    static final String ALTERNATIVES_FILE = "ALTERNATIVES.zip";

    private JLabel sourceFileLabel;

    private JCheckBox staticAlternativeCheckBox;

    private boolean remoteFileExists;


    public StaticAlternativePanel(ResourceBundle textsBundle) {
        super(textsBundle,
                new FileNameExtensionFilter(textsBundle.getString("fileFilter.PACKAGE"), new String[] { "ZIP" }));

        layoutComponents();
        initGUITexts();
        addListeners();
    }


    @Override
    protected void refreshPanel() {
        if (sourceFile != null)
            sourcePathTextField.setText(sourceFile.getAbsolutePath());
        else if (remoteFileExists)
            sourcePathTextField.setText(ALTERNATIVES_FILE + textsBundle.getString("formatPanel.remoteFileSuffix"));

        boolean showFile = sourceFile != null || remoteFileExists;
        staticAlternativeCheckBox.setSelected(showFile);
        setSourceSelectionVisible(showFile);
    }


    @Override
    protected void layoutComponents() {
        super.layoutComponents();

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 5;
        c.weightx = 1;
        c.gridx = c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(staticAlternativeCheckBox = new JCheckBox(), c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(5, 5, 5, 5);
        add(sourceFileLabel = new JLabel(), c);
    }


    @Override
    protected void initGUITexts() {
        super.initGUITexts();
        sourceFileLabel.setText(textsBundle.getString("source.file.label"));
        staticAlternativeCheckBox.setText(textsBundle.getString("datasource.staticAlternative"));
    }


    @Override
    protected void addListeners() {
        super.addListeners();
        staticAlternativeCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = staticAlternativeCheckBox.isSelected();
                setSourceSelectionVisible(selected);
                if (selected && getSourceFile() == null && !remoteFileExists) {
                    selectFile();
                } else {
                    if (changesListener != null) {
                        changesListener.sourceFilesChanged();
                    }
                }
            }
        });
    }


    @Override
    public void setModificationAllowed(boolean allowed) {
        super.setModificationAllowed(allowed);
        staticAlternativeCheckBox.setEnabled(allowed);
    }


    public void setRemoteFile(boolean exists) {
        remoteFileExists = exists;
        refreshPanel();
    }


    public boolean isRemoteFileSet() {
        return remoteFileExists;
    }


    public boolean isSelected() {
        return staticAlternativeCheckBox.isSelected();
    }


    private void setSourceSelectionVisible(boolean visible) {
        sourceFileLabel.setVisible(visible);
        sourcePathTextField.setVisible(visible);
        sourceSelectButton.setVisible(visible);
    }
}
