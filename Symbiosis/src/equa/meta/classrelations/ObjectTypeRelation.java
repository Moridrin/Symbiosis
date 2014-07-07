 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.meta.objectmodel.ElementsFactType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class ObjectTypeRelation extends Relation {

    private static final long serialVersionUID = 1L;

    public ObjectTypeRelation(ObjectType owner, Role role) {
        super(owner, role);
    }

    @Override
    public ObjectType targetType() {
        if (role.getParent() instanceof ElementsFactType) {
            return ((ElementsFactType) role.getParent()).getCollectionType();
        } else {
            return role.getParent().getObjectType();
        }
    }

    @Override
    public String name() {
        String name;
        if (owner.isDoubleRelatedWith(role.getParent().getObjectType())) {
            name = role.getRoleName() + targetType().getName();
        } else {
            name = Naming.withoutCapital(targetType().getName());
        }

        return name;
    }

    public String getPluralName() {
        String pluralName = targetType().getPluralName();
        if (owner.isDoubleRelatedWith(role.getParent().getObjectType())) {
             return role.getRoleName() + pluralName;
        } else {
            return Naming.withoutCapital(pluralName);
           
        }

    }

    @Override
    public boolean isSettable() {
        return !hasMultipleTarget() && role.isSettable();
    }

    @Override
    public boolean isFinal() {
        return !isSettable() && !targetType().isRemovable();
    }

    @Override
    public boolean isNavigable() {
        return role.isNavigable();
    }

    @Override
    public Relation inverse() {
        if (role.isQualified()) {
            return new QualifiedIdRelation(role.getParent().getObjectType(), role, role.getParent().detectQualifiers(role));
        } else if (role.getParent() instanceof ElementsFactType) {
            ElementsFactType ft = (ElementsFactType) role.getParent();
            return new CollectionIdRelation(ft.getCollectionType(), ft.counterpart(role));
        } else {
            return new IdRelation(role.getParent().getObjectType(), role);
        }
    }

    @Override
    public String asAttribute() {
        if (hasMultipleTarget()) {
            return "- " + "coll" + Naming.withCapital(name()) + " : " + new CT(CollectionKind.COLL, targetType());
        } else {
            return "- " + name() + " : " + targetType().getName();
        }
    }

//    @Override
//    public String roleName() {
//        return role.detectRoleName();
//        
//    }
    @Override
    public boolean isAdjustable() {
        return false;
    }

    @Override
    public boolean isPartOfId() {
        return false;
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
        String roleName = role.detectRoleName();
        String fieldName;
        if (hasMultipleTarget()) {
            fieldName = role.getParent().getPlural();
        } else {
            fieldName = role.getParent().getName();
        }
        if (owner.isDoubleRelatedWith(role.getParent().getObjectType())) {
            fieldName = roleName + fieldName;
        } else {
            fieldName = Naming.withoutCapital(fieldName);
        }
        return fieldName;
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

        return role.getParent().size() > 2;
    }

    public String getAutoIncrField() {
        return role.getParent().getAutoIncr();
    }
}
