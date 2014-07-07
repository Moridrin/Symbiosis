/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import equa.factbreakdown.CollectionNode;
import equa.factbreakdown.ExpressionNode;
import equa.factbreakdown.SuperTypeNode;
import equa.factbreakdown.TextNode;
import equa.factbreakdown.ValueNode;

public class ExpressionTreeCellRenderer implements TreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JTextField tfExpression = new JTextField();
        Font font = tfExpression.getFont();
        tfExpression.setFont(font.deriveFont(Node.VALUE_FONTSTYLE, FactBreakdown.getFontSize()));
        tfExpression.setText(value.toString());
        panel.add(tfExpression, BorderLayout.WEST);

        if (value instanceof ValueNode) {
            final ValueNode vn = (ValueNode) value;

            JTextField tfName = new JTextField();
            tfName.setForeground(Color.blue);
            tfName.setText(vn.getTypeDescription());
            tfName.setFont(font.deriveFont(FactBreakdown.getFontSize()));
            tfName.setBorder(BorderFactory.createEmptyBorder());

            panel.add(tfName, BorderLayout.EAST);

            if (!(vn instanceof SuperTypeNode)) {
                JCheckBox cbReady = new JCheckBox();
                cbReady.setSelected(vn.isReady());
                cbReady.setEnabled(false);
                panel.add(cbReady, BorderLayout.CENTER);
            }
        }

        if (((ExpressionNode) value).isReady()) {
//            if (value instanceof SuperTypeNode) {
//                tfExpression.setBackground(Node.SUPERTYPE_COLOR);
//                tfExpression.setForeground(Color.WHITE);
//                tfExpression.setFont(font.deriveFont(Node.SUPERTYPE_FONTSTYLE, Node.FONT_SIZE));
//            } else if (value instanceof CollectionNode) {
//                tfExpression.setBackground(Node.COLLECTION_COLOR);
//                tfExpression.setForeground(Color.WHITE);
//                tfExpression.setFont(font.deriveFont(Node.COLLECTION_FONTSTYLE, Node.FONT_SIZE));
//
//            } else {
            //tfExpression.setBorder(BorderFactory.createLineBorder(Node.READY_COLOR));
            tfExpression.setBackground(Node.READY_COLOR);
            //tfExpression.setForeground(Color.WHITE);
            tfExpression.setFont(font.deriveFont(Node.READY_FONTSTYLE, FactBreakdown.getFontSize()));
//            if (value instanceof SuperTypeNode) {
//                tfExpression.setBorder(BorderFactory.createLineBorder(Node.SUPERTYPE_COLOR));
//            }else if (value instanceof ValueNode){
//                tfExpression.setBorder(BorderFactory.createLineBorder(Node.VALUE_COLOR));
//            }
//            }
        } else {
            if (value instanceof TextNode) {
                // tfExpression.setBorder(BorderFactory.createLineBorder(Node.TEXT_COLOR));
                //  tfExpression.setForeground(Color.WHITE);
                tfExpression.setBackground(Node.TEXT_COLOR);
                tfExpression.setFont(font.deriveFont(Node.TEXT_FONTSTYLE, FactBreakdown.getFontSize()));

            } else if (value instanceof SuperTypeNode) {
                //tfExpression.setBorder(BorderFactory.createLineBorder(Node.SUPERTYPE_COLOR));
                tfExpression.setForeground(Color.WHITE);
                tfExpression.setBackground(Node.SUPERTYPE_COLOR);
                tfExpression.setFont(font.deriveFont(Node.SUPERTYPE_FONTSTYLE, FactBreakdown.getFontSize()));
            } else if (value instanceof CollectionNode) {
                tfExpression.setForeground(Color.WHITE);
                tfExpression.setBackground(Node.COLLECTION_COLOR);
                tfExpression.setFont(font.deriveFont(Node.COLLECTION_FONTSTYLE, FactBreakdown.getFontSize()));

            } else {
                //tfExpression.setBorder(BorderFactory.createLineBorder(Node.VALUE_COLOR));
                // tfExpression.setForeground(Color.WHITE);
                tfExpression.setBackground(Node.VALUE_COLOR);
                tfExpression.setFont(font.deriveFont(Node.VALUE_FONTSTYLE, FactBreakdown.getFontSize()));
            }
        }

        // panel.setBorder(BorderFactory.createLineBorder(Color.yellow));
        panel.validate();
        return panel;
    }
}
