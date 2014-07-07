/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MatchDialog.java
 *
 * Created on 14-mei-2011, 11:57:25
 */
package equa.factbreakdown.gui;

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import equa.configurator.AbstractObjectTypeDialog;
import equa.configurator.InheritanceDialog;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.traceability.ExternalInput;
import equa.project.ProjectRole;

/**
 *
 * @author FrankP
 */
public class MatchDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    //private ParentNode node;
    private boolean match;
    private final String originalExpression;
    private final int[] originalSequence;
    private final TypeExpression te;
    private final int size;
    private final List<String> constants;
    private final ConstantMatchPanel head;
    private final boolean objectExpression;
    private final ProjectRole currentUser;

    public MatchDialog(FactType ft, TypeExpression te, String expression,
            int mismatchPosition, boolean objectExpression, ProjectRole currentUser, Frame parent) {
        super(parent, true);
        initComponents();
        match = false;

        this.objectExpression = objectExpression;
        this.currentUser = currentUser;

        originalExpression = expression;

        this.te = te;

        Iterator<String> itConstants = te.constants();
        //Iterator<Role> itRoles = ft.roles();

        size = ft.size();
        constants = new ArrayList<>();

        ConstantMatchPanel pnConstant;
        SubstitutionMatchPanel pnSubstitution;

        String constant = itConstants.next();
        int correctConstantsToGo = mismatchPosition;
        String remainingExpression = expression;
        if (te.isParsable() && correctConstantsToGo > 0) {
            pnConstant = new ConstantMatchPanel(constant, constant);
            correctConstantsToGo--;
            remainingExpression = remainingExpression.substring(constant.length());
        } else {
            pnConstant = new ConstantMatchPanel(constant, expression);
            remainingExpression = "";
        }

        head = pnConstant;
        constants.add(constant);
        pnExpression.add(pnConstant);

        originalSequence = new int[size];

        for (int i = 0; i < ft.size(); i++) {
            int roleNumber = te.getRoleNumber(i);
            originalSequence[i] = roleNumber;
            Role role = ft.getRole(roleNumber);
            pnSubstitution = new SubstitutionMatchPanel(roleNumber, role.getRoleName(),
                    role.getSubstitutionType().getName());

            pnExpression.add(pnSubstitution);
            pnConstant.setNext(pnSubstitution);
            constant = itConstants.next();
            if (te.isParsable() && correctConstantsToGo > 0) {
                int j = remainingExpression.indexOf(constant);
                correctConstantsToGo--;
                pnSubstitution.appendFront(remainingExpression.substring(0, j));
                pnConstant = new ConstantMatchPanel(constant, constant);
                remainingExpression = remainingExpression.substring(j + constant.length());
            } else {
                pnSubstitution.appendFront(remainingExpression);
                remainingExpression = "";
                pnConstant = new ConstantMatchPanel(constant, "");
            }

            constants.add(constant);
            pnSubstitution.setNext(pnConstant);
            pnExpression.add(pnSubstitution);
            pnExpression.add(pnConstant);

        }
        setSize(500, 400);
        setTitle(ft.getName());
    }

    public boolean match() {
        return match;
    }
    

    public boolean areRoleNamesChanged() {
        SubstitutionMatchPanel panel = (SubstitutionMatchPanel) head.getNext();
        while (panel != null) {
            if (panel.isRoleNameChanged()) {
                return true;
            }
            panel = (SubstitutionMatchPanel) panel.getNext().getNext();
        }
        return false;
    }

    /**
     *
     * @return null if constants didn't change, else the changed constants
     */
    public List<String> getConstants() {
        ArrayList<String> newConstants = new ArrayList<>();
        boolean changed = false;
        MatchPanel panel = head;
        String newConstant = panel.getText();
        if (objectExpression && !newConstant.isEmpty()) {
            newConstant = newConstant.substring(0, 1).toLowerCase()
                    + newConstant.substring(1);
        }
        newConstants.add(newConstant);
        if (!newConstant.equals(constants.get(0))) {
            changed = true;
        }
        panel = panel.getNext();
        int i = 1;
        while (panel != null) {
            panel = panel.getNext();
            newConstant = panel.getText();
            newConstants.add(newConstant);
            if (!newConstant.equals(constants.get(i))) {
                changed = true;
            }
            panel = panel.getNext();
            i++;
        }
        if (changed) {
            return newConstants;
        } else {
            return null;
        }
    }

    /**
     *
     * @return the rolenames
     */
    public List<String> getRoleNames() {
        List<String> roleNames = new ArrayList<>();
        SubstitutionMatchPanel panel = (SubstitutionMatchPanel) head.getNext();
        while (panel != null) {
            roleNames.add(panel.getRoleName());
            panel = (SubstitutionMatchPanel) panel.getNext().getNext();
        }
        return roleNames;
    }

    /**
     *
     * @return the sequence in which the roles are used in respect to the
     * concerning type expression
     */
    public List<Integer> getSubstitutionSequence() {
        List<Integer> substitutionSequence = new ArrayList<>(size);
        SubstitutionMatchPanel panel = (SubstitutionMatchPanel) head.getNext();
        while (panel != null) {
            substitutionSequence.add(panel.getRoleNumber());
            panel = (SubstitutionMatchPanel) panel.getNext().getNext();

        }
        return substitutionSequence;
    }

//    public int[] getNodeSequence() {
//        int[] newSeq = getSubstitutionSequence();
//        int[] nodeSeq = new int[size];
//        for (int i = 0; i < size; i++) {
//            int j = 0;
//            while (newSeq[j] != originalSequence[i]) {
//                j++;
//            }
//            nodeSeq[i] = j;
//        }
//        return nodeSeq;
//    }
    public String getExpression() {

        StringBuilder expression = new StringBuilder();
        MatchPanel panel = head;
        expression.append(panel.getText());

        while (panel.getNext() != null) {
            panel = panel.getNext();
            expression.append(panel.getText());
        }
        return expression.toString().trim();

    }

    public List<String> getSubstitutionStrings() {
        List<String> substitutionParts = new ArrayList<>();
        MatchPanel panel = head;

        while (panel.getNext() != null) {
            panel = panel.getNext();
            substitutionParts.add(panel.getText());
            panel = panel.getNext();
        }
        return substitutionParts;
    }

    public List<String> getExpressionParts() {
        List<String> expressionParts = new ArrayList<>();
        MatchPanel panel = head;
        expressionParts.add(constants.get(0));
        int constantNr = 1;
        while (panel.getNext() != null) {
            panel = panel.getNext();
            expressionParts.add(panel.getText());
            expressionParts.add(constants.get(constantNr));
            constantNr++;
            panel = panel.getNext();
        }
        return expressionParts;
    }

    public void setSubstitutionStrings(List<String> substitutionStrings) {

        MatchPanel panel = head.getNext();

        for (int index = 0; index < substitutionStrings.size(); index++) {
            ((SubstitutionMatchPanel) panel).setText(substitutionStrings.get(index));
            panel = panel.getNext();
            panel = panel.getNext();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnTypeExpression = new javax.swing.JPanel();
        pnExpression = new javax.swing.JPanel();
        btCancel = new javax.swing.JButton();
        btRectifyConstants = new javax.swing.JButton();
        btMatch = new javax.swing.JButton();
        btOtherSubtype = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(MatchDialog.class);
        pnTypeExpression.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnTypeExpression.border.title"))); // NOI18N
        pnTypeExpression.setName("pnTypeExpression"); // NOI18N

        pnExpression.setBackground(resourceMap.getColor("pnExpression.background")); // NOI18N
        pnExpression.setName("pnExpression"); // NOI18N
        pnExpression.setPreferredSize(new java.awt.Dimension(350, 200));

        btCancel.setText(resourceMap.getString("btCancel.text")); // NOI18N
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        btRectifyConstants.setText(resourceMap.getString("btRectifyConstants.text")); // NOI18N
        btRectifyConstants.setToolTipText(resourceMap.getString("btRectifyConstants.toolTipText")); // NOI18N
        btRectifyConstants.setName("btRectifyConstants"); // NOI18N
        btRectifyConstants.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRectifyConstantsActionPerformed(evt);
            }
        });

        btMatch.setText(resourceMap.getString("btMatch.text")); // NOI18N
        btMatch.setToolTipText(resourceMap.getString("btMatch.toolTipText")); // NOI18N
        btMatch.setName("btMatch"); // NOI18N
        btMatch.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMatchActionPerformed(evt);
            }
        });

        btOtherSubtype.setText(resourceMap.getString("btOtherSubtype.text")); // NOI18N
        btOtherSubtype.setName("btOtherSubtype"); // NOI18N
        btOtherSubtype.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOtherSubtypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnTypeExpressionLayout = new javax.swing.GroupLayout(pnTypeExpression);
        pnTypeExpression.setLayout(pnTypeExpressionLayout);
        pnTypeExpressionLayout.setHorizontalGroup(
            pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnTypeExpressionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnExpression, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                    .addGroup(pnTypeExpressionLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btMatch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btRectifyConstants)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btOtherSubtype)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btCancel)))
                .addContainerGap())
        );
        pnTypeExpressionLayout.setVerticalGroup(
            pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnTypeExpressionLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(pnExpression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btMatch)
                    .addComponent(btRectifyConstants)
                    .addComponent(btCancel)
                    .addComponent(btOtherSubtype)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnTypeExpression, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnTypeExpression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        match = false;
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    private void btMatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btMatchActionPerformed
        if (checkMatchingConstants()) {
            try {
                te.setRoleNumbers(getSubstitutionSequence());
                te.setRoleNames(getRoleNames());
                List<String> newConstants = getConstants();
                if (newConstants != null) {
                    ExternalInput input = new ExternalInput("modification caused by textual mismatch with other fact", currentUser);
                    te.setConstants(newConstants, input);
                }

                System.out.println("Match ");
                match = true;
                setVisible(false);

            } catch (DuplicateException | ChangeNotAllowedException ex) {
                JOptionPane.showMessageDialog(getParent(), ex.getMessage());
            }
        }
    }//GEN-LAST:event_btMatchActionPerformed

    private void btRectifyConstantsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRectifyConstantsActionPerformed
        MatchPanel panel = head.getNext();

        // if one of the substitutionpanels got an empty substitution
        // rectifying of the constants wouldn't be usefull
        while (panel != null) {
            if (panel.getText().trim().isEmpty()) {
                return;
            }
            panel = panel.getNext().getNext();
        }

        panel = head;
        ((ConstantMatchPanel) panel).copy();
        panel = panel.getNext();
        while (panel != null) {
            panel = panel.getNext();
            ((ConstantMatchPanel) panel).copy();
            panel = panel.getNext();
        }

    }//GEN-LAST:event_btRectifyConstantsActionPerformed

    private void btOtherSubtypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOtherSubtypeActionPerformed

        ObjectModel om = (ObjectModel) te.getParent().getParent();
        (new AbstractObjectTypeDialog((Frame) getParent(), true, om)).setVisible(true);
        (new InheritanceDialog((Frame) getParent(), true, om)).setVisible(true);
        match = false;
        setVisible(false);
    }//GEN-LAST:event_btOtherSubtypeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btMatch;
    private javax.swing.JButton btOtherSubtype;
    private javax.swing.JButton btRectifyConstants;
    private javax.swing.JPanel pnExpression;
    private javax.swing.JPanel pnTypeExpression;
    // End of variables declaration//GEN-END:variables

    boolean expressionChanged() {
        return !getExpression().equals(originalExpression);
    }

    private boolean checkMatchingConstants() {
        ConstantMatchPanel panel = head;
        boolean allRight = true;
        while (panel != null) {
            if (panel.hasConflictingConstants()) {
                panel.setColor(Color.orange);
                allRight = false;
            } else {
                panel.setColor(Color.white);
            }

            if (panel.getNext() == null) {
                panel = null;
            } else {
                panel = (ConstantMatchPanel) panel.getNext().getNext();
            }
        }
        return allRight;
    }
}
