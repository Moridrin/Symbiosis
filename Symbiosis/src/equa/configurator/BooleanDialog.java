/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.configurator;

/**
 *
 * @author frankpeeters
 */
public class BooleanDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Creates new form BaseTypeDialog
     *
     * @param parent
     * @param modal
     */
    public BooleanDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        cbBoolean = new javax.swing.JComboBox<>();
        btOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(equa.desktop.Symbiosis.class).getContext().getResourceMap(BaseTypeDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        cbBoolean.setModel(new javax.swing.DefaultComboBoxModel<>(new Boolean[]{Boolean.FALSE, Boolean.TRUE, null}));
        cbBoolean.setName("cbBoolean"); // NOI18N
        cbBoolean.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBooleanStateChanged(evt);
            }
        });

        btOk.setText(resourceMap.getString("btOk.text")); // NOI18N
        btOk.setName("btOk"); // NOI18N
        btOk.addActionListener(new java.awt.event.ActionListener() {
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
                        .add(26, 26, 26)
                        .add(cbBoolean, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(29, 29, 29)
                        .add(btOk)
                        .addContainerGap(58, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(btOk)
                                .add(cbBoolean, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(102, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void cbBooleanStateChanged(java.awt.event.ItemEvent evt) {
        setVisible(false);
    }

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
    }

    public Boolean getBoolean() {
        if (cbBoolean.getSelectedItem() != null) {
            return (Boolean) cbBoolean.getSelectedItem();
        } else {
            return null;
        }
    }
    // Variables declaration - do not modify                     
    private javax.swing.JButton btOk;
    private javax.swing.JComboBox<Boolean> cbBoolean;
    // End of variables declaration                   
}
