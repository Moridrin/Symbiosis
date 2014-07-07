package equa.project.dialog;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import equa.controller.IView;
import equa.controller.PersistanceManager;
import equa.controller.ProjectController;
import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.Requirement;
import equa.meta.traceability.Category;
import equa.project.Project;

@SuppressWarnings("serial")
public class CategoryDialog extends JDialog implements IView, WindowListener {

    private JList<Category> listCategories;
    private ProjectController projectController;
    private DefaultListModel<Category> categoriesModel;

    /**
     * Create the dialog.
     */
    public CategoryDialog(ProjectController controller) {
        setTitle("Categories");
        projectController = controller;
        projectController.addView(this);

        addWindowListener(this);

        setBounds(100, 100, 278, 300);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        getContentPane().add(panel, BorderLayout.CENTER);

        JLabel lblCategory = new JLabel("Categories:");
        lblCategory.setBounds(10, 11, 98, 14);
        panel.add(lblCategory);
        categoriesModel = new DefaultListModel<Category>();
        refreshCategories();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 36, 242, 181);
        panel.add(scrollPane);

        listCategories = new JList<Category>();
        scrollPane.setViewportView(listCategories);
        listCategories.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        listCategories.setModel(categoriesModel);

        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedCategory(e);
            }
        });
        btnRemove.setBounds(10, 228, 74, 23);
        panel.add(btnRemove);

        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editCategory(e);
            }
        });
        btnEdit.setBounds(94, 228, 74, 23);
        panel.add(btnEdit);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCategory(e);
            }
        });
        btnAdd.setBounds(184, 228, 74, 23);
        panel.add(btnAdd);
    }

    protected void refreshCategories() {
        Project project = projectController.getProject();
        Iterator<Category> categories = project.getCategories();
        categoriesModel.clear();
        while (categories.hasNext()) {
            categoriesModel.addElement(categories.next());
        }
    }

    private void removeSelectedCategory(ActionEvent e) {
        for (Category c : listCategories.getSelectedValuesList()) {
            Iterator<Requirement> requirements = projectController.getProject().getRequirementModel().requirements();

            while (requirements.hasNext()) {
                Requirement req = requirements.next();
                if (req.getCategory().equals(c)) {
                    JOptionPane.showMessageDialog(this, "There are still requirements in the Category, please move them to another Category before deleting.");
                    return;
                }
            }

            try {
                if (JOptionPane.showConfirmDialog(this, "Remove " + c.getName()) == JOptionPane.OK_OPTION) {
                    Project p = projectController.getProject();
                    p.removeCategory(c);

                    if (projectController.isUseJPA()) {
                        EntityManager em = PersistanceManager.getEntityManager();
                        em.getTransaction().begin();
                        em.merge(p);
                        em.getTransaction().commit();
                    }

                    refreshCategories();
                }
            } catch (ChangeNotAllowedException exception) {
                // TODO Auto-generated catch block
            }
        }
    }

    private void editCategory(ActionEvent e) {
        if (listCategories.getSelectedValue() != null) {
            ChangeCategoryDialog changeDialog = new ChangeCategoryDialog(projectController, listCategories.getSelectedValue());
            Point point = getLocation();
            point.setLocation(point.x + 25, point.y + 25);
            changeDialog.setLocation(point);
            changeDialog.setVisible(true);
        }
    }

    private void addCategory(ActionEvent e) {
        AddCategoryDialog addDialog = new AddCategoryDialog(projectController);
        Point point = getLocation();
        point.setLocation(point.x + 25, point.y + 25);
        addDialog.setLocation(point);
        addDialog.setVisible(true);
    }

    @Override
    public void refresh() {
        refreshCategories();
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
