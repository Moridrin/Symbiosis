/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.ActionRequirement;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ParentElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public abstract class ActionPermission extends Constraint {

    private static final long serialVersionUID = 1L;

    private final List<ActionPrecondition> preconditions;

    public ActionPermission(ParentElement parent, ActionRequirement source) {
        super(parent, source);
        preconditions = new ArrayList<>();
    }

    public List<ActionPrecondition> preconditions() {
        return Collections.unmodifiableList(preconditions);
    }

    public void addPrecondition(FactType condition, ObjectRole role, boolean negation, RuleRequirement rule) throws ChangeNotAllowedException {
        for (ActionPrecondition pre : preconditions) {
            if (pre.getCondition().equals(condition) && pre.getObjectRole().equals(role)) {
                return;
            }
        }
        ActionPrecondition pre = new ActionPrecondition(this, condition, role, rule);
        pre.setNegation(negation);
        preconditions.add(pre);
    }

    public void removePrecondition(FactType condition, ObjectRole role) {
        ActionPrecondition toRemove = null;
        for (ActionPrecondition pre : preconditions) {
            if (pre.getCondition().equals(condition) && pre.getObjectRole().equals(role)) {
                toRemove = pre;
            }
        }
        if (toRemove != null) {
            preconditions.remove(toRemove);
        }
    }

    @Override
    public boolean isRealized() {
        return true;
    }

}
