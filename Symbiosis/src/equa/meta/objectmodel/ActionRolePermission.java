/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.ActionRequirement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public abstract class ActionRolePermission extends ActionPermission implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ObjectRole role;
    private final List<ActionPrecondition> preconditions;

    public ActionRolePermission(ObjectRole role, ActionRequirement source) {
        super(role.getParent(), source);
        preconditions = new ArrayList<>();
        this.role = role;
    }

    public ObjectRole getRole() {
        return role;
    }

    @Override
    public FactType getFactType() {
        return role.getParent();
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        super.remove();

//        for (Source source : sources()) {
//            if (source instanceof Requirement) {
//                ((Requirement) source).remove();
//            }
//        }

    }

}
