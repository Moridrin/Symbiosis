/*
 * TypeDialog.java
 *
 * Created on 3-mei-2011, 14:28:45
 */
package equa.factbreakdown.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import equa.meta.MismatchException;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.FactType;
import equa.util.Naming;

/**
 *
 * @author FrankP, RuudLenders
 */
public class SubstitutionTypeDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private TreeController treeController;
    private String supertypeName; // the name of the supertype
    private String typeName; // current configured type name with correct casing
    private String roleName; // current configured role name with correct casing
    private String begin;
    private String separator;
    private String end;
    private String elementTypeName;
    private String elementRoleName;

    /**
     * Creates new form SubstitutionTypeDialog
     *
     * @param treeController the parent treecontroller of this dialog
     * @param expression the expression selected while decompositioning
     */
    public SubstitutionTypeDialog(TreeController treeController, String expression) {
        super(treeController.getFrame(), true);
        this.treeController = treeController;

        supertypeName = "";
        typeName = "";
        roleName = "";
        begin = "";
        separator = "";
        end = "";
        elementTypeName = "";
        elementRoleName = "";
        initComponents();
        taExpression.setLineWrap(true);
        taExpression.setText(expression);

        tfTypeName.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String foundName = GuiTools.intellisense(tfTypeName, lsFactTypes);

                if (foundName.equalsIgnoreCase(tfTypeName.getText())) {
                    tfRoleName.setText(toRoleName(tfTypeName.getText()));
                } else {
                    tfRoleName.setText("");
                }
            }
        });

        tfTypeName.requestFocus();

        tfSupertypeName.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String foundName = GuiTools.intellisense(tfSupertypeName, lsFactTypes);

                if (foundName.equalsIgnoreCase(tfSupertypeName.getText())) {
                    tfRoleName.setText(toRoleName(tfSupertypeName.getText()));
                } else {
                    tfRoleName.setText("");
                }
            }
        });

        lsBaseTypes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                String typeName = (String) lsBaseTypes.getSelectedValue();
                if (typeName == null || typeName.isEmpty()) {
                    return;
                }

                tfTypeName.setText(typeName);
            }
        });

        rbList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initCollectionControls();
            }
        });

        rbSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initCollectionControls();
            }
        });

        rbSingle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initCollectionControls();
            }
        });

        resetDialog();

        guessObjectType();

    }

    private void guessObjectType() {
        String expression = taExpression.getText();
        ListModel<FactType> model = lsFactTypes.getModel();
        FactType guess = null;
        int sizeGuess = -1;
        int indexGuess = -1;
        for (int i = 0; i < model.getSize(); i++) {
            FactType ft = model.getElementAt(i);
            if (ft.isObjectType()) {
                int sizeConstants = ft.matchTypeExpression(expression);
                if (sizeConstants > sizeGuess) {
                    guess = ft;
                    sizeGuess = sizeConstants;
                    indexGuess = i;
                }
            }
        }
        if (guess != null) {
            tfTypeName.setText(guess.getName());
            lsFactTypes.setSelectedIndex(indexGuess);
            lsFactTypes.ensureIndexIsVisible(indexGuess);

        }
    }

    private void initCollectionControls() {
        boolean isCollection = !rbSingle.isSelected();
        if (!isCollection) {
            begin = "";
            separator = "";
            end = "";
            elementTypeName = "";
            taElementInfo.setText("");
        } else {

            CollectionElementDialog dialog = new CollectionElementDialog(treeController.getFrame(), true,
                    taExpression.getText(), begin, separator, end, elementTypeName,
                    elementRoleName);
            dialog.setVisible(true);
            if (!dialog.getSeparator().isEmpty() && !dialog.getElementType().isEmpty()) {
                taExpression.setText(dialog.getCollection());
                begin = dialog.getBegin();
                separator = dialog.getSeparator();
                end = dialog.getEnd();
                elementTypeName = dialog.getElementType();
                elementRoleName = dialog.getElementRole();
                taElementInfo.setText("begin: [" + begin + "] sep: [" + separator + "] end: [" + end + "]\n"
                        + elementRoleName + " : " + elementTypeName);
                btOK.requestFocus();
            }
        }
    }

    public String getSeparator() {
        return separator;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public String getElementType() {
        return elementTypeName;
    }

    public String getElementRole() {
        return elementRoleName;
    }

    public boolean isList() {
        return rbList.isSelected();
    }

    public boolean isSet() {
        return rbSet.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupCollection = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        btOK = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();
        btReset = new javax.swing.JButton();
        lbTypeName = new javax.swing.JLabel();
        tfTypeName = new javax.swing.JTextField();
        lbRoleName = new javax.swing.JLabel();
        tfRoleName = new javax.swing.JTextField();
        spBaseTypes = new javax.swing.JScrollPane();
        lsBaseTypes = new javax.swing.JList();
        spObjectTypes = new javax.swing.JScrollPane();
        lsFactTypes = new javax.swing.JList();
        tfSupertypeName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        taExpression = new javax.swing.JTextArea();
        pnRadioButtons = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rbSingle = new javax.swing.JRadioButton();
        rbList = new javax.swing.JRadioButton();
        rbSet = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        taElementInfo = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N
        setResizable(false);
        setSize(new java.awt.Dimension(400, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(SubstitutionTypeDialog.class);
        btOK.setText(resourceMap.getString("btOK.text")); // NOI18N
        btOK.setName("btOK"); // NOI18N
        btOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOKActionPerformed(evt);
            }
        });

        btCancel.setText(resourceMap.getString("btCancel.text")); // NOI18N
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        btReset.setText(resourceMap.getString("btReset.text")); // NOI18N
        btReset.setName("btReset"); // NOI18N
        btReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btResetActionPerformed(evt);
            }
        });

        lbTypeName.setText(resourceMap.getString("lbTypeName.text")); // NOI18N
        lbTypeName.setName("lbTypeName"); // NOI18N

        tfTypeName.setBorder(tfRoleName.getBorder());
        tfTypeName.setName("tfTypeName"); // NOI18N
        tfTypeName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfTypeNameFocusGained(evt);
            }
        });
        tfTypeName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTypeNameActionPerformed(evt);
            }
        });

        lbRoleName.setText(resourceMap.getString("lbRoleName.text")); // NOI18N
        lbRoleName.setName("lbRoleName"); // NOI18N

        tfRoleName.setName("tfRoleName"); // NOI18N
        tfRoleName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfRoleNameActionPerformed(evt);
            }
        });

        spBaseTypes.setBorder(javax.swing.BorderFactory.createTitledBorder("Base types"));
        spBaseTypes.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spBaseTypes.setName("spBaseTypes"); // NOI18N
        spBaseTypes.setPreferredSize(new java.awt.Dimension(75, 155));

        lsBaseTypes.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "String", "Natural", "Real", "Integer", "Character" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lsBaseTypes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lsBaseTypes.setFocusable(false);
        lsBaseTypes.setName("lsBaseTypes"); // NOI18N
        lsBaseTypes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMouseReleased(evt);
            }
        });
        lsBaseTypes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lsBaseTypesValueChanged(evt);
            }
        });
        spBaseTypes.setViewportView(lsBaseTypes);

        spObjectTypes.setBorder(javax.swing.BorderFactory.createTitledBorder("Fact types"));
        spObjectTypes.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spObjectTypes.setName("spObjectTypes"); // NOI18N

        lsFactTypes.setModel(treeController.getObjectModel());
        lsFactTypes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lsFactTypes.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lsFactTypes.setFocusable(false);
        lsFactTypes.setName("lsFactTypes"); // NOI18N
        lsFactTypes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMouseReleased(evt);
            }
        });
        lsFactTypes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lsFactTypesValueChanged(evt);
            }
        });
        spObjectTypes.setViewportView(lsFactTypes);

        tfSupertypeName.setName("tfSupertypeName"); // NOI18N
        tfSupertypeName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSupertypeNameActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        taExpression.setEditable(false);
        taExpression.setColumns(20);
        taExpression.setLineWrap(true);
        taExpression.setRows(5);
        taExpression.setWrapStyleWord(true);
        taExpression.setName("taExpression"); // NOI18N
        jScrollPane1.setViewportView(taExpression);

        pnRadioButtons.setName("pnRadioButtons"); // NOI18N
        pnRadioButtons.setLayout(new java.awt.BorderLayout());

        jLabel1.setForeground(resourceMap.getColor("jLabel1.foreground")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        buttonGroupCollection.add(rbSingle);
        rbSingle.setForeground(resourceMap.getColor("rbSingle.foreground")); // NOI18N
        rbSingle.setText(resourceMap.getString("rbSingle.text")); // NOI18N
        rbSingle.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        rbSingle.setName("rbSingle"); // NOI18N

        buttonGroupCollection.add(rbList);
        rbList.setForeground(resourceMap.getColor("rbSingle.foreground")); // NOI18N
        rbList.setText(resourceMap.getString("rbList.text")); // NOI18N
        rbList.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        rbList.setName("rbList"); // NOI18N

        buttonGroupCollection.add(rbSet);
        rbSet.setForeground(resourceMap.getColor("rbSingle.foreground")); // NOI18N
        rbSet.setText(resourceMap.getString("rbSet.text")); // NOI18N
        rbSet.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        rbSet.setName("rbSet"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        taElementInfo.setEditable(false);
        taElementInfo.setColumns(20);
        taElementInfo.setLineWrap(true);
        taElementInfo.setRows(5);
        taElementInfo.setWrapStyleWord(true);
        taElementInfo.setName("taElementInfo"); // NOI18N
        taElementInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taElementInfoMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(taElementInfo);

        jLabel2.setForeground(resourceMap.getColor("jLabel2.foreground")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnRadioButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(spBaseTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lbTypeName)
                                        .addComponent(lbRoleName)
                                        .addComponent(jLabel1))))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btOK)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btReset)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btCancel))
                            .addComponent(tfRoleName, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spObjectTypes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                            .addComponent(tfTypeName, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfSupertypeName)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbSingle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rbList)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbSet))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbSet)
                    .addComponent(rbList)
                    .addComponent(rbSingle, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnRadioButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfSupertypeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfTypeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbTypeName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spObjectTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(spBaseTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfRoleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbRoleName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btCancel)
                    .addComponent(btReset)
                    .addComponent(btOK))
                .addGap(11, 11, 11))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Resets the dialog components.
     * <p>
     * This event is fired when the user closes the dialog, not using the cancel
     * button.</p>
     *
     * @see JDialog
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        resetDialog();
    }//GEN-LAST:event_formWindowClosing

    /**
     * Closes the dialog and continues the decomposition.
     * <p>
     * This event is fired when the user clicked btOK.</p>
     *
     * @see JButton
     */
    private void btOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOKActionPerformed
        if (getTypeName().isEmpty()) {
            JOptionPane.showMessageDialog(this, ("Please fill in type name."));
            return;
        }
        if (!getRoleName().isEmpty() && !Naming.isIdentifier(this.getRoleName())) {
            JOptionPane.showMessageDialog(getOwner(), "Rolename must fullfill the rules "
                    + "of an identifier: it must begin with a letter or an underscore (_) and"
                    + "may be followed by one or more letters, underscores and/or digits.");
            return;
        }
        if (!Naming.isIdentifier(this.getTypeName())) {
            JOptionPane.showMessageDialog(getOwner(), "Type name must fullfill the rules "
                    + "of an identifier: it must begin with a letter or an underscore (_) and"
                    + "may be followed by one or more letters, underscores and/or digits.");
            return;
        }
        if (isBaseType()) {
            String baseTypeName = getTypeName();
            try {
                BaseType.getBaseType(baseTypeName).checkSyntaxis(getTitle());
                if (getRoleName().isEmpty()) {
                    JOptionPane.showMessageDialog(getOwner(), "Role name is "
                            + "required in case of a basetype.");
                    return;
                }
            } catch (MismatchException ex) {
                JOptionPane.showMessageDialog(getOwner(), "Value (expression) "
                        + "doesn't match with chosen base type.");
                return;
            }
        }

        if (isSupertypeAsSubstitution()) {
            if (BaseType.getBaseType(supertypeName) != null) {
                JOptionPane.showMessageDialog(getOwner(),
                        "Chosen super type is base type while an object type is required.");
                return;
            }
            if (BaseType.getBaseType(typeName) != null) {
                JOptionPane.showMessageDialog(getOwner(),
                        "Chosen sub type is base type while an object type is required.");
                return;
            }
        }

        FactType ft = treeController.getObjectModel().getFactType(tfTypeName.getText());
        if (ft != null) {
            if (ft.isObjectType() && ft.getObjectType().isAbstract()) {
                JOptionPane.showMessageDialog(getOwner(),
                        "Chosen object type is abstract while a concrete object type is required.");
                return;
            }
        }

        if (!rbSingle.isSelected()) {
            if (separator.isEmpty()) {
                JOptionPane.showMessageDialog(getOwner(),
                        "Seperator cannot be empty");
                return;
            }
            if (elementTypeName.isEmpty()) {
                JOptionPane.showMessageDialog(getOwner(),
                        "Element type name cannot be empty.");
                return;
            }
        }

        setVisible(false);
    }//GEN-LAST:event_btOKActionPerformed

    /**
     * Resets the dialog components.
     * <p>
     * This event is fired when the user clicked btReset.</p>
     *
     * @see JButton
     */
    private void btResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btResetActionPerformed
        resetDialog();
    }//GEN-LAST:event_btResetActionPerformed

    /**
     * Closes the dialog and ignores any changes made.
     * <p>
     * This event is fired when the user clicked btCancel.</p>
     *
     * @see JButton
     */
    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        resetDialog();
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    /**
     * Replaces tfTypeName's text with the selected object type name.
     * <p>
     * This event is fired when the user selects an object type.</p>
     *
     * @see JList
     */
    private void lsFactTypesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lsFactTypesValueChanged
        if (!evt.getValueIsAdjusting() && lsFactTypes.getSelectedValue() != null) {
            lsBaseTypes.clearSelection();
//            if (btSupertype.getText().equals("Cancel Super Type")) {
//                supertypeName = ((FactType) lsObjectTypes.getSelectedValue()).getName();
//            } else {
            FactType ft = (FactType) lsFactTypes.getSelectedValue();
            String objectTypeName = ft.getName();
            setType(objectTypeName, supertypeName, true);
//            }
        }
    }//GEN-LAST:event_lsFactTypesValueChanged

    /**
     * Replaces tfTypeName's text with the selected base type name.
     * <p>
     * This event is fired when the user selects a base type.</p>
     *
     * @see JList
     */
    private void lsBaseTypesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lsBaseTypesValueChanged
        if (!evt.getValueIsAdjusting() && lsBaseTypes.getSelectedValue() != null) {
            lsFactTypes.clearSelection();
            String baseTypeName = lsBaseTypes.getSelectedValue().toString();
            setType(baseTypeName, supertypeName, false);
        }
    }//GEN-LAST:event_lsBaseTypesValueChanged

    /**
     * Sets the focus to tfRoleName, and select all text in tfRoleName.
     * <p>
     * This event is fired when the user releases the mouse on lsBaseTypes or
     * lsObjectTypes.</p>
     */
    private void listMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseReleased
        tfRoleName.requestFocus();
        tfRoleName.selectAll();
    }//GEN-LAST:event_listMouseReleased

    private void tfSupertypeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSupertypeNameActionPerformed
        tfRoleName.requestFocus();
    }//GEN-LAST:event_tfSupertypeNameActionPerformed

    private void tfTypeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTypeNameActionPerformed
        tfRoleName.requestFocus();
    }//GEN-LAST:event_tfTypeNameActionPerformed

    private void tfRoleNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfRoleNameActionPerformed
        btOKActionPerformed(evt);
    }//GEN-LAST:event_tfRoleNameActionPerformed

    private void taElementInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taElementInfoMouseClicked
        initCollectionControls();
    }//GEN-LAST:event_taElementInfoMouseClicked

    private void tfTypeNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfTypeNameFocusGained
        lsBaseTypes.clearSelection();
    }//GEN-LAST:event_tfTypeNameFocusGained

    /**
     * Sets both type and role name values, and their corresponding textfields.
     *
     * @param typeName the new name for the current configured type
     * @param supertypeName the new name for the supertype of the current
     * configured type
     * @param isObjectType a boolean indicating whether the type is an object
     * type
     */
    private void setType(String typeName, String supertypeName, boolean isObjectType) {
        this.typeName = typeName;
        this.supertypeName = supertypeName;

//        if (autoRoleName) 
        {
            if (!supertypeName.isEmpty()) {
                roleName = toRoleName(supertypeName);
            } else if (isObjectType) {
                roleName = toRoleName(typeName);
            } else if (typeName.equals("Natural")) {
                roleName = "nr";
            } else {
                roleName = "";
            }

        }

        int selectionStart, selectionEnd;

        if (tfTypeName.hasFocus()) {
            selectionStart = tfTypeName.getSelectionStart();
            selectionEnd = tfTypeName.getSelectionEnd();
        } else {
            selectionStart = tfSupertypeName.getSelectionStart();
            selectionEnd = tfSupertypeName.getSelectionEnd();
        }

        tfSupertypeName.setText(supertypeName);
        tfTypeName.setText(typeName);
        tfRoleName.setText(roleName);

        if (tfTypeName.hasFocus()) {
            tfTypeName.select(selectionStart, selectionEnd);
        } else {
            tfSupertypeName.select(selectionStart, selectionEnd);
        }
    }

    /**
     * Indicates whether the current configured type is a base type.
     *
     * @return a boolean indicating whether the current configured type is a
     * base type
     */
    public boolean isBaseType() {
        return !lsBaseTypes.isSelectionEmpty();
    }

    /**
     * Unidentified object types are currently not implemented. Indicates
     * whether the current configured type is unidentified.
     *
     * @return a boolean indicating whether the current configured type is
     * unidentified
     */
    public boolean isUnidentifiedObjectType() {
        String expression = this.getTitle();
        return isObjectType()
                && expression.substring(1, expression.length() - 1).equals(getTypeName());
    }

    /**
     * Gets the type name for the current configured type.
     *
     * @return the type name for the current configured type
     */
    public String getTypeName() {
        return tfTypeName.getText().trim();
    }

    /**
     * Gets the role name for the current configured type.
     *
     * @return the role name for the current configured type
     */
    public String getRoleName() {

        if (tfRoleName.getText().isEmpty()) {
            if (isSupertypeAsSubstitution()) {
                return toRoleName(tfSupertypeName.getText());
            } else {
                return toRoleName(tfTypeName.getText());
            }
        }
        return tfRoleName.getText().trim();
    }

    /**
     * Indicates whether there is a supertype as substitution.
     *
     * @return a boolean indicating whether there is a supertype as substitution
     */
    public boolean isSupertypeAsSubstitution() {
        return !getSupertypeName().equals("");
    }

    /**
     * Gets the supertype name for the current configured type.
     *
     * @return the supertype name for the current configured type
     */
    public String getSupertypeName() {
        return tfSupertypeName.getText().trim();
    }

    /**
     * Indicates whether the current configured type is an object type.
     *
     * @return a boolean indicating whether the current configured type is an
     * object type
     */
    public boolean isObjectType() {
        return !isBaseType() && rbSingle.isSelected();
    }

    public boolean isCollection() {
        return !isBaseType() && !rbSingle.isSelected();
    }

    /**
     * Resets all components inside this dialog.
     */
    private void resetDialog() {
        tfTypeName.setText("");
        tfSupertypeName.setText("");
        lsBaseTypes.clearSelection();
        lsFactTypes.clearSelection();
        rbSingle.setSelected(true);
        rbSet.setSelected(false);
        rbList.setSelected(false);
        initCollectionControls();
    }

    /**
     * Converts a name to a role name.
     *
     * <p>
     * This method decapitalizes the first letter of the given name, even if it
     * already was a lowercase letter. If the found letter is part of a sequence
     * of capitals letters, other capital letters in that sequence will be
     * decapitalized as well. The last capital letter of such sequence will not
     * be decapitalized when it is followed by a lowercase letter.</p>
     *
     * <p>
     * For example, "StudentName" becomes "studentName", and "ICTStudent"
     * becomes "ictStudent", while "teacherIndex" stays the same.</p>
     *
     * @param typeName the type name to be converted to a role name
     * @returns a correctly cased role name
     *
     * @see equa.Naming
     */
    static String toRoleName(String name) {
        String nameToLower = name.toLowerCase();
        int i = 0;
        // name[0..i-1] is capitalized within nameToLOwer AND 0<=i<=name.length()
        while (i < name.length()
                && Character.isUpperCase(name.charAt(i))) {
            i++;
        }
        if (i == name.length()) {
            // i==name.length() and name[0..i-1] is capitalized within nameToLOwer
            return nameToLower;
        } else /*
         * name[0..i-1] is capitalized within nameToLOwer AND 
         * name[i] is not capitalized within nameToLOwer
         */ if (i == 0) {
            return name;
        } else if (i == 1) {
            return nameToLower.substring(0, i) + name.substring(i);
        } else {
            // two or more capitals: last capital stays the same
            return nameToLower.substring(0, i - 1) + name.substring(i - 1);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOK;
    private javax.swing.JButton btReset;
    private javax.swing.ButtonGroup buttonGroupCollection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbRoleName;
    private javax.swing.JLabel lbTypeName;
    private javax.swing.JList lsBaseTypes;
    private javax.swing.JList lsFactTypes;
    private javax.swing.JPanel pnRadioButtons;
    private javax.swing.JRadioButton rbList;
    private javax.swing.JRadioButton rbSet;
    private javax.swing.JRadioButton rbSingle;
    private javax.swing.JScrollPane spBaseTypes;
    private javax.swing.JScrollPane spObjectTypes;
    private javax.swing.JTextArea taElementInfo;
    private javax.swing.JTextArea taExpression;
    private javax.swing.JTextField tfRoleName;
    private javax.swing.JTextField tfSupertypeName;
    private javax.swing.JTextField tfTypeName;
    // End of variables declaration//GEN-END:variables
}
