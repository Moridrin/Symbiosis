/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RangeDialog.java
 *
 * Created on 19-dec-2011, 14:53:35
 */
package equa.configurator;

import java.util.logging.Level;
import java.util.logging.Logger;

import equa.meta.MismatchException;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.BaseValue;

/**
 *
 * @author frankpeeters
 */
public class RangeDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private boolean ok;
    private BaseType bt;

    /**
     * Creates new form RangeDialog
     */
    public RangeDialog(java.awt.Frame parent, boolean modal, BaseType bt) {
        super(parent, modal);
        initComponents();
        tfBaseType.setText(bt.getName());
        ok = false;
        this.bt = bt;
    }

    public BaseValue getLower() {
        try {
            if (tfLower.getText().isEmpty()) {
                return bt.getMinValue();
            } else {
                return new BaseValue(tfLower.getText(), bt);
            }
        } catch (MismatchException ex) {
            Logger.getLogger(RangeDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        // will never happen:
        return null;

    }

    public BaseValue getUpper() {
        try {
            if (tfUpper.getText().isEmpty()) {
                return bt.getMaxValue();
            } else {
                return new BaseValue(tfUpper.getText(), bt);
            }
        } catch (MismatchException ex) {
            Logger.getLogger(RangeDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        // will never happen:
        return null;
    }

    public boolean isOk() {
        return ok;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tfLower = new javax.swing.JTextField();
        lbLower = new javax.swing.JLabel();
        tfUpper = new javax.swing.JTextField();
        lbUpper = new javax.swing.JLabel();
        lbBaseType = new javax.swing.JLabel();
        tfBaseType = new javax.swing.JTextField();
        btOk = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(RangeDialog.class);
        tfLower.setText(resourceMap.getString("tfLower.text")); // NOI18N
        tfLower.setName("tfLower"); // NOI18N

        lbLower.setText(resourceMap.getString("lbLower.text")); // NOI18N
        lbLower.setName("lbLower"); // NOI18N

        tfUpper.setName("tfUpper"); // NOI18N

        lbUpper.setText(resourceMap.getString("lbUpper.text")); // NOI18N
        lbUpper.setName("lbUpper"); // NOI18N

        lbBaseType.setText(resourceMap.getString("lbBaseType.text")); // NOI18N
        lbBaseType.setName("lbBaseType"); // NOI18N

        tfBaseType.setEditable(false);
        tfBaseType.setName("tfBaseType"); // NOI18N

        btOk.setText(resourceMap.getString("btOk.text")); // NOI18N
        btOk.setName("btOk"); // NOI18N
        btOk.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOkActionPerformed(evt);
            }
        });

        btCancel.setText(resourceMap.getString("btCancel.text")); // NOI18N
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(btOk)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btCancel))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(layout.createSequentialGroup()
                            .add(lbUpper, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(tfUpper))
                        .add(layout.createSequentialGroup()
                            .add(lbLower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(tfLower))
                        .add(layout.createSequentialGroup()
                            .add(lbBaseType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(tfBaseType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {lbBaseType, lbLower, lbUpper}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfBaseType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbBaseType))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfLower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbLower))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfUpper, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbUpper))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btCancel)
                    .add(btOk))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOkActionPerformed
        ok = true;
        setVisible(false);
    }//GEN-LAST:event_btOkActionPerformed

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        ok = false;
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOk;
    private javax.swing.JLabel lbBaseType;
    private javax.swing.JLabel lbLower;
    private javax.swing.JLabel lbUpper;
    private javax.swing.JTextField tfBaseType;
    private javax.swing.JTextField tfLower;
    private javax.swing.JTextField tfUpper;
    // End of variables declaration//GEN-END:variables
}