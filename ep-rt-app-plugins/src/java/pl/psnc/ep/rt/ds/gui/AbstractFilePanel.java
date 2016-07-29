package pl.psnc.ep.rt.ds.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

public abstract class AbstractFilePanel extends JPanel {

    protected final static Logger logger = Logger.getLogger(AbstractFormatPanel.class);

    protected ResourceBundle textsBundle;

    protected JTextField sourcePathTextField;

    protected JButton sourceSelectButton;

    protected File sourceFile;

    protected JFileChooser fileChooser;

    protected final FileFilter fileFilter;

    protected WOMIDefinitionPanel changesListener;


    public AbstractFilePanel(ResourceBundle textsBundle, FileFilter fileFilter) {
        this.textsBundle = textsBundle;
        this.fileFilter = fileFilter;
    }


    protected void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(sourcePathTextField = new JTextField(), c);

        c.gridx++;
        c.weightx = 0;
        add(sourceSelectButton = new JButton(), c);

    }


    protected void initGUITexts() {
        sourceSelectButton.setText(textsBundle.getString("file.select"));
    }


    protected void addListeners() {
        sourceSelectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });

        sourcePathTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                sourcePathTypedIn();
            }


            @Override
            public void focusGained(FocusEvent e) {
                sourcePathTextField.selectAll();
            }
        });
        sourcePathTextField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        refreshPanel();
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                        break;
                }
            }
        });
    }


    protected abstract void refreshPanel();


    protected void selectFile() {
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileFilter(fileFilter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            setSourceFile(fileChooser.getSelectedFile());
        } else {
            refreshPanel();
            if (changesListener != null) {
                changesListener.sourceFilesChanged();
            }
        }
    }


    protected void sourcePathTypedIn() {
        String newPath = sourcePathTextField.getText();
        if (newPath.isEmpty()) {
            setSourceFile(null);
            return;
        }
        File file = new File(newPath);
        if (!file.isFile()) {
            JOptionPane.showMessageDialog(AbstractFilePanel.this,
                String.format(textsBundle.getString("formatPanel.fileDoesntExist"), file.getAbsolutePath()),
                textsBundle.getString("formatPanel.errorTitle"), JOptionPane.ERROR_MESSAGE);
            refreshPanel();
            return;
        }
        if (!fileFilter.accept(file)) {
            JOptionPane.showMessageDialog(AbstractFilePanel.this,
                String.format(textsBundle.getString("formatPanel.fileWrongExtension"), fileFilter.getDescription(),
                    file.getAbsolutePath()),
                textsBundle.getString("formatPanel.errorTitle"), JOptionPane.ERROR_MESSAGE);
            refreshPanel();
            return;
        }
        setSourceFile(file);
    }


    public void setSourceFile(File file) {
        if (file != null) {
            try {
                file = file.getCanonicalFile();
            } catch (IOException e) {
                logger.error("Could not get canonical path for " + file, e);
                JOptionPane.showMessageDialog(this,
                    textsBundle.getString("formatPanel.canonicalPathError") + e.getMessage(),
                    textsBundle.getString("formatPanel.errorTitle"), JOptionPane.ERROR_MESSAGE);
                file = sourceFile;
            }
            if (!validateFile(file))
                file = sourceFile;
        }

        File oldFile = sourceFile;
        sourceFile = file;
        refreshPanel();
        if (changesListener != null && oldFile != file && (oldFile == null || !oldFile.equals(file))) {
            changesListener.sourceFilesChanged();
        }
    }


    protected boolean validateFile(File file) {
        return true;
    }


    public void setChangesListener(WOMIDefinitionPanel changesListener) {
        this.changesListener = changesListener;
    }


    public void setFileChooser(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }


    public File getSourceFile() {
        return sourceFile;
    }


    public void setModificationAllowed(boolean allowed) {
        sourcePathTextField.setEnabled(allowed);
        sourceSelectButton.setEnabled(allowed);
    }
}
