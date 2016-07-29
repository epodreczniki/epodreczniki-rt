package pl.psnc.ep.rt.ds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.mgmt.UnavailableServiceException;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.util.ImageConverter;

import com.kitfox.svg.app.beans.SVGIcon;

public class ImagePreview extends JDialog {

    private static class ImageView extends JPanel {

        private Image image;

        private String message;


        public void setImage(Image image) {
            this.image = image;
            if (image != null) {
                setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
                revalidate();
            }
            repaint();
        }


        public void setMessage(String message) {
            this.image = null;
            this.message = message;
            setPreferredSize(new Dimension(200, 200));
            revalidate();
            repaint();
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null)
                g.drawImage(image, (getWidth() - image.getWidth(null)) / 2, (getHeight() - image.getHeight(null)) / 2,
                    null);
            else {
                g.setColor(getForeground());
                int stringWidth = g.getFontMetrics().stringWidth(message);
                g.drawString(message, (getWidth() - stringWidth) / 2, getHeight() / 2);
            }
        }
    }


    private ImageView imageView;

    private JPanel resolutionSelectionPanel;

    private JComboBox resolutionSelectionCombo;

    private boolean isComboUpdateInProgress = false;

    private JLabel resolutionSelectionLabel;

    private SwingWorker<?, ?> imageLoader;

    private File currentFile;

    private EditionId currentEdition;

    private String currentFileDLibraPath;

    private TargetFormat currentTargetFormat;

    private boolean currentIs3D;

    private int currentResolution;

    private ResourceBundle textsBundle;


    public ImagePreview(Window parent, ResourceBundle textsBundle) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setSize(new Dimension(600, 600));
        setTitle(textsBundle.getString("imagePreview.title"));
        this.textsBundle = textsBundle;

        layoutComponents();
        initGUITexts();
        addListeners();
    }


    private void layoutComponents() {
        imageView = new ImageView();
        imageView.setBackground(Color.WHITE);
        imageView.setForeground(Color.BLACK);
        imageView.setFocusable(true);
        add(new JScrollPane(imageView), BorderLayout.CENTER);

        resolutionSelectionPanel = new JPanel();
        resolutionSelectionPanel.setLayout(new BoxLayout(resolutionSelectionPanel, BoxLayout.LINE_AXIS));
        resolutionSelectionPanel.add(Box.createHorizontalGlue());
        resolutionSelectionPanel.add(resolutionSelectionLabel = new JLabel());
        resolutionSelectionPanel.add(resolutionSelectionCombo = new JComboBox());
        resolutionSelectionPanel.add(Box.createHorizontalGlue());
        resolutionSelectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(resolutionSelectionPanel, BorderLayout.PAGE_START);
    }


    private void initGUITexts() {
        resolutionSelectionLabel.setText(textsBundle.getString("imagePreview.selectResolution"));
    }


    private void addListeners() {
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                imageView.setImage(null);
                if (imageLoader != null) {
                    imageLoader.cancel(true);
                    imageLoader = null;
                }
            }
        });

        resolutionSelectionCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isComboUpdateInProgress)
                    return;
                int selectedResolution = (Integer) resolutionSelectionCombo.getSelectedItem();
                if (currentResolution != selectedResolution)
                    showImage(selectedResolution);
            }
        });

        InputMap iMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        ActionMap aMap = getRootPane().getActionMap();
        aMap.put("escape", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }


    public void showLocal(final File imageFile, final TargetFormat targetFormat) {
        currentFile = imageFile;
        currentTargetFormat = targetFormat;
        int[] resolutions = updateResolutions();
        showImage(resolutions[0]);
    }


    private int[] updateResolutions() {
        int[] resolutions = currentTargetFormat != null ? currentTargetFormat.getResolutions() : new int[] { 0 };
        if (resolutions.length == 1) {
            resolutionSelectionPanel.setVisible(false);
        } else {
            isComboUpdateInProgress = true;
            DefaultComboBoxModel model = (DefaultComboBoxModel) resolutionSelectionCombo.getModel();
            model.removeAllElements();
            for (int res : resolutions) {
                model.addElement(res);
            }
            model.setSelectedItem(resolutions[0]);
            isComboUpdateInProgress = false;
            resolutionSelectionPanel.setVisible(true);
        }
        return resolutions;
    }


    public void showEmissive(EditionId editionId, WOMIFormat format) {
        currentFile = null;
        currentTargetFormat = format.targetFormat;
        currentIs3D = format.is3D;
        currentEdition = editionId;
        int[] resolutions = updateResolutions();
        showImage(resolutions[0]);
    }


    public void showSource(EditionId editionId, String dLibraPath) {
        currentFile = null;
        currentTargetFormat = null;
        currentEdition = editionId;
        currentFileDLibraPath = dLibraPath;
        updateResolutions();
        showImage(0);
    }


    private void showImage(final int resolution) {
        if (imageLoader != null)
            imageLoader.cancel(true);

        imageLoader = new SwingWorker<Image, Object>() {

            @Override
            protected Image doInBackground()
                    throws Exception {
                if (currentFile != null) {
                    if (currentTargetFormat == null)
                        return ImageIO.read(currentFile);
                    if (currentFile.getName().endsWith(".svg")) {
                        return loadSVGImage(currentFile.toURI(), resolution);
                    }
                    return ImageConverter.getImage(currentFile, currentTargetFormat, resolution);
                }

                if (currentTargetFormat != null)
                    return downloadEmissiveFormat(resolution);

                return downloadSourceImage();
            }


            @Override
            protected void done() {
                imageLoader = null;
                if (!isVisible() || resolution != currentResolution)
                    return;
                try {
                    Image image = get();
                    imageView.setImage(image);
                } catch (InterruptedException e) {
                    throw new RuntimeException("SwingWorker thread interrupted unintentionally.");
                } catch (CancellationException e) {
                } catch (ExecutionException e) {
                    imageView.setMessage(
                        String.format(textsBundle.getString("imagePreview.message.error"), e.getCause().getMessage()));
                }
            }
        };
        imageLoader.execute();
        currentResolution = resolution;
        imageView.setMessage(textsBundle.getString("imagePreview.message.wait"));
        setLocationRelativeTo(getParent());
        setVisible(true);
    }


    private Image downloadEmissiveFormat(int resolution)
            throws IOException {
        URI uri = URI.create(ApplicationContext.getInstance().getReaderURL());
        StringBuilder path = new StringBuilder("womi/").append(currentEdition);
        path.append("/").append(MediaFormat.IMAGE.name().toLowerCase());
        path.append("/").append(currentTargetFormat.name().toLowerCase());
        if (resolution != 0)
            path.append("?res=").append(resolution);
        if (currentIs3D)
            path.append(resolution != 0 ? "&" : "?").append("3D");
        uri = uri.resolve(path.toString());

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        if ("image/svg+xml".equals(connection.getContentType())) {
            return loadSVGImage(uri, resolution);
        }

        Image image = ImageIO.read(connection.getInputStream());
        return image;
    }


    private Image downloadSourceImage()
            throws IdNotFoundException, RemoteException, DLibraException, UnavailableServiceException,
            AccessDeniedException, IOException {
        ApplicationContext ac = ApplicationContext.getInstance();
        VersionId versionId = (VersionId) ac.getEventMetadataServer().getFileManager()
                .getObjects(new FileFilter(currentFileDLibraPath).setEditionId(currentEdition),
                    new OutputFilter(VersionId.class))
                .getResultId();
        ContentServer cs = (ContentServer) ac.getUserServiceResolver().getService(ContentServer.SERVICE_TYPE, null);
        InputStream inputStream = cs.getVersionInputStream(versionId);
        try {
            return ImageIO.read(inputStream);
        } finally {
            inputStream.close();
            cs.releaseElement(versionId);
        }
    }


    private Image loadSVGImage(URI uri, int resolution) {
        SVGIcon icon = new SVGIcon();
        icon.setSvgURI(uri);

        Dimension size = icon.getPreferredSize();
        double scale = resolution / size.getWidth();
        size.setSize(size.getWidth() * scale, size.getHeight() * scale);
        icon.setPreferredSize(size);

        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        icon.setAntiAlias(true);
        icon.setScaleToFit(true);
        icon.paintIcon(imageView, image.getGraphics(), 0, 0);
        return image;
    }
}
