package equa.project.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
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
import equa.meta.traceability.Category;
import equa.project.Project;
import equa.project.ProjectRole;
import equa.project.StakeholderRole;
import equa.swing.gui.JTextFieldLimit;

@SuppressWarnings("serial")
public class AddCategoryDialog extends JDialog implements IView, WindowListener {

    private ProjectController projectController;

    private final JPanel contentPanel = new JPanel();
    private JTextField textFieldName;
    private JComboBox<StakeholderRole> comboBoxOwner;
    private JTextField textFieldCode;
    private JComboBox<StakeholderRole> cbViceOwner;

    /**
     * Create the dialog.
     */
    public AddCategoryDialog(ProjectController controller) {
        projectController = controller;
        projectController.addView(this);

        addWindowListener(this);

        setTitle("Add category");
        setBounds(100, 100, 287, 190);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);
        {
            JLabel lblCategoryName = new JLabel("Category name:");
            lblCategoryName.setHorizontalAlignment(SwingConstants.TRAILING);
            lblCategoryName.setBounds(-19, 34, 110, 14);
            contentPanel.add(lblCategoryName);
        }
        {
            JLabel lblOwner = new JLabel("Owner:");
            lblOwner.setHorizontalAlignment(SwingConstants.TRAILING);
            lblOwner.setBounds(10, 59, 81, 14);
            contentPanel.add(lblOwner);
        }
        {
            textFieldName = new JTextField();
            textFieldName.setBounds(120, 31, 141, 20);
            contentPanel.add(textFieldName);
            textFieldName.setColumns(10);
        }
        {
            comboBoxOwner = new JComboBox<StakeholderRole>();
            Iterator<ProjectRole> participants = projectController.getProject().getParticipants().projectRoles();
            while (participants.hasNext()) {
                ProjectRole participant = participants.next();

                if (participant instanceof StakeholderRole) {
                    comboBoxOwner.addItem((StakeholderRole) participant);
                }
            }
            comboBoxOwner.setBounds(120, 56, 119, 20);
            contentPanel.add(comboBoxOwner);
        }
        {
            JLabel lblCode = new JLabel("Code:");
            lblCode.setHorizontalAlignment(SwingConstants.TRAILING);
            lblCode.setBounds(35, 11, 53, 14);
            contentPanel.add(lblCode);
        }
        {
            textFieldCode = new JTextField();
            textFieldCode.setColumns(10);
            textFieldCode.setBounds(208, 8, 53, 20);
            textFieldCode.setDocument(new JTextFieldLimit(3));
            contentPanel.add(textFieldCode);
        }
        {
            cbViceOwner = new JComboBox<StakeholderRole>();
            cbViceOwner.addItem(null);
            Iterator<ProjectRole> participants = projectController.getProject().getParticipants().projectRoles();
            while (participants.hasNext()) {
                ProjectRole participant = participants.next();

                if (participant instanceof StakeholderRole) {
                    cbViceOwner.addItem((StakeholderRole) participant);
                }
            }
            cbViceOwner.setBounds(120, 82, 119, 20);
            contentPanel.add(cbViceOwner);
        }
        {
            JLabel lblViceOwner = new JLabel("Vice owner:");
            lblViceOwner.setHorizontalAlignment(SwingConstants.RIGHT);
            lblViceOwner.setBounds(10, 85, 81, 14);
            contentPanel.add(lblViceOwner);
        }
        {
            JButton button = new JButton("...");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    OpenParticipantWindow();
                }
            });
            button.setBounds(241, 55, 20, 23);
            contentPanel.add(button);
        }
        {
            JButton button = new JButton("...");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    OpenParticipantWindow();
                }
            });
            button.setBounds(241, 81, 20, 23);
            contentPanel.add(button);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                        setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
            {
                JButton confirmButton = new JButton("Confirm");
                confirmButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addCategory();
                    }
                });
                confirmButton.setActionCommand("Confirm");
                buttonPane.add(confirmButton);
                getRootPane().setDefaultButton(confirmButton);
            }
        }
    }

    protected void OpenParticipantWindow() {
        // TODO Auto-generated method stub
        ParticipantDialog participants = new ParticipantDialog(null, false, projectController);
        Point point = getLocation();
        point.setLocation(point.x + 25, point.y + 25);
        participants.setLocation(point);
        participants.setVisible(true);
    }

    private void addCategory() {
        if (!textFieldCode.getText().isEmpty() && !textFieldName.getText().isEmpty()) {

            Project p = projectController.getProject();

            Category category = p.addCategory(textFieldCode.getText().toUpperCase(), textFieldName.getText());
            if ((StakeholderRole) comboBoxOwner.getSelectedItem() != null) {
                category.setOwner((StakeholderRole) comboBoxOwner.getSelectedItem());
            }
            if ((StakeholderRole) cbViceOwner.getSelectedItem() != null) {
                category.setViceOwner((StakeholderRole) comboBoxOwner.getSelectedItem(), (StakeholderRole) cbViceOwner.getSelectedItem());
            }

            if (projectController.isUseJPA()) {
                EntityManager em = PersistanceManager.getEntityManager();
                EntityTransaction transaction = em.getTransaction();

                transaction.begin();
                em.persist(category);
                transaction.commit();
            }

            projectController.refreshViews();

            this.setVisible(false);
            this.dispose();
        } else {
            if (textFieldCode.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please insert a Code for the Category");
            } else if (textFieldName.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please insert a Name for the Category");
            }
        }
    }

    @Override
    public void refresh() {
        comboBoxOwner.removeAllItems();
        cbViceOwner.removeAllItems();
        comboBoxOwner.addItem(null);
        cbViceOwner.addItem(null);
        Iterator<ProjectRole> participants = projectController.getProject().getParticipants().projectRoles();
        while (participants.hasNext()) {
            ProjectRole participant = participants.next();

            if (participant instanceof StakeholderRole) {
                comboBoxOwner.addItem((StakeholderRole) participant);
                cbViceOwner.addItem((StakeholderRole) participant);
            }
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
}
