/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.configurator;

import javax.swing.ListModel;

import equa.meta.objectmodel.FactType;

/**
 *
 * @author frankpeeters
 */
public class FactTypeTableModel extends AbstractTableAdapter<FactType> {

    private static final long serialVersionUID = 1L;
    public static String[] COLUMN_NAMES = {"Kind", "Name", "Expression", "Constraints", "Inherits"};

    public FactTypeTableModel(ListModel<FactType> listModel) {
        super(listModel, COLUMN_NAMES);
        createChangeHandler();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FactType factType = getRow(rowIndex);
        if (factType == null) {
            return null;
        }

        switch (columnIndex) {
            case 0:
                return factType.getKind();
            case 1:
                return factType.getName();
            case 2:
                return factType.getTypeExpression();
            case 4:
                return factType.inheritsString();
            case 3:
                return factType.constraintString();
            default:
                return null;
        }
    }

}
