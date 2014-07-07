/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.ObjectModel;
import javax.swing.tree.TreeNode;

/**
 *
 * @author FrankP
 */
public abstract class ParentNode extends ValueNode {

    private static final long serialVersionUID = 1L;
    private ExpressionTreeModel model;

    public ParentNode(ExpressionTreeModel model, ParentNode parent, String typeName) {
        super(parent, typeName);
        this.model = model;
    }

    public abstract void registerAtObjectModel()
            throws MismatchException,
            ChangeNotAllowedException, DuplicateException;

    public abstract void deregisterAtObjectModel() throws ChangeNotAllowedException;

    public ExpressionTreeModel getExpressionTreeModel() {
        return model;
    }

    public ObjectModel getObjectModel() {
        return model.getObjectModel();
    }

    public boolean existingRoleName(String roleName) {
        for (int i = 0; i < this.getChildCount(); i++) {
            TreeNode node = getChildAt(i);
            if (node instanceof ISubstitution) {
                if (((ISubstitution) node).getRoleName().equalsIgnoreCase(roleName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ParentNode getRoot() {
        ParentNode node = this;
        while (node.getParent() != null) {
            node = node.getParent();

        }
        return node;
    }

    void notifyModel() throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        try {
            model.updateObjectModel(this);
        } catch (MismatchException exc) {
            model.setReady(this, false);
            throw exc;
        }
    }

    abstract ObjectNode addObjectNodeAt(int nr, int from, int unto, String typeName,
            String roleName, int roleNumber, boolean otherOptions) throws MismatchException,
            ChangeNotAllowedException, DuplicateException;

    ValueNode removeValueNodeAt(int nr) throws ChangeNotAllowedException, MismatchException, DuplicateException {

        int count = getChildCount();
        if (nr < 0 || nr >= count || nr % 2 == 0) {
            throw new ChangeNotAllowedException("removing textnode is not allowed");
        }

        ValueNode vn = (ValueNode) getChildAt(nr);
        String textValueNode = vn.getText();
        ExpressionTreeModel model = getExpressionTreeModel();

//        if (vn instanceof ParentNode) {
//            model.removeChildrenValueNodes((ParentNode) vn);
//        }
        model.removeNodeFromParent(vn);
        TextNode tn1 = (TextNode) getChildAt(nr - 1);
        TextNode tn2 = (TextNode) getChildAt(nr);
        model.setText(tn1, tn1.getText() + textValueNode + tn2.getText());
        model.removeNodeFromParent(tn2);

        return vn;
    }

    void calculateReadyness() {
        if (allSubNodesAreReady()) {
            getExpressionTreeModel().setReady(this, true);
        }
    }

    @Override
    boolean setReady(boolean ready) {
        if (ready == this.ready) {
            return false;
        }
        if (ready && !allValueSubNodesAreReady()) {
            return false;
        }

        return setReadyHelperMethod(ready);

    }

    abstract int roleNumber(int childNr);

    void setUnconditionallyReady() {
        setReadyHelperMethod(true);
    }

    private boolean setReadyHelperMethod(boolean ready) {
        boolean backup = this.ready;
        this.ready = ready;
        // adjust readyness of children nodes
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpressionNode child = (ExpressionNode) getChildAt(i);
            if (child instanceof ISubstitution) {
                ((ISubstitution) child).setRoleNumber(roleNumber(i));
            }
            model.setReady(child, ready);
        }

        try {
            if (this.ready) {
                registerAtObjectModel();
                ParentNode parentNode = getParent();
                if (parentNode != null) {
                    parentNode.calculateReadyness();
                }
            } else {
                deregisterAtObjectModel();
            }
            return true;
        } catch (MismatchException | ChangeNotAllowedException | DuplicateException exc) {
            this.ready = backup;
            return false;
        }
    }

    @Override
    public boolean allSubNodesAreReady() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpressionNode node = (ExpressionNode) getChildAt(i);
            if (!node.isReady()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean allValueSubNodesAreReady() {
        int childCount = getChildCount();
        for (int i = 1; i < childCount; i += 2) {
            ExpressionNode node = (ExpressionNode) getChildAt(i);
            if (!node.isReady()) {
                return false;
            }
        }
        return true;
    }

}
