package pl.psnc.ep.rt.ds.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;

import javax.swing.FocusManager;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.ExceptionHandler;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.server.EPService.AVInfo;

public class AudioVideoPreview {

    private static final String PREVIEW_URL = "/womi-selection?id=";

    private static final Logger logger = Logger.getLogger(AudioVideoPreview.class);

    private ResourceBundle textsBundle;


    public AudioVideoPreview(ResourceBundle textsBundle) {
        this.textsBundle = textsBundle;
    }


    public void showPreview(EditionId womiId) {
        AVInfo avInfo = null;
        boolean isError = false;
        try {
            EPService ep = (EPService) ApplicationContext.getInstance().getUserServiceResolver()
                    .getService(EPService.SERVICE_TYPE, null);
            avInfo = ep.getAVInfo(womiId);
        } catch (Exception e) {
            logger.error("Could not get audio/video material status", e);
            isError = true;
        }
        if (avInfo != null) {
            String uri = ApplicationContext.getInstance().getReaderURL() + PREVIEW_URL + womiId;
            try {
                Desktop.getDesktop().browse(URI.create(uri));
            } catch (IOException e) {
                ExceptionHandler.getInstance().handleException(e, FocusManager.getCurrentManager().getActiveWindow());
            }
        } else {
            String message = textsBundle.getString(isError ? "audioVideoPreview.error" : "audioVideoPreview.recoding");
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(), message,
                textsBundle.getString("audioVideoPreview.title"), JOptionPane.WARNING_MESSAGE);
        }
    }
}
