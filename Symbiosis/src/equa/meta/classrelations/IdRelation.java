/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.meta.objectmodel.BaseValueRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class IdRelation extends Relation {

    private static final long serialVersionUID = 1L;

    public IdRelation(ObjectType owner, Role role) {
        super(owner, role);
    }

    @Override
    public String multiplicity() {
        return "1";
    }
    
    @Override
    public boolean isCreational() {
        return false;
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public boolean hasMultipleTarget() {
        return false;
    }

    @Override
    public boolean isComposition() {
        return false;
    }

    @Override
    public String name() {
        return role.detectRoleName();
    }

    @Override
    public boolean isNavigable() {
        return true;
    }

    @Override
    public SubstitutionType targetType() {
        SubstitutionType st = role.getSubstitutionType();
        if (st instanceof ObjectType) {
            ObjectType ot = (ObjectType) st;
            if (ot.isAbstract()) {
                ObjectType concreteSubType = ot.concreteSubType();
                if (concreteSubType != null) {
                    st = concreteSubType;
                }
            }
        }
        return st;
    }

//    @Override
//    public boolean isAnchorRelation() {
//        Relation inverse = inverse();
//        if (inverse == null) {
//            return false;
//        } else {
//            return inverse.multiplicity().equals("1");
//        }
//    }
    @Override
    public Relation inverse() {
        if (role instanceof BaseValueRole) {
            return null;
        } else {
            return new ObjectTypeRelation((ObjectType) role.getSubstitutionType(), role);
        }
    }

    @Override
    public String asAttribute() {
        return "- " + name() + " : " + targetType().getName();
    }

    @Override
    public boolean isAdjustable() {
        return false;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public boolean isAddable() {
        return false;
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public boolean isPartOfId() {
        return true;
    }

    @Override
    public boolean hasNoDefaultValue() {
        return true;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public String fieldName() {

        if (role.hasDefaultName()) {
            return Naming.withoutCapital(role.getSubstitutionType().getName());
        } else {
            return role.getRoleName();
        }
    }

    @Override
    public boolean isSeqRelation() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return !role.isSettable() && !role.getSubstitutionType().isRemovable();
    }

    @Override
    public boolean isMapRelation() {
        return false;
    }

}
