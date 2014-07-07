package equa.project.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import equa.controller.IView;
import equa.controller.PersistanceManager;
import equa.controller.ProjectController;
import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.Requirement;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.project.Project;
import equa.project.ProjectRole;
import equa.project.StakeholderRole;

@SuppressWarnings("serial")
public class ChangeCategoryDialog extends JDialog implements IView, WindowListener {

    private ProjectController projectController;
    private final JPanel contentPanel = new JPanel();
    private JTextField textFieldName;
    private Category category;
    private JComboBox<StakeholderRole> comboBoxOwners;
    private JComboBox<StakeholderRole> cbViceOwner;

    /**
     * Create the dialog.
     *
     * @param category
     */
    public ChangeCategoryDialog(ProjectController controller, Category category) {
        projectController = controller;
        projectController.addView(this);

        addWindowListener(this);

        this.category = category;
        setTitle("Change category");
        setBounds(100, 100, 288, 165);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);
        {
            JLabel labelCategoryName = new JLabel("Category name:");
            labelCategoryName.setHorizontalAlignment(SwingConstants.RIGHT);
            labelCategoryName.setBounds(-17, 14, 105, 14);
            contentPanel.add(labelCategoryName);
        }
        {
            JLabel labelOwner = new JLabel("Owner:");
            labelOwner.setHorizontalAlignment(SwingConstants.RIGHT);
            labelOwner.setBounds(-7, 39, 95, 14);
            contentPanel.add(labelOwner);
        }
        {
            textFieldName = new JTextField();
            textFieldName.setColumns(10);
            textFieldName.setBounds(120, 11, 141, 20);
            textFieldName.setText(category.getName());
            contentPanel.add(textFieldName);
        }
        {
            comboBoxOwners = new JComboBox<StakeholderRole>();
            Iterator<ProjectRole> participants = projectController.getProject().getParticipants().projectRoles();
            while (participants.hasNext()) {
                ProjectRole participant = participants.next();

                if (participant instanceof StakeholderRole) {
                    comboBoxOwners.addItem((StakeholderRole) participant);
                }
            }

            comboBoxOwners.setBounds(120, 36, 141, 20);
            comboBoxOwners.setSelectedItem(category.getOwner());
            //comboBoxOwners.
            contentPanel.add(comboBoxOwners);
        }

        cbViceOwner = new JComboBox<StakeholderRole>();
        cbViceOwner.addItem(null);
        Iterator<ProjectRole> participants = projectController.getProject().getParticipants().projectRoles();
        while (participants.hasNext()) {
            ProjectRole participant = participants.next();

            if (participant instanceof StakeholderRole) {
                cbViceOwner.addItem((StakeholderRole) participant);
            }
        }
        cbViceOwner.setBounds(120, 64, 141, 20);
        cbViceOwner.setSelectedItem(category.getViceOwner());
        contentPanel.add(cbViceOwner);

        JLabel lblViceOwner = new JLabel("Vice Owner:");
        lblViceOwner.setHorizontalAlignment(SwingConstants.RIGHT);
        lblViceOwner.setBounds(-17, 67, 105, 14);
        contentPanel.add(lblViceOwner);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                        dispose();
                    }
                });
                buttonPane.add(cancelButton);
            }
            {
                JButton confirmButton = new JButton("Confirm");
                confirmButton.setActionCommand("OK");
                confirmButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        changeCategory();
                    }
                });
                buttonPane.add(confirmButton);
                getRootPane().setDefaultButton(confirmButton);
            }
        }
    }

    private void changeCategory() {
        Project project = projectController.getProject();

        if (category.getOwner() != (StakeholderRole) comboBoxOwners.getSelectedItem() && category.getOwner() != null) {
            Iterator<Requirement> iterator = project.getRequirementModel().requirements();
            while (iterator.hasNext()) {
                Requirement req = iterator.next();
                if (req.getCategory().equals(category)) {
                    try {
                        req.getReviewState().change(new ExternalInput("Change", projectController.getCurrentUser()), req.getText());
                    } catch (ChangeNotAllowedException e) {
                        JOptionPane.showMessageDialog(this, e.getMessage());
                    }
                }
            }
        }
        category.setOwner((StakeholderRole) comboBoxOwners.getSelectedItem());
        category.setViceOwner((StakeholderRole) cbViceOwner.getSelectedItem());
        category.setName(textFieldName.getText());

        if (projectController.isUseJPA()) {
            EntityManager em = PersistanceManager.getEntityManager();
            em.getTransaction().begin();
            em.merge(category);
            em.getTransaction().commit();
        }

        projectController.refreshViews();
        dispose();
    }

    @Override
    public void refresh() {
        // No Action
    }

    @Override
    public void windowOpened(WindowEvent e) {
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
    public void windowClosing(WindowEvent e) {
        projectController.removeView(this);
    }
}
