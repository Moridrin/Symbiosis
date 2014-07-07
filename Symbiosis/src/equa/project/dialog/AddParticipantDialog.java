package equa.project.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import equa.controller.IView;
import equa.controller.PersistanceManager;
import equa.controller.ProjectController;
import equa.meta.DuplicateException;
import equa.meta.IncorrectNameException;
import equa.project.Project;
import equa.project.ProjectMemberRole;
import equa.project.ProjectRole;
import equa.project.ProjectRoles;
import equa.project.StakeholderRole;

public class AddParticipantDialog extends JDialog implements IView {

    private static final long serialVersionUID = 1L;
    private JTextField tfParticipantName;
    private JTextField tfParticipantRole;
    private JPasswordField pwdParticipantPassword;
    private JComboBox<String> cbParticipantType;
    private ProjectRole participant;
    private ProjectController projectController;
    private EntityManager em;
    private EntityTransaction userTransaction;

    public AddParticipantDialog(ProjectRole projectRole, ProjectController controller) {
        getContentPane().setLayout(null);

        participant = projectRole;
        projectController = controller;

        setBounds(100, 100, 287, 190);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addParticipant();
                dispose();
                setVisible(false);
            }
        });
        btnAdd.setBounds(172, 118, 89, 23);
        getContentPane().add(btnAdd);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(73, 118, 89, 23);
        getContentPane().add(btnCancel);

        cbParticipantType = new JComboBox<>();
        cbParticipantType.setBounds(136, 11, 125, 20);
        getContentPane().add(cbParticipantType);
        cbParticipantType.addItem("Project Member");
        cbParticipantType.addItem("Stakeholder");

        tfParticipantName = new JTextField();
        tfParticipantName.setBounds(136, 36, 125, 20);
        getContentPane().add(tfParticipantName);
        tfParticipantName.setColumns(10);

        tfParticipantRole = new JTextField();
        tfParticipantRole.setBounds(136, 60, 125, 20);
        getContentPane().add(tfParticipantRole);
        tfParticipantRole.setColumns(10);

        JLabel lblType = new JLabel("Type:");
        lblType.setHorizontalAlignment(SwingConstants.TRAILING);
        lblType.setBounds(20, 15, 77, 14);
        getContentPane().add(lblType);

        JLabel lblNaam = new JLabel("Name:");
        lblNaam.setHorizontalAlignment(SwingConstants.TRAILING);
        lblNaam.setBounds(20, 40, 77, 14);
        getContentPane().add(lblNaam);

        JLabel lblRole = new JLabel("Role:");
        lblRole.setHorizontalAlignment(SwingConstants.TRAILING);
        lblRole.setBounds(20, 64, 77, 14);
        getContentPane().add(lblRole);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setHorizontalAlignment(SwingConstants.TRAILING);
        lblPassword.setBounds(20, 91, 77, 14);
        getContentPane().add(lblPassword);

        pwdParticipantPassword = new JPasswordField();
        pwdParticipantPassword.setBounds(136, 87, 125, 20);
        getContentPane().add(pwdParticipantPassword);

        if (projectRole != null) {
            setTitle("Change Name: " + projectRole.getName());
            btnAdd.setText("Change");

            if (projectRole instanceof StakeholderRole) {
                cbParticipantType.setSelectedItem(StakeholderRole.class);
            } else {
                cbParticipantType.setSelectedItem(ProjectMemberRole.class);
            }

            tfParticipantName.setText(projectRole.getName());
            tfParticipantRole.setText(projectRole.getRole());
        } else {
            setTitle("Add Participant");
        }
    }

    protected void addParticipant() {
        String value = (String) cbParticipantType.getSelectedItem();
        if (participant == null) {
            if (value.equalsIgnoreCase("Stakeholder")) {
                StakeholderRole stakeholder = projectController.getProject().getParticipants().addStakeholder(tfParticipantName.getText(), tfParticipantRole.getText());
                if (projectController.isUseJPA()) {
                    em = PersistanceManager.getEntityManager();
                    userTransaction = em.getTransaction();
                    userTransaction.begin();
                    em.persist(stakeholder);
                    Project p = projectController.getProject();
                    em.merge(p);
                    ProjectRoles participants = projectController.getProject().getParticipants();
                    em.merge(participants);
                    userTransaction.commit();
                }
            } else {
                ProjectMemberRole member = projectController.getProject().getParticipants().addProjectMember(tfParticipantName.getText(), tfParticipantRole.getText());
                if (projectController.isUseJPA()) {
                    em = PersistanceManager.getEntityManager();
                    userTransaction = em.getTransaction();
                    userTransaction.begin();
                    em.persist(member);
                    Project p = projectController.getProject();
                    em.merge(p);
                    ProjectRoles participants = projectController.getProject().getParticipants();
                    em.merge(participants);
                    userTransaction.commit();
                }
            }
        } else {
            //Edit current user
            if (!participant.getName().equalsIgnoreCase(tfParticipantName.getText())) {

                if (value.equalsIgnoreCase("Stakeholder") && participant instanceof StakeholderRole) {
                    try {
                        projectController.getProject().getParticipants().changeId(participant, tfParticipantName.getText(), tfParticipantRole.getText());
                    } catch (DuplicateException | IncorrectNameException e) {
                        // TODO Auto-generated catch block
                        JOptionPane.showMessageDialog(this, e.getMessage());
                    }
                } else if (value.equalsIgnoreCase("Project Member") && participant instanceof ProjectMemberRole) {
                    try {
                        projectController.getProject().getParticipants().changeId(participant, tfParticipantName.getText(), tfParticipantRole.getText());
                    } catch (DuplicateException | IncorrectNameException e) {
                        // TODO Auto-generated catch block
                        JOptionPane.showMessageDialog(this, e.getMessage());
                    }
                }
            }

            if ((!value.equalsIgnoreCase("Stakeholder") && participant instanceof StakeholderRole)
                    || (!value.equalsIgnoreCase("Project Member") && participant instanceof ProjectMemberRole)) {
                JOptionPane.showMessageDialog(this, "Switching Participant type isn't possible at this time.");
            }
        }
        projectController.refreshViews();
    }

    @Override
    public void refresh() {
        // Do nothing		
    }
}
