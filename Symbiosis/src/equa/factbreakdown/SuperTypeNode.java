 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import javax.swing.tree.TreeNode;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Value;
import equa.meta.traceability.ExternalInput;

/**
 *
 * @author FrankP
 */
public class SuperTypeNode extends ParentNode implements ISubstitution {

    private static final long serialVersionUID = 1L;
    private String roleName;
    public int roleNumber;

    public SuperTypeNode(ExpressionTreeModel model, ParentNode parent,
            String supertypeName, String text, String concreteTypeName,
            String roleName, int roleNumber) throws MismatchException, ChangeNotAllowedException {
        super(model, parent, supertypeName);
        this.roleName = roleName;
        this.roleNumber = roleNumber;
        createSubnode(text, concreteTypeName, roleName);
    }

    private void createSubnode(String text, String concreteTypeName,
            String roleName) throws MismatchException,
            ChangeNotAllowedException {

        // shrinking of object expression at the begin and end
        String textTrimmed = text.trim();

        ExpressionNode subNode;
        ExpressionTreeModel model = getExpressionTreeModel();
        if (concreteTypeName == null) {
            subNode = new TextNode(null, textTrimmed);
        } else {
            ObjectModel om = model.getObjectModel();
            ObjectType concreteType = om.getObjectType(concreteTypeName);

            if (concreteType != null && concreteType.isParsable()) {
                Value sv = concreteType.parse(textTrimmed, true, getExpressionTreeModel().getSource());
                subNode = new ObjectNode(model, null, sv, roleName, 0, concreteType.getOTE());
            } else {
                subNode = new ObjectNode(model, null, textTrimmed, concreteTypeName, roleName, 0);
            }
        }
        model.insertNodeInto(subNode, this, 0);
    }

    public ExpressionNode getSubNode() {
        if (getChildCount() > 0) {
            return (ExpressionNode) getChildAt(0);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        ExpressionNode subNode = getSubNode();
        if (subNode != null) {
            return getSubNode().toString();
        } else {
            return "No SubNode";
        }
    }

    @Override
    public void registerAtObjectModel() throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        try {
            ParentNode parentSubNode = (ParentNode) getSubNode();
            System.out.println("register subnode of " + getTypeName() + ": " + parentSubNode.getTypeName());
            parentSubNode.registerAtObjectModel();
            om.addSuperType(getTypeName(), parentSubNode.getTypeName(), new ExternalInput("", om.getProject().getCurrentUser()));
            System.out.println("register " + getTypeName());

            FactType ft = om.getFactType(getTypeName());
            if (ft != null) {
                ft.addSource(getExpressionTreeModel().getSource());
            }

        } catch (DuplicateException ex) {
            throw new MismatchException(null,ex.getMessage());
        }
    }

    @Override
    public void deregisterAtObjectModel() throws ChangeNotAllowedException {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        om.removeSuperTypeNode(this);
    }

    @Override
    public ObjectType getDefinedType() {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        return om.getFactType(getTypeName()).getObjectType();
    }

    public ObjectNode getConcreteNode() {
        ExpressionNode subNode = getSubNode();
        if (subNode instanceof ObjectNode) {
            return (ObjectNode) subNode;
        }
        return ((SuperTypeNode) subNode).getConcreteNode();
    }

    @Override
    public String getTypeDescription() {
        return getRoleName() + " : " + getTypeName();
    }

    @Override
    public String getText() {
        return getSubNode().getText();
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public boolean isReady() {
        ExpressionNode subNode = getSubNode();
        if (subNode != null) {
            return subNode.isReady();
        } else {
            return false;
        }
    }

    @Override
    boolean setReady(boolean ready) {
        if (ready == this.ready) {
            return false;
        }

        boolean backup = this.ready;
        try {
            if (ready) {
                if (getSubNode().isReady()) {
                    this.ready = true;
                    registerAtObjectModel();
                    ParentNode parentNode = getParent();
                    if (parentNode != null) {
                        parentNode.calculateReadyness();
                    }
                } else {
                    return false;
                }
            } else {
                // adjust readyness of subnode
                this.ready = false;
                deregisterAtObjectModel();
                ExpressionTreeModel model = getExpressionTreeModel();
                model.setReady(getSubNode(), false);
            }

            return true;

        } catch (MismatchException | ChangeNotAllowedException | DuplicateException exc) {
            this.ready = backup;
            return false;
        }
    }

    @Override
    public boolean allSubNodesAreReady() {
        return getSubNode().isReady();
    }

    @Override
    public boolean allValueSubNodesAreReady() {
        return getSubNode().isReady();
    }

    @Override
    public int getRoleNumber() {
        return roleNumber;
    }

    @Override
    public void setRoleNumber(int nr) {
        roleNumber = nr;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public int getIndex(TreeNode node) {
        if (getSubNode() == node) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    ObjectNode addObjectNodeAt(int nr, int from, int unto, String typeName,
            String roleName, int roleNumber, boolean otherOptions)
            throws MismatchException, ChangeNotAllowedException, DuplicateException {
        if (nr != 0) {
            throw new RuntimeException("supertype node has only one child with index 0");
        }
        ExpressionNode textNode = getSubNode();
        String text = textNode.getText();
        ObjectNode subNode;
        ExpressionTreeModel model = getExpressionTreeModel();

        ObjectModel om = model.getObjectModel();
        ObjectType concreteType = om.getObjectType(typeName);

        if (concreteType != null && concreteType.getOTE().isParsable()) {
            Value sv = concreteType.parse(text, false, getExpressionTreeModel().getSource());
            subNode = new ObjectNode(model, null, sv, roleName, 0, concreteType.getOTE());
        } else {
            subNode = new ObjectNode(model, null, text, typeName, roleName, 0);
        }

        model.removeNodeFromParent(textNode);
        model.insertNodeInto(subNode, this, 0);

        return subNode;
    }

    @Override
    int roleNumber(int childNr) {
        return 0;
    }
}
