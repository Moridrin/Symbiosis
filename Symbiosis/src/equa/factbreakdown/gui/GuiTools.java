/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import javax.swing.ListModel;

import equa.meta.objectmodel.FactType;

/**
 *
 * @author frankpeeters
 */
public class GuiTools {

    public static String intellisense(javax.swing.JTextField tfTypeName, javax.swing.JList<FactType> lsTypes) {
        ListModel<FactType> model = lsTypes.getModel();
        String name = tfTypeName.getText();
        if (name.isEmpty()) {
            return name;
        }

        int upper = model.getSize();
        if (upper == 0) {
            return name;
        }
        int lower = 0;
        String foundName;
        while (upper - lower > 1) {
            int middle = (lower + upper) / 2;
            foundName = ((FactType) model.getElementAt(middle)).getName();
            if (name.compareToIgnoreCase(foundName) < 0) {
                upper = middle;
            } else {
                lower = middle;
            }
        }
        foundName = ((FactType) model.getElementAt(lower)).getName();

        lsTypes.ensureIndexIsVisible(lower);
        lsTypes.ensureIndexIsVisible(lower + 1);
        return foundName;
    }
}
