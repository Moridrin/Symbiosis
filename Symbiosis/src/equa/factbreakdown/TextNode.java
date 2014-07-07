/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import javax.swing.tree.TreeNode;

import equa.meta.ChangeNotAllowedException;

/**
 *
 * @author FrankP
 */
public final class TextNode extends ExpressionNode {

    private static final long serialVersionUID = 1L;
    private String text;

    public TextNode(ParentNode parent, String text) {
        super(parent);
        setText(text);
    }

    @Override
    public ExpressionNode getChildAt(int nr) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String toString() {
        return getText();
    }

    @Override
    public int getChildIndex() {
        return getParent().getIndex(this);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean allSubNodesAreReady() {
        return true;
    }

    @Override
    public boolean allValueSubNodesAreReady() {
        return true;
    }

    @Override
    public int getIndex(TreeNode node) {
        return -1;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    void replace(int from, int unto, String expression) throws ChangeNotAllowedException {
        if (from < 0 || unto > text.length()) {
            throw new RuntimeException("range not allowed");
        }

        text = text.substring(0, from) + expression + text.substring(unto);
    }

    void setText(String value) {
        this.text = value;
        if (parent != null) {
            getParent().getExpressionTreeModel().setReady(this, value.trim().isEmpty());
        } else {
            setReady(value.trim().isEmpty());
        }
    }

    @Override
    boolean setReady(boolean ready) {
        if (ready == this.ready) {
            return false;
        }

        this.ready = ready;
        return true;
    }
}
