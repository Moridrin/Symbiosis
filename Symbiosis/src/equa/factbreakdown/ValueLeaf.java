/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import javax.swing.tree.TreeNode;

import equa.meta.objectmodel.BaseType;

/**
 *
 * @author FrankP
 */
public class ValueLeaf extends ValueNode implements ISubstitution {

    private static final long serialVersionUID = 1L;
    private String value;
    private String roleName;
    private int roleNumber;

    /**
     * creation of an treenode with a basevalue, belonging to a basetype
     * typeName and an optional rolename; isReady() is allways true
     *
     * @param value may not be empty
     * @param typeName may not be empty
     * @param roleName an empty string is allowed
     * @param roleNumber the number of the role according to the ranking at the
     * (prospective) facttype;
     */
    public ValueLeaf(ParentNode parent, String value, String typeName,
            String roleName, int roleNumber) {
        super(parent, typeName);
        if (value.isEmpty()) {
            throw new RuntimeException("base value cannot be empty");
        }
        this.value = value;
        this.roleName = roleName;
        this.roleNumber = roleNumber;
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public boolean isReady() {
        return true;
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
        return value;
    }

    @Override
    public int getChildIndex() {
        return getParent().getIndex(this);
    }

    @Override
    public BaseType getDefinedType() {
        return BaseType.getBaseType(getTypeName());
    }

    @Override
    public String getTypeDescription() {
        return getRoleName() + " : " + getTypeName();
    }

    @Override
    public String getText() {
        return toString();
    }

    @Override
    public int getRoleNumber() {
        return roleNumber;
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

    @Override
    boolean setReady(boolean ready) {
        return ready == true;
    }

    @Override
    public void setRoleNumber(int nr) {
        roleNumber = nr;
    }
}
