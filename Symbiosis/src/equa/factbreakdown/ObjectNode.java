/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import java.util.List;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.objectmodel.Value;

/**
 *
 * @author FrankP
 */
public class ObjectNode extends FactNode implements ISubstitution {

    private static final long serialVersionUID = 1L;
    private String roleName;
    private int roleNumber;

    public ObjectNode(ExpressionTreeModel model, ParentNode parent, String text,
            String typeName, String roleName, int roleNumber) {
        super(model, parent, text, typeName);
        this.roleName = roleName;
        this.roleNumber = roleNumber;
    }

    /**
     * creation of object node as root of the expression tree model
     *
     * @param model
     * @param text
     * @param typeName
     */
    public ObjectNode(ExpressionTreeModel model, String text, String typeName) {
        super(model, null, text, typeName);
    }

    public ObjectNode(ExpressionTreeModel model, ParentNode parent,
            Value sv, String roleName, int roleNumber, TypeExpression ote) throws MismatchException, ChangeNotAllowedException {
        super(model, parent, sv, ote);
        this.roleName = roleName;
        this.roleNumber = roleNumber;
    }

    // objectnode with abstract roles
    public ObjectNode(ExpressionTreeModel model, ParentNode parent, String expression,
            String roleName, int roleNumber,
            TypeExpression te) throws MismatchException, ChangeNotAllowedException, DuplicateException {
        super(model, parent, expression, te);
        this.roleName = roleName;
        this.roleNumber = roleNumber;
    }

    @Override
    int roleNumber(int childNumber) {
        FactType ft = getObjectModel().getFactType(getTypeName());
        if (ft == null) {
            return childNumber / 2;
        }

        if (ft.isObjectType()) {
            if (ft.size() == 0) {
                return 0;
            } else {
                return ft.getObjectType().getOTE().getRoleNumber(childNumber / 2);
            }
        } else {
            return childNumber / 2;
        }

    }

    @Override
    public ObjectType getDefinedType() {
        return getExpressionTreeModel().getObjectModel().getObjectType(getTypeName());
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public String getTypeDescription() {
        return getRoleName() + " : " + getTypeName();
    }

    @Override
    public void registerAtObjectModel() throws MismatchException,
            ChangeNotAllowedException, DuplicateException {

        List<Integer> roleNumbers = getRoleNumbers();
        List<String> constants = getConstants();

        List<String> roleNames = getRoleNames(roleNumbers);

        ObjectModel om = getExpressionTreeModel().getObjectModel();
        FactType ft = om.getFactType(getTypeName());

        if (ft == null) {
            om.addObjectType(getTypeName(), constants,
                    getDefinedTypes(roleNumbers), roleNames,
                    roleNumbers, getExpressionTreeModel().getSource());
        }

        om.addObject(this);

        if (getParent() instanceof CollectionNode) {
            CollectionNode cn = (CollectionNode) getParent();
            cn.registerOtherElements();
            cn.setUnconditionallyReady();
        }
    }

    @Override
    public int getRoleNumber() {
        return roleNumber;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public void setRoleNumber(int nr) {
        roleNumber = nr;
    }
}
