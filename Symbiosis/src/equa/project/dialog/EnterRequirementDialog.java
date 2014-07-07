package equa.project.dialog;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import equa.controller.IView;
import equa.controller.PersistanceManager;
import equa.controller.ProjectController;
import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.ActionRequirement;
import equa.meta.requirements.ChanceOfFailure;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.MoSCoW;
import equa.meta.requirements.QualityAttribute;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RuleRequirement;
import equa.meta.requirements.UrgencyKind;
import equa.meta.requirements.VerifyMethod;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.Impact;
import equa.project.Project;
import equa.project.ProjectRole;

@SuppressWarnings("serial")
public class EnterRequirementDialog extends JDialog implements IView, WindowListener {

    private ProjectController projectController;
    private JTextField txtFieldJustification;
    private JComboBox<String> cbKind;
    private JComboBox<Category> cbCategory;
    private JComboBox<ChanceOfFailure> cbChanceOfFailure;
    private JComboBox<Impact> cbImportance;
    private JComboBox<UrgencyKind> cbUrgency;
    private JComboBox<MoSCoW> cbMoskow;
    private JComboBox<VerifyMethod> cbVerify;
    private JTextArea textPane;
    private JButton btnInsertRequirement;
    private Requirement requirementToChange;
    EntityManager em;
    EntityTransaction userTransaction;
    private JTextField tfRisk;
    private JLabel lblCreatedAt;
    private JLabel lblModifiedAt;
    private JLabel lblCreationSource;
    private JLabel lblReviewstate;
    private JLabel lblRealized;
    private JLabel lblRealizedvar;
    private JLabel lblVerifymethod;
    private JLabel lblExternalinputfrom;
    private JLabel lblExternalinputcreatedat;

    public EnterRequirementDialog(ProjectController controller) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Enter Requirement");
        projectController = controller;
        projectController.addView(this);
        addWindowListener(this);
        setMinimumSize(new Dimension(500, 520));

        getContentPane().setLayout(null);

        cbKind = new JComboBox<String>();
        cbKind.addItem("Action Requirement");
        cbKind.addItem("Fact Requirement");
        cbKind.addItem("Quality Attribute");
        cbKind.addItem("Rule Requirement");
        cbKind.setBounds(317, 36, 158, 20);
        cbKind.setSelectedIndex(1);
        getContentPane().add(cbKind);

        cbCategory = new JComboBox<Category>();
        Iterator<Category> categories = projectController.getProject().getCategories();
        while (categories.hasNext()) {
            Category cat = categories.next();
            if (!cat.getCode().equalsIgnoreCase(Category.SYSTEM.getCode())) {
                cbCategory.addItem(cat);
            }
        }
        cbCategory.setBounds(317, 66, 125, 20);
        getContentPane().add(cbCategory);

        textPane = new JTextArea();
        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textPane.getText().equalsIgnoreCase("[Enter requirement here]")) {
                    textPane.setText("");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (textPane.getText().equalsIgnoreCase("[Enter requirement here]")) {
                    textPane.setText("");
                }
            }
        });
        textPane.setText("[Enter requirement here]");
        textPane.setWrapStyleWord(true);
        textPane.setLineWrap(true);
        textPane.setBounds(10, 96, 465, 98);
        getContentPane().add(textPane);

        txtFieldJustification = new JTextField();
        txtFieldJustification.setBounds(10, 221, 465, 20);
        getContentPane().add(txtFieldJustification);
        txtFieldJustification.setColumns(10);

        cbChanceOfFailure = new JComboBox<ChanceOfFailure>();
        cbChanceOfFailure.addItem(ChanceOfFailure.UNDEFINED);
        cbChanceOfFailure.addItem(ChanceOfFailure.LOW);
        cbChanceOfFailure.addItem(ChanceOfFailure.MEDIUM);
        cbChanceOfFailure.addItem(ChanceOfFailure.HIGH);

        cbChanceOfFailure.setBounds(303, 277, 169, 20);
        getContentPane().add(cbChanceOfFailure);
        cbImportance = new JComboBox<Impact>();
        cbImportance.addItem(Impact.UNDEFINED);
        cbImportance.addItem(Impact.ZERO);
        cbImportance.addItem(Impact.LIGHT);
        cbImportance.addItem(Impact.NORMAL);
        cbImportance.addItem(Impact.SERIOUS);

        cbChanceOfFailure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChangeRisk();
            }
        });

        cbImportance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChangeRisk();
            }
        });

        cbImportance.setBounds(303, 308, 169, 20);
        getContentPane().add(cbImportance);

        cbUrgency = new JComboBox<UrgencyKind>();
        cbUrgency.addItem(UrgencyKind.UNDEFINED);
        cbUrgency.addItem(UrgencyKind.LOW);
        cbUrgency.addItem(UrgencyKind.MEDIUM);
        cbUrgency.addItem(UrgencyKind.HIGH);
        cbUrgency.setBounds(303, 361, 169, 20);
        getContentPane().add(cbUrgency);

        cbMoskow = new JComboBox<MoSCoW>();
        cbMoskow.addItem(MoSCoW.UNDEFINED);
        cbMoskow.addItem(MoSCoW.MUST);
        cbMoskow.addItem(MoSCoW.SHOULD);
        cbMoskow.addItem(MoSCoW.COULD);
        cbMoskow.addItem(MoSCoW.WONT);
        cbMoskow.setBounds(303, 392, 169, 20);
        getContentPane().add(cbMoskow);

        cbVerify = new JComboBox<>();
        cbVerify.addItem(VerifyMethod.UNDEFINED);
        cbVerify.addItem(VerifyMethod.NONE);
        cbVerify.addItem(VerifyMethod.ANALYSIS);
        cbVerify.addItem(VerifyMethod.DEMONSTRATION);
        cbVerify.addItem(VerifyMethod.INSPECTION);
        cbVerify.addItem(VerifyMethod.TEST);

        btnInsertRequirement = new JButton("Insert Requirement");
        btnInsertRequirement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRequirement();
            }
        });
        btnInsertRequirement.setBounds(303, 448, 169, 23);
        getContentPane().add(btnInsertRequirement);

        JLabel lblKind = new JLabel("Kind");
        lblKind.setHorizontalAlignment(SwingConstants.TRAILING);
        lblKind.setBounds(272, 39, 35, 14);
        getContentPane().add(lblKind);

        JLabel lblCategory = new JLabel("Category");
        lblCategory.setHorizontalAlignment(SwingConstants.TRAILING);
        lblCategory.setBounds(244, 69, 63, 14);
        getContentPane().add(lblCategory);

        JLabel lblChanceOfFailure = new JLabel("Chance of Failure");
        lblChanceOfFailure.setHorizontalAlignment(SwingConstants.TRAILING);
        lblChanceOfFailure.setBounds(168, 280, 125, 14);
        getContentPane().add(lblChanceOfFailure);

        JLabel lblImportance = new JLabel("Impact");
        lblImportance.setHorizontalAlignment(SwingConstants.TRAILING);
        lblImportance.setBounds(189, 311, 104, 14);
        getContentPane().add(lblImportance);

        JLabel lblUrgency = new JLabel("Urgency");
        lblUrgency.setHorizontalAlignment(SwingConstants.TRAILING);
        lblUrgency.setBounds(189, 364, 104, 14);
        getContentPane().add(lblUrgency);

        JLabel lblMoscow = new JLabel("MoSCoW");
        lblMoscow.setHorizontalAlignment(SwingConstants.TRAILING);
        lblMoscow.setBounds(192, 395, 101, 14);
        getContentPane().add(lblMoscow);

        JLabel lblJustification = new JLabel("Justification");
        lblJustification.setHorizontalAlignment(SwingConstants.TRAILING);
        lblJustification.setBounds(344, 199, 131, 14);
        getContentPane().add(lblJustification);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        btnCancel.setBounds(157, 448, 125, 23);
        getContentPane().add(btnCancel);

        JLabel lblRisk = new JLabel("Risk");
        lblRisk.setHorizontalAlignment(SwingConstants.TRAILING);
        lblRisk.setBounds(193, 336, 100, 14);
        getContentPane().add(lblRisk);

        tfRisk = new JTextField();
        tfRisk.setBounds(386, 335, 86, 20);
        getContentPane().add(tfRisk);
        tfRisk.setColumns(10);
        tfRisk.setEnabled(false);

        JButton button = new JButton("...");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewCategory();
            }
        });
        button.setBounds(447, 65, 28, 23);
        getContentPane().add(button);

        lblRealized = new JLabel("Realized");
        lblRealized.setHorizontalAlignment(SwingConstants.TRAILING);
        lblRealized.setBounds(255, 11, 61, 14);
        lblRealized.setVisible(false);
        getContentPane().add(lblRealized);

        lblRealizedvar = new JLabel("");
        lblRealizedvar.setBounds(326, 11, 152, 14);
        getContentPane().add(lblRealizedvar);

        lblVerifymethod = new JLabel("VerifyMethod:");
        lblVerifymethod.setHorizontalAlignment(SwingConstants.TRAILING);
        lblVerifymethod.setBounds(189, 423, 104, 14);
        getContentPane().add(lblVerifymethod);

        cbVerify.setBounds(303, 421, 169, 20);
        getContentPane().add(cbVerify);

        lblExternalinputfrom = new JLabel("From");
        lblExternalinputfrom.setBounds(10, 252, 185, 14);
        lblExternalinputfrom.setVisible(false);
        getContentPane().add(lblExternalinputfrom);

        lblExternalinputcreatedat = new JLabel("Created at");
        lblExternalinputcreatedat.setBounds(241, 252, 217, 14);
        lblExternalinputcreatedat.setVisible(false);
        getContentPane().add(lblExternalinputcreatedat);

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Requirement info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 3, 238, 82);
        getContentPane().add(panel);
        panel.setLayout(null);

        lblCreatedAt = new JLabel("Created at: ");
        lblCreatedAt.setBounds(8, 16, 223, 14);
        panel.add(lblCreatedAt);

        lblModifiedAt = new JLabel("Modified at:");
        lblModifiedAt.setBounds(8, 30, 216, 14);
        panel.add(lblModifiedAt);

        lblCreationSource = new JLabel("Creation source:");
        lblCreationSource.setBounds(8, 45, 216, 14);
        panel.add(lblCreationSource);

        lblReviewstate = new JLabel("ReviewState: ");
        lblReviewstate.setBounds(8, 59, 218, 14);
        panel.add(lblReviewstate);
        lblReviewstate.setVisible(false);
        lblCreationSource.setVisible(false);
        lblModifiedAt.setVisible(false);
        lblCreatedAt.setVisible(false);
        setVisible(true);
        textPane.requestFocus();
    }

    protected void NewCategory() {
        AddCategoryDialog categoryDialog = new AddCategoryDialog(projectController);
        Point point = getLocation();
        point.setLocation(point.x + 25, point.y + 25);
        categoryDialog.setLocation(point);
        categoryDialog.setVisible(true);
    }

    public void changeRequirement(Requirement req) {
        try {
            if (req != null) {
                setTitle(req.getName());
                requirementToChange = req;
                req.getVerifyMethod();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss");
                lblCreatedAt.setVisible(true);
                lblModifiedAt.setVisible(true);
                lblCreationSource.setVisible(true);
                lblReviewstate.setVisible(true);
                lblRealized.setVisible(true);
                lblExternalinputcreatedat.setVisible(true);
                lblExternalinputfrom.setVisible(true);
                lblCreatedAt.setText(lblCreatedAt.getText() + " " + formatter.format(req.getCreatedAt().getTime()));
                lblModifiedAt.setText(lblModifiedAt.getText() + " " + formatter.format(req.getModifiedAt().getTime()));
                lblCreationSource.setText(lblCreationSource.getText() + " " + req.creationSource());
                txtFieldJustification.setText(req.getJustification());

                lblExternalinputfrom.setText(lblExternalinputfrom.getText() + " " + req.getReviewState().getExternalInput().getFrom().getName());
                req.getReviewState().getExternalInput().getJustification();
                lblExternalinputcreatedat.setText(lblExternalinputcreatedat.getText() + " " + formatter.format(req.getReviewState().getExternalInput().getCreatedAt().getTime()));

                lblReviewstate.setText(lblReviewstate.getText() + " " + req.getReviewState().toString());
                if (req.isRealized()) {
                    lblRealizedvar.setText("Yes");
                } else {
                    lblRealizedvar.setText("No");
                }

                if (req instanceof ActionRequirement) {
                    cbKind.setSelectedItem("Action Requirement");
                } else if (req instanceof FactRequirement) {
                    cbKind.setSelectedItem("Fact Requirement");
                } else if (req instanceof QualityAttribute) {
                    cbKind.setSelectedItem("Quality Attribute");
                } else if (req instanceof RuleRequirement) {
                    cbKind.setSelectedItem("Rule Requirement");
                }
                cbVerify.setSelectedItem(req.getVerifyMethod());
                cbCategory.setSelectedItem(req.getCategory());
                cbChanceOfFailure.setSelectedItem(req.getChanceOfFailure());
                cbImportance.setSelectedItem(req.getImpact());
                ChangeRisk();
                cbUrgency.setSelectedItem(req.getUrgency());
                cbMoskow.setSelectedItem(req.getMoSCoW());
                textPane.setText(req.getText());
                btnInsertRequirement.setText("Change Requirement");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        cbCategory.removeAllItems();

        Iterator<Category> categories = projectController.getProject().getCategories();
        while (categories.hasNext()) {
            Category cat = categories.next();
            if (!cat.getCode().equalsIgnoreCase(Category.SYSTEM.getCode())) {
                cbCategory.addItem(cat);
            }
        }
    }

    private void addRequirement() {
        if (textPane.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Requirement is empty.");
            return;
        }

        if (requirementToChange == null) {

            Category cat;
            Project pro;
            ProjectRole user;

            if (projectController.isUseJPA()) {
                em = PersistanceManager.getEntityManager();
                userTransaction = em.getTransaction();

                user = em.find(ProjectRole.class, projectController.getCurrentUser().getProjectRoleId());
                cat = em.find(Category.class, ((Category) cbCategory.getSelectedItem()).getId());
                pro = projectController.getProject();
            } else {
                user = projectController.getCurrentUser();
                pro = projectController.getProject();
                cat = (Category) cbCategory.getSelectedItem();
            }

            Requirement requirement = null;
            if ((String) cbKind.getSelectedItem() == "Action Requirement") {
                requirement = pro
                        .getRequirementModel()
                        .addActionRequirement(
                                cat,
                                textPane.getText(),
                                new ExternalInput(txtFieldJustification.getText(), user));
            } else if ((String) cbKind.getSelectedItem() == "Fact Requirement") {
                requirement = pro
                        .getRequirementModel()
                        .addFactRequirement(
                                cat,
                                textPane.getText(),
                                new ExternalInput(txtFieldJustification.getText(), user));
            } else if ((String) cbKind.getSelectedItem() == "Quality Attribute") {
                requirement = pro
                        .getRequirementModel()
                        .addQualityAttribute(
                                cat,
                                textPane.getText(),
                                new ExternalInput(txtFieldJustification.getText(), user));
            } else if ((String) cbKind.getSelectedItem() == "Rule Requirement") {
                requirement = pro
                        .getRequirementModel()
                        .addRuleRequirement(
                                cat,
                                textPane.getText(),
                                new ExternalInput(txtFieldJustification.getText(), user));
            }

            requirement.setImpact((Impact) cbImportance.getSelectedItem());
            requirement.setChanceOfFailure((ChanceOfFailure) cbChanceOfFailure.getSelectedItem());
            requirement.setMoSCoW((MoSCoW) cbMoskow.getSelectedItem());
            requirement.setUrgency((UrgencyKind) cbUrgency.getSelectedItem());
            requirement.setVerifyMethod((VerifyMethod) cbVerify.getSelectedItem());
            requirement.setManuallyCreated(true);

            //System.out.println("Category: " + ((Category)cbCategory.getSelectedItem()).getCode());
            if (projectController.isUseJPA()) {
                try {
                    GregorianCalendar start = new GregorianCalendar();
                    userTransaction.begin();
                    //em.persist(requirement);
                    em.merge(pro.getRequirementModel());
                    userTransaction.commit();
                    GregorianCalendar end = new GregorianCalendar();

                    System.out.println("Merge time: " + (end.getTimeInMillis() - start.getTimeInMillis()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (requirement.getCategory().isOwner(projectController.getCurrentUser())) {
                    try {
                        requirement.getReviewState().approve(new ExternalInput("added by owner of category", user));
                        userTransaction = em.getTransaction();
                        userTransaction.begin();
                        em.merge(requirement);
                        userTransaction.commit();
                    } catch (ChangeNotAllowedException e) {
                    }
                }
            }
        } else {
            Class<?> type = null;
            if (requirementToChange instanceof FactRequirement) {
                type = FactRequirement.class;
            } else if (requirementToChange instanceof RuleRequirement) {
                type = RuleRequirement.class;
            } else if (requirementToChange instanceof QualityAttribute) {
                type = QualityAttribute.class;
            } else if (requirementToChange instanceof ActionRequirement) {
                type = ActionRequirement.class;
            }

            Class<?> selectedType = null;
            String typeString = (String) cbKind.getSelectedItem();
            if (typeString.equalsIgnoreCase("Action Requirement")) {
                selectedType = ActionRequirement.class;
            } else if (typeString.equalsIgnoreCase("Fact Requirement")) {
                selectedType = FactRequirement.class;
            } else if (typeString.equalsIgnoreCase("Rule Requirement")) {
                selectedType = RuleRequirement.class;
            } else if (typeString.equalsIgnoreCase("Quality Attribute")) {
                selectedType = QualityAttribute.class;
            }

            if (type != selectedType) {
                projectController.getProject().getRequirementModel().remove(requirementToChange);
                requirementToChange = null;
                addRequirement();

            } else {
                if (requirementToChange.getCategory() != cbCategory.getSelectedItem()) {

                    requirementToChange.setCategory((Category) cbCategory.getSelectedItem());
                    if (requirementToChange instanceof FactRequirement /**
                             * && !requirementToChange.isRealized()*
                             */
                            ) {
                        try {
                            requirementToChange.getReviewState().change(new ExternalInput("Changed to added due to category change.", projectController.getCurrentUser()), requirementToChange.getText());
                        } catch (ChangeNotAllowedException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage());
                        }
                    }
                }
                if (requirementToChange.getImpact() != cbImportance.getSelectedItem()) {
                    requirementToChange.setImpact((Impact) cbImportance.getSelectedItem());
                }
                if (requirementToChange.getUrgency() != cbUrgency.getSelectedItem()) {
                    requirementToChange.setUrgency((UrgencyKind) cbUrgency.getSelectedItem());
                }
                if (requirementToChange.getMoSCoW() != cbMoskow.getSelectedItem()) {
                    requirementToChange.setMoSCoW((MoSCoW) cbMoskow.getSelectedItem());
                }
                if (requirementToChange.getChanceOfFailure() != cbChanceOfFailure.getSelectedItem()) {
                    requirementToChange.setChanceOfFailure((ChanceOfFailure) cbChanceOfFailure.getSelectedItem());
                }
                if (requirementToChange.getVerifyMethod() != cbVerify.getSelectedItem()) {
                    requirementToChange.setVerifyMethod((VerifyMethod) cbVerify.getSelectedItem());
                }
                if (!requirementToChange.getText().equalsIgnoreCase(textPane.getText())) {
                    try {
                        requirementToChange.setText(new ExternalInput("", projectController.getCurrentUser()), textPane.getText());
                    } catch (ChangeNotAllowedException e) {
                        JOptionPane.showMessageDialog(this, e.getMessage());
                    }
                }

                if (projectController.isUseJPA()) {
                    try {
                        em = PersistanceManager.getEntityManager();
                        userTransaction = em.getTransaction();
                        userTransaction.begin();
                        em.merge(requirementToChange);
                        userTransaction.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        projectController.refreshViews();
        setVisible(false);
        dispose();
    }

    private void ChangeRisk() {
        ChanceOfFailure cof = (ChanceOfFailure) cbChanceOfFailure.getSelectedItem();
        Impact imp = (Impact) cbImportance.getSelectedItem();

        if (cof == ChanceOfFailure.UNDEFINED || imp == Impact.UNDEFINED) {
            tfRisk.setText("-1");
        } else {
            tfRisk.setText("" + (cof.ordinal() * imp.ordinal()));
        }
    }
}
