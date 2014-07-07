/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.inspector;

import equa.desktop.Desktop;
import equa.desktop.DockableTree;

/**
 *
 * @author Simon
 */
public class ProjectInspector extends DockableTree {

    private static final long serialVersionUID = 1L;
    private Desktop root;

    public ProjectInspector(Desktop root) {
        super("Project Inspector");

        this.root = root;
        refresh();
    }

    public final void refresh() {
        if (root.getCurrentProject() != null) {
            getTree().setModel(new InspectorTreeModel(root.getCurrentProject()));
        }
        getTree().setShowsRootHandles(true);
        getTree().setRootVisible(true);
        getTree().updateUI();
    }
}
