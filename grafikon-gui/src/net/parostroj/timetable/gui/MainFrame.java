/*
 * MainFrame.java
 *
 * Created on 26. srpen 2007, 19:40
 */
package net.parostroj.timetable.gui;

import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.parostroj.timetable.actions.TrainSortByNodeFilter;
import net.parostroj.timetable.gui.components.TrainColorChooser;
import net.parostroj.timetable.gui.dialogs.*;
import net.parostroj.timetable.gui.utils.*;
import net.parostroj.timetable.gui.views.*;
import net.parostroj.timetable.model.*;
import net.parostroj.timetable.model.ls.*;
import net.parostroj.timetable.output.*;
import net.parostroj.timetable.utils.ResourceLoader;


/**
 * Main frame for the application.
 *
 * @author jub
 */
public class MainFrame extends javax.swing.JFrame implements ApplicationModelListener, StorableGuiData {
    
    private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());
    private static final String FILE_EXTENSION = "gtm";
    private static final String FRAME_TITLE = "Grafikon";

    private ApplicationModel model;
    private SettingsDialog settingsDialog;
    private EditImagesDialog imagesDialog;
    private EditInfoDialog infoDialog;
    private NewModelDialog newModelDialog;
    private FloatingDialogsList floatingDialogsList;
    private TrainTypesDialog trainTypesDialog;
    private LineClassesDialog lineClassesDialog;
    private EngineClassesDialog engineClassesDialog;
    private ImportDialog importDialog;
    private Locale locale;
    
    private static JFileChooser outputFileChooserInstance;
    private static JFileChooser xmlFileChooserInstance;
    private static JFileChooser allHtmlFileChooserInstance;
    
    private enum FileChooserType {
        OUTPUT, XML, ALL_HTML;
    }
    
    private static synchronized JFileChooser getFileChooser(FileChooserType type) {
        switch (type) {
            case ALL_HTML:
                if (allHtmlFileChooserInstance == null) {
                    allHtmlFileChooserInstance = new JFileChooser();
                    allHtmlFileChooserInstance.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    // last directory
                    try {
                        String lastDir = AppPreferences.getPreferences().getString("last.directory.html.dir", null);
                        if (lastDir != null) {
                            allHtmlFileChooserInstance.setCurrentDirectory(new File(lastDir));
                        }
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "Cannot get last directory from preferences.", e);
                    }
                }
                return allHtmlFileChooserInstance;
            case OUTPUT:
                if (outputFileChooserInstance == null) {
                    outputFileChooserInstance = new JFileChooser() {
                        @Override
                        public void approveSelection() {
                            if (getDialogType() == JFileChooser.SAVE_DIALOG)
                                if (!this.getSelectedFile().getName().toLowerCase().endsWith(".html"))
                                    this.setSelectedFile(new File(this.getSelectedFile().getAbsolutePath() + ".html"));
                            if ((getDialogType() == JFileChooser.SAVE_DIALOG) && getSelectedFile().exists()) {
                                int result = JOptionPane.showConfirmDialog(this, 
                                        String.format(ResourceLoader.getString("savedialog.overwrite.text"), getSelectedFile()), 
                                        ResourceLoader.getString("savedialog.overwrite.confirmation"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE); 
                                if(result != JOptionPane.YES_OPTION) {
                                    return;
                                }
                            }
                            super.approveSelection();
                        }
                    };
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(ResourceLoader.getString("output.html"), "html");
                    outputFileChooserInstance.setFileFilter(filter);
                    // last directory
                    try {
                        String lastDir = AppPreferences.getPreferences().getString("last.directory.output", null);
                        if (lastDir != null) {
                            outputFileChooserInstance.setCurrentDirectory(new File(lastDir));
                        }
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "Cannot get last directory from preferences.", e);
                    }
                }
                return outputFileChooserInstance;
            case XML:
                if (xmlFileChooserInstance == null) {
                    xmlFileChooserInstance = new JFileChooser() {
                        @Override
                        public void approveSelection() {
                            if (getDialogType() == JFileChooser.SAVE_DIALOG)
                                if (!this.getSelectedFile().getName().toLowerCase().endsWith("." + FILE_EXTENSION))
                                    this.setSelectedFile(new File(this.getSelectedFile().getAbsolutePath()+"." + FILE_EXTENSION));
                            if ((getDialogType() == JFileChooser.SAVE_DIALOG) && getSelectedFile().exists()) {
                                int result = JOptionPane.showConfirmDialog(this, 
                                        String.format(ResourceLoader.getString("savedialog.overwrite.text"), getSelectedFile()), 
                                        ResourceLoader.getString("savedialog.overwrite.confirmation"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE); 
                                if(result != JOptionPane.YES_OPTION) {
                                    return;
                                }
                            }
                            super.approveSelection();
                        }
                    };
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(ResourceLoader.getString("files.description"), FILE_EXTENSION);
                    xmlFileChooserInstance.setFileFilter(filter);
                    // last directory
                    try {
                        String lastDir = AppPreferences.getPreferences().getString("last.directory.model", null);
                        if (lastDir != null) {
                            xmlFileChooserInstance.setCurrentDirectory(new File(lastDir));
                        }
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "Cannot get last directory from preferences.", e);
                    }
                }
                return xmlFileChooserInstance;
        }
        return null;
    }

    public MainFrame(SplashScreenInfo info) {
        ResourceBundle bundle = ResourceBundle.getBundle("grafikon_version");
        String version = bundle.getString("grafikon.version");
        info.setText("Starting build " + version + " ...");
        this.initializeFrame();
    }

    public MainFrame() {
        this.initializeFrame();
    }

    /**
     * initializes frame.
     */
    private void initializeFrame() {
        // set local before anything else
        String loadedLocale = null;
        try {
            loadedLocale = AppPreferences.getPreferences().getString("locale.program", null);
            String templateLocale = AppPreferences.getPreferences().getString("locale.output", null);
            if (loadedLocale != null) {
                locale = Templates.parseLocale(loadedLocale);
                Locale.setDefault(locale);
            }
            if (templateLocale != null) {
                Templates.setLocale(Templates.parseLocale(templateLocale));
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot load preferences.", e);
        }
        
        initComponents();

        model = new ApplicationModel();
        
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                final int result = checkModelChangedContinue();
                if (result != JOptionPane.CANCEL_OPTION)
                    ActionHandler.getInstance().executeAction(MainFrame.this, ResourceLoader.getString("wait.message.programclose"), 0, new ModelAction() {
                        private String errorMessage;
                        @Override
                        public void run() {
                            try {
                                if (result == JOptionPane.YES_OPTION)
                                    saveModelData(model.getOpenedFile());
                                MainFrame.this.cleanUpBeforeApplicationEnd();
                            } catch (Exception e) {
                                LOG.log(Level.WARNING, "Error saving model.", e);
                                errorMessage = ResourceLoader.getString("dialog.error.saving");
                            }
                        }

                        @Override
                        public void afterRun() {
                            if (errorMessage != null) {
                                showError(errorMessage);
                                return;
                            }
                            dispose();
                        }
                        });
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                System.exit(0);
            }
        });
        
        trainsPane.setModel(model);
        engineCyclesPane.setModel(model, new EngineCycleDelegate(),new TrainColorChooser() {
            @Override
            public Color getIntervalColor(TimeInterval interval) {
                if (!interval.getTrain().isCovered(TrainsCycleType.ENGINE_CYCLE, interval))
                    return Color.black;
                else
                    return Color.gray;
            }
        });
        trainUnitCyclesPane.setModel(model, new TrainUnitCycleDelegate(),new TrainColorChooser() {
            @Override
            public Color getIntervalColor(TimeInterval interval) {
                if (!interval.getTrain().isCovered(TrainsCycleType.TRAIN_UNIT_CYCLE, interval))
                    return Color.black;
                else
                    return Color.gray;
            }
        });
        driverCyclesPane.setModel(model, new DriverCycleDelegate(),new TrainColorChooser() {
            @Override
            public Color getIntervalColor(TimeInterval interval) {
                if (!interval.getTrain().isCovered(TrainsCycleType.DRIVER_CYCLE, interval))
                    return Color.black;
                else
                    return Color.gray;
            }
        });
        
        // add languages to menu
        LanguageMenuBuilder languageMenuBuilder = new LanguageMenuBuilder();
        List<LanguageMenuBuilder.LanguageMenuItem> languages = languageMenuBuilder.createLanguageMenuItems();
        ActionListener langListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                languageRadioButtonMenuItemActionPerformed(e);
            }
        };
        for (LanguageMenuBuilder.LanguageMenuItem item : languages) {
            languageMenu.add(item);
            item.addActionListener(langListener);
            languageButtonGroup.add(item);
        }
        List<LanguageMenuBuilder.LanguageMenuItem> oLanguages = languageMenuBuilder.createLanguageMenuItems();
        ActionListener oLangListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputLanguageRadioButtonMenuItemActionPerformed(e);
            }
        };
        for (LanguageMenuBuilder.LanguageMenuItem item : oLanguages) {
            oLanguageMenu.add(item);
            item.addActionListener(oLangListener);
            outputLbuttonGroup.add(item);
        }
        
        model.addListener(this);
        model.addListener(statusBar);
        
        settingsDialog = new SettingsDialog(this, true);
        
        imagesDialog = new EditImagesDialog(this, true);
        imagesDialog.setModel(model);
        
        infoDialog = new EditInfoDialog(this, true);
        infoDialog.setModel(model);

        floatingDialogsList = FloatingDialogsFactory.createDialogs(this, model.getMediator(), model);
        floatingDialogsList.addToMenuItem(viewsMenu);
        
        newModelDialog = new NewModelDialog(this, true);
        newModelDialog.setModel(model);
        
        trainTypesDialog = new TrainTypesDialog(this, true);
        trainTypesDialog.setModel(model);
        
        lineClassesDialog = new LineClassesDialog(this, true);
        lineClassesDialog.setModel(model);
        
        engineClassesDialog = new EngineClassesDialog(this, true);
        engineClassesDialog.setModel(model);

        importDialog = new ImportDialog(this, true);
        
        netPane.setModel(model);
        
        this.updateView();

        model.setDiagram(null);
        
        // apply preferences
        try {
            AppPreferences.getPreferences().load();
            this.loadFromPreferences(AppPreferences.getPreferences());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error loading preferences.", e);
        }
        
        this.setSelectedLocale();
        this.setSelectedTemplateLocale();
        
        // preload file dialogs
        getFileChooser(MainFrame.FileChooserType.ALL_HTML);
        getFileChooser(MainFrame.FileChooserType.OUTPUT);
        getFileChooser(MainFrame.FileChooserType.XML);
    }

    @Override
    public void modelChanged(ApplicationModelEvent event) {
        if (event.getType() == ApplicationModelEventType.SET_DIAGRAM_CHANGED) {
            this.updateView();
            tabbedPane.setEnabled(model.getDiagram() != null);
            this.setTitleChanged(false);
        }
        if (event.getType() == ApplicationModelEventType.MODEL_CHANGED) {
            this.setTitleChanged(true);
        }
        if (event.getType() == ApplicationModelEventType.MODEL_SAVED) {
            this.setTitleChanged(false);
        }
    }

    private void setTitleChanged(boolean b) {
        this.setTitle(this.getTitleString(b));
    }
    
    private String getTitleString(boolean b) {
        String title = FRAME_TITLE;
        ResourceBundle bundle = ResourceBundle.getBundle("grafikon_version");
        String version = bundle.getString("grafikon.version");
        if (version != null)
            title += " (" + version + ")";
        if (model != null && model.getDiagram() != null) {
            if (model.getOpenedFile() == null)
                title += " - " + ResourceLoader.getString("title.new");
            else
                title += " - " + model.getOpenedFile().getName();
            if (b)
                title += " *";
        }
        return title;
    }
    
    private int checkModelChangedContinue() {
        if (!model.isModelChanged())
            return JOptionPane.NO_OPTION;
        else {
            int result = JOptionPane.showConfirmDialog(this, ResourceLoader.getString("model.not.saved.question"),ResourceLoader.getString("model.not.saved"),JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION && model.getOpenedFile() == null) {
                JFileChooser xmlFileChooser = getFileChooser(MainFrame.FileChooserType.XML);
                int retVal = xmlFileChooser.showSaveDialog(this);
                if (retVal == JFileChooser.APPROVE_OPTION)
                    model.setOpenedFile(xmlFileChooser.getSelectedFile());
                else
                    result = JOptionPane.CANCEL_OPTION;
            }
            return result;
        }
    }
    
    private void updateView() {
        fileSaveMenuItem.setEnabled(model.getDiagram() != null);
        fileSaveAsMenuItem.setEnabled(model.getDiagram() != null);
        trainTimetableListMenuItem.setEnabled(model.getDiagram() != null);
        trainTimetableListByDcMenuItem.setEnabled(model.getDiagram() != null);
        recalculateMenuItem.setEnabled(model.getDiagram() != null);
        recalculateStopsMenuItem.setEnabled(model.getDiagram() != null);
        nodeTimetableListMenuItem.setEnabled(model.getDiagram() != null);
        ecListMenuItem.setEnabled(model.getDiagram() != null);
        dcListMenuItem.setEnabled(model.getDiagram() != null);
        tucListMenuItem.setEnabled(model.getDiagram() != null);
        settingsMenuItem.setEnabled(model.getDiagram() != null);
        allHtmlMenuItem.setEnabled(model.getDiagram() != null);
        imagesMenuItem.setEnabled(model.getDiagram() != null);
        infoMenuItem.setEnabled(model.getDiagram() != null);
        spListMenuItem.setEnabled(model.getDiagram() != null);
        epListMenuItem.setEnabled(model.getDiagram() != null);
        trainTypesMenuItem.setEnabled(model.getDiagram() != null);
        lineClassesMenuItem.setEnabled(model.getDiagram() != null);
        weightTablesMenuItem.setEnabled(model.getDiagram() != null);
        penaltyTableMenuItem.setEnabled(model.getDiagram() != null);
        trainTimetableListByTimeFilteredMenuItem.setEnabled(model.getDiagram() != null);
        fileImportMenuItem.setEnabled(model.getDiagram() != null);
        dcListSelectMenuItem.setEnabled(model.getDiagram() != null);
        trainTimetableListByDcSelectMenuItem.setEnabled(model.getDiagram() != null);
        nodeTimetableListSelectMenuItem.setEnabled(model.getDiagram() != null);
        ecListSelectMenuItem.setEnabled(model.getDiagram() != null);
        tucListSelectMenuItem.setEnabled(model.getDiagram() != null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languageButtonGroup = new javax.swing.ButtonGroup();
        outputLbuttonGroup = new javax.swing.ButtonGroup();
        applicationPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        trainsPane = new net.parostroj.timetable.gui.panes.TrainsPane();
        engineCyclesPane = new net.parostroj.timetable.gui.panes.TrainsCyclesPane();
        trainUnitCyclesPane = new net.parostroj.timetable.gui.panes.TrainsCyclesPane();
        driverCyclesPane = new net.parostroj.timetable.gui.panes.TrainsCyclesPane();
        netPane = new net.parostroj.timetable.gui.panes.NetPane();
        statusBar = new net.parostroj.timetable.gui.StatusBar();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem fileNewMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator separator3 = new javax.swing.JSeparator();
        fileOpenMenuItem = new javax.swing.JMenuItem();
        fileSaveMenuItem = new javax.swing.JMenuItem();
        fileSaveAsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator separator1 = new javax.swing.JSeparator();
        fileImportMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator separator5 = new javax.swing.JSeparator();
        settingsMenuItem = new javax.swing.JMenuItem();
        imagesMenuItem = new javax.swing.JMenuItem();
        infoMenuItem = new javax.swing.JMenuItem();
        trainTypesMenuItem = new javax.swing.JMenuItem();
        lineClassesMenuItem = new javax.swing.JMenuItem();
        weightTablesMenuItem = new javax.swing.JMenuItem();
        penaltyTableMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator separator2 = new javax.swing.JSeparator();
        languageMenu = new javax.swing.JMenu();
        systemLanguageRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        javax.swing.JSeparator separator4 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu actionMenu = new javax.swing.JMenu();
        trainTimetableListMenuItem = new javax.swing.JMenuItem();
        nodeTimetableListMenuItem = new javax.swing.JMenuItem();
        ecListMenuItem = new javax.swing.JMenuItem();
        tucListMenuItem = new javax.swing.JMenuItem();
        dcListMenuItem = new javax.swing.JMenuItem();
        spListMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        trainTimetableListByDcMenuItem = new javax.swing.JMenuItem();
        trainTimetableListByTimeFilteredMenuItem = new javax.swing.JMenuItem();
        epListMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        trainTimetableListByDcSelectMenuItem = new javax.swing.JMenuItem();
        nodeTimetableListSelectMenuItem = new javax.swing.JMenuItem();
        ecListSelectMenuItem = new javax.swing.JMenuItem();
        tucListSelectMenuItem = new javax.swing.JMenuItem();
        dcListSelectMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator4 = new javax.swing.JSeparator();
        allHtmlMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
        oLanguageMenu = new javax.swing.JMenu();
        oSystemLRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        viewsMenu = new javax.swing.JMenu();
        javax.swing.JMenu specialMenu = new javax.swing.JMenu();
        recalculateMenuItem = new javax.swing.JMenuItem();
        recalculateStopsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu settingsMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem columnsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem sortColumnsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem resizeColumnsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(this.getTitleString(false));
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(800, 600));

        tabbedPane.addTab(ResourceLoader.getString("tab.trains"), trainsPane); // NOI18N
        tabbedPane.addTab(ResourceLoader.getString("tab.engine.cycles"), engineCyclesPane); // NOI18N
        tabbedPane.addTab(ResourceLoader.getString("tab.trainunit.cycle"), trainUnitCyclesPane); // NOI18N
        tabbedPane.addTab(ResourceLoader.getString("tab.driver.cycles"), driverCyclesPane); // NOI18N
        tabbedPane.addTab(ResourceLoader.getString("tab.net"), netPane); // NOI18N

        javax.swing.GroupLayout applicationPanelLayout = new javax.swing.GroupLayout(applicationPanel);
        applicationPanel.setLayout(applicationPanelLayout);
        applicationPanelLayout.setHorizontalGroup(
            applicationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 961, Short.MAX_VALUE)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, 961, Short.MAX_VALUE)
        );
        applicationPanelLayout.setVerticalGroup(
            applicationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, applicationPanelLayout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tabbedPane.getAccessibleContext().setAccessibleName(ResourceLoader.getString("tab.trains")); // NOI18N

        fileMenu.setText(ResourceLoader.getString("menu.file")); // NOI18N

        fileNewMenuItem.setText(ResourceLoader.getString("menu.file.new")); // NOI18N
        fileNewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNewMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(fileNewMenuItem);
        fileMenu.add(separator3);

        fileOpenMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        fileOpenMenuItem.setText(ResourceLoader.getString("menu.file.open")); // NOI18N
        fileOpenMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileOpenMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(fileOpenMenuItem);

        fileSaveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        fileSaveMenuItem.setText(ResourceLoader.getString("menu.file.save")); // NOI18N
        fileSaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileSaveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(fileSaveMenuItem);

        fileSaveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        fileSaveAsMenuItem.setText(ResourceLoader.getString("menu.file.saveas")); // NOI18N
        fileSaveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileSaveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(fileSaveAsMenuItem);
        fileMenu.add(separator1);

        fileImportMenuItem.setText(ResourceLoader.getString("menu.file.exportimport")); // NOI18N
        fileImportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileImportMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(fileImportMenuItem);
        fileMenu.add(separator5);

        settingsMenuItem.setText(ResourceLoader.getString("menu.file.settings")); // NOI18N
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(settingsMenuItem);

        imagesMenuItem.setText(ResourceLoader.getString("menu.file.images")); // NOI18N
        imagesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imagesMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(imagesMenuItem);

        infoMenuItem.setText(ResourceLoader.getString("menu.file.info")); // NOI18N
        infoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(infoMenuItem);

        trainTypesMenuItem.setText(ResourceLoader.getString("menu.file.traintypes")); // NOI18N
        trainTypesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainTypesMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(trainTypesMenuItem);

        lineClassesMenuItem.setText(ResourceLoader.getString("menu.file.lineclasses")); // NOI18N
        lineClassesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineClassesMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(lineClassesMenuItem);

        weightTablesMenuItem.setText(ResourceLoader.getString("menu.file.weighttables")); // NOI18N
        weightTablesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                weightTablesMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(weightTablesMenuItem);

        penaltyTableMenuItem.setText(ResourceLoader.getString("menu.file.penaltytable")); // NOI18N
        penaltyTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                penaltyTableMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(penaltyTableMenuItem);
        fileMenu.add(separator2);

        languageMenu.setText(ResourceLoader.getString("menu.language")); // NOI18N

        languageButtonGroup.add(systemLanguageRadioButtonMenuItem);
        systemLanguageRadioButtonMenuItem.setSelected(true);
        systemLanguageRadioButtonMenuItem.setText(ResourceLoader.getString("menu.language.system")); // NOI18N
        systemLanguageRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageRadioButtonMenuItemActionPerformed(evt);
            }
        });
        languageMenu.add(systemLanguageRadioButtonMenuItem);

        fileMenu.add(languageMenu);
        fileMenu.add(separator4);

        exitMenuItem.setText(ResourceLoader.getString("menu.file.exit")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        actionMenu.setText(ResourceLoader.getString("menu.action")); // NOI18N

        trainTimetableListMenuItem.setText(ResourceLoader.getString("menu.action.traintimetableslist")); // NOI18N
        trainTimetableListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainTimetableListMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(trainTimetableListMenuItem);

        nodeTimetableListMenuItem.setText(ResourceLoader.getString("menu.action.nodetimetableslist")); // NOI18N
        nodeTimetableListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodeTimetableListMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(nodeTimetableListMenuItem);

        ecListMenuItem.setText(ResourceLoader.getString("menu.action.eclist")); // NOI18N
        ecListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ecListMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(ecListMenuItem);

        tucListMenuItem.setText(ResourceLoader.getString("menu.action.tuclist")); // NOI18N
        tucListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tucListMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(tucListMenuItem);

        dcListMenuItem.setText(ResourceLoader.getString("menu.acion.dclist")); // NOI18N
        dcListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dcListMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(dcListMenuItem);

        spListMenuItem.setText(ResourceLoader.getString("menu.acion.splist")); // NOI18N
        spListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spListMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(spListMenuItem);
        actionMenu.add(jSeparator1);

        trainTimetableListByDcMenuItem.setText(ResourceLoader.getString("menu.action.traintimetableslistbydc")); // NOI18N
        trainTimetableListByDcMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainTimetableListByDcMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(trainTimetableListByDcMenuItem);

        trainTimetableListByTimeFilteredMenuItem.setText(ResourceLoader.getString("menu.action.traintimetableslistbytimefiltered")); // NOI18N
        trainTimetableListByTimeFilteredMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainTimetableListByTimeFilteredMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(trainTimetableListByTimeFilteredMenuItem);

        epListMenuItem.setText(ResourceLoader.getString("menu.acion.eplist")); // NOI18N
        epListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                epListMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(epListMenuItem);
        actionMenu.add(jSeparator2);

        trainTimetableListByDcSelectMenuItem.setText(ResourceLoader.getString("menu.action.traintimetableslistbydc.select")); // NOI18N
        trainTimetableListByDcSelectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainTimetableListByDcSelectMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(trainTimetableListByDcSelectMenuItem);

        nodeTimetableListSelectMenuItem.setText(ResourceLoader.getString("menu.action.nodetimetableslist.select")); // NOI18N
        nodeTimetableListSelectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodeTimetableListSelectMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(nodeTimetableListSelectMenuItem);

        ecListSelectMenuItem.setText(ResourceLoader.getString("menu.action.eclist.select")); // NOI18N
        ecListSelectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ecListSelectMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(ecListSelectMenuItem);

        tucListSelectMenuItem.setText(ResourceLoader.getString("menu.action.tuclist.select")); // NOI18N
        tucListSelectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tucListSelectMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(tucListSelectMenuItem);

        dcListSelectMenuItem.setText(ResourceLoader.getString("menu.acion.dclist.select")); // NOI18N
        dcListSelectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dcListSelectMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(dcListSelectMenuItem);
        actionMenu.add(jSeparator4);

        allHtmlMenuItem.setText(ResourceLoader.getString("menu.action.all.html")); // NOI18N
        allHtmlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allHtmlMenuItemActionPerformed(evt);
            }
        });
        actionMenu.add(allHtmlMenuItem);
        actionMenu.add(jSeparator3);

        oLanguageMenu.setText(ResourceLoader.getString("menu.language.output")); // NOI18N

        outputLbuttonGroup.add(oSystemLRadioButtonMenuItem);
        oSystemLRadioButtonMenuItem.setSelected(true);
        oSystemLRadioButtonMenuItem.setText(ResourceLoader.getString("menu.language.program")); // NOI18N
        oSystemLRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputLanguageRadioButtonMenuItemActionPerformed(evt);
            }
        });
        oLanguageMenu.add(oSystemLRadioButtonMenuItem);

        actionMenu.add(oLanguageMenu);

        menuBar.add(actionMenu);

        viewsMenu.setText(ResourceLoader.getString("menu.views")); // NOI18N
        menuBar.add(viewsMenu);

        specialMenu.setText(ResourceLoader.getString("menu.special")); // NOI18N

        recalculateMenuItem.setText(ResourceLoader.getString("menu.special.recalculate")); // NOI18N
        recalculateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recalculateMenuItemActionPerformed(evt);
            }
        });
        specialMenu.add(recalculateMenuItem);

        recalculateStopsMenuItem.setText(ResourceLoader.getString("menu.special.recalculate.stops")); // NOI18N
        recalculateStopsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recalculateStopsMenuItemActionPerformed(evt);
            }
        });
        specialMenu.add(recalculateStopsMenuItem);

        menuBar.add(specialMenu);

        settingsMenu.setText(ResourceLoader.getString("menu.settings")); // NOI18N

        columnsMenuItem.setText(ResourceLoader.getString("menu.settings.columns")); // NOI18N
        columnsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                columnsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(columnsMenuItem);

        sortColumnsMenuItem.setText(ResourceLoader.getString("menu.settings.sort.columns")); // NOI18N
        sortColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortColumnsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(sortColumnsMenuItem);

        resizeColumnsMenuItem.setText(ResourceLoader.getString("menu.settings.resize.columns")); // NOI18N
        resizeColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resizeColumnsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(resizeColumnsMenuItem);

        menuBar.add(settingsMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(applicationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(applicationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void ecListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ecListMenuItemActionPerformed
    // write
    final EngineCyclesList list = new EngineCyclesList(model.getDiagram().getCycles(TrainsCycleType.ENGINE_CYCLE));
    this.engineCyclesList(list);
}//GEN-LAST:event_ecListMenuItemActionPerformed

private void nodeTimetableListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodeTimetableListMenuItemActionPerformed
    // write
    final NodeTimetablesList list = new NodeTimetablesList(model.getDiagram().getNet().getNodes(), model.getDiagram());
    this.nodeTimetablesList(list);
}//GEN-LAST:event_nodeTimetableListMenuItemActionPerformed

private void recalculateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recalculateMenuItemActionPerformed
    ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.recalculate"), new SwingWorker<Void, Train>() {

        private Lock lock = new ReentrantLock();
        private Condition condition = lock.newCondition();
        private boolean chunksFinished = false;
        private static final int CHUNK_SIZE = 10;

        @Override
        protected Void doInBackground() throws Exception {
            // recalculate all trains
            lock.lock();
            try {
                int cnt = 0;
                for (Train train : model.getDiagram().getTrains()) {
                    publish(train);
                    if (++cnt >= CHUNK_SIZE) {
                        chunksFinished = false;
                        while(!chunksFinished)
                            condition.await();
                        cnt = 0;
                    }
                }
            } finally {
                lock.unlock();
            }
            return null;
        }

        @Override
        protected void done() {
            model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.SET_DIAGRAM_CHANGED, model));
            // set back modified status (SET_DIAGRAM_CHANGED unfortunately clears the modified status)
            model.setModelChanged(true);
        }

        @Override
        protected void process(List<Train> chunks) {
            LOG.finest("Recalculate chunk of trains. Size: " + chunks.size());
            lock.lock();
            try {
                for (Train train : chunks) {
                    train.recalculate();
                }
                chunksFinished = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    });
}//GEN-LAST:event_recalculateMenuItemActionPerformed

private void trainTimetableListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainTimetableListMenuItemActionPerformed
    // write
    final TrainTimetablesList list = new TrainTimetablesList(model.getDiagram(), model.getDiagram().getTrains(),model.getDiagram().getImages(),TrainTimetablesList.Binding.BOOK, false);
    HtmlAction action = new HtmlAction() {
            @Override
            public void write(Writer writer) throws Exception {
                list.writeTo(writer);
            }

            @Override
            public void writeToDirectory(File directory) throws Exception {
                list.saveImages(model.getDiagram().getImages(), directory);
                new ImageSaver().saveTrainTimetableImages(directory);
            }
    };
    this.saveHtml(action);
}//GEN-LAST:event_trainTimetableListMenuItemActionPerformed

private void fileSaveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileSaveMenuItemActionPerformed
    if (model.getOpenedFile() == null) {
        this.fileSaveAsMenuItemActionPerformed(evt);
        return;
    }
    if (model.getDiagram() == null) {
        this.showError(ResourceLoader.getString("dialog.error.nodiagram"));
        return;
    }
    this.saveModel(model.getOpenedFile());
}//GEN-LAST:event_fileSaveMenuItemActionPerformed

    private void saveModel(final File file) {
        if (model.getDiagram() == null) {
            this.showError(ResourceLoader.getString("dialog.error.nodiagram"));
            return;
        }

        ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.savemodel"), new ModelAction() {
            private String errorMessage = null;

            @Override
            public void run() {
                try {
                    saveModelData(file);
                } catch (LSException e) {
                    LOG.log(Level.WARNING, "Error saving model.", e);
                    errorMessage = ResourceLoader.getString("dialog.error.saving");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error saving model.", e);
                    errorMessage = ResourceLoader.getString("dialog.error.saving");
                }
            }

            @Override
            public void afterRun() {
                if (errorMessage != null) {
                    showError(errorMessage + " " + file.getName());
                } else {
                    model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.MODEL_SAVED, model));
                }
            }
        });
    }

    private void saveModelData(File file) throws LSException {
        FileLoadSave ls = LSFileFactory.getInstance().createLatestForSave();
        ls.save(model.getDiagram(), file);
    }
    
private void fileOpenMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileOpenMenuItemActionPerformed
    // check changes
    final int result = this.checkModelChangedContinue();
    if (result == JOptionPane.CANCEL_OPTION)
        return;
    // loading train diagram
    final JFileChooser xmlFileChooser = getFileChooser(MainFrame.FileChooserType.XML);
    final int retVal = xmlFileChooser.showOpenDialog(this);
    ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.loadmodel"), new ModelAction() {

        private TrainDiagram diagram;
        private String errorMessage;
        private String errorSaveMessage;
        private Exception errorException;

        @Override
        public void run() {
            try {
                if (result == JOptionPane.YES_OPTION) {
                    saveModelData(model.getOpenedFile());
                }
                try {
                    if (retVal == JFileChooser.APPROVE_OPTION) {
                        model.setOpenedFile(xmlFileChooser.getSelectedFile());
                        FileLoadSave ls = LSFileFactory.getInstance().createForLoad(xmlFileChooser.getSelectedFile());
                        diagram = ls.load(xmlFileChooser.getSelectedFile());
                    }
                } catch (LSException e) {
                    LOG.log(Level.WARNING, "Error loading model.", e);
                    if (e.getCause() instanceof FileNotFoundException)
                        errorMessage = ResourceLoader.getString("dialog.error.filenotfound");
                    else if (e.getCause() instanceof IOException)
                        errorMessage = ResourceLoader.getString("dialog.error.loading");
                    else {
                        errorMessage = ResourceLoader.getString("dialog.error.loading");
                        errorException = e;
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error loading model.", e);
                    errorMessage = ResourceLoader.getString("dialog.error.loading");
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error saving model.", e);
                errorSaveMessage = ResourceLoader.getString("dialog.error.saving");
            }
        }

        @Override
        public void afterRun() {
            if (errorSaveMessage != null) {
                showError(errorSaveMessage);
                return;
            }
            if (result == JOptionPane.YES_OPTION) {
                model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.MODEL_SAVED, model));
            }
            if (retVal == JFileChooser.APPROVE_OPTION) {
                if (diagram != null)
                    model.setDiagram(diagram);
                else {
                    String text = errorMessage + " " + xmlFileChooser.getSelectedFile().getName();
                    if (errorException != null)
                        text = text + "\n(" + errorException.getMessage() + ")";
                    showError(text);
                    model.setDiagram(null);
                }
            }
        }
        });
}//GEN-LAST:event_fileOpenMenuItemActionPerformed

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
    // exiting application
    final int result = this.checkModelChangedContinue();
    if (result != JOptionPane.CANCEL_OPTION)
        ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.programclose"), 0, new AbstractModelAction() {
            private String errorMessage;
            @Override
            public void run() {
                try {
                    if (result == JOptionPane.YES_OPTION)
                        saveModelData(model.getOpenedFile());
                    MainFrame.this.cleanUpBeforeApplicationEnd();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error saving model.", e);
                    errorMessage = ResourceLoader.getString("dialog.error.saving");
                }
            }

            @Override
            public void afterRun() {
                if (errorMessage != null) {
                    showError(errorMessage);
                    return;
                }
                // dispose main window - it should close application
                dispose();
                // close application by force (possible problems with web start)
                System.exit(0);
            }
            });
}//GEN-LAST:event_exitMenuItemActionPerformed

private void cleanUpBeforeApplicationEnd() {
    try {
        // save preferences
        AppPreferences prefs = AppPreferences.getPreferences();
        this.saveToPreferences(prefs);
        prefs.save();
    } catch (IOException ex) {
        LOG.log(Level.SEVERE, "Error saving preferences.", ex);
    }
}

private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
    settingsDialog.setLocationRelativeTo(this);
    settingsDialog.setTrainDiagram(model.getDiagram());
    settingsDialog.setVisible(true);
    // check and send event if neccessary
    if (settingsDialog.isDiagramChanged()) {
        model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.SET_DIAGRAM_CHANGED,model));
        model.setModelChanged(true);
    }
}//GEN-LAST:event_settingsMenuItemActionPerformed

private void tucListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tucListMenuItemActionPerformed
    final TrainUnitCyclesList list = new TrainUnitCyclesList(model.getDiagram().getCycles(TrainsCycleType.TRAIN_UNIT_CYCLE));
    this.trainUnitCyclesList(list);
}//GEN-LAST:event_tucListMenuItemActionPerformed

private void dcListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dcListMenuItemActionPerformed
    DriverCyclesList list = new DriverCyclesList(model.getDiagram().getCycles(TrainsCycleType.DRIVER_CYCLE),model.getDiagram().getAttributes());
    this.driverCyclesList(list);
}//GEN-LAST:event_dcListMenuItemActionPerformed

private void allHtmlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allHtmlMenuItemActionPerformed
    final JFileChooser allHtmlFileChooser = getFileChooser(MainFrame.FileChooserType.ALL_HTML);
    int result = allHtmlFileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        final File directory = allHtmlFileChooser.getSelectedFile();

        ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.genoutput"), new ModelAction() {
            private String errorMessage = null;
            
            @Override
            public void run() {
                try {
                    // node timetable
                    NodeTimetablesList ll = new NodeTimetablesList((model.getDiagram().getNet().getNodes()), model.getDiagram());
                    Writer out = openFile(directory, ResourceLoader.getString("out.nodes") + ".html");
                    ll.writeTo(out);
                    out.close();
                    // trains timetable
                    TrainTimetablesList tl = new TrainTimetablesList(model.getDiagram(), model.getDiagram().getTrains(), model.getDiagram().getImages(), TrainTimetablesList.Binding.BOOK, false);
                    out = openFile(directory, ResourceLoader.getString("out.trains") + ".html");
                    tl.writeTo(out);
                    tl.saveImages(model.getDiagram().getImages(), directory);
                    out.close();
                    // engine cycles
                    EngineCyclesList el = new EngineCyclesList(model.getDiagram().getCycles(TrainsCycleType.ENGINE_CYCLE));
                    out = openFile(directory, ResourceLoader.getString("out.ec") + ".html");
                    el.writeTo(out);
                    out.close();
                    // train unit cycles
                    TrainUnitCyclesList tul = new TrainUnitCyclesList(model.getDiagram().getCycles(TrainsCycleType.TRAIN_UNIT_CYCLE));
                    out = openFile(directory, ResourceLoader.getString("out.tuc") + ".html");
                    tul.writeTo(out);
                    out.close();
                    // driver cycles
                    DriverCyclesList dl = new DriverCyclesList(model.getDiagram().getCycles(TrainsCycleType.DRIVER_CYCLE), model.getDiagram().getAttributes());
                    out = openFile(directory, ResourceLoader.getString("out.dc") + ".html");
                    dl.writeTo(out);
                    out.close();
                    // starting positions
                    StartingPositionsList spl = new StartingPositionsList(model.getDiagram());
                    out = openFile(directory, ResourceLoader.getString("out.sp") + ".html");
                    spl.writeTo(out);
                    out.close();

                    new ImageSaver().saveTrainTimetableImages(directory);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, e.getMessage());
                    errorMessage = ResourceLoader.getString("dialog.error.saving");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, e.getMessage());
                    errorMessage = ResourceLoader.getString("dialog.error.saving");
                }
            }

            @Override
            public void afterRun() {
                if (errorMessage != null)
                    showError(errorMessage + " " + allHtmlFileChooser.getSelectedFile().getName());
            }
            });

    }
}//GEN-LAST:event_allHtmlMenuItemActionPerformed

private void fileNewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNewMenuItemActionPerformed
    // check changes
    final int result = this.checkModelChangedContinue();
    if (result == JOptionPane.CANCEL_OPTION)
        return;
    ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.newmodel"), new ModelAction() {
            private String errorMessage;
            @Override
            public void run() {
                try {
                    if (result == JOptionPane.YES_OPTION)
                        saveModelData(model.getOpenedFile());
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error saving model.", e);
                    errorMessage = ResourceLoader.getString("dialog.error.saving");
                }
            }

            @Override
            public void afterRun() {
                if (errorMessage != null) {
                    showError(errorMessage);
                    return;
                }
                if (result == JOptionPane.YES_OPTION) {
                    model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.MODEL_SAVED, model));
                }
                // create new model
                newModelDialog.setLocationRelativeTo(MainFrame.this);
                newModelDialog.setVisible(true);
            }
    });
}//GEN-LAST:event_fileNewMenuItemActionPerformed

private void imagesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imagesMenuItemActionPerformed
    imagesDialog.setLocationRelativeTo(this);
    imagesDialog.setVisible(true);
}//GEN-LAST:event_imagesMenuItemActionPerformed

private void infoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoMenuItemActionPerformed
    infoDialog.setLocationRelativeTo(this);
    infoDialog.updateValues();
    infoDialog.setVisible(true);
}//GEN-LAST:event_infoMenuItemActionPerformed

private void fileSaveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileSaveAsMenuItemActionPerformed
    if (model.getDiagram() == null) {
        this.showError(ResourceLoader.getString("dialog.error.nodiagram"));
        return;
    }
    // saving train diagram
    JFileChooser xmlFileChooser = getFileChooser(MainFrame.FileChooserType.XML);
    int retVal = xmlFileChooser.showSaveDialog(this);
    if (retVal == JFileChooser.APPROVE_OPTION) {
        model.setOpenedFile(xmlFileChooser.getSelectedFile());
        this.saveModel(xmlFileChooser.getSelectedFile());
    }
}//GEN-LAST:event_fileSaveAsMenuItemActionPerformed

private void trainTimetableListByDcMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainTimetableListByDcMenuItemActionPerformed
    trainTimetableListByDc(model.getDiagram().getCycles(TrainsCycleType.DRIVER_CYCLE));
}//GEN-LAST:event_trainTimetableListByDcMenuItemActionPerformed

private void languageRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageRadioButtonMenuItemActionPerformed
    if (systemLanguageRadioButtonMenuItem.isSelected())
        locale = null;
    else if (evt.getSource() instanceof LanguageMenuBuilder.LanguageMenuItem) {
        locale = ((LanguageMenuBuilder.LanguageMenuItem)evt.getSource()).getLanguage();
    }
}//GEN-LAST:event_languageRadioButtonMenuItemActionPerformed

private void outputLanguageRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputLanguageRadioButtonMenuItemActionPerformed
    if (oSystemLRadioButtonMenuItem.isSelected())
        Templates.setLocale(null);
    else if (evt.getSource() instanceof LanguageMenuBuilder.LanguageMenuItem) {
        Templates.setLocale(((LanguageMenuBuilder.LanguageMenuItem)evt.getSource()).getLanguage());
    }
}//GEN-LAST:event_outputLanguageRadioButtonMenuItemActionPerformed

private void spListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spListMenuItemActionPerformed
    final StartingPositionsList list = new StartingPositionsList(model.getDiagram());
    HtmlAction action = new HtmlAction() {
            @Override
            public void write(Writer writer) throws Exception {
                list.writeTo(writer);
            }

            @Override
            public void writeToDirectory(File directory) throws Exception {
                // do nothing
            }
    };
    this.saveHtml(action);
}//GEN-LAST:event_spListMenuItemActionPerformed

private void trainTypesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainTypesMenuItemActionPerformed
    trainTypesDialog.updateValues();
    trainTypesDialog.setLocationRelativeTo(this);
    trainTypesDialog.setVisible(true);
}//GEN-LAST:event_trainTypesMenuItemActionPerformed

private void lineClassesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineClassesMenuItemActionPerformed
    lineClassesDialog.updateValues();
    lineClassesDialog.setLocationRelativeTo(this);
    lineClassesDialog.setVisible(true);
}//GEN-LAST:event_lineClassesMenuItemActionPerformed

private void weightTablesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_weightTablesMenuItemActionPerformed
    engineClassesDialog.updateValues();
    engineClassesDialog.setLocationRelativeTo(this);
    engineClassesDialog.setVisible(true);
}//GEN-LAST:event_weightTablesMenuItemActionPerformed

private void epListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_epListMenuItemActionPerformed
    final EndPositionsList list = new EndPositionsList(model.getDiagram());
    HtmlAction action = new HtmlAction() {
            @Override
            public void write(Writer writer) throws Exception {
                list.writeTo(writer);
            }

            @Override
            public void writeToDirectory(File directory) throws Exception {
                // do nothing
            }
    };
    this.saveHtml(action);
}//GEN-LAST:event_epListMenuItemActionPerformed

private void columnsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_columnsMenuItemActionPerformed
    trainsPane.editColumns();
}//GEN-LAST:event_columnsMenuItemActionPerformed

private void sortColumnsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortColumnsMenuItemActionPerformed
    trainsPane.sortColumns();
}//GEN-LAST:event_sortColumnsMenuItemActionPerformed

private void resizeColumnsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizeColumnsMenuItemActionPerformed
    trainsPane.resizeColumns();
}//GEN-LAST:event_resizeColumnsMenuItemActionPerformed

private void recalculateStopsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recalculateStopsMenuItemActionPerformed
    // get ratio
    String ratioStr = JOptionPane.showInputDialog(this, ResourceLoader.getString("recalculate.stops.ratio"), "0.5");

    if (ratioStr == null)
        return;

    // convert do double
    double cRatio = 1.0d;
    try {
        cRatio = Double.parseDouble(ratioStr);
    } catch (NumberFormatException e) {
        LOG.warning("Cannot convert to double: " + ratioStr);
        return;
    }

    final double ratio = cRatio;

    ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.recalculate"), new ModelAction() {

        @Override
        public void run() {
            // recalculate all trains
            for (Train train : model.getDiagram().getTrains()) {
                // convert stops ...
                for (TimeInterval interval : train.getTimeIntervalList()) {
                    if (interval.isInnerStop()) {
                        // recalculate time ...
                        int time = interval.getLength();
                        time = this.convertTime(time, ratio);
                        // change stop time
                        train.changeStopTime(interval, time);
                    }
                }
                int time = 0;
                // convert time before
                if (train.getTimeBefore() != 0) {
                    time = train.getTimeBefore();
                    time = this.convertTime(time, ratio);
                    train.setTimeBefore(time);
                }
                // convert time after
                if (train.getTimeAfter() != 0) {
                    time = train.getTimeAfter();
                    time = this.convertTime(time, ratio);
                    train.setTimeAfter(time);
                }
            }
        }

        private int convertTime(int time, double convertRatio) {
            // recalculate
            time = (int)(convertRatio * time);
            // round to minutes
            time = ((time + 40) / 60) * 60;
            // do not change stop to 0
            time = (time == 0) ? 1 : time;
            return time;
        }

        @Override
        public void afterRun() {
            model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.SET_DIAGRAM_CHANGED, model));
            // set back modified status (SET_DIAGRAM_CHANGED unfortunately clears the modified status)
            model.setModelChanged(true);
        }
    });
}//GEN-LAST:event_recalculateStopsMenuItemActionPerformed

private void trainTimetableListByTimeFilteredMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainTimetableListByTimeFilteredMenuItemActionPerformed
    // choose nodes
    SelectNodesDialog dialog = new SelectNodesDialog(this, true);
    dialog.setNodes(model.getDiagram().getNet().getNodes());
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);

    if (dialog.getSelectedNode() == null)
        return;

    List<Train> trains = (new TrainSortByNodeFilter()).sortAndFilter(model.getDiagram().getTrains(), dialog.getSelectedNode());

    // write
    final TrainTimetablesList list = new TrainTimetablesList(
            model.getDiagram(), trains,
            Collections.<TimetableImage>emptyList(), TrainTimetablesList.Binding.BOOK,
            true);
    HtmlAction action = new HtmlAction() {

        @Override
        public void write(Writer writer) throws Exception {
            list.writeTo(writer);
        }

        @Override
        public void writeToDirectory(File directory) throws Exception {
            new ImageSaver().saveTrainTimetableImages(directory);
        }
    };
    this.saveHtml(action);
}//GEN-LAST:event_trainTimetableListByTimeFilteredMenuItemActionPerformed

private void fileImportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileImportMenuItemActionPerformed
    // select imported model
    final JFileChooser xmlFileChooser = getFileChooser(MainFrame.FileChooserType.XML);
    final int retVal = xmlFileChooser.showOpenDialog(this);
    String errorMessage = null;
    TrainDiagram diagram = null;

    try {
        if (retVal == JFileChooser.APPROVE_OPTION) {
            FileLoadSave ls = LSFileFactory.getInstance().createForLoad(xmlFileChooser.getSelectedFile());
            diagram = ls.load(xmlFileChooser.getSelectedFile());
        } else {
            // skip the rest
            return;
        }
    } catch (LSException e) {
        LOG.log(Level.WARNING, "Error loading model.", e);
        if (e.getCause() instanceof FileNotFoundException)
            errorMessage = ResourceLoader.getString("dialog.error.filenotfound");
        else
            errorMessage = ResourceLoader.getString("dialog.error.loading");
    } catch (Exception e) {
        LOG.log(Level.WARNING, "Error loading model.", e);
        errorMessage = ResourceLoader.getString("dialog.error.loading");
    }

    if (errorMessage != null) {
        String text = errorMessage + " " + xmlFileChooser.getSelectedFile().getName();
        showError(text);
        return;
    }

    importDialog.setTrainDiagrams(model.getDiagram(), diagram);
    importDialog.setLocationRelativeTo(this);
    importDialog.setVisible(true);

    this.processImportedObjects(importDialog.getImportedObjects());
}//GEN-LAST:event_fileImportMenuItemActionPerformed

private void trainTimetableListByDcSelectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainTimetableListByDcSelectMenuItemActionPerformed
    ElementSelectionDialog<TrainsCycle> selDialog = new ElementSelectionDialog<TrainsCycle>(this, true);
    selDialog.setLocationRelativeTo(this);
    List<TrainsCycle> selection = selDialog.selectElements(model.getDiagram().getCycles(TrainsCycleType.DRIVER_CYCLE));
    if (selection != null)
        this.trainTimetableListByDc(selection);
}//GEN-LAST:event_trainTimetableListByDcSelectMenuItemActionPerformed

private void dcListSelectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dcListSelectMenuItemActionPerformed
    ElementSelectionDialog<TrainsCycle> selDialog = new ElementSelectionDialog<TrainsCycle>(this, true);
    selDialog.setLocationRelativeTo(this);
    List<TrainsCycle> selection = selDialog.selectElements(model.getDiagram().getCycles(TrainsCycleType.DRIVER_CYCLE));
    if (selection != null) {
        DriverCyclesList list = new DriverCyclesList(selection, model.getDiagram().getAttributes());
        this.driverCyclesList(list);
    }

}//GEN-LAST:event_dcListSelectMenuItemActionPerformed

private void tucListSelectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tucListSelectMenuItemActionPerformed
    ElementSelectionDialog<TrainsCycle> selDialog = new ElementSelectionDialog<TrainsCycle>(this, true);
    selDialog.setLocationRelativeTo(this);
    List<TrainsCycle> selection = selDialog.selectElements(model.getDiagram().getCycles(TrainsCycleType.TRAIN_UNIT_CYCLE));
    if (selection != null) {
        TrainUnitCyclesList list = new TrainUnitCyclesList(selection);
        this.trainUnitCyclesList(list);
    }
}//GEN-LAST:event_tucListSelectMenuItemActionPerformed

private void ecListSelectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ecListSelectMenuItemActionPerformed
    ElementSelectionDialog<TrainsCycle> selDialog = new ElementSelectionDialog<TrainsCycle>(this, true);
    selDialog.setLocationRelativeTo(this);
    List<TrainsCycle> selection = selDialog.selectElements(model.getDiagram().getCycles(TrainsCycleType.ENGINE_CYCLE));
    if (selection != null) {
        final EngineCyclesList list = new EngineCyclesList(selection);
        this.engineCyclesList(list);
    }

}//GEN-LAST:event_ecListSelectMenuItemActionPerformed

private void nodeTimetableListSelectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodeTimetableListSelectMenuItemActionPerformed
    ElementSelectionDialog<Node> selDialog = new ElementSelectionDialog<Node>(this, true);
    selDialog.setLocationRelativeTo(this);
    List<Node> selection = selDialog.selectElements(new ArrayList<Node>(model.getDiagram().getNet().getNodes()));
    if (selection != null) {
        final NodeTimetablesList list = new NodeTimetablesList(selection, model.getDiagram());
        this.nodeTimetablesList(list);
    }
}//GEN-LAST:event_nodeTimetableListSelectMenuItemActionPerformed

private void penaltyTableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_penaltyTableMenuItemActionPerformed
    TrainTypesCategoriesDialog dialog = new TrainTypesCategoriesDialog(this, true);
    dialog.setTrainDiagram(model.getDiagram());
    dialog.updateValues();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);

}//GEN-LAST:event_penaltyTableMenuItemActionPerformed

    private void trainTimetableListByDc(final List<TrainsCycle> cycles) {
        final JFileChooser allHtmlFileChooser = getFileChooser(MainFrame.FileChooserType.ALL_HTML);
        int result = allHtmlFileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            final File directory = allHtmlFileChooser.getSelectedFile();

            ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.genoutput"), new ModelAction() {
                private String errorMessage = null;

                @Override
                public void run() {
                    try {
                        Writer out = null;
                        // trains timetable - for each DC
                        for (TrainsCycle dc : cycles) {
                            // get list of trains
                            List<Train> trains = new LinkedList<Train>();
                            for (TrainsCycleItem item : dc) {
                                trains.add(item.getTrain());
                            }
                            TrainTimetablesList tl = new TrainTimetablesList(model.getDiagram(), trains, Collections.<TimetableImage>emptyList(), TrainTimetablesList.Binding.BOOK, true);
                            out = openFile(directory, ResourceLoader.getString("out.trains") + "_" + dc.getName() + ".html");
                            tl.writeTo(out);
                            out.close();
                        }

                        new ImageSaver().saveTrainTimetableImages(directory);
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, e.getMessage());
                        errorMessage = ResourceLoader.getString("dialog.error.saving");
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, e.getMessage());
                        errorMessage = ResourceLoader.getString("dialog.error.saving");
                    }
                }

                @Override
                public void afterRun() {
                    if (errorMessage != null) {
                        showError(errorMessage + " " + allHtmlFileChooser.getSelectedFile().getName());
                    }
                }
                });
        }
    }

    private void driverCyclesList(final DriverCyclesList list) {
        HtmlAction action = new HtmlAction() {
                @Override
                public void write(Writer writer) throws Exception {
                    list.writeTo(writer);
                }

                @Override
                public void writeToDirectory(File directory) throws Exception {
                    // do nothing
                }
        };
        this.saveHtml(action);
    }

    private void nodeTimetablesList(final NodeTimetablesList list) {
        HtmlAction action = new HtmlAction() {
                @Override
                public void write(Writer writer) throws Exception {
                    list.writeTo(writer);
                }

                @Override
                public void writeToDirectory(File directory) throws Exception {
                    // do nothing
                }
        };
        this.saveHtml(action);
    }

    private void trainUnitCyclesList(final TrainUnitCyclesList list) {
        HtmlAction action = new HtmlAction() {
                @Override
                public void write(Writer writer) throws Exception {
                    list.writeTo(writer);
                }

                @Override
                public void writeToDirectory(File directory) throws Exception {
                    // do nothing
                }
        };
        this.saveHtml(action);
    }

    private void engineCyclesList(final EngineCyclesList list) {
        HtmlAction action = new HtmlAction() {
                @Override
                public void write(Writer writer) throws Exception {
                    list.writeTo(writer);
                }

                @Override
                public void writeToDirectory(File directory) throws Exception {
                    // do nothing
                }
        };
        this.saveHtml(action);
    }

    private void processImportedObjects(Set<ObjectWithId> objects) {
        boolean trainTypesEvent = false;
        for (ObjectWithId o : objects) {
            // process new trains
            if (o instanceof Train) {
                model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.NEW_TRAIN, model, o));
            } else if (o instanceof Node) {
                model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.NEW_NODE, model, o));
            } else if (o instanceof TrainType) {
                trainTypesEvent = true;
            }
        }
        if (trainTypesEvent) {
            model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.TRAIN_TYPES_CHANGED, model));
        }
    }

    private void setSelectedLocale() {
        if (locale == null)
            systemLanguageRadioButtonMenuItem.setSelected(true);
        else {
            for (Enumeration<AbstractButton> en = languageButtonGroup.getElements(); en.hasMoreElements();) {
                AbstractButton e = en.nextElement();
                if (e instanceof LanguageMenuBuilder.LanguageMenuItem) {
                    LanguageMenuBuilder.LanguageMenuItem item = (LanguageMenuBuilder.LanguageMenuItem)e;
                    if (locale.equals(item.getLanguage())) {
                        item.setSelected(true);
                        return;
                    }
                }
            }
            systemLanguageRadioButtonMenuItem.setSelected(true);
        }
    }

    private void setSelectedTemplateLocale() {
        if (Templates.getLocale() == null)
            oSystemLRadioButtonMenuItem.setSelected(true);
        else {
            for (Enumeration<AbstractButton> en = outputLbuttonGroup.getElements(); en.hasMoreElements();) {
                AbstractButton e = en.nextElement();
                if (e instanceof LanguageMenuBuilder.LanguageMenuItem) {
                    LanguageMenuBuilder.LanguageMenuItem item = (LanguageMenuBuilder.LanguageMenuItem)e;
                    if (Templates.getLocale().equals(item.getLanguage())) {
                        item.setSelected(true);
                        return;
                    }
                }
            }
            oSystemLRadioButtonMenuItem.setSelected(true);
        }
    }

    private Writer openFile(File directory,String name) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File(directory, name);
        Writer writer = new OutputStreamWriter(new FileOutputStream(f),"utf-8");
        return writer;
    }

    private void saveHtml(final HtmlAction action) {
        final JFileChooser outputFileChooser = getFileChooser(MainFrame.FileChooserType.OUTPUT);
        int retVal = outputFileChooser.showSaveDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            ActionHandler.getInstance().executeAction(this, ResourceLoader.getString("wait.message.genoutput"), new ModelAction() {
                private String errorMessage;
                
                @Override
                public void run() {
                    try {
                        Writer writer = new OutputStreamWriter(new FileOutputStream(outputFileChooser.getSelectedFile()), "utf-8");
                        action.write(writer);
                        writer.close();
                        action.writeToDirectory(outputFileChooser.getSelectedFile().getParentFile());
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, e.getMessage(), e);
                        errorMessage = ResourceLoader.getString("dialog.error.saving");
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, e.getMessage(), e);
                        errorMessage = ResourceLoader.getString("dialog.error.saving");
                    }
                }

                @Override
                public void afterRun() {
                    if (errorMessage != null)
                        showError(errorMessage + " " + outputFileChooser.getSelectedFile().getName());
                }
            });
        }
    }

    private void showError(String text) {
        JOptionPane.showMessageDialog(this, text, ResourceLoader.getString("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
    }
    
    @Override
    public void saveToPreferences(AppPreferences prefs) {
        boolean maximized = (this.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        prefs.removeWithPrefix("main.");

        prefs.setBoolean("main.maximized", maximized);

        if (!maximized) {
            // set position
            String positionStr = GuiUtils.getPosition(this);
            prefs.setString("main.position", positionStr);
        }
        
        // save to preferences last file chooser directories
        prefs.setString("last.directory.model", getFileChooser(MainFrame.FileChooserType.XML).getCurrentDirectory().getAbsolutePath());
        prefs.setString("last.directory.output", getFileChooser(MainFrame.FileChooserType.OUTPUT).getCurrentDirectory().getAbsolutePath());
        prefs.setString("last.directory.html.dir", getFileChooser(MainFrame.FileChooserType.ALL_HTML).getCurrentDirectory().getAbsolutePath());
        
        // save locales
        if (locale != null)
            prefs.setString("locale.program", locale.toString());
        else
            prefs.remove("locale.program");
        if (Templates.getLocale() != null)
            prefs.setString("locale.output", Templates.getLocale().toString());
        else
            prefs.remove("locale.output");
        
        trainsPane.saveToPreferences(prefs);
        floatingDialogsList.saveToPreferences(prefs);
    }
    
    @Override
    public void loadFromPreferences(AppPreferences prefs) {
        if (!prefs.contains("main.maximized")) {
            return;
        }
        if (prefs.getBoolean("main.maximized", false)) {
            // setting maximized state
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            // set position
            GuiUtils.setPosition(prefs.getString("main.position", null), this);
        }
        trainsPane.loadFromPreferences(prefs);
        floatingDialogsList.loadFromPreferences(prefs);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        floatingDialogsList.setVisibleOnInit();
        // set focus back on the frame
        this.requestFocus();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem allHtmlMenuItem;
    private javax.swing.JPanel applicationPanel;
    private javax.swing.JMenuItem dcListMenuItem;
    private javax.swing.JMenuItem dcListSelectMenuItem;
    private net.parostroj.timetable.gui.panes.TrainsCyclesPane driverCyclesPane;
    private javax.swing.JMenuItem ecListMenuItem;
    private javax.swing.JMenuItem ecListSelectMenuItem;
    private net.parostroj.timetable.gui.panes.TrainsCyclesPane engineCyclesPane;
    private javax.swing.JMenuItem epListMenuItem;
    private javax.swing.JMenuItem fileImportMenuItem;
    private javax.swing.JMenuItem fileOpenMenuItem;
    private javax.swing.JMenuItem fileSaveAsMenuItem;
    private javax.swing.JMenuItem fileSaveMenuItem;
    private javax.swing.JMenuItem imagesMenuItem;
    private javax.swing.JMenuItem infoMenuItem;
    private javax.swing.ButtonGroup languageButtonGroup;
    private javax.swing.JMenu languageMenu;
    private javax.swing.JMenuItem lineClassesMenuItem;
    private net.parostroj.timetable.gui.panes.NetPane netPane;
    private javax.swing.JMenuItem nodeTimetableListMenuItem;
    private javax.swing.JMenuItem nodeTimetableListSelectMenuItem;
    private javax.swing.JMenu oLanguageMenu;
    private javax.swing.JRadioButtonMenuItem oSystemLRadioButtonMenuItem;
    private javax.swing.ButtonGroup outputLbuttonGroup;
    private javax.swing.JMenuItem penaltyTableMenuItem;
    private javax.swing.JMenuItem recalculateMenuItem;
    private javax.swing.JMenuItem recalculateStopsMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JMenuItem spListMenuItem;
    private net.parostroj.timetable.gui.StatusBar statusBar;
    private javax.swing.JRadioButtonMenuItem systemLanguageRadioButtonMenuItem;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JMenuItem trainTimetableListByDcMenuItem;
    private javax.swing.JMenuItem trainTimetableListByDcSelectMenuItem;
    private javax.swing.JMenuItem trainTimetableListByTimeFilteredMenuItem;
    private javax.swing.JMenuItem trainTimetableListMenuItem;
    private javax.swing.JMenuItem trainTypesMenuItem;
    private net.parostroj.timetable.gui.panes.TrainsCyclesPane trainUnitCyclesPane;
    private net.parostroj.timetable.gui.panes.TrainsPane trainsPane;
    private javax.swing.JMenuItem tucListMenuItem;
    private javax.swing.JMenuItem tucListSelectMenuItem;
    private javax.swing.JMenu viewsMenu;
    private javax.swing.JMenuItem weightTablesMenuItem;
    // End of variables declaration//GEN-END:variables
}

interface HtmlAction {
    public void write(Writer writer) throws Exception;
    
    public void writeToDirectory(File directory) throws Exception;
}
