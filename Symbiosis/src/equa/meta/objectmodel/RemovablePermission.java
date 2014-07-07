/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.ActionRequirement;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class RemovablePermission extends ActionRolePermission {

    private static final long serialVersionUID = 1L;

    RemovablePermission(ObjectRole role, ActionRequirement source) {
        super(role, source);
    }

    @Override
    public String getName() {
         FactType ft = (FactType) getParent();
        ObjectType ot = getRole().getSubstitutionType();
        return ot.getName() + ".remove." + ft.getName();
    }

    @Override
    public void remove() throws ChangeNotAllowedException {

        getRole().deleteRemovable();
        super.remove();
    }

    @Override
    public String getAbbreviationCode() {
        return "rem";
    }

    @Override
    public boolean equals(Object member) {
        if (member instanceof RemovablePermission) {
            return getParent().equals(((RemovablePermission) member).getParent());
        } else {
            return false;
        }
    }

    @Override
    public String getRequirementText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Some actor of the system must get the opportunity to remove ");
        FactType ft = getFactType();
        if (ft.isGenerated()) {
            ObjectRole role = (ObjectRole) ft.counterpart(getRole());
            ObjectType registeredOT = role.getSubstitutionType();
            ObjectType unmanagedSubType = registeredOT.getUnmanagedSubType();
            if (unmanagedSubType != null) {
                registeredOT = unmanagedSubType;
            }
            sb.append("a <").append(registeredOT.getName()).append(">");

        } else {
            if (ft.isObjectType()) {
                sb.append("a ");
                sb.append(getFactType().getFactTypeString());
            } else if (ft instanceof ElementsFactType) {
                sb.append("a <");
                sb.append(((ElementsFactType)ft).getCollectionType().getName());
                sb.append(">");
            } else {
                sb.append("a fact about ");
                sb.append(getFactType().getFactTypeString());
            }
        }
        sb.append(".");
        return sb.toString();
    }

    @Override
    public boolean isRealized() {
        return true;
    }

}
