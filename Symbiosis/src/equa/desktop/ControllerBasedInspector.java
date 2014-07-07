package equa.desktop;

import equa.controller.ProjectController;
import equa.inspector.InspectorTreeModel;

/**
 * MVC Version of Inspector
 *
 * @author maurice.hagemeijer
 *
 */
public class ControllerBasedInspector extends DockableTree {

    private static final long serialVersionUID = 1L;
    private ProjectController root;

    public ControllerBasedInspector(ProjectController root) {
        super("Project Inspector");

        this.root = root;
        getTree().setEditable(false);
        refresh();
    }

    public void refresh() {
        if (root.getProject() != null) {
            getTree().setModel(new InspectorTreeModel(root.getProject()));
        }
        getTree().setShowsRootHandles(true);
        getTree().setRootVisible(true);
        getTree().updateUI();
    }
}
