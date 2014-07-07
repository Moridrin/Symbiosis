package equa.desktop;

import static equa.code.CodeNames.TEMPLATE;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.xml.sax.SAXException;

import com.mxgraph.util.mxResources;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.DockingSelectorDialog;
import com.vlsolutions.swing.docking.event.DockableSelectionEvent;
import com.vlsolutions.swing.docking.event.DockableSelectionListener;

import equa.code.CodeClass;
import equa.code.operations.Operation;
import equa.configurator.AbstractObjectTypeDialog;
import equa.configurator.InheritanceDialog;
import equa.configurator.TypeConfigurator;
import equa.controller.IView;
import equa.controller.PersistanceManager;
import equa.controller.SwingProjectController;
import equa.diagram.cd.ClassDiagram;
import equa.diagram.cd.ClassDiagramPanel;
import equa.factbreakdown.gui.FactBreakdown;
import equa.factbreakdown.gui.RequirementsBreakdownPanel;
import equa.inspector.InspectorTreeNode;
import equa.inspector.ProjectInspector;
import equa.meta.DuplicateException;
import equa.meta.Message;
import equa.meta.MismatchException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.traceability.Category;
import equa.project.Project;
import equa.project.ProjectRole;
import equa.project.dialog.CategoryDialog;
import equa.project.dialog.CodeGeneratorDialog;
import equa.project.dialog.LoginDialog;
import equa.project.dialog.NewProjectWizardProvider;
import equa.project.dialog.OpenProjectJPADialog;
import equa.project.dialog.ParticipantDialog;
import equa.requirementsGui.RequirementViewer;
import fontys.observer.PropertyListener;
import java.awt.HeadlessException;

/**
 * The application's main frame.
 */
public final class Desktop extends FrameView implements PropertyListener, IView, DockableSelectionListener {

    private SwingProjectController projectController;
    private ProjectNavigator projectNavigator;
    private ProjectInspector projectInspector;
    private FactBreakdown factBreakdown;
    private RequirementViewer requirementViewer;
    private RequirementsBreakdownPanel requirementsBreakdownPanel;
    private TypeConfigurator typeConfigurator;
    private java.util.Timer autoSaveTimer;
    private final static long AUTO_SAVE_DELAY = 120000; //Delay between autosaves in miliseconds.
    private boolean autosave;
    private File[] autosaveFiles;
    private int autosaveNext;
    private DockingDesktop dockingRoot;
    private boolean init;
    private javax.swing.JMenuItem behaviorAsTextMenuItem;
    private javax.swing.JMenuItem chooserMenuItem;
    private javax.swing.JMenu generateMenu;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem generalMenuItem;
    private javax.swing.JMenuItem generateCDMenuItem;
    //SR
    private javax.swing.JMenuItem generateCodeMenuItem;
    private javax.swing.JMenuItem importCodeMenuItem;
    private javax.swing.JMenuItem imageClassDiagramMenuItem;
    private javax.swing.JMenuItem mifontsizeFactBreakdown;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem miAbstractObjectType;
    private javax.swing.JMenuItem miBehavior;
    private javax.swing.JMenuItem miBehaviorWithRegistries;
    private javax.swing.JMenuItem miInheritance;
    private javax.swing.JMenuItem miRemoveBehavior;
    private javax.swing.JMenuItem miScan;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenu objectMenu;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem participantsMenuItem;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenu requirementMenu;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenu useCaseMenu;
    private javax.swing.JMenuItem vocabularyMenuItem;
    private javax.swing.JMenuItem changeProjectRoleMenuItem;
    private javax.swing.JMenu windowMenu;
    private javax.swing.Timer messageTimer;
    private javax.swing.Timer busyIconTimer;
    private Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JMenuItem categoriesMenuItem;
    private JMenuItem importReqMenuItem;
    private boolean saveNeeded;
    private JMenu projectMenu;
    private JMenu helpMenu;
    private JCheckBoxMenuItem autosaveMenuItem;

    public Desktop(SingleFrameApplication app) {
        super(app);
        timerShowAutoBox();

        projectController = SwingProjectController.getReference();
        projectController.addView(this);
        projectController.setDesktop(this);
        autosave = true;
        initComponents();

        dockingRoot = new DockingDesktop();
        dockingRoot.addDockableSelectionListener(this);
        dockingRoot.setAutoscrolls(true);
        mainPanel.add(dockingRoot, BorderLayout.CENTER);

        init = false;
        saveNeeded = false;

        initAutosave();
        initListenersAndMenu();

        this.getFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.getFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                quitApplication();
            }
        });
    }

    public Desktop(SingleFrameApplication app, File startUp) {
        this(app);
        openProject(startUp);
    }

    public DockingDesktop getDockingRoot() {
        return dockingRoot;
    }

    private void closeDeprecatedTabs() {
        //Close all tabs from previous projects
        //Close all CD, ORD dockingpanels.
        if (dockingRoot == null) {
            return;
        }

        for (DockableState ds : dockingRoot.getDockables()) {
            Dockable d = ds.getDockable();
            if (d instanceof ClassDiagramPanel) {
                dockingRoot.remove(d);
                dockingRoot.unregisterDockable(d);
            }
        }

    }

    public void initDockingRoot() {
        if (projectNavigator != null) {
            dockingRoot.close(projectNavigator);
        }
        //    projectNavigator = new ProjectNavigator(this);
        if (projectInspector != null) {
            dockingRoot.close(projectInspector);
        }
        //    projectInspector = new ProjectInspector(this);

        if (requirementViewer != null) {
            projectController.removeView(requirementViewer);
            dockingRoot.close(requirementViewer);
        }
        requirementViewer = new RequirementViewer(projectController);
        dockingRoot.addDockable(requirementViewer);

        if (factBreakdown != null) {
            dockingRoot.close(factBreakdown);
        }
        factBreakdown = new FactBreakdown(projectController);
        dockingRoot.createTab(requirementViewer, factBreakdown, 1, false);

        if (typeConfigurator != null) {

            dockingRoot.close(typeConfigurator);
        }
        typeConfigurator = new TypeConfigurator(getFrame(), projectController.getProject().getObjectModel());
        dockingRoot.createTab(requirementViewer, typeConfigurator, 2, false);

        // Dockables gets 85% of the space
        //   dockingRoot.split(requirementViewer, projectNavigator, DockingConstants.SPLIT_LEFT, 0.15);
        // Dockables loses 25% of the current 85% to propertyEditor
        // dockingRoot.split(factDecomposer, propertyEditor, DockingConstants.SPLIT_RIGHT, 0.75);
        //   dockingRoot.createTab(projectNavigator, projectInspector, 0, false);
        DockableSelectionListener listener = new DockableSelectionListener() {
            @Override
            public void selectionChanged(DockableSelectionEvent event) {
                Dockable dockable = event.getSelectedDockable();
                if (dockable == typeConfigurator) {
                    typeConfigurator.refresh();
                }
                if (dockable == requirementViewer) {
                    requirementViewer.refresh();
                }
            }
        };
        dockingRoot.addDockableSelectionListener(listener);
    }

    public Project getCurrentProject() {
        return projectController.getProject();
    }

    public ProjectNavigator getProjectNavigator() {
        return projectNavigator;
    }

    public ProjectInspector getProjectInspector() {
        return projectInspector;
    }

    /**
     * Initializes the statusbar at the bottom of the main form.
     */
    private void initStatusBar() {
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new javax.swing.Timer(messageTimeout, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new javax.swing.Timer(busyAnimationRate, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                switch (propertyName) {
                    case "started":
                        if (!busyIconTimer.isRunning()) {
                            statusAnimationLabel.setIcon(busyIcons[0]);
                            busyIconIndex = 0;
                            busyIconTimer.start();
                        }
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(true);
                        break;
                    case "done":
                        busyIconTimer.stop();
                        statusAnimationLabel.setIcon(idleIcon);
                        progressBar.setVisible(false);
                        progressBar.setValue(0);
                        break;
                    case "message":
                        String text = (String) (evt.getNewValue());
                        statusMessageLabel.setText((text == null) ? "" : text);
                        messageTimer.restart();
                        break;
                    case "progress":
                        int value = (Integer) (evt.getNewValue());
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(value);
                        break;
                }
            }
        });
    }

    /**
     * Shows an EQuA_AboutBox when the about menu-item is clicked.
     */
    public void showAboutBox() {
        getFrame().setIconImage(Toolkit.getDefaultToolkit().getImage("images\\symbiosis2.png"));
        if (aboutBox == null) {
            JFrame mainFrame = Symbiosis.getApplication().getMainFrame();
            aboutBox = new AboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Symbiosis.getApplication().show(aboutBox);
    }

    /**
     * Saves the current windowstates within the dockingcontrol to an XML file
     * dockingstate.xml
     *
     * @throws IOException
     */
    public void saveDockingState() {
        BufferedOutputStream out = null;
        try {
            File saveFile = new File("dockingstate.xml");
            if (!saveFile.exists()) {

                saveFile.createNewFile();
            }
            out = new BufferedOutputStream(new FileOutputStream(saveFile));
            dockingRoot.writeXML(out);
            out.close();

        } catch (IOException ex) {
            Logger.getLogger(Desktop.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Desktop.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Loads the saved windowstates within the dockingcontrol from the XML file
     * dockingstate.xml, if this file exists the dockingcontrol wil adjust to
     * these settings.
     *
     * @throws IOException
     */
    public final void loadDockingState() {
        File loadFile = new File("dockingstate.xml");

        if (loadFile.exists()) {
            try {
                initDockingRoot();
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(loadFile))) {
                    dockingRoot.readXML(in);
                }
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(Desktop.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void quitApplication() {
        boolean exit;
        if (projectController.getProject() == null) {
            exit = true;
        } else {
            exit = !saveNeeded || showSavePrompt();
        }
        if (exit == true) {
            this.getApplication().exit();
            PersistanceManager.getReference().close();
        }
    }

    /**
     * Disables the main menu so only the new and open items are accessible.
     *
     */
    public void setMenuStateStartUp() {
        projectMenu.setEnabled(true);
        newMenuItem.setEnabled(true);
        openMenuItem.setEnabled(true);
        helpMenu.setEnabled(true);

        Project project = projectController.getProject();
        if (project == null) {
            printMenuItem.setEnabled(false);
            viewMenu.setEnabled(false);
            windowMenu.setEnabled(false);
            requirementMenu.setEnabled(false);
            useCaseMenu.setEnabled(false);
            objectMenu.setEnabled(false);
            generateMenu.setEnabled(false);
            saveMenuItem.setEnabled(false);
            saveAsMenuItem.setEnabled(false);
            participantsMenuItem.setEnabled(false);
            vocabularyMenuItem.setEnabled(false);
            imageClassDiagramMenuItem.setEnabled(false);
            behaviorAsTextMenuItem.setEnabled(false);

            categoriesMenuItem.setEnabled(false);
            changeProjectRoleMenuItem.setEnabled(false);

            useCaseMenu.setEnabled(false);
            requirementMenu.setEnabled(false);
            objectMenu.setEnabled(false);
            generateMenu.setEnabled(false);
        } else {
            printMenuItem.setEnabled(true);
            viewMenu.setEnabled(true);
            windowMenu.setEnabled(true);
            requirementMenu.setEnabled(true);
            useCaseMenu.setEnabled(true);
            objectMenu.setEnabled(true);
            generateMenu.setEnabled(true);
            printMenuItem.setEnabled(true);

            saveMenuItem.setEnabled(true);
            saveAsMenuItem.setEnabled(true);
            participantsMenuItem.setEnabled(true);
            vocabularyMenuItem.setEnabled(true);
            imageClassDiagramMenuItem.setEnabled(true);
            behaviorAsTextMenuItem.setEnabled(true);

            categoriesMenuItem.setEnabled(true);
            changeProjectRoleMenuItem.setEnabled(true);

        }
        if (requirementViewer != null) {
            requirementViewer.refresh();
        }
    }

    /**
     * Shows an input dialog with the given text as a message.
     *
     * @param text The message shown to the user.
     * @return The given input.
     */
    private String showInputTabDialog(String text) {
        JOptionPane pane = new JOptionPane();
        String inputValue = JOptionPane.showInputDialog(text);
        if (inputValue != null && inputValue.isEmpty()) {
            inputValue = showInputTabDialog(text);
        }
        for (DockableState d : dockingRoot.getDockables()) {
            if (d.getDockable().getDockKey().getName().equals(inputValue)) {
                JOptionPane.showMessageDialog(pane, "Name of Tab already exists.");
                inputValue = showInputTabDialog(text);
            }
        }
        return inputValue;
    }

    /**
     * Writes the given facttype's behavior into the selected file.
     *
     * @param ft The facttype from which the behavior should be written.
     * @param out The out-stream which is used to write the file.
     */
    private void writeBehavior(FactType ft, PrintWriter out) {
        StringBuilder sb = new StringBuilder();
        if (ft.isEnum()) {
            sb.append("public enum ");
        } else {
            sb.append("public class ");
        }
        sb.append(ft.getName()).append(" ").append(inheritString(ft.getObjectType())).append(" {");

        out.println(sb.toString());
        CodeClass operations = ft.getObjectType().getCodeClass();
        Iterator<Operation> itOperations = operations.getOperations();
        while (itOperations.hasNext()) {
            Operation feature = itOperations.next();
            out.println("\t" + feature.toString());
        }
        out.println("}");
        out.println();
    }

    private static String inheritString(ObjectType ot) {
        String s = ot.getFactType().inheritsString();
        if (s.isEmpty()) {
            return s;
        } else {
            return "inherits " + s;
        }
    }

    /**
     * Prompts the user if he wants to save the current project first.
     *
     * @return True if the user has selected yes or no.
     */
    public boolean showSavePrompt() {
        boolean result = false;

        //Check to see if there is a project to save.
        if (projectController.getProject() != null && saveNeeded) {
            JOptionPane pane = new JOptionPane("Do you want to save the current project first?", JOptionPane.QUESTION_MESSAGE);
            pane.setOptionType(JOptionPane.YES_NO_CANCEL_OPTION);
            JDialog dialog = pane.createDialog(dockingRoot, "Save project?");
            dialog.setVisible(true);

            Object selectedValue = pane.getValue(); //Result

            if (selectedValue.equals(JOptionPane.YES_OPTION)) {
                // Save
                result = true;
                saveProject(false);

            } else if (selectedValue.equals(JOptionPane.NO_OPTION)) {
                // Dont save
                result = true;
            } else if (selectedValue.equals(JOptionPane.CANCEL_OPTION)) {
                //Dont save
                result = false;
            }
        }
        return result;
    }

    /**
     * Saves the current project and shows a save dialog when this is required.
     *
     * @param showDialog Whether or not the user has to be prompted for the save
     * location.
     */
    private void saveProject(boolean showDialog) {
        try {
            /**
             * If the project hasn't been saved before, or the user has selected
             * 'save as' the user will be prompted for a file location and saved
             * afterwards. If this is not the case the project will be saved to
             * it's existing projectfiles.
             */
            if (projectController.getProject().getFile() == null || projectController.getProject().getFile().exists() == false || showDialog) {
                JFileChooser chooser = new JFileChooser();
                String filename = projectController.getProject().getName() + ".sym";

                chooser.setSelectedFile(new File(filename));
                int result = chooser.showSaveDialog(getFrame());
                if (result == JFileChooser.APPROVE_OPTION) {
                    filename = chooser.getSelectedFile().getName();
                    if (!filename.endsWith(".sym")) {
                        int indexDot = filename.lastIndexOf(".");
                        if (indexDot >= 0) {
                            filename = filename.substring(0, indexDot) + ".sym";
                        } else {
                            filename = filename + ".sym";
                        }
                        chooser.setSelectedFile(new File(filename));
                    }
                    projectController.getProject().setFile(chooser.getSelectedFile());
                }
            }
            if (projectController.getProject().getFile() != null) {
                projectController.getProject().save(projectController.getProject().getFile());
                saveNeeded = false;
                removeAutoSave();
                saveDockingState();

            }
        } catch (IOException ex) {
            Logger.getLogger(Desktop.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Removes the autosave file if it exists.
     */
    public void removeAutoSave() {
        for (int i = 0; i < 10; i++) {
            autosaveFiles[i].delete();
        }
    }

    File autosaveFile() {
        File result = autosaveFiles[autosaveNext];
        autosaveNext = (autosaveNext + 1) % 10;
        return result;
    }

    /**
     * Saves the current project automatically to a file called 'autosave'.
     */
    private void autoSaveProject() {
        saveNeeded = true;
        if (projectController.getProject() != null) {
            try {
                if (autosave) {
                    projectController.getProject().save(autosaveFile());
                }
            } catch (FileNotFoundException ex) { /*Will never occur but must be caught.*/ }
        }
    }

    /**
     * Shows the create new project wizard, sets the currentProject to this new
     * project and refreshes the UI.
     */
    private void createNewProject() {
        NewProjectWizardProvider provider = new NewProjectWizardProvider();
        Wizard wizard = provider.createWizard();
        Point point = getFrame().getLocation();
        Object wizardReturn = WizardDisplayer.showWizard(wizard, new Rectangle(point.x + 15, point.y + 15, 600, 250));
        if (wizardReturn instanceof Project) {
            Project p = (Project) wizardReturn;
            projectController.createNewProject(p);
            projectController.setCurrentUser(p.getCurrentUser());
            refresh();
        }
    }

    /**
     * Checks for the existance of an autosave, if this exists the user will be
     * asked whether or not to recover this project. If 'Yes' was selected the
     * project will be set as the current project.
     */
//    private void loadAutoSave() {
//        File autoSaveFile = new File("autosave.sym");
//        if (autoSaveFile.exists()) {
//            try {
//                Project autoSavedProject = Project.getProject(autoSaveFile);
//                if (JOptionPane.showConfirmDialog(
//                        this.getComponent(),
//                        "An unsaved project '" + autoSavedProject.getName()
//                        + " was found, would you like to recover the last autosave of this project?",
//                        "Project recovery",
//                        0,
//                        1) == 0) {
//                    //User chose to recover the project.
//                    projectController.openProject(autoSaveFile);
//
//                    //Set the currentUser of the project.
//                    LoginDialog loginDialog = new LoginDialog(getFrame(), true, projectController);
//                    loginDialog.setVisible(true);
//                    refresh();
//                }
//            } catch (IOException ex) {
//                JOptionPane.showMessageDialog(getFrame(), ex.getMessage());
//                createNewProject();
//            } catch (ClassNotFoundException ex) {
//                JOptionPane.showMessageDialog(getFrame(), "Projectfile isn't compatible: \n" + ex.getMessage());
//                createNewProject();
//            }
//        }
//    }
    /**
     * Refreshes projectnavigator and projectinspector.
     */
    public void refreshTrees() {
        InspectorTreeNode.setExtended(false);
//        projectNavigator.refresh();
//        projectInspector.refresh();
    }

    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        projectMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        autosaveMenuItem = new javax.swing.JCheckBoxMenuItem("AutoSave");
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        participantsMenuItem = new javax.swing.JMenuItem();
        vocabularyMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        imageClassDiagramMenuItem = new javax.swing.JMenuItem();
        behaviorAsTextMenuItem = new javax.swing.JMenuItem();
        printMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        requirementMenu = new javax.swing.JMenu();
        generalMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        useCaseMenu = new javax.swing.JMenu();
        objectMenu = new javax.swing.JMenu();
        miAbstractObjectType = new javax.swing.JMenuItem();
        miInheritance = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        miBehavior = new javax.swing.JMenuItem();
        miBehaviorWithRegistries = new javax.swing.JMenuItem();
        miRemoveBehavior = new javax.swing.JMenuItem();
        generateMenu = new javax.swing.JMenu();
        generateCDMenuItem = new javax.swing.JMenuItem();
        //SR
        generateCodeMenuItem = new javax.swing.JMenuItem();
        importCodeMenuItem = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();
        chooserMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout());

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(equa.desktop.Symbiosis.class).getContext().getResourceMap(Desktop.class);
        projectMenu.setText(resourceMap.getString("projectMenu.text")); // NOI18N
        projectMenu.setName(
                "projectMenu"); // NOI18N

        newMenuItem.setText(resourceMap.getString("newMenuItem.text")); // NOI18N
        newMenuItem.setName(
                "newMenuItem"); // NOI18N
        newMenuItem.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        newMenuItemActionPerformed(evt);
                    }
                });
        projectMenu.add(newMenuItem);

        openMenuItem.setText(resourceMap.getString("openMenuItem.text")); // NOI18N
        openMenuItem.setName(
                "openMenuItem"); // NOI18N
        openMenuItem.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        openMenuItemActionPerformed(evt);
                    }
                });
        projectMenu.add(openMenuItem);

        saveMenuItem.setText(resourceMap.getString("saveMenuItem.text")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);

            }
        });
        projectMenu.add(saveMenuItem);

        saveAsMenuItem.setText(resourceMap.getString("saveAsMenuItem.text")); // NOI18N
        saveAsMenuItem.setName("saveAsMenuItem"); // NOI18N
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(saveAsMenuItem);

        //autosaveMenuItem.setText(resourceMap.getString("autosaveMenuItem.text")); // NOI18N
        //autosaveMenuItem.setName("autosaveMenuItem"); // NOI18N
        autosaveMenuItem.setSelected(autosave);
        autosaveMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                autosave = !autosave;
            }
        });

        projectMenu.add(autosaveMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        projectMenu.add(jSeparator1);

        participantsMenuItem.setText(resourceMap.getString("participantsMenuItem.text")); // NOI18N
        participantsMenuItem.setName("participantsMenuItem"); // NOI18N
        participantsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                participantsMenuItemActionPerformed(evt);
            }
        });

        categoriesMenuItem = new JMenuItem("Categories");
        categoriesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (projectController.getProject() != null) {
                    CategoryDialog categoriesDialog = new CategoryDialog(projectController);
                    Point point = getFrame().getLocation();
                    point.setLocation(point.x + 25, point.y + 25);
                    categoriesDialog.setLocation(point);
                    categoriesDialog.setVisible(true);
                }
            }
        });
        projectMenu.add(categoriesMenuItem);
        projectMenu.add(participantsMenuItem);

        changeProjectRoleMenuItem = new JMenuItem("Change Project Role");
        changeProjectRoleMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginDialog login = new LoginDialog(null, true, projectController);
                login.setVisible(true);
                setMenuStateStartUp();
            }
        });
        projectMenu.add(changeProjectRoleMenuItem);

        vocabularyMenuItem.setText(resourceMap.getString("vocabularyMenuItem.text")); // NOI18N
        vocabularyMenuItem.setName("vocabularyMenuItem"); // NOI18N
        vocabularyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vocabularyMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(vocabularyMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        projectMenu.add(jSeparator2);

        imageClassDiagramMenuItem.setText(resourceMap.getString("imageClassDiagramMenuItem.text")); // NOI18N
        imageClassDiagramMenuItem.setName("imageClassDiagramMenuItem"); // NOI18N
        imageClassDiagramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageClassDiagramMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(imageClassDiagramMenuItem);

        behaviorAsTextMenuItem.setText(resourceMap.getString("behaviorAsTextMenuItem.text")); // NOI18N
        behaviorAsTextMenuItem.setName("behaviorAsTextMenuItem"); // NOI18N
        behaviorAsTextMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                behaviorAsTextMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(behaviorAsTextMenuItem);

        printMenuItem.setText(resourceMap.getString("printMenuItem.text")); // NOI18N
        printMenuItem.setEnabled(false);
        printMenuItem.setName("printMenuItem"); // NOI18N
        printMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(printMenuItem);

        jSeparator3.setName("jSeparator3"); // NOI18N
        projectMenu.add(jSeparator3);

        quitMenuItem.setText(resourceMap.getString("quitMenuItem.text")); // NOI18N
        quitMenuItem.setName("quitMenuItem"); // NOI18N
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(quitMenuItem);

        menuBar.add(projectMenu);

        viewMenu.setText(resourceMap.getString("viewMenu.text")); // NOI18N
        viewMenu.setName("viewMenu"); // NOI18N
        menuBar.add(viewMenu);

        mifontsizeFactBreakdown = new JMenuItem("Font Size Fact Breakdown");
        viewMenu.add(mifontsizeFactBreakdown);
        mifontsizeFactBreakdown.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String fs = JOptionPane.showInputDialog("Font Size Fact Breakdown", FactBreakdown.getFontSize());
                try {
                    FactBreakdown.setFontSize(Integer.parseInt(fs));
                    factBreakdown.repaint();
                } catch (NumberFormatException exc) {
                    JOptionPane.showMessageDialog(getFrame(), exc.getMessage());
                }
            }
        });
        requirementMenu.setText(resourceMap.getString("requirementMenu.text")); // NOI18N
        requirementMenu.setName("requirementMenu"); // NOI18N

        generalMenuItem.setText(resourceMap.getString("generalMenuItem.text")); // NOI18N
        generalMenuItem.setEnabled(false);
        generalMenuItem.setName("generalMenuItem"); // NOI18N
        requirementMenu.add(generalMenuItem);

        jSeparator4.setName("jSeparator4"); // NOI18N
        requirementMenu.add(jSeparator4);

        JMenuItem addRequirementMenuItem = new JMenuItem("Add Requirement");
        addRequirementMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requirementViewer.openEnterRequirementDialog();
            }
        });
        requirementMenu.add(addRequirementMenuItem);

        importReqMenuItem = new JMenuItem("Import Requirements");
        importReqMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Project project = projectController.getProject();
                if (project == null) {
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(getComponent());
                if (result != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File file = fileChooser.getSelectedFile();

                try {
                    checkLineFeed(file);
                    try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                        Map<String, List<String>> reqs = scanReqs(in);
                        ArrayList<Category> cats = new ArrayList<>();
                        Iterator<Category> it = project.getCategories();
                        while (it.hasNext()) {
                            cats.add(it.next());
                        }
                        cats.remove(Category.SYSTEM);
                        Category cat = (Category) JOptionPane.showInputDialog(getFrame(),
                                "The requirements within " + file.getName()
                                + " belong to which category?",
                                "Input", JOptionPane.QUESTION_MESSAGE, null, cats.toArray(), null);
                        if (cat != null) {
                            project.importReqs(cat, reqs);
                            requirementViewer.refresh();
                        }
                    }
                } catch (MismatchException | IOException exc) {
                    JOptionPane.showMessageDialog(getFrame(), exc.getMessage());
                }
            }

            private Map<String, List<String>> scanReqs(BufferedReader in) throws MismatchException, IOException {
                HashMap<String, List<String>> reqs = new HashMap<>();
                reqs.put("@action", new ArrayList<String>());
                reqs.put("@fact", new ArrayList<String>());
                reqs.put("@rule", new ArrayList<String>());
                reqs.put("@qa", new ArrayList<String>());
                reqs.put("@comment", new ArrayList<String>());

                String line = skipEmptyLines(in);

                while (line != null && isKey(line)) {
                    line = readReqs(line, reqs, in);
                }

                return reqs;
            }

            private String skipEmptyLines(BufferedReader in) throws IOException {
                // skip empty lines
                if (!in.ready()) {
                    return null;
                }
                String line = in.readLine().trim();
                while (line.isEmpty()) {
                    if (in.ready()) {
                        line = in.readLine().trim();
                    } else {
                        return null;
                    }
                }
                return line;
            }

            private boolean isKey(String line) throws MismatchException {
                switch (line.toLowerCase()) {
                    case "@action":
                        return true;
                    case "@fact":
                        return true;
                    case "@rule":
                        return true;
                    case "@qa":
                        return true;
                    case "@comment":
                        return true;
                    default:
                        return false;
                }
            }

            private String readReqs(String key, HashMap<String, List<String>> reqs, BufferedReader in) throws IOException, MismatchException {
                String line = skipEmptyLines(in);
                while (line != null && !isKey(line)) {
                    List<String> list = reqs.get(key);
                    list.add(line);
                    reqs.put(key, list);
                    line = skipEmptyLines(in);
                }
                return line;
            }

            private void checkLineFeed(File file) throws MismatchException, IOException {
                try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                    while (!in.ready()) {
                        if (in.readLine() == null) {
                            throw new MismatchException(null, "requirements file has not properly completed with a line feed");
                        }
                    }
                }
            }
        });
        requirementMenu.add(importReqMenuItem);

        menuBar.add(requirementMenu);

        useCaseMenu.setText(resourceMap.getString("useCaseMenu.text")); // NOI18N
        useCaseMenu.setName("useCaseMenu"); // NOI18N
        menuBar.add(useCaseMenu);

        objectMenu.setText(resourceMap.getString("objectMenu.text")); // NOI18N
        objectMenu.setName("objectMenu"); // NOI18N

        miAbstractObjectType.setText(resourceMap.getString("miAbstractObjectType.text")); // NOI18N
        miAbstractObjectType.setName("miAbstractObjectType"); // NOI18N
        miAbstractObjectType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAbstractObjectTypeActionPerformed(evt);
            }
        });
        objectMenu.add(miAbstractObjectType);

        miInheritance.setText(resourceMap.getString("miInheritance.text")); // NOI18N
        miInheritance.setName("miInheritance"); // NOI18N
        miInheritance.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miInheritanceActionPerformed(evt);
            }
        });
        objectMenu.add(miInheritance);

        jSeparator5.setName("jSeparator5"); // NOI18N
        objectMenu.add(jSeparator5);

        miBehavior.setText(resourceMap.getString("miBehavior.text")); // NOI18N
        miBehavior.setName("miBehavior"); // NOI18N
        miBehavior.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miBehaviorActionPerformed(evt);
            }
        });
        miScan = new javax.swing.JMenuItem();

        miScan.setText(resourceMap.getString("miScan.text")); // NOI18N
        miScan.setEnabled(false);
        miScan.setName("miScan"); // NOI18N
        objectMenu.add(miScan);
        generateMenu.add(miBehavior);

        miBehaviorWithRegistries.setText(resourceMap.getString("miBehaviorWithRegistries.text")); // NOI18N
        miBehaviorWithRegistries.setName("miBehaviorWithRegistries"); // NOI18N
        miBehaviorWithRegistries.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miBehaviorWithRegistriesActionPerformed(evt);
            }
        });
        generateMenu.add(miBehaviorWithRegistries);

        miRemoveBehavior.setText(resourceMap.getString("miRemoveBehavior.text")); // NOI18N
        miRemoveBehavior.setName("miRemoveBehavior"); // NOI18N
        miRemoveBehavior.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miRemoveBehaviorActionPerformed(evt);
            }
        });
        objectMenu.add(miRemoveBehavior);

        menuBar.add(objectMenu);

        generateMenu.setText(resourceMap.getString("diagramMenu.text")); // NOI18N
        generateMenu.setName("diagramMenu"); // NOI18N

        generateCDMenuItem.setText(resourceMap.getString("generateCDMenuItem.text")); // NOI18N
        generateCDMenuItem.setName("generateCDMenuItem"); // NOI18N
        generateCDMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateCDMenuItemActionPerformed(evt);
            }
        });
        generateMenu.add(generateCDMenuItem);

        generateCodeMenuItem.setText(resourceMap.getString("generateCodeMenuItem.text")); // NOI18N
        generateCodeMenuItem.setName("generateCodeMenuItem"); // NOI18N
        generateCodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (projectController.getProject() != null) {
                    CodeGeneratorDialog codeGeneratorDialog = new CodeGeneratorDialog(Desktop.this.getFrame(), projectController);
                    codeGeneratorDialog.setLocationRelativeTo(null);
                    codeGeneratorDialog.setVisible(true);
                }
            }
        });
        generateMenu.add(generateCodeMenuItem);

        importCodeMenuItem.setText(resourceMap.getString("importCodeMenuItem.text")); // NOI18N
        importCodeMenuItem.setName("importCodeMenuItem"); // NOI18N
        importCodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ObjectModel om = projectController.getProject().getObjectModel();
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);
                int action = fileChooser.showOpenDialog(Desktop.this.getFrame());
                if (action == JFileChooser.APPROVE_OPTION) {
                    for (File f : fileChooser.getSelectedFiles()) {
                        ObjectType ot = om.getObjectType(f.getName().replace(TEMPLATE, "").split("\\.")[0]);
                        if (ot != null) {
                            ot.sealOperations(f);
                        }
                    }
                }
            }
        });
        generateMenu.add(importCodeMenuItem);

        menuBar.add(generateMenu);

        windowMenu.setText(resourceMap.getString("windowMenu.text")); // NOI18N
        windowMenu.setName("windowMenu"); // NOI18N

        chooserMenuItem.setText(resourceMap.getString("chooserMenuItem.text")); // NOI18N
        chooserMenuItem.setName("chooserMenuItem"); // NOI18N
        chooserMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooserMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(chooserMenuItem);

        menuBar.add(windowMenu);

        helpMenu.setText("Help"); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setText(
                "About"); // NOI18N
        aboutMenuItem.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        aboutMenuItemActionPerformed(evt);
                    }
                });
        helpMenu.add(aboutMenuItem);

        miScan.setEnabled(
                true);
        miScan.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (projectController.getProject() == null) {
                            return;

                        }

                        ObjectModel om = projectController.getProject().getObjectModel();
                        List<Message> messages = om.scanModel();
                        typeConfigurator.setReliable(messages);
                        typeConfigurator.refresh();
                        refreshTrees();
                        showErrorsAndMessages(messages, "Scan yielded no errors or warnings up to now.");
                    }
                });

        menuBar.add(helpMenu);

        statusPanel.setName(
                "statusPanel"); // NOI18N

        statusPanelSeparator.setName(
                "statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName(
                "statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        statusAnimationLabel.setName(
                "statusAnimationLabel"); // NOI18N

        progressBar.setName(
                "progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);

        statusPanel.setLayout(statusPanelLayout);

        statusPanelLayout.setHorizontalGroup(
                statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
                .add(statusPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(statusMessageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 400, Short.MAX_VALUE)
                        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusAnimationLabel)
                        .addContainerGap()));
        statusPanelLayout.setVerticalGroup(
                statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(statusPanelLayout.createSequentialGroup()
                        .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(statusMessageLabel)
                                .add(statusAnimationLabel)
                                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(3, 3, 3)));

        setComponent(mainPanel);

        setMenuBar(menuBar);

        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    public void breakdownSelected() {
        ArrayList<FactRequirement> factReqs = requirementViewer.getBreakdownReadyFactRequirements();
        if (factReqs.size() > 0) {
            dockingRoot.close(requirementsBreakdownPanel);
            requirementsBreakdownPanel = new RequirementsBreakdownPanel(projectController, factReqs);
            dockingRoot.createTab(requirementViewer, requirementsBreakdownPanel, 1, true);
            dockingRoot.close(factBreakdown);
            dockingRoot.createTab(requirementViewer, factBreakdown, 2, true);
            init = true;
        } else {
            JOptionPane.showMessageDialog(getFrame(), "No approved/non realized fact requirements found.");
        }
    }

    /**
     * Prompts the user to save the current project and shows the wizard for
     * creating a new project. If a valid name has been provided the new project
     * will be created and the UI elements will be refreshed.. This occurs when
     * the menu-item 'New' is clicked.
     *
     * @param evt
     */
    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        showSavePrompt();
        createNewProject();
    }

    /**
     * Prompts the user to save the current project and shows the fileselection
     * dialog from which the user can browse projects. When a file is selected
     * the current project will be set to the project within the selected file.
     * If an error occurs an empty project will be set as the current project
     * and an appropriate messagedialog will be shown. This occurs when the
     * menu-item 'Open' is clicked.
     *
     * @param evt
     */
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (projectController.isUseJPA()) {
            OpenProjectJPADialog openProjectDialog = new OpenProjectJPADialog(projectController);
            //openProject.setLocation(x, y)
            openProjectDialog.setVisible(true);
            refresh();
        } else {
            showSavePrompt();

            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(getFrame());

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                openProject(file);
            }
        }
    }

    private void openProject(File file) throws HeadlessException {
        if (file == null) {
            return;
        }
        try {

            projectController.openProject(file);

            //Set the currentUser of the project.
            LoginDialog loginDialog = new LoginDialog(getFrame(), true, projectController);
            loginDialog.setVisible(true);
            projectController.getProject().setCurrentUserAndInform();
            refresh();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(getFrame(), ex.getMessage());
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(getFrame(), "Projectfile isn't compatible: \n" + ex.getMessage());
            return;
        }
    }

    /**
     * Saves the current project. This occurs when the menu-item 'Save' is
     * clicked.
     *
     * @param evt
     */
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        saveProject(false);
    }

    /**
     * Prompts the user to save the project and requests a file location. This
     * occurs when the menu-item 'Save as' is clicked.
     *
     * @param evt
     */
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        saveProject(true);
    }

    /**
     * Prompts the user to save the project. This occurs when the menu-item
     * 'Quit' is clicked.
     *
     * @param evt
     */
    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        quitApplication();
    }

    private void participantsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (projectController.getProject() != null) {
            ParticipantDialog pd = new ParticipantDialog(getFrame(), false, projectController);
            pd.setVisible(true);
        }
    }//GEN-LAST:event_participantsMenuItemActionPerformed

    private void vocabularyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }//GEN-LAST:event_vocabularyMenuItemActionPerformed

    /**
     * Checks if the selected item in the project navigator is a CD and prompts
     * the user to save the CD as an image. If this is not the case an
     * appropriate message will be shown. This occurs when the menu-item 'Export
     * CD' is clicked.
     *
     * @param evt
     */
    private void imageClassDiagramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageClassDiagramMenuItemActionPerformed
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) projectNavigator.getTree().getLastSelectedPathComponent();

        if (node != null && node.getUserObject() != null && node.getUserObject() instanceof ClassDiagram) {
            //If the selected item in the project navigator is a CD.
            ClassDiagram cd = (ClassDiagram) node.getUserObject();
            for (DockableState ds : getDockingRoot().getDockables()) {
                //Search for the CD panel.
                if (ds.getDockable() instanceof ClassDiagramPanel) {
                    ClassDiagramPanel cdp = (ClassDiagramPanel) ds.getDockable();
                    if (cdp.getClassDiagram().equals(cd)) {
                        //If the CD within the CD panel is equal to the selected CD show save to image dialog.
                        cdp.saveCDToImage();
                    }
                }
            }
        } else {
            //The selected node in the project navigator is not a CD.
            JOptionPane.showMessageDialog(getFrame(), "The CD could not be exported. Make sure you have a CD selected in the Project Navigator.", mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_imageClassDiagramMenuItemActionPerformed

    /**
     * Shows a saveprompt where the behavior should be exported and writes the
     * behavior to the selected location. This occurs when the menu-item 'Save
     * Behavior as Text' is clicked.
     *
     * @param evt
     */
    private void behaviorAsTextMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_behaviorAsTextMenuItemActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(getComponent());
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fileChooser.getSelectedFile();
        {
            try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
                ObjectModel om = projectController.getProject().getObjectModel();
                Iterator<FactType> types = om.typesIterator();
                while (types.hasNext()) {
                    FactType ft = types.next();
                    if (ft.isClass()) {
                        writeBehavior(ft, out);
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(getFrame(), ex.getMessage());
            }
        }

    }//GEN-LAST:event_behaviorAsTextMenuItemActionPerformed

    private void printMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_printMenuItemActionPerformed

    /**
     * Prompts the user with an inheritanceDialog from which the user can define
     * super- and subtypes. Afterwards the UI elements will be refreshed. This
     * occurs when the menu-item 'Add Inheritance' is clicked.
     *
     * @param evt
     */
    private void miAbstractObjectTypeActionPerformed(java.awt.event.ActionEvent evt) {
        AbstractObjectTypeDialog dialog
                = new AbstractObjectTypeDialog(this.getFrame(), true,
                        projectController.getProject().getObjectModel());
        dialog.setVisible(true);

        refreshTrees();
        typeConfigurator.refresh();
    }

    /**
     * Generates a CD for the current project and shows it within a
     * dockingpanel. been selected. This occurs when the menu-item 'Generate CD'
     * is clicked.
     *
     * @param evt
     */
    private void generateCDMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateCDMenuItemActionPerformed
        String name = showInputTabDialog("Please enter a name of the CD");
        if (name != null && !name.isEmpty()) {
            ClassDiagramPanel cdp = new ClassDiagramPanel(name, this, true);
            ClassDiagram cd = cdp.getClassDiagram();
            try {
                projectController.getProject().addClassDiagram(cd);
                // ((ProjectTreeModel) projectNavigator.getTree().getModel()).addClassDiagram(cd);
                refreshTrees();
                showDiagram(cdp);

            } catch (DuplicateException ex) {
                JOptionPane.showMessageDialog(getFrame(), ex.getMessage());
            }
        }

    }

    /**
     * Shows the dockingselectordialog in which the user can change
     * dockingsettings such as visibility.
     *
     * @param evt
     */
    private void chooserMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserMenuItemActionPerformed
        DockingSelectorDialog selectorDialog = new DockingSelectorDialog(getFrame());
        selectorDialog.setDockingDesktop(dockingRoot);
        selectorDialog.setSize(getFrame().getWidth(), getFrame().getHeight());
        selectorDialog.setVisible(true);

    }//GEN-LAST:event_chooserMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        showAboutBox();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void miBehaviorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miBehaviorActionPerformed
        if (projectController.getProject() == null) {
            return;
        }
        ObjectModel om = projectController.getProject().getObjectModel();
        Object[] objects = new Object[2];
        objects[0] = "Light";
        objects[1] = "Verbose";
        int result = JOptionPane.showOptionDialog(getFrame(), "Kind of Behavior", "Light version of behavior ?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, objects, objects[0]);

        List<Message> messages = om.generateClasses(false, result == 0);
        typeConfigurator.setReliable(messages);
        typeConfigurator.refresh();
        requirementViewer.refresh();
        // refreshTrees();
        showErrorsAndMessages(messages, "The behavior was generated succesfully.");
    }//GEN-LAST:event_miBehaviorActionPerformed

    private void miBehaviorWithRegistriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miBehaviorWithRegistriesActionPerformed
        if (projectController.getProject() == null) {
            return;
        }
        ObjectModel om = projectController.getProject().getObjectModel();
        Object[] objects = new Object[2];
        objects[0] = "Light";
        objects[1] = "Verbose";
        int result = JOptionPane.showOptionDialog(getFrame(), "Kind of Behavior", "Light version of behavior ?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, objects, objects[0]);
        List<Message> messages = om.generateClasses(true, false);
        typeConfigurator.setReliable(messages);
        typeConfigurator.refresh();
        requirementViewer.refresh();
        //   refreshTrees();
        showErrorsAndMessages(messages, "The behavior was generated succesfully.");
    }//GEN-LAST:event_miBehaviorWithRegistriesActionPerformed

    public void initFactBreakdown(FactRequirement requirement) {
        if (requirement != null) {
            factBreakdown.initExpressionTree(requirement);
        }
    }

    private void miRemoveBehaviorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRemoveBehaviorActionPerformed
        if (projectController.getProject() == null) {
            return;
        }
        ObjectModel om = projectController.getProject().getObjectModel();
        om.removeBehavior();
        typeConfigurator.setReliable(false);
        typeConfigurator.refresh();
        requirementViewer.refresh();
        refreshTrees();
    }//GEN-LAST:event_miRemoveBehaviorActionPerformed

    private void miInheritanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miInheritanceActionPerformed

        InheritanceDialog dialog = new InheritanceDialog(this.getFrame(), true, projectController.getProject().getObjectModel());
        dialog.setVisible(true);
        typeConfigurator.setReliable(false);
        typeConfigurator.refresh();
        refreshTrees();
    }//GEN-LAST:event_miInheritanceActionPerformed

    private void showErrorsAndMessages(List<Message> messages, String header) {
        if (messages.isEmpty()) {
            String title = "Information";
            int messageType = JOptionPane.INFORMATION_MESSAGE;
            JOptionPane.showMessageDialog(this.getFrame(), header, title, messageType);
            projectController.getProject().showStatistics();
        } else {
            // seperate the warnings from the errors
            StringBuilder errors = new StringBuilder();
            StringBuilder warnings = new StringBuilder();
            for (Message message : messages) {
                if (message.isError()) {
                    errors.append(message.getText()).append("\n");
                } else {
                    warnings.append(message.getText()).append("\n");
                }
            }

            // show the error(s)
            if (errors.length() > 0) {

                String title = "Error";
                int messageType = JOptionPane.ERROR_MESSAGE;

                JOptionPane.showMessageDialog(this.getFrame(), errors.toString(), title, messageType);

            } else {
                projectController.getProject().showStatistics();

            }

            // show the warning(s)
            if (warnings.length() > 0) {

                String title = "Warning";
                int messageType = JOptionPane.WARNING_MESSAGE;

                JOptionPane.showMessageDialog(this.getFrame(), warnings.toString(), title, messageType);
            }

            typeConfigurator.showMessages(errors.toString() + "\n" + warnings.toString());
        }
    }

    void showDiagram(ClassDiagramPanel cdp) {
        int order = 2;
        for (DockableState ds : dockingRoot.getDockables()) {
            if (ds.getDockable().getDockKey().getKey().equals(cdp.getClassDiagram().getName())) {
                return;
            }
            if (ds.getDockable() instanceof ClassDiagramPanel) {
                order++;
            }
        }

        dockingRoot.createTab(factBreakdown, cdp, order, true);
    }

    void createDiagram(ArrayList<FactType> selection) {

        String name = showInputTabDialog("Please enter a name of the CD");
        if (name != null && !name.isEmpty()) {
            ClassDiagramPanel cdp = new ClassDiagramPanel(name, this, selection, true);
            ClassDiagram cd = cdp.getClassDiagram();
            try {
                projectController.getProject().addClassDiagram(cd);
                ((ProjectTreeModel) projectNavigator.getTree().getModel()).addClassDiagram(cd);
                refreshTrees();
                showDiagram(cdp);

            } catch (DuplicateException ex) {
                JOptionPane.showMessageDialog(getFrame(), ex.getMessage());
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getSource() instanceof ObjectModel || pce.getSource() instanceof RequirementModel) {
//            this.projectNavigator.refresh();
//            this.projectInspector.refresh();
        } else if (pce.getSource() instanceof Project) {
            initProjectRole(((Project) pce.getSource()).getCurrentUser());
        }
    }

    public void initListenersAndMenu() {
        setMenuStateStartUp();
        Project project = projectController.getProject();
        if (project == null) {
            return;
        }

        project.addListener(this, "currentUser");
        ObjectModel om = project.getObjectModel();
        om.addListener(this, "newType");
        om.addListener(this, "removedType");
        RequirementModel rm = project.getRequirementModel();
        rm.addListener(this, "newReq");
        rm.addListener(this, "remReq");

        initProjectRole(project.getCurrentUser());

    }

    @Override
    public void refresh() {
        if (init) {
            closeDeprecatedTabs();
            refreshTrees();
        } else {
            initDockingRoot();
            initStatusBar();
            init = true;
        }

        typeConfigurator.setObjectModel(projectController.getProject().getObjectModel());
        typeConfigurator.refresh();
        requirementViewer.refresh();
        initListenersAndMenu();
    }

    @Override
    public void selectionChanged(DockableSelectionEvent e) {
        Dockable dockable = e.getSelectedDockable();
        if (dockable instanceof IView) {
            ((IView) dockable).refresh();
        }

    }

    private void initProjectRole(ProjectRole currentUser) {
        if (currentUser == null) {
            this.getFrame().setTitle(projectController.getProject().getName()
                    + "\t\tlogged: ?");
        } else {
            this.getFrame().setTitle(projectController.getProject().getName()
                    + "\t\tlogged: " + currentUser.toString());
        }
        setMenuStateStartUp();
    }

    private void initAutosave() {
        File folder = new File("autosave");

        if (!folder.exists()) {
            folder.mkdir();
        }

        autosaveFiles = new File[10];
        for (int i = 0; i < 10; i++) {
            autosaveFiles[i] = new File(folder, "autosave" + i + ".sym");
        }
        autosaveNext = 0;

        autoSaveTimer = new java.util.Timer();
        autoSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                autoSaveProject();
            }
        }, AUTO_SAVE_DELAY, AUTO_SAVE_DELAY);

    }

    private void timerShowAutoBox() {
        // showing AboutBox during 5 seconds
        javax.swing.Timer timer;
        timer = new javax.swing.Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aboutBox.dispose();
            }
        });
        timer.setRepeats(false);
        timer.setInitialDelay(5000);
        timer.start();
        showAboutBox();
    }
}
