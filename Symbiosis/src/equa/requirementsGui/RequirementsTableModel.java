package equa.requirementsGui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import equa.meta.requirements.Requirement;

@SuppressWarnings("serial")
public class RequirementsTableModel extends AbstractTableModel {

    public static final int COLUMN_NUMBER = 0;
    public static final int COLUMN_NAME = 1;
    private String[] columnNames = new String[]{"Realized", "Name", "Kind", "RevState", "RevImpact", "Text"};
    private ArrayList<Requirement> requirements;

    public RequirementsTableModel(ArrayList<Requirement> reqs) {
        requirements = reqs;

    }

    @Override
    public int getRowCount() {
        return requirements.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Requirement req = requirements.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return req.isRealized();
            case 1:
                return req.getName();
            case 2:
                return req.getReqKind();
            case 3:
                return req.getReviewState().toString();
            case 4:
                return req.getReviewState().getReviewImpact().toString();
            case 5:
                return req.getText();
            default:
                return null;
        }
    }

    public Requirement getRequirementAt(int rowIndex) {
        try {
            return requirements.get(rowIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
