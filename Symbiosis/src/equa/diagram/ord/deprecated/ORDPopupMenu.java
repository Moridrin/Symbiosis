/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.Role;

/**
 *
 * @author Bas
 */
public class ORDPopupMenu extends JPanel {

    private static final long serialVersionUID = 1L;
    private JPopupMenu popup;
    private ArrayList<Role> selectedRoles;
    private HashMap<String, JMenuItem> menuItems;
    private FactType selectedFT;
    private ObjectRoleDiagramPanel ordp;

    public ORDPopupMenu(Frame frame, Object[] selection, ObjectModel om) {
        menuItems = new HashMap<String, JMenuItem>();

        popup = new JPopupMenu();
        ActionListener rightClickListener = new ItemHandler();

        // Add menu Items
        JMenuItem mergeRolesItem;
        popup.add(mergeRolesItem = new JMenuItem("Merge Roles"));
        mergeRolesItem.setHorizontalTextPosition(SwingConstants.LEFT);
        mergeRolesItem.addActionListener(rightClickListener);
        menuItems.put("Merge Roles", mergeRolesItem);

        JMenuItem exportORDItem;
        popup.add(exportORDItem = new JMenuItem("Export ORD"));
        exportORDItem.setHorizontalTextPosition(SwingConstants.LEFT);
        exportORDItem.addActionListener(rightClickListener);
        menuItems.put("Export ORD", exportORDItem);

        //Check if the Merge Roles MenuItem should be enabled.
        //This is determined by if every object in the selection is a role, and if these roles have the same parent(FactType)
        selectedRoles = new ArrayList<Role>();
        Role role = null;
        selectedFT = null;
        for (Object o : selection) {
            if (o instanceof BaseValueRoleBox || o instanceof ObjectRoleBox) {
                role = ((RoleBox) o).getRole();
                if (selectedFT == null) {
                    selectedFT = role.getParent();
                    selectedRoles.add(role);
                } else if (selectedFT == role.getParent()) {
                    selectedRoles.add(role);
                } else {
                    selectedFT = null;
                    mergeRolesItem.setEnabled(false);
                    break;
                }
            } else {
                mergeRolesItem.setEnabled(false);
                break;
            }
        }
        //disable  merge roles menuitem if count of selected roles is < 2
        if (selectedRoles.size() < 2) {
            mergeRolesItem.setEnabled(false);
        }

        popup.addPopupMenuListener(new PopupPrintListener());
        addMouseListener(new MousePopupListener());
    }

    public JPopupMenu getJPopupMenu() {
        return this.popup;
    }

    private class ItemHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String selected = e.getActionCommand();
            if (selected.equals("Merge Roles")) {
                //Make and Show menu for merge roles
                //ObjectifyRolesDialog mergingOptions /*= new MergeRolesDialog(selectedRoles, selectedFT, frame, om)*/;
                // mergingOptions.setVisible(true);         

                //MatchDialog md = new MatchDialog(selectedFT, selectedFT.getFTE(), "", 0, selectedFT.isObjectType(), frame);
            }
            if (selected.equals("Export ORD")) {
                //Export the ORD
                ordp.saveORDToImage();
            }
        }
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
                popup.show(ORDPopupMenu.this, e.getX(), e.getY());
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
