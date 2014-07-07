package equa.factbreakdown.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

import equa.controller.IView;
import equa.controller.ProjectController;
import equa.controller.SwingProjectController;
import equa.meta.requirements.FactRequirement;
import equa.swing.gui.StepByStepListCellRenderer;

public class RequirementsBreakdownPanel extends JPanel implements IView, Dockable {

    private static final long serialVersionUID = 1L;
    private ProjectController projectController;
    JList<FactRequirement> list;
    private DefaultListModel<FactRequirement> listRequirements;
    private ArrayList<FactRequirement> requirements;
    private DockKey dockKey;
    private int selectedIndex = 0;

    public RequirementsBreakdownPanel(ProjectController controller, ArrayList<FactRequirement> reqs) {

        dockKey = new DockKey("Requirement Breakdown");
        projectController = controller;
        requirements = reqs;

        listRequirements = new DefaultListModel<>();

        refreshRequirements();
        SpringLayout springLayout = new SpringLayout();
        setLayout(springLayout);

        JButton button_1 = new JButton("<-");
        button_1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousRequirement();
            }
        });
        add(button_1);

        JButton button_2 = new JButton("Load");
        button_2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadRequirement();
            }
        });
        springLayout.putConstraint(SpringLayout.NORTH, button_1, 0, SpringLayout.NORTH, button_2);
        springLayout.putConstraint(SpringLayout.NORTH, button_2, 261, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, button_2, 45, SpringLayout.WEST, this);
        add(button_2);

        JButton button = new JButton("->");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextRequirement();
            }
        });
        springLayout.putConstraint(SpringLayout.NORTH, button, 261, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, button, 100, SpringLayout.WEST, this);
        add(button);

        list = new JList<FactRequirement>();
        springLayout.putConstraint(SpringLayout.WEST, button_1, 0, SpringLayout.WEST, list);
        springLayout.putConstraint(SpringLayout.NORTH, list, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, list, 0, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, list, -6, SpringLayout.NORTH, button_2);
        springLayout.putConstraint(SpringLayout.EAST, list, -5, SpringLayout.EAST, this);
        add(list);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

        StepByStepListCellRenderer cellRenderer = new StepByStepListCellRenderer(requirements);
        list.setModel(listRequirements);
        list.setCellRenderer(cellRenderer);

        JLabel label = new JLabel("");
        springLayout.putConstraint(SpringLayout.NORTH, label, 272, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, label, 145, SpringLayout.WEST, this);
        add(label);

        JLabel label_1 = new JLabel("");
        springLayout.putConstraint(SpringLayout.NORTH, label_1, 272, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, label_1, 145, SpringLayout.WEST, this);
        add(label_1);

        JLabel label_2 = new JLabel("");
        springLayout.putConstraint(SpringLayout.NORTH, label_2, 272, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, label_2, 145, SpringLayout.WEST, this);
        add(label_2);

        if (projectController instanceof SwingProjectController) {
            SwingProjectController scontroller = (SwingProjectController) projectController;
            scontroller.getDesktop().initFactBreakdown(requirements.get(selectedIndex));
        }
    }

    private void nextRequirement() {
        if (projectController instanceof SwingProjectController) {

            SwingProjectController controller = (SwingProjectController) projectController;

            if (selectedIndex + 1 < requirements.size()) {
                selectedIndex++;
                controller.getDesktop().initFactBreakdown(requirements.get(selectedIndex));
            }
        }
    }

    private void previousRequirement() {
        if (projectController instanceof SwingProjectController) {

            SwingProjectController controller = (SwingProjectController) projectController;

            if (selectedIndex - 1 > 0) {
                selectedIndex--;
                controller.getDesktop().initFactBreakdown(requirements.get(selectedIndex));
            }
        }
    }

    private void loadRequirement() {
        if (projectController instanceof SwingProjectController) {
            SwingProjectController controller = (SwingProjectController) projectController;

            FactRequirement factRequirement = requirements.get(list.getSelectedIndex());
            selectedIndex = list.getSelectedIndex();

            if (!factRequirement.isRealized()) {
                controller.getDesktop().initFactBreakdown(factRequirement);
            } else {
                int result = JOptionPane.showConfirmDialog(null, "Fact requirement already decomposed, undo decomposition to decompose agian?");
                if (result == JOptionPane.OK_OPTION) {
                    //TODO: Frank vragen: undo decomposition and load fact requirement again.
                    //projectController.getProject().getObjectModel().removeFact();
                    controller.getDesktop().initFactBreakdown(factRequirement);
                    //factDecomposer.getTreeController().getRoot().deregisterAtObjectModel();
                }
            }
        }
    }

    private void refreshRequirements() {
        listRequirements.clear();
        for (FactRequirement r : requirements) {
            listRequirements.addElement(r);
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public DockKey getDockKey() {
        return dockKey;
    }

    @Override
    public void refresh() {
        //Do nothing
    }
}
