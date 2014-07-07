/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;

import equa.configurator.InheritanceDialog;
import equa.factbreakdown.CollectionNode;
import equa.factbreakdown.ExpressionNode;
import equa.factbreakdown.ExpressionTreeModel;
import equa.factbreakdown.FactNode;
import equa.factbreakdown.ISubstitution;
import equa.factbreakdown.ObjectNode;
import equa.factbreakdown.ParentNode;
import equa.factbreakdown.SuperTypeNode;
import equa.factbreakdown.TextNode;
import equa.factbreakdown.ValueNode;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.util.Naming;

/**
 *
 * @author FrankP
 */
public class ExpressionTreeCellEditor extends AbstractCellEditor
        implements javax.swing.tree.TreeCellEditor {

    private static final long serialVersionUID = 1L;
    private JComponent panel;
    private TextFieldWithListeners tf;
    private JTextField tfName;
    private JCheckBox cbReady;
    private JPopupMenu popup;
    private JMenuItem miReady;
    private JMenuItem miRemove;
    private JMenuItem miType;
    private JMenuItem miRoleName;
    private JMenuItem miTypeName;
    private JMenuItem miEditText;
    private JMenuItem miInheritance;
    private final Frame frame;
    /**
     * ***********************************************************************
     */
    private ExpressionTreeModel model;
    private ExpressionNode selectedNode;
    private ValueNode valueNode;
    private TreeController controller;

    /**
     * ***********************************************************************
     */
    public ExpressionTreeCellEditor(TreeController controller,
            ExpressionTreeModel model, Frame frame) {
        this.controller = controller;
        this.model = model;
        this.frame = frame;
    }

    private void initTreeCellEditor() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        //panel.setBorder(BorderFactory.createLineBorder(Color.black));
        tf = new TextFieldWithListeners(frame);
        Font font = tf.getFont();
        tf.setEditable(false);
        tf.setFont(font.deriveFont(Node.VALUE_FONTSTYLE, FactBreakdown.getFontSize()));
        tf.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.add(tf, BorderLayout.WEST);

        tfName = new JTextField();
        tfName.setForeground(Color.blue);
        tfName.setFont(font.deriveFont(FactBreakdown.getFontSize()));
        tfName.setVisible(false);
        tfName.setBorder(BorderFactory.createEmptyBorder());
        panel.add(tfName, BorderLayout.EAST);

        cbReady = new JCheckBox();
        cbReady.setVisible(false);
        cbReady.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cbReadyClicked();

            }
        });
    }

    private void cbReadyClicked() {
        if (selectedNode instanceof ParentNode) {
            ParentNode pn = (ParentNode) selectedNode;
            if (cbReady.isSelected()) {
                if (pn.getChildCount() == 1) {
                    if (pn instanceof ObjectNode) {
                        int reply = JOptionPane.showConfirmDialog(frame,
                                "Do you really want to introduce a singleton object type?");
                        if (reply != JOptionPane.OK_OPTION) {
                            cbReady.setSelected(pn.isReady());
                            return;
                        }
                    } else if (pn instanceof FactNode) {
                        JOptionPane.showMessageDialog(frame, "fact type with zero roles is not allowed");
                        cbReady.setSelected(pn.isReady());
                        return;
                    } else // substitutionnode 
                    {
                    }
                }
            }
            model.setReady(pn, cbReady.isSelected());
        }
        stopCellEditing();

    }

//    /**
//     * At this point in time the component is added to the tree (not
//     * documented!) but tree's internal cleanup might not yet be ready
//     */
//    @Override
//    public boolean shouldSelectCell(EventObject anEvent) {
//        if (anEvent instanceof MouseEvent) {
//            redirect((MouseEvent) anEvent);
//        }
//        return false;
//    }
//
//    private void redirect(final MouseEvent anEvent) {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                MouseEvent ev = SwingUtilities.convertMouseEvent(anEvent.getComponent(), anEvent, panel);
//                panel.dispatchEvent(ev);
//                ev = SwingUtilities.convertMouseEvent(anEvent.getComponent(), anEvent, cbReady);
//                cbReady.dispatchEvent(ev);
//            }
//        });
//    }
    @Override
    public Object getCellEditorValue() {
        return tf.getText();
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        initTreeCellEditor();

        tf.setText(value.toString());
        model = (ExpressionTreeModel) tree.getModel();
        selectedNode = (ExpressionNode) value;
        Font font = tf.getFont();
        if (selectedNode.isReady()) {
            if (selectedNode instanceof SuperTypeNode) {
                tf.setBackground(Node.SUPERTYPE_COLOR);
                tf.setForeground(Color.WHITE);
                tf.setFont(font.deriveFont(Node.SUPERTYPE_FONTSTYLE, FactBreakdown.getFontSize()));
            } else if (selectedNode instanceof CollectionNode) {
                tf.setBackground(Node.COLLECTION_COLOR);
                tf.setForeground(Color.WHITE);
                tf.setFont(font.deriveFont(Node.COLLECTION_FONTSTYLE, FactBreakdown.getFontSize()));

            } else {
                tf.setBackground(Node.READY_COLOR);
                tf.setFont(font.deriveFont(Node.READY_FONTSTYLE, FactBreakdown.getFontSize()));
            }
//            if (value instanceof SuperTypeNode) {
//                tf.setBorder(BorderFactory.createLineBorder(Node.SUPERTYPE_COLOR));
//            } else if (value instanceof ValueNode) {
//                tf.setBorder(BorderFactory.createLineBorder(Node.VALUE_COLOR));
//            }
        } else {
            if (selectedNode instanceof TextNode) {
                tf.setBackground(Node.TEXT_COLOR);
                tf.setFont(font.deriveFont(Node.TEXT_FONTSTYLE, FactBreakdown.getFontSize()));
            } else if (value instanceof SuperTypeNode) {
                tf.setBackground(Node.SUPERTYPE_COLOR);
                tf.setForeground(Color.WHITE);
                tf.setFont(font.deriveFont(Node.SUPERTYPE_FONTSTYLE, FactBreakdown.getFontSize()));
            } else if (selectedNode instanceof CollectionNode) {
                tf.setBackground(Node.COLLECTION_COLOR);
                tf.setForeground(Color.WHITE);
                tf.setFont(font.deriveFont(Node.COLLECTION_FONTSTYLE, FactBreakdown.getFontSize()));
            } else {
                tf.setBackground(Node.VALUE_COLOR);
                tf.setFont(font.deriveFont(Node.VALUE_FONTSTYLE, FactBreakdown.getFontSize()));
            }
        }
        if (selectedNode instanceof TextNode) {
            tfName.setVisible(false);
            cbReady.setVisible(false);
            setTextNode();
        } else {
            ValueNode valueNode = (ValueNode) selectedNode;
            tfName.setVisible(true);
            tfName.setText(valueNode.getTypeDescription());
            setValueNode(valueNode);
            if (valueNode instanceof SuperTypeNode) {
                cbReady.setVisible(false);
            } else {
                cbReady.setSelected(valueNode.isReady());
                cbReady.setVisible(true);
                panel.add(cbReady, BorderLayout.CENTER);
            }
        }
        panel.validate();
        return panel;
    }

    private void initPopup(final Frame frame) {
        this.popup = new JPopupMenu();
        this.popup.setLightWeightPopupEnabled(true);

        miType = new JMenuItem("Choose Substitution Type");
        miType.setFocusable(true);
        miType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popup.setVisible(false);
                openTypeDialog();
                popup.setVisible(false);
                stopCellEditing();
            }
        });
        popup.add(miType);

        popup.addSeparator();

        miReady = new JMenuItem("FactBreakdown Finished");
        miReady.setToolTipText("When you press on this item you are "
                + "switching between: 'this expression needs further "
                + "investigation' or 'investigation is closed'.");
        miReady.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (selectedNode instanceof FactNode) {
                    FactNode fn = (FactNode) selectedNode;
                    if (fn.getChildCount() == 1) {
                        if (fn instanceof ObjectNode) {
                            int reply = JOptionPane.showConfirmDialog(frame,
                                    "Do you really want to introduce a singleton object type?");
                            if (reply != JOptionPane.OK_OPTION) {
                                return;
                            }
                        } else {
                            JOptionPane.showMessageDialog(frame, "fact type with zero roles is not allowed");
                            return;
                        }
                    }

                    model.setReady(selectedNode, !selectedNode.isReady());

                    if (!selectedNode.isReady()) {
                        miReady.setText("FactBreakdown Finished");

                    } else {
                        miReady.setText("Further Investigation Needed");
                    }
                    popup.setVisible(false);
                    stopCellEditing();

                }
            }
        });

        popup.add(miReady);

        miRemove = new JMenuItem("Undo Fact Breakdown");
        miRemove.setToolTipText(
                "Press this item if you want to remove the subnodes of this value node.");
        miRemove.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        try {
                            if (valueNode != null && valueNode.getParent() == null) {

                                model.removeValueNodeAt((ParentNode) valueNode, -1);
                                setTextNode();

                            } else {
                                if (valueNode.hasReadyParent()) {
                                    throw new ChangeNotAllowedException("Removing isn't allowed "
                                            + "because parent node is ready;"
                                            + " please select parent node");
                                } else {
                                    model.removeValueNodeAt(valueNode.getParent(), valueNode.getChildIndex());
                                    setTextNode();
                                }
                            }

                        } catch (ChangeNotAllowedException | MismatchException | DuplicateException ex) {

                            JOptionPane.showMessageDialog(frame, "Removing isn't allowed: " + ex.getMessage());

                        }

                        popup.setVisible(false);
                        stopCellEditing();
                    }
                });

        popup.add(miRemove);
        miRoleName = new JMenuItem("Change Rolename");

        miRoleName.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popup.setVisible(false);
                        ISubstitution substitution = (ISubstitution) valueNode;
                        String roleName = JOptionPane.showInputDialog(frame, "Change rolename", substitution.getRoleName());
                        if (roleName != null) {
                            if (!roleName.isEmpty() && !Naming.isIdentifier(roleName)) {
                                JOptionPane.showMessageDialog(frame, "Rolename must fullfill the rules "
                                        + "of an identifier: it must begin with a letter or a underscore (_) and"
                                        + "may be followed by one or more of: letter, underscore or digit.");
                                return;
                            }
                            model.setRoleName(substitution, roleName);

                        }
                        stopCellEditing();
                    }
                });

        popup.add(miRoleName);
        miTypeName = new JMenuItem("Change Typename");

        miTypeName.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popup.setVisible(false);
                        String typeName = JOptionPane.showInputDialog(frame, "Change typename", valueNode.getTypeName());
                        if (typeName != null) {
                            if (!Naming.isIdentifier(typeName)) {
                                JOptionPane.showMessageDialog(frame, "Typename must fullfill the rules "
                                        + "of an identifier: it must begin with a letter or an underscore (_) and"
                                        + "may be followed by one or more letters, underscores or digits.");
                                return;
                            }
                            model.setTypeName(valueNode, typeName);

                        }
                        stopCellEditing();
                    }
                });

        popup.add(miTypeName);
        miEditText = new JMenuItem("Change Text");

        miEditText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popup.setVisible(false);
                        TextNode textnode = (TextNode) selectedNode;

                        String text = JOptionPane.showInputDialog(frame, "Change Text", textnode.getText());
                        if (text != null) {
                            model.replace(textnode, 0, textnode.getText().length(), text);

                        }
                        stopCellEditing();
                    }
                });
        popup.add(miEditText);

        miInheritance = new JMenuItem("Change Inheritance");
        miInheritance.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                InheritanceDialog dialog = new InheritanceDialog(frame, true,
                        controller.getObjectModel());
                dialog.setVisible(true);
            }
        });
        popup.add(miInheritance);

    }

    private void openTypeDialog() {
        if (tf.getSelectedText() == null || tf.getSelectedText().isEmpty()) {
            return;
        }

        int from = tf.getSelectionStart();
        int unto = tf.getSelectionEnd();
        String expression = tf.getText().substring(from, unto).trim();
        if (expression.isEmpty()) {
            return;
        }

        Point p = panel.getLocationOnScreen();
        controller.executeSubstitutionTypeDialog(p, expression,
                (TextNode) selectedNode, from, unto);
    }

    private void setValueNode(ValueNode valueNode) {
        this.valueNode = valueNode;
        popup.setVisible(false);
        miReady.setEnabled(!(valueNode instanceof SuperTypeNode));
        if (!valueNode.isReady()) {
            miReady.setText("FactBreakdown finished");
            boolean allValueNodeReady = valueNode.allValueSubNodesAreReady();
            miReady.setEnabled(allValueNodeReady && !(valueNode instanceof SuperTypeNode));
        } else {
            miReady.setText("Further investigation needed");

            //miReady has to be disabled when its parent is ready or it doesn't have any textNodes.
            boolean hasTextNode = false;
            for (int i = 0; i < selectedNode.getChildCount(); i++) {
                Object child = selectedNode.getChildAt(i);
                if (child instanceof TextNode && !((TextNode) child).getText().trim().isEmpty()) {
                    hasTextNode = true;
                }
            }
            if (!hasTextNode || selectedNode.hasReadyParent()) {
                miReady.setEnabled(false);
            }
        }

        if (valueNode.hasReadyParent()) {
            miRemove.setEnabled(false);
        } else {
            miRemove.setEnabled(true);
        }
        miType.setEnabled(false);
        miRoleName.setEnabled((valueNode instanceof ISubstitution));
        miTypeName.setEnabled(!valueNode.isReady() && valueNode instanceof ParentNode);
        miEditText.setEnabled(false);
    }

    private void setTextNode() {
        valueNode = null;
        popup.setVisible(false);
        miReady.setEnabled(false);
        miRemove.setEnabled(false);
        miType.setEnabled(!selectedNode.hasReadyParent());
        miRoleName.setEnabled(false);
        miTypeName.setEnabled(false);
        miEditText.setEnabled(!selectedNode.hasReadyParent());
    }

    class TextFieldWithListeners extends JTextField {

        private static final long serialVersionUID = 1L;

        public TextFieldWithListeners(Frame frame) {
            initPopup(frame);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {

                    checkPopup(e);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    checkPopup(e);
                    //Shotcut so the Decomposition finished menu item can be
                    //accessed by double clicking.
                    if (e.getClickCount() > 1) {
                        if (miReady.isEnabled()) {
                            miReady.doClick();
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    checkPopup(e);
                }

                private void checkPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popup.show(TextFieldWithListeners.this, e.getX(), e.getY());
                    }
                }
            });

            //Shortcut so the Choose substitutiontype menu item can be accessed
            //using Enter.
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (miType.isEnabled() && !TextFieldWithListeners.this.getSelectedText().trim().isEmpty()) {
                        miType.doClick();
                    }
                }
            });
        }
    }
}
