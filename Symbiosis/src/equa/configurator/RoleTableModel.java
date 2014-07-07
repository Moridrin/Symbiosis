package equa.configurator;

import javax.swing.ListModel;

import equa.meta.objectmodel.Role;

/**
 *
 * @author Jaron
 */
public class RoleTableModel extends AbstractTableAdapter<Role> {

    private static final long serialVersionUID = 1L;
    public static String[] COLUMN_NAMES = {"RoleName", "RoleType", "Constraints"};

    public RoleTableModel(ListModel<Role> listModel) {
        super(listModel, COLUMN_NAMES);
        createChangeHandler();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Role role = getRow(rowIndex);
        switch (columnIndex) {
            case 0:
                return role.getRoleName();
            case 1:
                return role.getSubstitutionType().toString();
            case 2:
                return role.getConstraintString();
            default:
                return null;
        }
    }
}
