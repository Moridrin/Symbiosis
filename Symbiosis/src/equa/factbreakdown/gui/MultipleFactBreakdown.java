package equa.factbreakdown.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import equa.controller.IView;
import equa.controller.ProjectController;
import equa.meta.requirements.FactRequirement;
import equa.swing.gui.StepByStepListCellRenderer;

@SuppressWarnings("serial")
public class MultipleFactBreakdown extends JFrame implements IView,
        WindowListener {

    private FactBreakdown factBreakdown;
    private ArrayList<FactRequirement> requirements;
    private ProjectController projectController;
    private final JList<FactRequirement> listRequirements = new JList<>();
    private DefaultListModel<FactRequirement> listModelRequirements;
    private int selectedIndex = 0;

    public MultipleFactBreakdown(ProjectController controller, ArrayList<FactRequirement> reqs) {
        setTitle("Fact Breakdown");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        projectController = controller;
        projectController.addView(this);

        requirements = reqs;
        addWindowListener(this);
        getContentPane().setLayout(null);
        setMinimumSize(new Dimension(760, 370));

        JPanel panelDecomposition = new JPanel();
        panelDecomposition.setBorder(new TitledBorder(null, "Breakdown", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelDecomposition.setBounds(278, 0, 466, 333);
        getContentPane().add(panelDecomposition);
        panelDecomposition.setLayout(null);
        factBreakdown = new FactBreakdown(projectController);
        factBreakdown.getTree().setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        factBreakdown.setBounds(10, 23, 446, 277);
        panelDecomposition.add(factBreakdown);
        factBreakdown.getTree().setRowHeight(FactBreakdown.getRowHeight());

        JButton btnFinishedDecomposition = new JButton("Close");
        btnFinishedDecomposition.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Frank vragen wat hij vindt
                dispose();
                setVisible(false);
            }
        });
        btnFinishedDecomposition.setBounds(313, 303, 143, 23);
        panelDecomposition.add(btnFinishedDecomposition);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                setVisible(false);
            }
        });
        btnCancel.setBounds(214, 303, 89, 23);
        panelDecomposition.add(btnCancel);

        JPanel panelRequirements = new JPanel();
        panelRequirements.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Requirements", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelRequirements.setBounds(0, 0, 276, 333);
        getContentPane().add(panelRequirements);
        panelRequirements.setLayout(null);
        listRequirements.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        listRequirements.setBounds(6, 23, 260, 275);
        panelRequirements.add(listRequirements);

        listModelRequirements = new DefaultListModel<>();
        refreshRequirements();

        ListCellRenderer<FactRequirement> cellRenderer = new StepByStepListCellRenderer(requirements);

        listRequirements.setModel(listModelRequirements);
        listRequirements.setCellRenderer(cellRenderer);

        JButton btnNext = new JButton("->");
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                nextRequirement();
            }
        });
        btnNext.setBounds(221, 304, 45, 23);
        panelRequirements.add(btnNext);

        JButton btnPrevious = new JButton("<-");
        btnPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex >= 0 && selectedIndex < requirements.size()) {
                    factBreakdown.initExpressionTree(requirements.get(selectedIndex));
                    selectedIndex--;
                }
            }
        });
        btnPrevious.setBounds(114, 304, 45, 23);
        panelRequirements.add(btnPrevious);

        JButton btnLoad = new JButton("Load");
        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FactRequirement factRequirement = requirements.get(listRequirements.getSelectedIndex());
                selectedIndex = listRequirements.getSelectedIndex();
                System.out.println("SelectedIndex: " + selectedIndex);

                if (!factRequirement.isRealized()) {
                    factBreakdown.initExpressionTree(factRequirement);
                }
            }
        });
        btnLoad.setBounds(163, 304, 55, 23);
        panelRequirements.add(btnLoad);
    }

    private void nextRequirement() {

        if (selectedIndex < requirements.size()) {

            System.out.println("Init: " + listModelRequirements.get(selectedIndex));
            factBreakdown.initExpressionTree(requirements.get(selectedIndex));
            selectedIndex++;
        }
    }

    private void refreshRequirements() {
        listModelRequirements.clear();
        for (FactRequirement r : requirements) {
            listModelRequirements.addElement(r);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        projectController.removeView(this);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void refresh() {
        refreshRequirements();
    }
}
