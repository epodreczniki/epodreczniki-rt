package pl.psnc.ep.rt.ds.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JLabel;

import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.util.ImageConverter;

public class DefaultFormatPanel extends AbstractFormatPanel {

    private JLabel sourceFileLabel;

    private JLabel getSoftwareLabel;


    public DefaultFormatPanel(ResourceBundle textsBundle, WOMIFormat womiFormat) {
        super(textsBundle, womiFormat);
    }


    @Override
    protected void layoutComponents() {
        super.layoutComponents();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(5, 5, 5, 5);
        add(sourceFileLabel = new JLabel(), c);

        if (womiFormat.mediaFormat == MediaFormat.IMAGE) {
            emissivePreviewPanel.add(Box.createHorizontalStrut(5));
            emissivePreviewPanel.add(getSoftwareLabel = new JLabel());
        }
    }


    @Override
    protected void initGUITexts() {
        super.initGUITexts();
        sourceFileLabel.setText(textsBundle.getString("source.file.label"));
        if (getSoftwareLabel != null) {
            getSoftwareLabel.setText(
                "<html><font color=\"#0000CF\"><u>" + textsBundle.getString("software.get") + "</u></font></html>");
            getSoftwareLabel.setToolTipText(textsBundle.getString("software.tooltip"));
        }
    }


    @Override
    protected void addListeners() {
        super.addListeners();

        if (getSoftwareLabel == null)
            return;
        getSoftwareLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getSoftwareLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(textsBundle.getString("software.url")));
                } catch (Exception ex) {
                    logger.warn("Could not open browser with software url ", ex);
                }
            }
        });
    }


    @Override
    public boolean isFormatSelected() {
        return true;
    }


    @Override
    public void setPreviewEnabled(boolean emissiveEnabled, boolean sourceEnabled) {
        super.setPreviewEnabled(emissiveEnabled, sourceEnabled);
        if (getSoftwareLabel != null)
            getSoftwareLabel.setVisible(!emissiveEnabled && !ImageConverter.isEnabled());
    }
}
