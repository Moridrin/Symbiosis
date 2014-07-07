package equa.desktop;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import equa.controller.IView;
import equa.controller.ProjectController;
import equa.controller.SwingProjectController;
import equa.diagram.cd.ClassDiagram;
import equa.diagram.cd.ClassDiagramPanel;
import equa.diagram.ord.deprecated.ObjectRoleDiagram;

@SuppressWarnings("serial")
public class ControllerBasedNavigator extends DockableTree implements IView {

    private ProjectController projectController;
    private JPopupMenu popup;
    private JMenuItem miDeleteDiagram;
    private JMenuItem miShowDiagram;
    private JMenuItem miCreateCD;

    public ControllerBasedNavigator(ProjectController controller) {
        super("Project Navigator");

        this.projectController = controller;
        if (projectController instanceof SwingProjectController) {
            projectController.addView(this);
        }

        initPopup();

        addDeleteListener();
        addMouseListener();
        getTree().setEditable(false);

        refresh();
    }

    private void initPopup() {
        this.popup = new JPopupMenu();
        this.popup.setLightWeightPopupEnabled(true);

        miDeleteDiagram = new JMenuItem("Delete Diagram");
        miDeleteDiagram.setFocusable(true);
        popup.add(miDeleteDiagram);
        miDeleteDiagram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                popup.setVisible(false);
                for (TreePath path : getTree().getSelectionPaths()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    Object object = node.getUserObject();
                    //If the selected item is a diagram.
                    if (object instanceof ClassDiagram) {
                        int n = showDeleteDialog();
                        if (n == JOptionPane.YES_OPTION) {
                            ClassDiagram cd = (ClassDiagram) object;
                            projectController.getProject().removeClassDiagram(cd);
                            //removeDockableDiagram(cd.getName());
                        }
                    } else if (object instanceof ObjectRoleDiagram) {
                        int n = showDeleteDialog();
                        if (n == JOptionPane.YES_OPTION) {
                            ObjectRoleDiagram ord = (ObjectRoleDiagram) object;
                            projectController.getProject().removeObjectRoleDiagram(ord);
                            //removeDockableDiagram(ord.getName());
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Only diagrams can be deleted");
                        return;
                    }
                    refresh();
                }
            }
        });

        miShowDiagram = new JMenuItem("Show Diagram");
        miShowDiagram.setFocusable(true);
        popup.add(miShowDiagram);
        miShowDiagram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                popup.setVisible(false);
                if (getTree().getSelectionPath() != null) {
                    for (TreePath path : getTree().getSelectionPaths()) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object object = node.getUserObject();
                        //If the selected item is a diagram.
                        if (object instanceof ClassDiagram) {
                            ClassDiagram cd = (ClassDiagram) object;
                            projectController.showView(cd);
                            //ClassDiagramPanel cdp = new ClassDiagramPanel(root, cd);
                            //root.showDiagram(cdp);

                        } else if (object instanceof ObjectRoleDiagram) {
                        }
                    }
                }
            }
        });

        miCreateCD = new JMenuItem("Create CD");
        miCreateCD.setFocusable(true);
        popup.add(miCreateCD);
        miCreateCD.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                projectController.createClassDiagram("123");
            }
        });
    }

    /**
     * Adds a listener to the projectnavigator that records the delete key and
     * prompts the user if he wants to delete the selected item if possible.
     */
    private void addDeleteListener() {
        getTree().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                int keyCode = e.getKeyCode();

                if (getTree().getSelectionCount() > 0) {
                    //If the delete key is pressed.
                    if (keyCode == KeyEvent.VK_DELETE) {

                        for (TreePath path : getTree().getSelectionPaths()) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                            Object object = node.getUserObject();

                            //If the selected item is a diagram.
                            if (object instanceof ClassDiagram) {
                                int n = showDeleteDialog();
                                if (n == JOptionPane.YES_OPTION) {
                                    ClassDiagram cd = (ClassDiagram) object;
                                    projectController.getProject().removeClassDiagram(cd);
                                    //removeDockableDiagram(cd.getName());
                                }

                            } else if (object instanceof ObjectRoleDiagram) {
                                int n = showDeleteDialog();
                                if (n == JOptionPane.YES_OPTION) {
                                    ObjectRoleDiagram ord = (ObjectRoleDiagram) object;
                                    projectController.getProject().removeObjectRoleDiagram(ord);
                                    //removeDockableDiagram(ord.getName());
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Only diagrams can be deleted");
                                return;
                            }
                        }
                        refresh();
                    }
                }
            }
        });
    }

    private int showDeleteDialog() throws HeadlessException {
        int n = JOptionPane.showConfirmDialog(
                null,
                String.format("Are you sure to delete %d item(s)?", getTree().getSelectionCount()),
                "Delete?",
                JOptionPane.YES_NO_OPTION);
        return n;
    }

    /*
     private void removeDockableDiagram(String diagramName) {
     for (DockableState ds : root.getDockingRoot().getDockables()) {
     Dockable dockable = ds.getDockable();

     if (dockable.getDockKey().getKey().equals(diagramName)) {
     root.getDockingRoot().remove(dockable);
     root.getDockingRoot().unregisterDockable(dockable);
     return;
     }
     }
     }*/
    private void addMouseListener() {
        getTree().addMouseListener(new MouseAdapter() {
            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.setVisible(true);
                    popup.setLocation(e.getLocationOnScreen());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                //super.mouseClicked(e);
                checkPopup(e);
                if (e.getClickCount() == 1) {
                    if (getTree().getSelectionCount() == 0) {
                        //root.getPropertySheetMain().removeAll();
                        //root.getPropertyEditor().updateUI();
                    }
                } else if (e.getClickCount() == 2) {
                    //Boolean panelExists = false;

                    if (getTree().getSelectionCount() > 0) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getTree().getSelectionPath().getLastPathComponent();
                        Object object = node.getUserObject();

                        if (object instanceof ObjectRoleDiagram) {
                            /*
                             ObjectRoleDiagram c = (ObjectRoleDiagram) object;
                             for (DockableState ds : root.getDockingRoot().getDockables()) {
                             if (ds.getDockable() instanceof ObjectRoleDiagramPanel) {

                             ObjectRoleDiagramPanel ordp = (ObjectRoleDiagramPanel) ds.getDockable();
                             if (ordp.getController().equals(c)) {
                             panelExists = true;

                             if (!ds.isClosed()) {
                             JOptionPane.showMessageDialog(null, "Panel is already open.");
                             return;
                             } else {
                             root.getDockingRoot().createTab(root.getExpressionEditor(), ordp, 0, true);
                             return;
                             }
                             }
                             }
                             }

                             if (panelExists == false) {
                             ObjectRoleDiagramPanel ordpanel = new ObjectRoleDiagramPanel(root.getCurrentProject().getObjectModel(), c, root);

                             root.getDockingRoot().createTab(root.getExpressionEditor(), ordpanel, 0, true);
                             }*/
                        }

                        // Next code contains suspicious statement (ClassDiagramPanel versus ClassDiagram)
                        if (object instanceof ClassDiagramPanel) {
                            /*
                             ClassDiagramPanel cdp = (ClassDiagramPanel) object;
                            
                             for (DockableState ds : root.getDockingRoot().getDockables()) {
                             if (ds.getDockable() instanceof ClassDiagramPanel) {
                             ClassDiagramPanel cdpanel = (ClassDiagramPanel) ds.getDockable();
                             if (cdpanel.getClassDiagram().equals(cdp)) {
                             panelExists = true;

                             if (!ds.isClosed()) {
                             JOptionPane.showMessageDialog(null, "Panel is already open.");
                             return;
                             } else {
                             root.getDockingRoot().createTab(root.getExpressionEditor(), cdpanel, 0, true);
                             return;
                             }
                             }
                             }
                             }

                             if (panelExists == false) {
                             // root.getDockingRoot().createTab(root.getExpressionEditor(), cdpanel, 0, true);
                             }
                            
                             */
                        }
                    }
                }
            }
        });
    }

//    private void addValueChangedListener() {
//        getTree().addTreeSelectionListener(new TreeSelectionListener() {
//            @Override
//            public void valueChanged(TreeSelectionEvent e) {
//                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) getTree().getLastSelectedPathComponent();
//                if (treeNode != null) {
//                    if (treeNode.isRoot()) {
//                        //root.getPropertyEditor().initPropertySheet(root.getProject());
//                    } else {
//                        Object object = treeNode.getUserObject();
//                        if (object instanceof FactType
//                                || object instanceof ObjectRoleDiagram
//                                || object instanceof ClassDiagram
//                                || object instanceof Attribute
//                                || object instanceof BaseValueRoleBox) {
//                            //root.getPropertyEditor().initPropertySheet(object);
//                        }
//                    }
//                }
//            }
//        });
//    }
    @Override
    public final void refresh() {
        if (projectController.getProject() != null) {
            getTree().setModel(new ProjectTreeModel(projectController.getProject()));
        }
        getTree().setShowsRootHandles(true);
        getTree().setRootVisible(true);
        getTree().updateUI();
    }

}
