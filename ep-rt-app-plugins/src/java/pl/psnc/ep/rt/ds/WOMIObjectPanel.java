package pl.psnc.ep.rt.ds;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.eventserver.metadata.manager.EventPublicationManager;
import pl.psnc.dlibra.app.extension.objectpanel.ObjectPanel;
import pl.psnc.dlibra.app.extension.objectpanel.PanelContext;
import pl.psnc.dlibra.app.gui.common.iconcache.GraphicsManager;
import pl.psnc.dlibra.app.operationsqueue.manager.OperationsManager;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.ElementInfo;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.user.PublicationRightId;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.Util;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.ds.gui.WOMIDefinitionPanel;
import pl.psnc.ep.rt.ds.gui.WOMIDefinitionPanel.ChangesListener;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

public class WOMIObjectPanel implements ObjectPanel, ChangesListener {

    private static final String BUNDLE_PATH = "GUI";

    private final static Logger logger = Logger.getLogger(GraphicsDataSource.class);

    private ResourceBundle textsBundle;

    private ElementId selectedObjectCache;

    private EditionId editionId;

    private boolean isEditionAllowed;

    private WOMIDefinitionPanel panel;

    private JLabel datesLabel;

    private PanelContext context;

    private JButton sendLaterButton;

    private Map<WOMIFormat, String> fileNames;

    private Map<WOMIFormat, java.io.File> sourceFiles;


    @Override
    public void initialize(Map<String, Object> initPrefs) {
        textsBundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault());
    }


    @Override
    public String getTabTitle(ElementInfo selectedObject) {
        if (findObjectData(selectedObject.getId()))
            return textsBundle.getString("objectPanel.title");
        return null;
    }


    @Override
    public JPanel getPanel(PanelContext context) {
        sourceFiles = null;
        if (!findObjectData(context.getPresentedObject().getId())) {
            return panel = null;
        }

        this.context = context;
        try {
            fileNames = Util.findFileNames(editionId);
        } catch (DLibraException e) {
            logger.error("WOMI reading error", e);
            return panel = null;
        } catch (IOException e) {
            logger.error("WOMI reading error", e);
            return panel = null;
        }
        WOMIType womiType = null;
        for (WOMIFormat format : fileNames.keySet()) {
            if (format == WOMIXMLHandler.STATIC_ALTERNATIVE_FORMAT)
                continue;
            if (womiType == null)
                womiType = format.womiType;
            if (womiType != format.womiType)
                throw new RuntimeException("Inconsistent WOMI types in object: " + womiType + ", " + format.womiType);
        }

        if (panel == null || panel.getWomiType() != womiType) {
            panel = new WOMIDefinitionPanel(womiType, textsBundle);
            createAdditionalComponents(womiType);
            updatePreviewVisibility();
        }

        checkRight();

        panel.setRemoteFileNames(fileNames);
        panel.setEditionId(editionId);
        panel.setChangesListener(this);
        sendLaterButton.setEnabled(false);
        updateDates();
        return panel;
    }


    private void createAdditionalComponents(final WOMIType womiType) {
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.ipady = 10;
        c.insets = new Insets(5, 10, 1, 10);
        c.fill = GridBagConstraints.BOTH;
        panel.add(buttonsPanel, c);

        datesLabel = new JLabel();
        c.gridy++;
        panel.add(datesLabel, c);

        JButton referenceButton = new JButton(textsBundle.getString("objectPanel.referenceButton"));
        referenceButton.setIcon(GraphicsManager.getIconFromResource("attr-table.png"));
        referenceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                WOMIReferenceCreator.copyToClipboard(editionId, womiType);
            }
        });
        c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        buttonsPanel.add(referenceButton, c);

        sendLaterButton = new JButton(textsBundle.getString("objectPanel.sendLaterButton"));
        sendLaterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveUpdateOperation();
            }
        });
        c.weightx = 0.125;
        buttonsPanel.add(sendLaterButton);
    }


    void saveUpdateOperation() {
        try {
            OperationsManager.getInstance().addOperation(new UpdateWOMIOperation(sourceFiles, editionId, fileNames));
            cancelChanges();
            context.changesCancelled();
        } catch (Exception e) {
            logger.error("Problem while saving update WOMI operation", e);
            JOptionPane.showMessageDialog(panel,
                String.format(textsBundle.getString("objectPanel.sendLaterError"), e.getMessage()));
        }
    }


    private void updatePreviewVisibility() {
        WOMIFormat format = new WOMIFormat(WOMIType.MOVIE, MediaFormat.VIDEO, TargetFormat.CLASSIC, false);
        panel.setPreviewVisible(format, true, false);
        format = new WOMIFormat(WOMIType.MOVIE, MediaFormat.VIDEO, TargetFormat.CLASSIC, true);
        panel.setPreviewVisible(format, true, false);
        format = new WOMIFormat(WOMIType.SOUND, MediaFormat.AUDIO, TargetFormat.CLASSIC, false);
        panel.setPreviewVisible(format, true, false);
    }


    private void checkRight() {
        ApplicationContext ac = ApplicationContext.getInstance();
        try {
            PublicationId publicationId = (PublicationId) ac.getEventMetadataServer().getPublicationManager()
                    .getObjects(new EditionFilter(editionId), new OutputFilter(PublicationId.class)).getResultId();
            isEditionAllowed = ac.getEventUserServer().getRightManager().checkPublicationRight(publicationId,
                ac.getLoggedUser().getId(), PublicationRightId.PUBLICATION_EDIT);
            panel.setModificationAllowed(isEditionAllowed);
        } catch (RemoteException e) {
            throw new RuntimeException("Server communication error", e);
        } catch (DLibraException e) {
            throw new RuntimeException("Server communication error", e);
        }
    }


    private void updateDates()

    {
        try {
            EventPublicationManager pm = ApplicationContext.getInstance().getEventMetadataServer()
                    .getPublicationManager();
            Edition edition = (Edition) pm.getObjects(new EditionFilter(editionId), new OutputFilter(Edition.class))
                    .getResult();
            datesLabel.setText(String.format(textsBundle.getString("objectPanel.datesLabel.text"),
                edition.getCreationDate(), edition.getCreatedBy().getLabel(), edition.getModificationTime(),
                edition.getLastModifiedBy().getLabel(), edition.getId()));
        } catch (Exception e) {
            logger.error("Could not get dates information", e);
            datesLabel.setText("??");
        }
    }


    @Override
    public void filesChanged(Map<WOMIFormat, java.io.File> sourceFiles) {
        if (!isEditionAllowed)
            return;
        if (sourceFiles != null)
            context.contentChanged();
        this.sourceFiles = sourceFiles;
        sendLaterButton.setEnabled(true);
    }


    @Override
    public void applyChanges() {
        if (sourceFiles == null || sourceFiles.isEmpty())
            return;

        try {
            new UpdateWOMIOperation(sourceFiles, editionId, fileNames).run();
        } catch (IOException e) {
            throw new RuntimeException("IO error", e);
        } catch (Exception e) {
            throw new RuntimeException("Server communication error", e);
        }

        sendLaterButton.setEnabled(false);
        sourceFiles = null;
    }


    @Override
    public void cancelChanges() {
        if (panel != null && editionId != null)
            panel.setRemoteFileNames(fileNames);
        sourceFiles = null;
        if (sendLaterButton != null)
            sendLaterButton.setEnabled(false);
    }


    @Override
    public void updateLanguage() {
    }


    private boolean findObjectData(ElementId elementId) {
        if (elementId.equals(selectedObjectCache)) {
            return true;
        }

        this.editionId = null;
        selectedObjectCache = null;
        try {
            EditionId editionId;
            if (elementId instanceof EditionId) {
                editionId = (EditionId) elementId;
            } else if (elementId instanceof PublicationId) {
                editionId = findEditionId((PublicationId) elementId);
                if (editionId == null)
                    return false;
            } else
                return false;

            File mainFile = findMainFile(editionId);
            if (mainFile.getPath().equals("/" + WOMIXMLHandler.MAIN_FILE_NAME)) {
                selectedObjectCache = elementId;
                this.editionId = editionId;
                return true;
            }
        } catch (Exception e) {
            logger.error("Could not retrieve needed information", e);
        }
        return false;
    }


    private EditionId findEditionId(PublicationId id)
            throws RemoteException, DLibraException {
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<EditionId> editionIds = (List) ms.getPublicationManager()
                .getObjects(new PublicationFilter().setParentIds(Arrays.asList(id)), new OutputFilter(EditionId.class))
                .getResultIds();
        if (editionIds.size() != 1)
            return null;
        return editionIds.get(0);
    }


    private File findMainFile(EditionId editionId)
            throws RemoteException, DLibraException {
        MetadataServer ms = ApplicationContext.getInstance().getEventMetadataServer();
        Publication publication = (Publication) ms.getPublicationManager()
                .getObjects(new EditionFilter(editionId), new OutputFilter(Publication.class)).getResult();
        FileId mainFileId = publication.getMainFileId();
        File file = (File) ms.getFileManager().getObjects(new FileFilter(mainFileId), new OutputFilter(File.class))
                .getResult();
        return file;
    }
}
