/*
 * EditTypeExpressionDialog.java
 *
 * Created on 4-jan-2012, 11:08:03
 */
package equa.configurator;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import equa.meta.ChangeNotAllowedException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.traceability.ExternalInput;
import equa.project.ProjectRole;

/**
 *
 * @author frankpeeters
 */
public class EditTypeExpressionDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private FactType ft;
    private List<Role> roles;
    private TypeExpression te;
    private JTextField[] tfConstants;
    private JPanel[] pnRoles;
    private ProjectRole currentUser;

    /**
     * Creates new form EditTypeExpressionDialog
     */
    public EditTypeExpressionDialog(java.awt.Frame parent, FactType ft, TypeExpression te, String title, ProjectRole currentUser) {
        super(parent, true);
        this.ft = ft;
        this.roles = null;
        this.te = te;
        this.currentUser = currentUser;

        initComponents();
        initMyComponents();
        setSize(600, 300);
        setTitle(title + " of " + ft.getName());
        btOk.setText("Ready");
        btCancel.setText("Cancel");
    }

    public EditTypeExpressionDialog(java.awt.Frame parent, List<Role> roles, String title, ProjectRole currentUser) {
        super(parent, true);
        this.ft = null;
        this.roles = roles;
        this.te = null;
        this.currentUser = currentUser;

        initComponents();
        initMyComponents();
        setSize(600, 300);
        setTitle(title);
    }

    public List<String> getConstants() {
        ArrayList<String> constants = new ArrayList<>();
        for (JTextField tf : tfConstants) {
            constants.add(tf.getText());
        }
        return constants;
    }

    public List<Role> getRoles() {
        ArrayList<Role> newRoles = new ArrayList<>();
//        for (JPanel panel : pnRoles) {
//            newRoles.add(roles.get(indexOf(panel)));
//        }
        for (JPanel panel : pnRoles) {
            int roleNr = Integer.parseInt(panel.getName());
            newRoles.add(roles.get(roleNr));
        }
        return newRoles;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnTypeExpression = new javax.swing.JPanel();
        pnButtons = new javax.swing.JPanel();
        btOk = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        pnTypeExpression.setName("pnTypeExpression"); // NOI18N
        getContentPane().add(pnTypeExpression, java.awt.BorderLayout.CENTER);

        pnButtons.setName("pnButtons"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(EditTypeExpressionDialog.class);
        btOk.setText(resourceMap.getString("btOk.text")); // NOI18N
        btOk.setName("btOk"); // NOI18N
        btOk.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOkActionPerformed(evt);
            }
        });
        pnButtons.add(btOk);

        btCancel.setText(resourceMap.getString("btCancel.text")); // NOI18N
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });
        pnButtons.add(btCancel);

        getContentPane().add(pnButtons, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOkActionPerformed
        List<String> constants = new ArrayList<>();
        for (JTextField tfConstant : tfConstants) {
            constants.add(tfConstant.getText());
        }

        List<Integer> roleNumbers = new ArrayList<>(pnRoles.length);

        for (JPanel pnRole : pnRoles) {
            roleNumbers.add(Integer.parseInt(pnRole.getName()));
        }

        if (te != null) {
            ExternalInput input = new ExternalInput("type expression has been changed during type configuration", currentUser);
            try {
                te.setConstants(constants, input);
                te.setRoleNumbers(roleNumbers);
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(EditTypeExpressionDialog.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
        }

        setVisible(false);
    }//GEN-LAST:event_btOkActionPerformed

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOk;
    private javax.swing.JPanel pnButtons;
    private javax.swing.JPanel pnTypeExpression;
    // End of variables declaration//GEN-END:variables

    private void initMyComponents() {
        int size;
        if (ft != null) {
            size = ft.size();
        } else {
            size = roles.size();
        }
        tfConstants = new JTextField[size + 1];
        if (te != null) {
            Iterator<String> itConstants = te.constants();
            int i = 0;
            while (itConstants.hasNext()) {
                JTextField tfConstant = new JTextField(itConstants.next());
                tfConstant.setColumns(10);
                tfConstants[i] = tfConstant;
                i++;
            }
        } else {
            for (int j = 0; j < tfConstants.length; j++) {
                tfConstants[j] = new JTextField(10);
            }
        }

        pnRoles = new JPanel[size];
        int i = 0;
        Role role;
        while (i < pnRoles.length) {

            if (ft != null) {
                role = ft.getRole(te.getRoleNumber(i));
            } else {
                role = roles.get(i);
            }
            JPanel rolePanel = new JPanel();
            pnRoles[i] = rolePanel;
            rolePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            JLabel label = new JLabel(role.getRoleName()
                    + " : " + role.getSubstitutionType().getName());
            rolePanel.setName(role.getNr() + "");
            rolePanel.add(label);
            final JButton btMove = new JButton("move");
            rolePanel.add(btMove);
            rolePanel.setBorder(new LineBorder(Color.BLUE));
            btMove.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            if (pnRoles.length <= 1) {
                                return;
                            }
                            JPanel panel = (JPanel) btMove.getParent();
                            int i = indexOf(panel);
                            int j = (i + 1) % pnRoles.length;
                            pnRoles[i] = pnRoles[j];
                            pnRoles[j] = panel;
                            rearrange();
                        }
                    });
            i++;
        }
        rearrange();
    }

    private int indexOf(JPanel panel) {
        for (int i = 0; i < pnRoles.length; i++) {
            if (pnRoles[i] == panel) {
                return i;
            }
        }
        return -1;
    }

    private void rearrange() {
        pnTypeExpression.removeAll();
        pnTypeExpression.add(tfConstants[0]);
        for (int i = 0; i < pnRoles.length; i++) {
            pnTypeExpression.add(pnRoles[i]);
            pnTypeExpression.add(tfConstants[i + 1]);
        }
        pnTypeExpression.validate();
    }
}
