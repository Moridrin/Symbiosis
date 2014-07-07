/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author FrankP
 */
public abstract class ExpressionNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;
    protected boolean ready;

    public ExpressionNode(ParentNode parent) {
        this.parent = parent;
        this.ready = false;
    }

    @Override
    public ParentNode getParent() {
        return (ParentNode) parent;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean hasReadyParent() {
        ParentNode parentNode = getParent();
        if (parentNode == null) {
            return false;
        }
        if (parentNode.isReady()) {
            return true;
        } else {
            return parentNode.hasReadyParent();
        }
    }

    abstract boolean setReady(boolean ready);

    public abstract String getText();

    /**
     * this node must have a parent
     *
     * @return the number of this child at his parent
     */
    public int getChildIndex() {
        ParentNode pn = getParent();
        if (pn == null) {
            //return -1;
            throw new RuntimeException("child must have a parent");
        }
        return pn.getIndex(this);
    }

    public abstract boolean allSubNodesAreReady();

    public abstract boolean allValueSubNodesAreReady();

}
