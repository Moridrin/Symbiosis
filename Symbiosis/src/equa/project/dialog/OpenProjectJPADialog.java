package equa.project.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import equa.controller.IView;
import equa.controller.ProjectController;
import equa.controller.SwingProjectController;
import equa.project.Project;

public class OpenProjectJPADialog extends JDialog implements IView {

    private static final long serialVersionUID = 1L;

    ProjectController projectController;
    JList<Project> projectsList;
    DefaultListModel<Project> projectListModel;

    public OpenProjectJPADialog(ProjectController controller) {
        getContentPane().setLayout(null);

        projectController = controller;

        setMinimumSize(new Dimension(230, 335));
        projectsList = new JList<Project>();
        projectsList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        projectsList.setBounds(10, 29, 195, 224);
        getContentPane().add(projectsList);

        JLabel lblProjects = new JLabel("Projects");
        lblProjects.setBounds(10, 11, 46, 14);
        getContentPane().add(lblProjects);

        JButton btnSelectProject = new JButton("Select Project");
        btnSelectProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectProject();
            }
        });
        btnSelectProject.setBounds(106, 262, 99, 23);
        getContentPane().add(btnSelectProject);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        btnCancel.setBounds(7, 262, 89, 23);
        getContentPane().add(btnCancel);

        projectListModel = new DefaultListModel<Project>();

        initProjects();
    }

    protected void selectProject() {
        projectController.openProject(projectsList.getSelectedValue());
        if (projectController.getProject() != null) {
            if (projectController instanceof SwingProjectController) {
                ((SwingProjectController) projectController).initDesktop();
            }
            LoginDialog loginDialog = new LoginDialog(null, false, projectController);
            loginDialog.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "No project found.");
        }
    }

    protected void cancel() {
        dispose();
    }

    private void initProjects() {
        List<?> projects = projectController.getAllProjects();
        for (Object p : projects) {
            if (p instanceof Project) {
                projectListModel.addElement((Project) p);
            }
        }
        projectsList.setModel(projectListModel);
    }

    @Override
    public void refresh() {

    }
}
