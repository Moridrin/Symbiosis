/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

/**
 *
 * @author Jeroen
 */
public class DockableTree extends JPanel implements Dockable {

    private static final long serialVersionUID = 1L;
    protected JTree tree;
    private DockKey key;

    public DockableTree(String name) {
        tree = new JTree();
        key = new DockKey(name);
        key.setCloseEnabled(false);
        tree.setModel(null);
        tree.setEditable(true);
        tree.addMouseListener(new MouseAdapter() {
            // clicking outside tree results in a deselection of all nodes
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int row = tree.getRowForLocation(e.getX(), e.getY());
                if (row == -1) {
                    tree.clearSelection();
                }
            }
        });

        setLayout(new BorderLayout());
        JScrollPane jsp = new JScrollPane(tree);
        jsp.setPreferredSize(new Dimension(200, 200));
        add(jsp, BorderLayout.CENTER);
    }

    @Override
    public DockKey getDockKey() {
        return key;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    public JTree getTree() {
        return tree;
    }
}
