package equa.swing.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import equa.meta.requirements.FactRequirement;

public class StepByStepListCellRenderer extends JLabel implements ListCellRenderer<FactRequirement> {

    private static final long serialVersionUID = 1L;
    private ArrayList<FactRequirement> requirements;

    public StepByStepListCellRenderer(ArrayList<FactRequirement> reqs) {
        setOpaque(true);
        requirements = reqs;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends FactRequirement> list, FactRequirement value,
            int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.toString());
        FactRequirement r = requirements.get(index);
        if (!isSelected) {
            if (r.isRealized()) {
                setBackground(Color.GREEN);
            } else {
                setBackground(Color.YELLOW);
            }
        } else {
            setBackground(Color.cyan);
        }
        return this;
    }
}
