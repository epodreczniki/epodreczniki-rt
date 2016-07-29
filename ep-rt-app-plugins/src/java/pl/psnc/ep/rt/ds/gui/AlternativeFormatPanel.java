package pl.psnc.ep.rt.ds.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;

import pl.psnc.ep.rt.WOMIFormat;

public class AlternativeFormatPanel extends AbstractFormatPanel {

    private JCheckBox overrideSourceCheckBox;


    public AlternativeFormatPanel(ResourceBundle textsBundle, WOMIFormat womiFormat) {
        super(textsBundle, womiFormat);
    }


    @Override
    protected void layoutComponents() {
        super.layoutComponents();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(5, 5, 5, 5);
        add(overrideSourceCheckBox = new JCheckBox(), c);
    }


    @Override
    protected void initGUITexts() {
        super.initGUITexts();
        overrideSourceCheckBox.setText(textsBundle.getString("source.file.override"));
    }


    @Override
    protected void addListeners() {
        super.addListeners();
        overrideSourceCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean override = overrideSourceCheckBox.isSelected();
                setSourceSelectionVisible(override);
                if (override && getSourceFile() == null) {
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
    protected void refreshPanel() {
        super.refreshPanel();
        boolean showFile = sourceFile != null || remoteFileName != null;
        overrideSourceCheckBox.setSelected(showFile);
        setSourceSelectionVisible(showFile);
    }


    private void setSourceSelectionVisible(boolean visible) {
        sourcePathTextField.setVisible(visible);
        sourceSelectButton.setVisible(visible);
        sourcePreviewButton.setVisible(visible);
    }


    @Override
    public boolean isFormatSelected() {
        return overrideSourceCheckBox.isSelected();
    }


    @Override
    public void setModificationAllowed(boolean allowed) {
        super.setModificationAllowed(allowed);
        overrideSourceCheckBox.setEnabled(allowed);
    }
}
