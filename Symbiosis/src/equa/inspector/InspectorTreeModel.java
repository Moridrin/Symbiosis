/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.inspector;

import java.util.EventListener;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import equa.project.Project;

/**
 *
 * @author FrankP based on demo of custom TreeModels in Core Java - Volume II
 */
public class InspectorTreeModel implements TreeModel {

    private InspectorTreeNode root;
    private EventListenerList listenerList;

    public InspectorTreeModel(Project project) {
        root = new InspectorTreeNode(Project.class, project.getName(), project);
        listenerList = new EventListenerList();
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        InspectorTreeNode node = (InspectorTreeNode) parent;
        SubNode item = node.getItem(index);
        if (item == null) {
            return null;
        }

        return new InspectorTreeNode(item.getType(), item.getName(), item.getValue());

    }

    @Override
    public int getChildCount(Object parent) {
        return ((InspectorTreeNode) parent).getItemCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int n = getChildCount(parent);
        for (int i = 0; i < n; i++) {
            if (getChild(parent, i).equals(child)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    protected void fireTreeStructureChanged(Object oldRoot) {
        TreeModelEvent event = new TreeModelEvent(this, new Object[]{oldRoot});
        EventListener[] listeners = listenerList.getListeners(TreeModelListener.class);
        for (EventListener l : listeners) {
            ((TreeModelListener) l).treeStructureChanged(event);
        }
    }
}
