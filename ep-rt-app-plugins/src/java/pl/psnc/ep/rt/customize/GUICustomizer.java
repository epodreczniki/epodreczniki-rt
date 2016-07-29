package pl.psnc.ep.rt.customize;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.Bootstrap;
import pl.psnc.dlibra.app.ExceptionHandler;
import pl.psnc.dlibra.app.ResourceManager;
import pl.psnc.dlibra.app.common.PublicationFilesInfo;
import pl.psnc.dlibra.app.common.VersionAppInfo;
import pl.psnc.dlibra.app.common.langstab.LanguagesTabbedPane;
import pl.psnc.dlibra.app.eventserver.metadata.EventMetadataServer;
import pl.psnc.dlibra.app.eventserver.metadata.manager.EventDirectoryManager;
import pl.psnc.dlibra.app.eventserver.metadata.manager.EventElementMetadataManager;
import pl.psnc.dlibra.app.eventserver.metadata.manager.EventFileManager;
import pl.psnc.dlibra.app.eventserver.metadata.manager.EventPublicationManager;
import pl.psnc.dlibra.app.eventserver.user.EventUserServer;
import pl.psnc.dlibra.app.gui.BootstrapSplashWindow;
import pl.psnc.dlibra.app.gui.common.LanguagesMenu;
import pl.psnc.dlibra.app.gui.common.autocompletetextfield.AutoCompleteConstrainedTextArea;
import pl.psnc.dlibra.app.gui.editor.attributesvalueseditor.attributesviews.AttributesView;
import pl.psnc.dlibra.app.gui.editor.attributesvalueseditor.attributesviews.AttributesViewsManager;
import pl.psnc.dlibra.app.gui.editor.attributesvalueseditor.attributesviews.AttributesViewsModel;
import pl.psnc.dlibra.app.gui.editor.attributesvalueseditor.treetable.AttributesValuesTreeTable;
import pl.psnc.dlibra.app.gui.editor.pubcolseditor.EditorPubDirLibCollectionsWindow;
import pl.psnc.dlibra.app.gui.editor.publicationbrowser.PublicationBrowser;
import pl.psnc.dlibra.app.gui.editor.publicationbrowser.list.ElementsTable;
import pl.psnc.dlibra.app.gui.editor.rightseditor.EditorPubDirRightsEditor;
import pl.psnc.dlibra.app.gui.editor.search.SearchAction;
import pl.psnc.dlibra.app.gui.login.LoginWindow;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryInfo;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.EditionInfo;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.ElementInfo;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.GroupPublicationInfo;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.ElementMetadataManager;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.multipubuploader.transformer.AbstractPropertiesToPublicationTransformer;
import pl.psnc.dlibra.multipubuploader.wizard.MultiPubUploadManager;
import pl.psnc.dlibra.multipubuploader.wizard.MultiPubUploadProgressMonitor;
import pl.psnc.dlibra.multipubuploader.wizard.MultiPublicationUploadSelectPage;
import pl.psnc.dlibra.multipubuploader.wizard.TransformerPublicationsTree;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserId;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.gui.wizard.JWizard;

public class GUICustomizer {

    private static final int ATTRIBUTE_VALUE_LENGTH_LIMIT = 16000;

    private static final String[][] COMPONENTS_TO_HIDE = { { "mainMenuBar", "connectionMenuItem" },
            { "mainMenuBar", "startupScreenMenuItem" }, { "mainMenuBar", "configurationMenuItem" },
            { "mainMenuBar", "advancedModeMenuItem" }, { "mainMenuBar", "managementMenu" },
            { "mainMenuBar", "pubCreatorMenuItem" }, { "mainMenuBar", "createPubPropertiesFile" },
            { "mainMenuBar", "tagsManager" }, { "mainMenuBar", "messagesPresenter" }, { "mainMenuBar", "helpMenu" },
            { "mainToolBar", "newGroupPublicationButton" }, { "mainToolBar", "newPlannedPublicationButton" },
            { "mainToolBar", "newEditionButton" }, { "mainToolBar", "lightDelButton" },
            { "mainToolBar", "connectionButton" }, { "elementsPanel", "hideShowPlanned" },
            { "elementsPanel", "hideShowDeleted" }, { "secondElementsPanel", "hideShowPlanned" },
            { "secondElementsPanel", "hideShowDeleted" },
            { "editorsPanel", "editorPubDirProperties", "avEditor", "importPanel" },
            { "editorsPanel", "editorPubDirProperties", "avEditor", "toolBar", "addSynonyms" },
            { "editorsPanel", "editorPubDirProperties", "avEditor", "toolBar", "showWithInherited" },
            { "editorsPanel", "editorPubDirProperties", "avEditor", "toolBar", "showHTMLAVSButton" },
            { "editorsPanel", "editorPubDirProperties", "avEditor", "toolBar", "copyInherited" },
            { "editorsPanel", "editorPubDirProperties", "illustratedElementWwwInfoEditor", "multilinTabbedPane" },
            { "newPublicationWizard", "newPublicationAttributesPage", "avEditor", "importPanel" },
            { "newPublicationWizard", "newPublicationAttributesPage", "avEditor", "toolBar", "addSynonyms" },
            { "newPublicationWizard", "newPublicationAttributesPage", "avEditor", "toolBar", "showWithInherited" },
            { "newPublicationWizard", "newPublicationAttributesPage", "avEditor", "toolBar", "showHTMLAVSButton" },
            { "newPublicationWizard", "newPublicationAttributesPage", "avEditor", "toolBar", "copyInherited" },
            { "newPublicationWizard", "newPublicationAttributesPage", "avEditor", "toolBar", "checkDuplicates" },
            { "newPublicationWizard", "newPublicationWWWInfoPage", "illustratedElementWWWInfoEditor",
                    "multilinTabbedPane" },
            { "newPublicationWizard", "newPublicationWWWInfoPage", "illustratedElementWWWInfoEditor", "plusButton" },
            { "newPublicationWizard", "newPublicationWWWInfoPage", "illustratedElementWWWInfoEditor", "minusButton" },
            { "newPublicationWizard", "defaultWizardNavigationPanel", "helpButton" },
            { "newPublicationWizard", "menuBar" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "avEditor", "importPanel" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "avEditor", "toolBar", "addSynonyms" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "avEditor", "toolBar", "showWithInherited" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "avEditor", "toolBar", "showHTMLAVSButton" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "avEditor", "toolBar", "copyInherited" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "avEditor", "toolBar", "checkDuplicates" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "mainPublicationFilePathLabel" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationAttributesPage",
                    "mainPublicationFilePath" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationWWWInfoPage",
                    "illustratedElementWWWInfoEditor", "multilinTabbedPane" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationWWWInfoPage",
                    "illustratedElementWWWInfoEditor", "plusButton" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationWWWInfoPage",
                    "illustratedElementWWWInfoEditor", "minusButton" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationWWWInfoPage",
                    "mainPublicationFilePathLabel" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "newPublicationWWWInfoPage",
                    "mainPublicationFilePath" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "defaultWizardNavigationPanel",
                    "helpButton" },
            { "sendPublicationsAction", "operationsManagerJDialog", "editPubAL", "npw", "menuBar" },
            { "multiPulblicationUploadWizard", "menuBar" }, { "newDirectoryWizard", "menuBar" },
            { "newDirectoryWizard", "attrPage", "avEditor", "importPanel" },
            { "newDirectoryWizard", "attrPage", "avEditor", "toolBar", "addSynonyms" },
            { "newDirectoryWizard", "attrPage", "avEditor", "toolBar", "showWithInherited" },
            { "newDirectoryWizard", "attrPage", "avEditor", "toolBar", "showHTMLAVSButton" },
            { "newDirectoryWizard", "attrPage", "avEditor", "toolBar", "copyInherited" },
            { "newDirectoryWizard", "attrPage", "avEditor", "toolBar", "checkDuplicates" }, { "helpToggleButton" }, };

    private static final Map<Class<?>, String[]> CONTEXT_MENUS_TO_HIDE = new HashMap<Class<?>, String[]>() {

        {
            put(DirectoryInfo.class, new String[] { "newGroupPublication", "newPlannedPublication" });
            put(GroupPublicationInfo.class,
                new String[] { "newMidPublication", "newLeafPublication", "newPlannedPublication" });
            put(PublicationInfo.class, new String[] { "lightDelete", "changeFiles", "newEdition" });
            put(EditionInfo.class, new String[] { "newEdition" });
            put(PublicationFilesInfo.class, new String[] { "addNewFiles", "deleteRedundant" });
            put(VersionAppInfo.class, new String[] { "changeVersion" });
        }
    };

    private static final Logger logger = Logger.getLogger(GUICustomizer.class);

    private ResourceBundle textsBundle;


    public void init(ResourceBundle textsBundle) {
        this.textsBundle = textsBundle;

        final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener("activeWindow", new PropertyChangeListener() {

            private boolean customizedSplash, customizedLogin, customizedBrowser;


            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Object source = evt.getNewValue();
                if (source instanceof BootstrapSplashWindow && !customizedSplash) {
                    customizeSplash((BootstrapSplashWindow) source);
                    customizedSplash = true;
                } else if (source instanceof LoginWindow && !customizedLogin) {
                    customizeLogin((LoginWindow) source);
                    enforcePolishLocale((LoginWindow) source);
                    customizedLogin = true;
                } else if (source instanceof PublicationBrowser && !customizedBrowser) {
                    focusManager.removePropertyChangeListener("activeWindow", this);
                    customizeBrowser((PublicationBrowser) source);
                    customizedBrowser = true;
                }
            }
        });
    }


    private void customizeSplash(final BootstrapSplashWindow window) {
        Component adminButton = (Component) getFieldValue(window, "splashPanel", "lmButton");
        adminButton.setVisible(false);
        final Bootstrap bootstrap = (Bootstrap) getFieldValue(window, "splashPanel", "bootstrap");
        final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Z && e.isAltDown() && window.isVisible()) {
                    bootstrap.launchLibraryManager();
                }
                return false;
            }
        });
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                bootstrap.launchPublicationBrowser();
                window.addComponentListener(new ComponentAdapter() {

                    @Override
                    public void componentShown(ComponentEvent e) {
                        int confirm = JOptionPane.showConfirmDialog(window,
                            textsBundle.getString("close.confirm.message"),
                            textsBundle.getString("close.confirm.title"), JOptionPane.OK_CANCEL_OPTION);
                        if (confirm == JOptionPane.OK_OPTION) {
                            bootstrap.close();
                        } else {
                            bootstrap.launchPublicationBrowser();
                            window.setVisible(false);
                        }
                    }
                });
            }
        });
    }


    private void customizeLogin(LoginWindow window) {
        Component helpMenu = (Component) getFieldValue(window, "helpMenu");
        helpMenu.setVisible(false);
    }


    private void enforcePolishLocale(LoginWindow loginWindow) {
        List<ResourceBundle> bundles = ResourceManager.getInstance().getAvailableBundles();
        for (ResourceBundle bundle : bundles) {
            if (bundle.getLocale().equals(new Locale("pl"))) {
                ApplicationContext.setDefaultLocale(bundle.getLocale());
                Object languagesMenu = getFieldValue(loginWindow, "languagesMenu");
                Method frbcMethod;
                try {
                    frbcMethod = LanguagesMenu.class.getDeclaredMethod("fireResourceBundleChanged",
                        ResourceBundle.class);
                    frbcMethod.setAccessible(true);
                    frbcMethod.invoke(languagesMenu, bundle);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
        throw new RuntimeException("Polish bundle not found!");
    }


    private void customizeBrowser(PublicationBrowser browser) {
        hideThings(browser);
        hideContextMenus(browser);
        hideEditors(browser);
        hideAttributeViewSelection(browser);
        handleMultiPubUpload(browser);
        changeSearchFeature(browser);
        changeAttributeValueLengthLimit(browser);
        handleVersioning(browser);
    }


    private void hideThings(PublicationBrowser browser) {
        ComponentListener hideEnforcer = new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                e.getComponent().setVisible(false);
            }


            @Override
            public void componentResized(ComponentEvent e) {
                e.getComponent().setVisible(false);
            }
        };

        for (String[] fields : COMPONENTS_TO_HIDE) {
            try {
                Component component = (Component) getFieldValue(browser, fields);
                component.setVisible(false);
                component.addComponentListener(hideEnforcer);
            } catch (Exception e) {
                logger.error("Could not hide field: " + fields, e);
            }
        }
    }


    private void hideContextMenus(PublicationBrowser browser) {
        Map<?, ?>[] handlersMaps = new Map<?, ?>[2];
        try {
            handlersMaps[0] = (Map<?, ?>) getFieldValue(browser, "elementHandlers");
            handlersMaps[1] = (Map<?, ?>) getFieldValue(browser, "directoriesTree", "libraryTree", "nodeTypeHandlers");
        } catch (Exception e) {
            logger.error("Could not get handlers map", e);
            return;
        }
        for (Map<?, ?> handlersMap : handlersMaps) {
            for (Map.Entry<Class<?>, String[]> entry : CONTEXT_MENUS_TO_HIDE.entrySet()) {
                Object handler = handlersMap.get(entry.getKey());
                if (handler == null) {
                    logger.error("No handler for " + entry.getKey());
                    continue;
                }
                for (String fieldName : entry.getValue()) {
                    try {
                        Component c = (Component) getFieldValue(handler, fieldName);
                        c.setVisible(false);
                    } catch (Exception e) {
                        logger.error("Could not hide " + fieldName + " in " + handler);
                    }
                }
            }
        }
    }


    private void hideEditors(PublicationBrowser browser) {
        final JTabbedPane editorsPane;
        final Object editionGeneralInfo;
        try {
            editorsPane = (JTabbedPane) getFieldValue(browser, "editorsPanel", "editorPubDirProperties",
                "propertiesTabbedPane");
            editionGeneralInfo = getFieldValue(browser, "editorsPanel", "editorPubDirProperties",
                "editorPubDirGeneralInfoSecond");
        } catch (Exception e) {
            logger.error("Could not get editors tabbed pane", e);
            return;
        }
        editorsPane.addContainerListener(new ContainerAdapter() {

            private Runnable tabsRemover = new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < editorsPane.getTabCount(); i++) {
                        Component tab = editorsPane.getComponentAt(i);
                        if (tab instanceof EditorPubDirRightsEditor || tab instanceof EditorPubDirLibCollectionsWindow
                                || tab == editionGeneralInfo) {
                            editorsPane.removeTabAt(i);
                            i--;
                        }
                    }
                }
            };


            @Override
            public void componentAdded(ContainerEvent e) {
                SwingUtilities.invokeLater(tabsRemover);
            }
        });
    }


    private void hideAttributeViewSelection(PublicationBrowser browser) {
        Object avEditor = getFieldValue(browser, "newPublicationWizard", "newPublicationAttributesPage", "avEditor");
        Component viewsLabel = (Component) getFieldValue(avEditor, "attributesViewsLabel");
        viewsLabel.setVisible(false);
        Container parent = viewsLabel.getParent();
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component component = parent.getComponent(i);
            if (component instanceof JComboBox) {
                component.setVisible(false);
                break;
            }
        }

        Component wizard = (Component) getFieldValue(browser, "newPublicationWizard");
        wizard.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                AttributesViewsModel model = AttributesViewsManager.getInstance().getComboBoxModel();
                for (int i = 0; i < model.getSize(); i++) {
                    AttributesView view = (AttributesView) model.getElementAt(i);
                    if (view.toString().contains("WOMI")) {
                        model.setSelectedItem(view);
                        break;
                    }
                }
            }
        });
    }


    private void handleMultiPubUpload(final PublicationBrowser browser) {
        final JButton nextButton = (JButton) getFieldValue(browser, "multiPulblicationUploadWizard",
            "wizardNavigationPanel", "nextButton");
        nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DirectoryId selectedDirId = null;
                ElementsTable elementsTable = browser.getFocusedPanel().getElementsTable();
                List<ElementInfo> path = elementsTable.getOpenPathElements();
                ElementInfo openElement = path.get(path.size() - 1);
                if (openElement instanceof DirectoryInfo)
                    selectedDirId = ((DirectoryInfo) openElement).getId();
                List<ElementInfo> selected = elementsTable.getSelectedValues();
                if (selected != null && selected.size() == 1 && selected.get(0) instanceof DirectoryInfo)
                    selectedDirId = ((DirectoryInfo) selected.get(0)).getId();
                if (selectedDirId == null)
                    return;

                List<?> transformers = (List<?>) getFieldValue(browser, "multiPulblicationUploadWizard",
                    "multiPubUploadSelectPage", "uploadManager", "transformers");
                if (transformers.size() > 1) {
                    JOptionPane.showMessageDialog(nextButton,
                        textsBundle.getString("handbook.import.manyBooks.warning"),
                        textsBundle.getString("handbook.import.manyBooks.title"), JOptionPane.WARNING_MESSAGE);
                }

                for (Object transformer : transformers) {
                    Publication publication = (Publication) getFieldValue(transformer, "publication");
                    DirectoryId targetDirId = (DirectoryId) publication.getParentId();
                    if (targetDirId.equals(selectedDirId))
                        continue;

                    String selectedDirName, targetDirName;
                    try {
                        EventDirectoryManager dm = ApplicationContext.getInstance().getEventMetadataServer()
                                .getDirectoryManager();
                        DirectoryInfo dirInfo = (DirectoryInfo) dm
                                .getObjects(new DirectoryFilter(selectedDirId), new OutputFilter(DirectoryInfo.class))
                                .getResultInfo();
                        selectedDirName = dirInfo.getLabel();
                        dirInfo = (DirectoryInfo) dm
                                .getObjects(new DirectoryFilter(targetDirId), new OutputFilter(DirectoryInfo.class))
                                .getResultInfo();
                        targetDirName = dirInfo.getLabel();
                    } catch (Exception ex) {
                        ExceptionHandler.getInstance().handleException(ex, browser);
                        return;
                    }

                    String bookName = publication.getName();
                    if (bookName.length() > 30)
                        bookName = bookName.substring(0, 30) + '\u2026';
                    String question = String.format(textsBundle.getString("handbook.import.changeTarget.question"),
                        bookName, targetDirName, targetDirId, selectedDirName, selectedDirId);
                    String[] options = new String[] {
                            String.format(textsBundle.getString("handbook.import.changeTarget.change"), selectedDirId),
                            String.format(textsBundle.getString("handbook.import.changeTarget.leave"), targetDirId),
                            textsBundle.getString("handbook.import.changeTarget.cancel") };
                    int decision = JOptionPane.showOptionDialog(nextButton, question,
                        textsBundle.getString("handbook.import.changeTarget.title"), JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                    if (decision == 0) {
                        changeTargetDirectory(transformer, selectedDirId);
                    } else if (decision != 1) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                JWizard wizard = (JWizard) getFieldValue(browser, "multiPulblicationUploadWizard");
                                wizard.back();
                            }
                        });
                    }
                }
            }


            private void changeTargetDirectory(Object transformer, DirectoryId dirId) {
                Queue<Object> queue = new ArrayDeque<Object>();
                queue.add(transformer);
                while (!queue.isEmpty()) {
                    transformer = queue.poll();
                    Publication publication = (Publication) getFieldValue(transformer, "publication");
                    publication.setParentId(dirId);
                    List<?> childTransformers = (List<?>) getFieldValue(transformer, "childTransformers");
                    if (childTransformers != null) {
                        queue.addAll(childTransformers);
                    }
                }
            }
        });

        final Field publishedField;
        try {
            publishedField = AbstractPropertiesToPublicationTransformer.class.getDeclaredField("published");
            publishedField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MultiPubUploadManager uploadManager = new MultiPubUploadManager() {

            @Override
            public ArrayList<PublicationId> startUpload(MultiPubUploadProgressMonitor monitor) {
                ArrayList<AbstractPropertiesToPublicationTransformer> transformers = getTransformers();
                for (AbstractPropertiesToPublicationTransformer t : transformers)
                    forcePublished(t);

                ArrayList<PublicationId> replaced = new ArrayList<PublicationId>();
                for (AbstractPropertiesToPublicationTransformer t : transformers) {
                    Publication publication = t.getPublication();
                    if (publication.getGroupStatus() == Publication.PUB_GROUP_ROOT && publication.getId() != null) {
                        replaced.add(publication.getId());
                        try {
                            EPService ep = (EPService) ApplicationContext.getInstance().getUserServiceResolver()
                                    .getService(EPService.SERVICE_TYPE, null);
                            ep.textbookModificationStart(publication.getId());
                        } catch (Exception e) {
                            logger.error("Could not start textbook modification phase", e);
                        }
                    }
                }

                ArrayList<PublicationId> result = super.startUpload(monitor);

                for (AbstractPropertiesToPublicationTransformer t : transformers) {
                    Publication publication = t.getPublication();
                    if (publication.getGroupStatus() == Publication.PUB_GROUP_ROOT) {
                        try {
                            EPService ep = (EPService) ApplicationContext.getInstance().getUserServiceResolver()
                                    .getService(EPService.SERVICE_TYPE, null);
                            ep.textbookModificationEnd(publication.getId(), !replaced.contains(publication.getId()));
                        } catch (Exception e) {
                            logger.error("Could not end textbook modification phase", e);
                        }
                    }
                }
                return result;
            }


            private void forcePublished(AbstractPropertiesToPublicationTransformer t) {
                try {
                    publishedField.set(t, true);
                } catch (Exception e) {
                    throw new RuntimeException("Could not force published state", e);
                }
                List<AbstractPropertiesToPublicationTransformer> children = t.getChildTransformers();
                if (children != null) {
                    for (AbstractPropertiesToPublicationTransformer child : children)
                        forcePublished(child);
                }
            }
        };
        try {
            MultiPublicationUploadSelectPage multiPubUploadSelectPage = (MultiPublicationUploadSelectPage) getFieldValue(
                browser, "multiPulblicationUploadWizard", "multiPubUploadSelectPage");
            Field field = multiPubUploadSelectPage.getClass().getDeclaredField("uploadManager");
            field.setAccessible(true);
            field.set(multiPubUploadSelectPage, uploadManager);

            TransformerPublicationsTree filesTree = (TransformerPublicationsTree) getFieldValue(
                multiPubUploadSelectPage, "filesTree");
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ArrayList<AbstractPropertiesToPublicationTransformer> transformers = (ArrayList) getFieldValue(
                uploadManager, "transformers");
            filesTree.setRootPublicationObjects(transformers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void handleVersioning(final PublicationBrowser browser) {
        try {
            final EventMetadataServer ems = ApplicationContext.getInstance().getEventMetadataServer();
            final EventUserServer us = ApplicationContext.getInstance().getEventUserServer();
            final EventPublicationManager pm = ems.getPublicationManager();
            final UserId userId = ApplicationContext.getInstance().getLoggedUser().getId();
            Field pubManagerField = pm.getClass().getDeclaredField("publicationManager");
            pubManagerField.setAccessible(true);
            pubManagerField.set(pm, new EventPublicationManager((PublicationManager) pubManagerField.get(pm)) {

                @Override
                public void removePublication(PublicationId publicationId, boolean permanent, String reason)
                        throws DLibraException, RemoteException {
                    if (checkBlocked(publicationId, userId, true))
                        super.removePublication(publicationId, permanent, reason);
                }


                @Override
                public void setPublicationData(Publication publication)
                        throws DLibraException, AccessDeniedException, IdNotFoundException, RemoteException {
                    if (checkBlocked(publication.getId(), userId, false))
                        super.setPublicationData(publication);
                }


                @Override
                public void setPublicationData(Publication publication, UserId userId)
                        throws DLibraException, AccessDeniedException, IdNotFoundException, RemoteException {
                    if (checkBlocked(publication.getId(), userId, false))
                        super.setPublicationData(publication, userId);
                }


                @Override
                public void setPositions(PublicationId parentId, List<PublicationId> positions)
                        throws DLibraException, AccessDeniedException, RemoteException, IdNotFoundException {
                    if (checkBlocked(parentId, userId, false))
                        super.setPositions(parentId, positions);
                }


                private boolean checkBlocked(PublicationId publicationId, UserId userId, boolean removing)
                        throws DLibraException, RemoteException {
                    if (Versioning.isBlocked(publicationId, us) && !User.isAdmin(userId)) {
                        String key = "object." + (removing ? "remove" : "modify") + ".blocked.";
                        JOptionPane.showMessageDialog(browser, textsBundle.getString(key + "message"),
                            textsBundle.getString(key + "title"), JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    return true;
                }
            });

            EventElementMetadataManager eemm = ems.getElementMetadataManager();
            Field metadataManagerField = eemm.getClass().getDeclaredField("elementMetadataManager");
            metadataManagerField.setAccessible(true);
            metadataManagerField.set(eemm,
                new EventElementMetadataManager((ElementMetadataManager) metadataManagerField.get(eemm)) {

                    @Override
                    public void setAttributeValueSet(AttributeValueSet metadata)
                            throws RemoteException, DLibraException {
                        if (isBlocked(metadata.getElementId()) && !User.isAdmin(userId)) {
                            JOptionPane.showMessageDialog(browser, textsBundle.getString("object.modify.block.message"),
                                textsBundle.getString("object.modify.block.title"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        AttributeValueSet oldAVS = getAttributeValueSet(metadata.getElementId(),
                            AttributeValue.AV_ASSOC_DIRECT);
                        UserServiceResolver resolver = ApplicationContext.getInstance().getUserServiceResolver();
                        Publication[] rootPub = { null };
                        String message = Versioning.checkVersions(metadata, oldAVS, resolver, userId, rootPub);
                        boolean confirmed = true;
                        if (message == null) {
                            checkEditorMode(metadata, oldAVS);
                        } else if (!message.isEmpty()) {
                            JOptionPane.showMessageDialog(browser,
                                String.format(textsBundle.getString("versioning.error.message"), message),
                                textsBundle.getString("versioning.error.title"), JOptionPane.ERROR_MESSAGE);
                        } else {
                            String rootName = rootPub[0].getName() + " (id=" + rootPub[0].getId() + ")";
                            int confirm = JOptionPane.showConfirmDialog(browser,
                                String.format(textsBundle.getString("versioning.question.message"), rootName),
                                textsBundle.getString("versioning.question.title"), JOptionPane.OK_CANCEL_OPTION);
                            confirmed = confirm == JOptionPane.OK_OPTION;
                        }
                        Versioning.checkVersionsFollowUp(metadata, oldAVS, confirmed, resolver);
                        super.setAttributeValueSet(metadata);
                    }


                    private void checkEditorMode(AttributeValueSet metadata, AttributeValueSet oldAVS)
                            throws RemoteException, DLibraException {
                        ElementId elementId = metadata.getElementId();
                        boolean isUpdating;
                        Publication publication = null;
                        if (elementId instanceof EditionId) {
                            EditionInfo editionInfo = (EditionInfo) pm
                                    .getObjects(new EditionFilter((EditionId) elementId),
                                        new OutputFilter(EditionInfo.class))
                                    .getResultInfo();
                            isUpdating = editionInfo.getState() == Edition.PUBLISHED;
                            publication = (Publication) pm.getObjects(new EditionFilter((EditionId) elementId),
                                new OutputFilter(Publication.class)).getResult();
                        } else if (elementId instanceof PublicationId) {
                            Collection<Id> children = pm
                                    .getObjects(new PublicationFilter(null, (PublicationId) elementId),
                                        new OutputFilter(PublicationId.class))
                                    .getResultIds();
                            isUpdating = !children.isEmpty();
                            publication = (Publication) pm.getObjects(new PublicationFilter((PublicationId) elementId),
                                new OutputFilter(Publication.class)).getResult();
                        } else {
                            isUpdating = true;
                        }
                        String editorMessage = Versioning.checkEditorMode(metadata, oldAVS, publication, null, null,
                            isUpdating, ems);
                        if (editorMessage != null) {
                            JOptionPane.showMessageDialog(browser,
                                String.format(textsBundle.getString("editor.error.message"), editorMessage),
                                textsBundle.getString("editor.error.title"), JOptionPane.ERROR_MESSAGE);
                            if (!isUpdating) {
                                throw new RemoteException("Stopped invalid upload");
                            }
                        }
                    }


                    private boolean isBlocked(ElementId id)
                            throws RemoteException, DLibraException {
                        if (id instanceof EditionId)
                            return Versioning.isBlocked((EditionId) id, pm, us);
                        if (id instanceof PublicationId)
                            return Versioning.isBlocked((PublicationId) id, us);
                        return false;
                    }
                });

            EventFileManager efm = ems.getFileManager();
            Field fileManagerField = efm.getClass().getDeclaredField("fileManager");
            fileManagerField.setAccessible(true);
            fileManagerField.set(efm, new EventFileManager((FileManager) fileManagerField.get(efm)) {

                @Override
                public Version createVersion(File file, long size, Date modificationDate, String description)
                        throws RemoteException, DLibraException {
                    checkBlocked(us, new File[] { file });
                    return super.createVersion(file, size, modificationDate, description);
                }


                @Override
                public Version[] createVersions(File[] files, Date[] modificationDates, String[] descriptions)
                        throws AccessDeniedException, IdNotFoundException, RemoteException, DLibraException {
                    checkBlocked(us, files);
                    return super.createVersions(files, modificationDates, descriptions);
                }


                @Override
                public Version[] createVersions(File[] files, Date[] modificationDates, String[] descriptions,
                        UserId userId)
                                throws AccessDeniedException, IdNotFoundException, RemoteException, DLibraException {
                    checkBlocked(us, files);
                    return super.createVersions(files, modificationDates, descriptions, userId);
                }


                private void checkBlocked(final EventUserServer us, File[] files)
                        throws RemoteException, DLibraException {
                    if (files.length > 0 && Versioning.isBlocked(files[0].getPublicationId(), us)) {
                        throw new AccessDeniedException(null, "Object is blocked", null);
                    }
                }
            });

            EventDirectoryManager dm = ems.getDirectoryManager();
            Field dirManagerField = dm.getClass().getDeclaredField("directoryManager");
            dirManagerField.setAccessible(true);
            dirManagerField.set(dm, new EventDirectoryManager((DirectoryManager) dirManagerField.get(dm)) {

                @Override
                public void removeDirectory(DirectoryId directoryId, boolean permanent, String reason)
                        throws RemoteException, DLibraException {
                    EventMetadataServer ems = ApplicationContext.getInstance().getEventMetadataServer();
                    List<PublicationId> unremovable = Versioning.findUnremovableHandbooks(directoryId, ems);
                    if (!unremovable.isEmpty()) {
                        JOptionPane.showMessageDialog(browser,
                            String.format(textsBundle.getString("directory.remove.oldVersion.message"), unremovable),
                            textsBundle.getString("directory.remove.oldVersion.title"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    super.removeDirectory(directoryId, permanent, reason);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void changeSearchFeature(PublicationBrowser browser) {
        try {
            SearchAction searchAction = (SearchAction) getFieldValue(browser, "searchFrame", "searchAction");
            Field field = SearchAction.class.getDeclaredField("searchText");
            field.setAccessible(true);
            final JTextField textField = (JTextField) field.get(searchAction);
            JTextField textFieldWrapper = new JTextField() {

                @Override
                public String getText() {
                    String text = textField.getText();
                    if (!text.contains("_"))
                        text += "*";
                    return text;
                }
            };
            field.set(searchAction, textFieldWrapper);
        } catch (Exception e) {
            logger.error("Could not modify search behavior", e);
        }
    }


    @SuppressWarnings("rawtypes")
    private void changeAttributeValueLengthLimit(PublicationBrowser browser) {
        LanguagesTabbedPane langPane = (LanguagesTabbedPane) getFieldValue(browser, "editorsPanel",
            "editorPubDirProperties", "avEditor", "metadataTabs");
        AttributesValuesTreeTable table = (AttributesValuesTreeTable) langPane.getTabEditors().iterator().next();
        AutoCompleteConstrainedTextArea textArea = (AutoCompleteConstrainedTextArea) getFieldValue(table,
            "autoCompleteConstrainedTextArea");
        try {
            Method installMethod = textArea.getClass().getDeclaredMethod("installDocumentFilter", int.class);
            installMethod.setAccessible(true);
            installMethod.invoke(textArea, ATTRIBUTE_VALUE_LENGTH_LIMIT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Object getFieldValue(Object object, String... fieldNames) {
        try {
            for (String fieldName : fieldNames) {
                Class<?> clazz = object.getClass();
                while (clazz != null) {
                    try {
                        Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        object = field.get(object);
                        break;
                    } catch (NoSuchFieldException e) {
                        clazz = clazz.getSuperclass();
                        if (clazz == null)
                            throw e;
                    }
                }
            }
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
