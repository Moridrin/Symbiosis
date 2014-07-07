/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.List;

import equa.meta.DuplicateException;

/**
 *
 * @author frankpeeters
 */
public class ElementsFactTypeExpression extends TypeExpression {

    private static final long serialVersionUID = 1L;

    ElementsFactTypeExpression(ElementsFactType eft, List<String> constants) {
        super(eft, constants);
    }

//    @Override
//    String makeExpression(Tuple tuple) {
//        ElementsFactType eft = (ElementsFactType) getParent();
//        //String collectionId = ((Tuple)tuple.getItem(0).getValue()).getItem(0).getValue().toString();
//        //String collection = eft.getCollectionType().getName() + " with artificial id " + collectionId + "";
//        String collection = ((Tuple) tuple.getItem(0).getValue()).toString();
//        return collection + " contains " + tuple.getItem(1).getValue().toString() + ".";
//    }
    /**
     *
     * @return the type-expression where every substitution place is filled in
     * with the optional rolename followed by the name of the substitutiontype
     * between jagged parenthesis (< ... >)
     */
//    @Override
//    public String toString() {
//        ElementsFactType eft = (ElementsFactType) getParent();
//        CollectionType ct = eft.getCollectionType();
//        String multiplicity = eft.getElementRole().getMultiplicity();
//
//        StringBuilder sb = new StringBuilder();
//
//        SubstitutionType st = ct.getElementType();
//        String separator = ((CollectionTypeExpression) ct.getOTE()).getSeparator();
//        sb.append(constant(0)).append("<").append(eft.roles.get(0).getRoleName()).append(">");
//        sb.append(constant(1));
//        if (multiplicity.indexOf("..") == -1 && !multiplicity.equals("*")) {
//            int n = Integer.parseInt(multiplicity);
//            sb.append("<").append(eft.roles.get(0).getRoleName()).append(">");
//            for (int i = 1; i < n; i++) {
//                sb.append(separator).append(eft.roles.get(0).getRoleName()).append(">");
//            }
//        } else {
//            sb.append("<").append(eft.roles.get(0).getRoleName()).append(">").append(separator);
//            sb.append(" ... ").append(separator);
//            sb.append("<").append(eft.roles.get(0).getRoleName()).append(">");
//        }
//        sb.append(constant(2));
//
//        return sb.toString();
//    }
    /**
     * setting of the role name of all roles where this type expression refers
     * to
     *
     * @param roleNames
     * @throws DuplicateException
     */
    @Override
    public void setRoleNames(List<String> roleNames) throws DuplicateException {
    }
}
