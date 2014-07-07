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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AdjustablePermission extends ActionRolePermission {

    private static final long serialVersionUID = 1L;

    AdjustablePermission(ObjectRole role, ActionRequirement source) {
        super(role, source);
    }

    @Override
    public String getName() {
        FactType ft = (FactType) getParent();
        ObjectType ot = getRole().getSubstitutionType();
        return ot.getName() + ".mayUpdate." + ft.getName();
    }

    @Override
    public void remove() throws ChangeNotAllowedException {

        getRole().deleteAdjustable();
        super.remove();

    }

    @Override
    public String getAbbreviationCode() {
        return "upd";
    }

    @Override
    public boolean equals(Object member) {
        if (member instanceof AdjustablePermission) {
            return getParent().equals(((AdjustablePermission) member).getParent());
        } else {
            return false;
        }
    }

    @Override
    public String getRequirementText() {
        return "Some actor of the system must get the opportunity to adjust a fact about " + getFactType().getFactTypeString();
    }

    @Override
    public boolean isRealized() {
        return true;
    }
}
