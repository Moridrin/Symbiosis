/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import java.awt.EventQueue;
import java.awt.Point;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import equa.controller.ProjectController;
import equa.desktop.DockableTree;
import equa.factbreakdown.ExpressionNode;
import equa.factbreakdown.ExpressionTreeModel;
import equa.factbreakdown.ParentNode;
import equa.factbreakdown.ValueNode;
import equa.meta.requirements.FactRequirement;

/**
 *
 * @author Simon
 */
public class FactBreakdown extends DockableTree implements TreeModelListener {

    private static final long serialVersionUID = 1L;
    private ProjectController projectController;
    private TreeController treeController;
    private static int row_height;

    public FactBreakdown(ProjectController controller) {
        super("Fact Beakdown");

        this.projectController = controller;

        addValueChangedListener();
        row_height = Node.ROW_HEIGHT;

    }

    public static void setFontSize(int font_size) {
        row_height = font_size + 12;
    }

    public static int getFontSize() {
        return row_height - 12;
    }

    public static int getRowHeight() {
        return row_height;
    }

    public TreeController getTreeController() {
        return treeController;
    }

    /*
     * Adds a listener to the Factdecomposer that listens for any changes made
     * to the expression
     */
    private void addValueChangedListener() {
        getTree().addTreeSelectionListener(new TreeSelectionListener() {
            /*
             * If the value changes the projectInspector and ProjectNavigator
             * are updated accordingly.
             */
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                projectController.refreshViews();

            }
        });
    }

    /*
     * This initializes the ExpressionTree by setting the correct renderer,
     * controller and other properties. TODO: currently the can be called
     * separately from the constructor of this class, which means it allows this
     * object to be reset. If multiple expressionEditors are made possible this
     * is not wanted.
     */
    public void initExpressionTree(FactRequirement factRequirement) {
        String expression = factRequirement.getText();
        ExpressionTreeModel expressionTreeModel = new ExpressionTreeModel(projectController.getProject().getObjectModel(),
                projectController.getProject().getCurrentUser(), factRequirement);

        expressionTreeModel.addTreeModelListener(this);

        treeController = new TreeController(null, expressionTreeModel);

        treeController.executeRootDialog(new Point(100, 200), expression);

        ParentNode root = treeController.getRoot();
        if (root != null) {
            expressionTreeModel.setRoot(root, factRequirement);
            getTree().setModel(expressionTreeModel);
            getTree().setCellEditor(new ExpressionTreeCellEditor(treeController, expressionTreeModel, null));
        }
        getTree().setCellRenderer(new ExpressionTreeCellRenderer());
        getTree().setShowsRootHandles(true);
        getTree().setRowHeight(row_height);
        getTree().setSize(500, 700);
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
    }

    @Override
    public void treeNodesInserted(final TreeModelEvent e) {
        if (e.getChildren().length > 0) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    tree.expandPath(new TreePath(e.getPath()).pathByAddingChild(e.getChildren()[0]));
                }
            });
        }
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        TreePath path = e.getTreePath();
        ExpressionNode node = (ExpressionNode) path.getLastPathComponent();
        if (node instanceof ValueNode) {
            if (node.getChildCount() > 0) {
                path = path.pathByAddingChild(node.getChildAt(0));
            }
        }
        getTree().scrollPathToVisible(path);
    }
}
