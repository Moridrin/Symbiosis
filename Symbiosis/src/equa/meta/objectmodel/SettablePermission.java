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
public class SettablePermission extends ActionRolePermission {

    private static final long serialVersionUID = 1L;

    SettablePermission(ObjectRole role, ActionRequirement source) {
        super(role, source);
    }

    @Override
    public String getName() {
         FactType ft = (FactType) getParent();
        ObjectType ot = getRole().getSubstitutionType();
        return ot.getName() + ".maySet." + ft.getName();
    }

    @Override
    public void remove() throws ChangeNotAllowedException {

        getRole().deleteSettable();
        super.remove();

    }

    @Override
    public String getAbbreviationCode() {
        return "set";
    }

    @Override
    public boolean equals(Object member) {
        if (member instanceof SettablePermission) {
            return getParent().equals(((SettablePermission) member).getParent());
        } else {
            return false;
        }
    }

    @Override
    public String getRequirementText() {
        return "Some actor of the system must get the opportunity to enter or "
                + "change a fact about " + getFactType().getFactTypeString();
    }

    @Override
    public boolean isRealized() {
        return true;
    }
}
