/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.cd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;

import equa.meta.objectmodel.ObjectType;

/**
 *
 * @author Stertefeld, Frank Peeters
 */
public class CDPopup extends JPanel {

    private static final long serialVersionUID = 1L;
    private ClassDiagramPanel cdp;
    private JPopupMenu popupMenu;
    private int xClick, yClick; // the click position of the selected item

    public CDPopup(final ClassDiagramPanel cdp, int x, int y) {
        this.cdp = cdp;
        this.xClick = x;
        this.yClick = y;
        initPopupMenu(cdp);
        //popup.addPopupMenuListener(new PopupPrintListener());
        addMouseListener(new MousePopupListener());
    }

    public JPopupMenu getJPopupMenu() {
        return popupMenu;
    }

    private void initPopupMenu(final ClassDiagramPanel cdp) {
        popupMenu = new JPopupMenu();

        JMenuItem unfoldItem;
        popupMenu.add(unfoldItem = new JMenuItem("Unfold Attribute"));
        unfoldItem.setHorizontalTextPosition(SwingConstants.LEFT);
        unfoldItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mxCell cell = (mxCell) cdp.getGraph().getSelectionCell();
                if (cell != null && cell instanceof Attribute
                        && ((Attribute) cell).getRelation().targetType() instanceof ObjectType) {
                    cdp.unfold((Attribute) cell);
                }
            }
        });

        JMenuItem foldItem;
        popupMenu.add(foldItem = new JMenuItem("Fold Class"));
        foldItem.setHorizontalTextPosition(SwingConstants.LEFT);
        foldItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mxCell cell = (mxCell) cdp.getGraph().getSelectionCell();
                if (cell != null && cell instanceof ClassBox) {
                    cdp.fold((ClassBox) cell);
                }
            }
        });

        popupMenu.add(new JSeparator());

        JMenuItem mirrorItem;
        popupMenu.add(mirrorItem = new JMenuItem("Mirror Text"));
        mirrorItem.setHorizontalTextPosition(SwingConstants.LEFT);
        mirrorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mxCell selected = selectedEdge();
                if (selected != null) {
                    mxGeometry geometry = selected.getGeometry();
                    double y = geometry.getY();
                    if (y == 0.0) {
                        y = -20.0;
                    }
                    selected.setGeometry(new mxGeometry(geometry.getX(), -y, 0, 0));
                    selected.getGeometry().setRelative(true);
                    cdp.getGraph().refresh();
                }
            }
        });

        JMenuItem lineItem;
        popupMenu.add(lineItem = new JMenuItem("Text On Line"));
        lineItem.setHorizontalTextPosition(SwingConstants.LEFT);
        lineItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mxCell selected = selectedEdge();
                if (selected != null) {
                    mxGeometry geometry = selected.getGeometry();
                    selected.setGeometry(new mxGeometry(geometry.getX(), 0, 0, 0));
                    selected.getGeometry().setRelative(true);
                    cdp.getGraph().refresh();
                }
            }
        });

        JMenuItem pushTowards;
        popupMenu.add(pushTowards = new JMenuItem("Push Text Towards Class"));
        pushTowards.setHorizontalTextPosition(SwingConstants.LEFT);
        pushTowards.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mxCell selected = selectedEdge();
                if (selected != null) {
                    mxGeometry geometry = selected.getGeometry();
                    selected.setGeometry(new mxGeometry(geometry.getX() + 0.05, geometry.getY(), 0, 0));
                    selected.getGeometry().setRelative(true);
                    cdp.getGraph().refresh();
                }
            }
        });

        JMenuItem pullFrom;
        popupMenu.add(pullFrom = new JMenuItem("Pull Text From Class"));
        pullFrom.setHorizontalTextPosition(SwingConstants.LEFT);
        pullFrom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mxCell selected = selectedEdge();
                if (selected != null) {
                    mxGeometry geometry = selected.getGeometry();
                    selected.setGeometry(new mxGeometry(geometry.getX() - 0.05, geometry.getY(), 0, 0));
                    selected.getGeometry().setRelative(true);
                    cdp.getGraph().refresh();
                }
            }
        });

        popupMenu.add(new JSeparator());

        JMenuItem exportCDItem;
        popupMenu.add(exportCDItem = new JMenuItem("Export CD"));
        exportCDItem.setHorizontalTextPosition(SwingConstants.LEFT);
        exportCDItem.setEnabled(false);
        exportCDItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cdp.saveCDToImage();
            }
        });

        foldItem.setEnabled((mxCell) cdp.getGraph().getSelectionCell() instanceof ClassBox);
        unfoldItem.setEnabled((mxCell) cdp.getGraph().getSelectionCell() instanceof Attribute);
        mirrorItem.setEnabled(selectedEdge() != null);
        pushTowards.setEnabled(selectedEdge() != null);
        pullFrom.setEnabled(selectedEdge() != null);
        lineItem.setEnabled(selectedEdge() != null);
    }

    private mxCell selectedEdge() {
        mxCell edge = (mxCell) cdp.getGraph().getSelectionCell();

        if (edge == null || !edge.isEdge()) {
            return null;
        }

        mxCell sourceBox = (mxCell) edge.getSource();
        mxCell targetBox = (mxCell) edge.getTarget();
        if (distance(xClick, yClick, targetBox) < distance(xClick, yClick, sourceBox)) {
            return edge;
        }

        HashSet<mxICell> edgesSource = new HashSet<>();
        HashSet<mxICell> edgesTarget = new HashSet<>();
        for (int i = 0; i < sourceBox.getEdgeCount(); i++) {
            edgesSource.add(sourceBox.getEdgeAt(i));
        }
        for (int i = 0; i < targetBox.getEdgeCount(); i++) {
            edgesTarget.add(targetBox.getEdgeAt(i));
        }
        edgesSource.retainAll(edgesTarget);
        Iterator<mxICell> it = edgesSource.iterator();

        mxCell reverseEdge = (mxCell) it.next();
        if (edge == reverseEdge) {
            if (it.hasNext()) {
                return (mxCell) it.next();
            } else {
                return edge;
            }

        } else {
            return reverseEdge;
        }

    }

    static double distance(int x, int y, mxCell cell) {
        mxGeometry geometry = cell.getGeometry();
        double diffx = x - (geometry.getX() + geometry.getWidth() / 2);
        double diffy = y - (geometry.getY() + geometry.getHeight() / 2);

        return Math.sqrt(diffx * diffx + diffy * diffy);

    }

    // An inner class to check whether mouse events are the popup trigger
    class MousePopupListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            checkPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(CDPopup.this, e.getX(), e.getY());
            }
        }
    }

// An inner class to show when popup events occur
    class PopupPrintListener implements PopupMenuListener {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            //.out.println("Popup menu will be visible!");
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            //System.out.println("Popup menu will be invisible!");
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            //System.out.println("Popup menu is hidden!");
        }
    }
}
