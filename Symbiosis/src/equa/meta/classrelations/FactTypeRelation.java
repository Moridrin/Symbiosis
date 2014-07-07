/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.meta.objectmodel.BaseValueRole;
import equa.meta.objectmodel.CollectionType;
import equa.meta.objectmodel.ElementsFactType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class FactTypeRelation extends Relation {

    private static final long serialVersionUID = 1L;
    private final Role relatedRole;

    public FactTypeRelation(ObjectType owner, Role role) {
        super(owner, role);

        if (role.isQualifier()) {
            throw new RuntimeException("qualifier role is not allowed in fact type relation");
        } else {
            relatedRole = role.getParent().counterpart(role);
            if (relatedRole == null) {
                System.out.println(owner.getName() + " ; " + role.toString());
            }
        }
    }

    @Override
    public SubstitutionType targetType() {
        if (relatedRole == null) {
            return null;
        }
        SubstitutionType st = relatedRole.getSubstitutionType();
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

    @Override
    public String name() {
        return relatedRole.detectRoleName();
    }

    @Override
    public boolean hasDefaultName() {
        return relatedRole.hasDefaultName();
    }

    @Override
    public FactTypeRelation inverse() {
        if (relatedRole instanceof BaseValueRole) {
            return null;
        } else {
            return new FactTypeRelation((ObjectType) relatedRole.getSubstitutionType(), relatedRole);
        }
    }

    @Override
    public boolean isFinal() {
        return !getParent().isMutable() && !relatedRole.getSubstitutionType().isRemovable();
    }

    @Override
    public boolean isSettable() {
        return role.isSettable() && role.hasSingleTarget();
    }

    @Override
    public boolean isNavigable() {
        return role.isNavigable();
    }

    @Override
    public String asAttribute() {
        if (hasMultipleTarget()) {
            return "- " + "coll" + Naming.withCapital(name()) + " : " + new CT(CollectionKind.COLL, targetType());
        } else {
            return "- " + name() + " : " + targetType();
        }
    }

    @Override
    public boolean hasNoDefaultValue() {
//        if (relatedRole == null) {
//            return true;
//        }
        return !relatedRole.hasDefaultValue();
    }

    @Override
    public boolean isAdjustable() {
        return role.isAdjustable();
    }

    @Override
    public boolean isPartOfId() {
        return (role.getParent() instanceof ElementsFactType)
                && (role.getSubstitutionType() instanceof CollectionType);
    }

    @Override
    public String getDefaultValue() {
        if (hasNoDefaultValue()) {
            return null;
        } else {
            return relatedRole.getDefaultValue();
        }
    }

    @Override
    public String fieldName() {
        if (hasMultipleTarget()) {
            return relatedRole.getPluralName();
        } else {
            return relatedRole.detectRoleName();
        }
    }

    @Override
    public String getPluralName() {
        return relatedRole.getPluralName();
    }

    @Override
    public boolean isSeqRelation() {
        return role.getParent().isSeqRole(role);
    }

    @Override
    public boolean hasMultipleQualifiedTarget() {
        if (qualifierRoles().isEmpty()) {
            return false;
        }

        return role.hasCommonUniquenessWith(relatedRole);
    }

    public String getAutoIncrField() {
        if (isCreational()) {
            if (isSeqRelation()) {
                String autoIncrRoleName = role.getParent().getAutoIncr();
                if (autoIncrRoleName != null) {
                    return autoIncrRoleName;
                }
            } else {
                Role counterpart = role.getParent().counterpart(role);
                if (counterpart != null && counterpart.getSubstitutionType() instanceof ObjectType) {
                    ObjectType cp = (ObjectType) counterpart.getSubstitutionType();
                    return cp.getFactType().getAutoIncr();
                } else {
                    return null;
                }

            }
        }
        return null;
    }

}
