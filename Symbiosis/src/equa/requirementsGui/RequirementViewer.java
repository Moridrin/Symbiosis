package equa.requirementsGui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

import equa.controller.IView;
import equa.controller.ProjectController;
import equa.controller.SwingProjectController;
import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.ActionRequirement;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.QualityAttribute;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementFilter;
import equa.meta.requirements.RequirementModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.AddRejectedState;
import equa.meta.traceability.AddedState;
import equa.meta.traceability.ApprovedState;
import equa.meta.traceability.Category;
import equa.meta.traceability.ChangeRejectedState;
import equa.meta.traceability.ChangedState;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.Impact;
import equa.meta.traceability.RemoveRejectedState;
import equa.meta.traceability.RemovedState;
import equa.project.Project;
import equa.project.ProjectRole;
import equa.project.dialog.EnterRequirementDialog;
import equa.util.SwingUtils;
import fontys.observer.PropertyListener;

public class RequirementViewer extends JPanel implements IView, Dockable, PropertyListener {

    private static final long serialVersionUID = 1L;
    public static final int CATEGORY = 0;
    public static final int REVIEW_STATE = 1;
    public static final int KIND = 2;
    public static final int REVIEW_IMPACT = 3;
    public static final int OTHER_FILTER = 4;
    private ProjectController projectController;
    private ArrayList<Requirement> filteredRequirements;
    private DockKey dockKey;
    private JTable[] filterTables;
    private JTable tblRequirements;
    private JPopupMenu requirementPopupMenu;
    private JMenuItem mntmAddRequirement;
    private JMenuItem mntmEditRequirement;
    private JMenuItem mntmApproveRequirement;
    private JMenuItem mntmRejectRequirement;
    private JMenuItem mntmRemoveRequirement;
    private JMenuItem mntmRollbackRequirement;
    private JMenuItem mntmDecompose;
    private JMenuItem mntmRuleAssignment;

    public RequirementViewer(ProjectController controller) {
        dockKey = new DockKey("Requirement Viewer");
        dockKey.setCloseEnabled(false);
        filteredRequirements = new ArrayList<>();
        setProjectController(controller);
    }

    public final void setProjectController(ProjectController controller) {
        projectController = controller;

        if (controller.getProject() != null) {
            projectController.addView(this);
            Project p = projectController.getProject();
            p.addListener(this, "currentUser");
            initView();
            deselectSYS();
            refresh();
        }
    }

    private void initPopup() {
        ProjectRole currentUser = projectController.getCurrentUser();

        if (currentUser != null) {

            Project project = projectController.getProject();
            mntmAddRequirement.setEnabled(true);

            Requirement req = getSelectedRequirement();
            if (req != null) {
                mntmEditRequirement.setEnabled(req.isChangeable(currentUser));
                mntmApproveRequirement.setEnabled(req.isApprovable(currentUser));
                mntmRejectRequirement.setEnabled(req.isRejectable(currentUser));
                mntmRemoveRequirement.setEnabled(req.isRemovable(currentUser));
                mntmRollbackRequirement.setEnabled(req.isRollBackable(currentUser));

            } else {
                mntmEditRequirement.setEnabled(false);
                mntmApproveRequirement.setEnabled(false);
                mntmRejectRequirement.setEnabled(false);
                mntmRemoveRequirement.setEnabled(false);
                mntmRollbackRequirement.setEnabled(false);
            }
            mntmDecompose.setEnabled(project.isLoggedUserProjectMember());
        } else {
            mntmAddRequirement.setEnabled(false);
            mntmEditRequirement.setEnabled(false);
            mntmDecompose.setEnabled(false);
            mntmApproveRequirement.setEnabled(false);
            mntmRejectRequirement.setEnabled(false);
            mntmRemoveRequirement.setEnabled(false);
            mntmRollbackRequirement.setEnabled(false);
        }
    }

    public void initView() {
        filterTables = new JTable[5];

        setMinimumSize(new Dimension(737, 530));

        JPanel panelRequirements = new JPanel();
        panelRequirements.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Requirements", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        JPanel panelCategories = new JPanel();
        panelCategories.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                " ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        JPanel panelState = new JPanel();
        panelState.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                " ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        JPanel panelKind = new JPanel();
        panelKind.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                " ", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        JScrollPane scrollPane_1 = new JScrollPane();
        GroupLayout gl_panelState = new GroupLayout(panelState);
        gl_panelState.setHorizontalGroup(
                gl_panelState.createParallelGroup(Alignment.LEADING)
                .addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE));
        gl_panelState.setVerticalGroup(
                gl_panelState.createParallelGroup(Alignment.LEADING)
                .addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE));

        JScrollPane scrollPane_2 = new JScrollPane();
        GroupLayout gl_panelKind = new GroupLayout(panelKind);
        gl_panelKind.setHorizontalGroup(
                gl_panelKind.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelKind.createSequentialGroup()
                        .addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(116, Short.MAX_VALUE)));
        gl_panelKind.setVerticalGroup(
                gl_panelKind.createParallelGroup(Alignment.LEADING)
                .addComponent(scrollPane_2, GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE));

        JPanel panelImpact = new JPanel();
        panelImpact.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), " ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelImpact.setLayout(null);

        panelRequirements.setLayout(new BoxLayout(panelRequirements, BoxLayout.X_AXIS));
        JScrollPane scrollPane_3 = new JScrollPane();
        panelRequirements.add(scrollPane_3);
        tblRequirements = new RequirementsTable(new RequirementsTableModel(filteredRequirements));
        //tblRequirements.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        //tca = new TableColumnAdjuster(tblRequirements);
        //tca.adjustColumns();
        TableColumnModel tcm = tblRequirements.getColumnModel();
        tcm.getColumn(0).setMinWidth(40); // realized
        tcm.getColumn(1).setMinWidth(70); // name
        tcm.getColumn(2).setMinWidth(70); // kind
        tcm.getColumn(3).setMinWidth(50); // revstate
        tcm.getColumn(4).setMinWidth(70); // revimpact
        tcm.getColumn(5).setMinWidth(200); // text
        scrollPane_3.setViewportView(tblRequirements);

        SpringLayout springLayout = new SpringLayout();
        springLayout.putConstraint(SpringLayout.NORTH, panelImpact, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, panelImpact, 441, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, panelImpact, 120, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, panelImpact, 584, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, panelRequirements, 121, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, panelRequirements, 4, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, panelRequirements, 0, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.EAST, panelRequirements, 0, SpringLayout.EAST, this);
        springLayout.putConstraint(SpringLayout.NORTH, panelKind, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, panelKind, 294, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, panelKind, 120, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, panelKind, 437, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, panelState, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, panelState, 149, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, panelCategories, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, panelCategories, 4, SpringLayout.WEST, this);
        setLayout(springLayout);
        this.add(panelCategories);
        this.add(panelState);
        this.add(panelKind);
        this.add(panelRequirements);
        this.add(panelImpact);

        List<RequirementFilter> reviewFilters = new ArrayList<>();
        reviewFilters.add(AddedState.getRequirementFilter());
        reviewFilters.add(AddRejectedState.getRequirementFilter());
        reviewFilters.add(ApprovedState.getRequirementFilter());
        reviewFilters.add(ChangeRejectedState.getRequirementFilter());
        reviewFilters.add(RemoveRejectedState.getRequirementFilter());
        reviewFilters.add(RemovedState.getRequirementFilter());
        reviewFilters.add(ChangedState.getRequirementFilter());
        filterTables[REVIEW_STATE] = new JTable(new FilterTableModel(this, "ReviewState", reviewFilters));
        scrollPane_1.setViewportView(filterTables[REVIEW_STATE]);
        panelState.setLayout(gl_panelState);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        GroupLayout gl_panelCategories = new GroupLayout(panelCategories);
        gl_panelCategories.setHorizontalGroup(
                gl_panelCategories.createParallelGroup(Alignment.LEADING)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE));
        gl_panelCategories.setVerticalGroup(
                gl_panelCategories.createParallelGroup(Alignment.LEADING)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE));

        initCategories();
        scrollPane.setViewportView(filterTables[CATEGORY]);
        panelCategories.setLayout(gl_panelCategories);

        List<RequirementFilter> reqFilters = new ArrayList<>();
        reqFilters.add(ActionRequirement.getRequirementFilter());
        reqFilters.add(FactRequirement.getRequirementFilter());
        reqFilters.add(RuleRequirement.getRequirementFilter());
        reqFilters.add(QualityAttribute.getRequirementFilter());

        filterTables[KIND] = new JTable(new FilterTableModel(this, "ReqKind", reqFilters));
        scrollPane_2.setViewportView(filterTables[KIND]);
        panelKind.setLayout(gl_panelKind);

        JScrollPane scrollPane_4 = new JScrollPane();
        scrollPane_4.setBounds(10, 15, 123, 94);
        panelImpact.add(scrollPane_4);
        ArrayList<RequirementFilter> impacts = new ArrayList<>();
        impacts.add(Impact.UNDEFINED);
        impacts.add(Impact.ZERO);
        impacts.add(Impact.LIGHT);
        impacts.add(Impact.NORMAL);
        impacts.add(Impact.SERIOUS);

        filterTables[REVIEW_IMPACT] = new JTable(new FilterTableModel(this, "ReviewImpact", impacts));
        scrollPane_4.setViewportView(filterTables[REVIEW_IMPACT]);

        JPanel panelOther = new JPanel();
        springLayout.putConstraint(SpringLayout.NORTH, panelOther, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, panelOther, 585, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, panelOther, 120, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, panelOther, 728, SpringLayout.WEST, this);
        panelOther.setLayout(null);
        panelOther.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), " ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        this.add(panelOther);
        JScrollPane scrollPane_5 = new JScrollPane();
        scrollPane_5.setBounds(10, 15, 123, 94);
        panelOther.add(scrollPane_5);

        ArrayList<RequirementFilter> others = new ArrayList<>();
        others.add(new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.isRealized();
            }

            @Override
            public String toString() {
                return "Realized";
            }
        });

        others.add(new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return !requirement.isRealized();
            }

            @Override
            public String toString() {
                return "Unrealized";
            }
        });

        others.add(new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getCategory().isOwner(projectController.getCurrentUser());
            }

            @Override
            public String toString() {
                return "Owner";
            }
        });

        others.add(new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return !requirement.getCategory().isOwner(projectController.getCurrentUser());
            }

            @Override
            public String toString() {
                return "NotOwner";
            }
        });

        filterTables[OTHER_FILTER] = new JTable(new FilterTableModel(this, "OtherFilter", others));
        scrollPane_5.setViewportView(filterTables[4]);
        filterTables[OTHER_FILTER].setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        for (int i = 0; i < filterTables.length; i++) {
            filterTables[i].setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            filterTables[i].getColumnModel().getColumn(0).setPreferredWidth(8);
        }

        requirementPopupMenu = new JPopupMenu();
        addPopup(tblRequirements, requirementPopupMenu);

        mntmAddRequirement = new JMenuItem("Add Requirement");
        mntmAddRequirement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openEnterRequirementDialog();
            }
        });
        requirementPopupMenu.add(mntmAddRequirement);

        mntmEditRequirement = new JMenuItem("Edit Requirement");
        mntmEditRequirement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openChangeRequirementDialog();
            }
        });
        requirementPopupMenu.add(mntmEditRequirement);

        requirementPopupMenu.add(new JSeparator());

        mntmDecompose = new JMenuItem("Breakdown Selection");
        mntmDecompose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decomposeSelection(tblRequirements.getSelectedRows());
            }
        });
        requirementPopupMenu.add(mntmDecompose);

        mntmRuleAssignment = new JMenuItem("Rule Assignment");
        mntmRuleAssignment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = tblRequirements.getSelectedRows();
                if (selectedRows.length == 0) {
                    return;
                }
                Requirement req = filteredRequirements.get(selectedRows[0]);
                if (req instanceof RuleRequirement) {
                    RuleAssignmentDialog dialog;
                    dialog = new RuleAssignmentDialog(null, true, projectController.getProject().getObjectModel());
                    dialog.setVisible(true);
                }

            }
        });
        requirementPopupMenu.add(mntmRuleAssignment);

        requirementPopupMenu.add(new JSeparator());

        mntmApproveRequirement = new JMenuItem("Approve Requirement");
        mntmApproveRequirement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                approveRequirement(tblRequirements.getSelectedRows());
                projectController.refreshViews();
            }
        });
        requirementPopupMenu.add(mntmApproveRequirement);

        mntmRejectRequirement = new JMenuItem("Reject Requirement");
        mntmRejectRequirement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rejectRequirement(tblRequirements.getSelectedRows());
                projectController.refreshViews();
            }
        });
        requirementPopupMenu.add(mntmRejectRequirement);

        mntmRemoveRequirement = new JMenuItem("Remove Requirement");
        mntmRemoveRequirement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeRequirement(tblRequirements.getSelectedRows());
                projectController.refreshViews();
            }
        });
        requirementPopupMenu.add(mntmRemoveRequirement);

        mntmRollbackRequirement = new JMenuItem("Rollback Requirement");
        mntmRollbackRequirement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollBackRequirement(tblRequirements.getSelectedRows());
                projectController.refreshViews();
            }
        });
        requirementPopupMenu.add(mntmRollbackRequirement);

        deselectSYS();

//        JMenu mnRequirements = new JMenu("Requirements");
//        JMenuItem mntmAction = new JMenuItem("Add Requirement");
//        mntmAction.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                openEnterRequirementDialog();
//            }
//        });
//        JMenu mnCategories = new JMenu("Categories");
//        JMenuItem mntmCategories = new JMenuItem("Categories");
//        mntmCategories.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                CategoryDialog categories = new CategoryDialog(projectController);
//                Point point = getLocation();
//                point.setLocation(point.x + 25, point.y + 25);
//                categories.setLocation(point);
//                categories.setVisible(true);
//            }
//        });
//        mnCategories.add(mntmCategories);
//        JMenuItem mntmAddCategory = new JMenuItem("Add Category");
//        mntmAddCategory.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                AddCategoryDialog categories = new AddCategoryDialog(projectController);
//                Point point = getLocation();
//                point.setLocation(point.x + 25, point.y + 25);
//                categories.setLocation(point);
//                categories.setVisible(true);
//            }
//        });
//        mnCategories.add(mntmAddCategory);
    }

    private void initCategories() {
        if (projectController.getProject() == null) {
            return;
        }
        Iterator<Category> itCategories = projectController.getProject().getCategories();
        ArrayList<RequirementFilter> categories;
        categories = new ArrayList<>();
        while (itCategories.hasNext()) {
            categories.add(itCategories.next());
        }
        if (filterTables[CATEGORY] == null) {
            filterTables[CATEGORY] = new JTable(new FilterTableModel(this, "Categories", categories));
        } else {
            filterTables[CATEGORY].setModel(new FilterTableModel(this, "Categories", categories));
        }
    }

    private boolean filtered(Requirement req) {
        for (JTable table : filterTables) {
            FilterTableModel ftm = (FilterTableModel) table.getModel();
            if (!ftm.accepts(req)) {
                return false;
            }
        }
        return true;
    }

    private void deselectSYS() {
        FilterTableModel tableModel = (FilterTableModel) filterTables[CATEGORY].getModel();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Category cat = (Category) tableModel.getValueAt(row, 1);
            if (cat.equals(Category.SYSTEM)) {
                tableModel.setValueAt(Boolean.FALSE, row, 0);
            } else {
                tableModel.setValueAt(Boolean.TRUE, row, 0);
            }
        }
    }

    public class RequirementsTable extends JTable {

        private static final long serialVersionUID = 1L;

        public RequirementsTable(TableModel model) {
            super(model);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            initPopup();
        }
    }

    void rollBackRequirement(int[] selectedRows) {
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "No requirements selected");
        } else {
            for (int i = 0; i < selectedRows.length; i++) {
                Requirement r = filteredRequirements.get(selectedRows[i]);
                try {
                    r.getReviewState().rollBack(projectController.getCurrentUser());
                } catch (ChangeNotAllowedException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
        }
    }

    void removeRequirement(int[] selectedRows) {
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "No requirements selected");
        } else {
            for (int i = 0; i < selectedRows.length; i++) {
                Requirement r = filteredRequirements.get(selectedRows[i]);
                try {
                    r.getReviewState().remove(new ExternalInput("No comment", projectController.getCurrentUser()));
                } catch (ChangeNotAllowedException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
        }
    }

    void rejectRequirement(int[] selectedRows) {
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "No requirements selected");
        } else {
            for (int i = 0; i < selectedRows.length; i++) {
                Requirement r = filteredRequirements.get(selectedRows[i]);
                if (r.getCategory().isOwner(projectController.getCurrentUser())) {
                    try {
                        r.getReviewState().reject(new ExternalInput("No comment", projectController.getCurrentUser()));
                    } catch (ChangeNotAllowedException e) {
                        JOptionPane.showMessageDialog(this, e.getMessage());
                    }
                }
            }
        }
    }

    void approveRequirement(int[] selectedRows) {
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "No requirements selected");
        } else {
            for (int i = 0; i < selectedRows.length; i++) {
                Requirement r = filteredRequirements.get(selectedRows[i]);
                if (r.getCategory().isOwner(projectController.getCurrentUser())) {
                    try {
                        r.getReviewState().approve(new ExternalInput("No comment", projectController.getCurrentUser()));

                    } catch (ChangeNotAllowedException e) {
                        JOptionPane.showMessageDialog(this, e.getMessage());
                    }
                }
            }
        }
    }

    void decomposeSelection(int[] selectedRows) {
        if (projectController instanceof SwingProjectController) {
            SwingProjectController controller = (SwingProjectController) projectController;

            controller.getDesktop().breakdownSelected();
        } else {
            ArrayList<FactRequirement> decomposeRequirements = new ArrayList<>();

            for (int i = 0; i < selectedRows.length; i++) {
                Requirement req = filteredRequirements.get(i);
                if (req instanceof FactRequirement) {
                    if (!req.isRealized() && req.isApproved()) {
                        decomposeRequirements.add((FactRequirement) req);
                    }
                }
            }

            if (decomposeRequirements.size() > 0) {
//                MultipleFactBreakdown decomposition = new MultipleFactBreakdown(projectController, decomposeRequirements);
//                Point point = this.getLocation();
//                point.setLocation(point.x + 25, point.y + 25);
//                decomposition.setLocation(point);
                //decomposition.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "No fact requirements found that are APPROVED and realized");
            }
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

    private Requirement getSelectedRequirement() {
        int[] rows = tblRequirements.getSelectedRows();

        if (rows.length == 0 || rows.length > 1) {
            return null;
        } else {
            return filteredRequirements.get(rows[0]);
        }
    }

    @Override
    public void refresh() {
        initCategories();
        deselectSYS();
        selectFilteredRequirements();
        initPopup();
    }

    public void selectFilteredRequirements() {
        Project p = projectController.getProject();
        if (p == null) {
            return;
        }
        RequirementModel rm = p.getRequirementModel();
        Iterator<Requirement> iterator = rm.requirements();
        filteredRequirements = new ArrayList<>();
        while (iterator.hasNext()) {
            Requirement req = iterator.next();
            if (filtered(req)) {
                filteredRequirements.add(req);
            }
        }
        tblRequirements.setModel(new RequirementsTableModel(filteredRequirements));
        //tca.adjustColumns();
        SwingUtils.resizeColumnWidth(tblRequirements);
    }

    public void openEnterRequirementDialog() {
        EnterRequirementDialog requirementDialog = new EnterRequirementDialog(projectController);
        Point point = this.getLocation();
        point.setLocation(point.x + 25, point.y + 25);
        requirementDialog.setLocation(point);
        requirementDialog.setVisible(true);
    }

    public void openChangeRequirementDialog() {
        Point point = this.getLocation();
        point.setLocation(point.x + 25, point.y + 25);
        RequirementsTableModel model = (RequirementsTableModel) tblRequirements.getModel();

        Requirement requirement = model.getRequirementAt(tblRequirements.getSelectedRow());
        if (requirement != null) {
            EnterRequirementDialog requirementDialog = new EnterRequirementDialog(projectController);
            requirementDialog.changeRequirement(requirement);
            requirementDialog.setLocation(point);
            requirementDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No requirement selected.");
        }
    }

    private static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    public ArrayList<FactRequirement> getBreakdownReadyFactRequirements() {
        int[] selectedRows = tblRequirements.getSelectedRows();
        ArrayList<FactRequirement> decomposeRequirements = new ArrayList<>();

        for (int i = 0; i < selectedRows.length; i++) {
            Requirement req = filteredRequirements.get(selectedRows[i]);
            if (req instanceof FactRequirement) {
                if (!req.isRealized() && req.isApproved()) {
                    decomposeRequirements.add((FactRequirement) req);
                }
            }
        }
        return decomposeRequirements;
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        initPopup();
    }
}
