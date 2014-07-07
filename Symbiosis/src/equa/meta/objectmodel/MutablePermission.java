/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.ActionRequirement;
import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public class MutablePermission extends ActionPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    MutablePermission(ObjectType ot, ActionRequirement source) {
        super(ot, source);
    }

    @Override
    public String getName() {
        return getParent().getName() + ".mut";
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        super.remove();
        ((ObjectType) getParent()).deleteMutable();
    }

    @Override
    public String getAbbreviationCode() {
        return "mut";
    }

    @Override
    public boolean equals(Object member) {
        if (member instanceof MutablePermission) {
            return getParent().equals(((MutablePermission) member).getParent());
        } else {
            return false;
        }
    }

    @Override
    public String getRequirementText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Some actor of the system must get the opportunity to change the id of a <");
        sb.append(getParent().getName()).append(">.");
        return sb.toString();
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public FactType getFactType() {
        return ((ObjectType) getParent()).getFactType();
    }
}
