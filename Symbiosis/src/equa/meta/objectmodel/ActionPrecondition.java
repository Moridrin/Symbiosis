/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RuleRequirement;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class ActionPrecondition extends Constraint {

    private static final long serialVersionUID = 1L;

    private final ActionPermission target;
    private final FactType condition;
    private final ObjectRole objectRole;  // role of unaryFactType and objectRole do have the same objectType 
    private boolean negation;

    /**
     *
     * @param target
     * @param name
     * @param source
     */
    ActionPrecondition(ActionPermission target, FactType condition, ObjectRole objectRole, RuleRequirement source)
            throws ChangeNotAllowedException {
        super(target.getFactType(), source);
        if (condition.size() != 1) {
            throw new ChangeNotAllowedException("condition should refer to unary fact type");
        }
        if (!condition.getRole(0).getSubstitutionType().equals(objectRole.getSubstitutionType())) {
            throw new ChangeNotAllowedException("role and condition do not refer to the same object type");
        }
        this.target = target;
        this.condition = condition;
        this.objectRole = objectRole;
        this.negation = false;
    }

    public ObjectRole getObjectRole() {
        return objectRole;
    }
    
    public FactType getCondition() {
        return condition;
    }

    public boolean isNegation() {
        return negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }

    public ActionPermission getTarget() {
        return target;
    }

    @Override
    public String getName() {
        String name = condition.getName();
        name = objectRole.detectRoleName() + "." + name;
        if (negation) {
            name = "!" + name;
        }
        return name;
    }

    @Override
    public String getAbbreviationCode() {
        String code = Naming.withoutVowels(objectRole.detectRoleName() + "." + condition.getName());

        if (negation) {
            code = "!" + code;
        }
        return code;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ActionPrecondition) {
            ActionPrecondition pre = (ActionPrecondition) object;
            return condition == pre.condition && objectRole == pre.objectRole;
        } else {
            return false;
        }
    }

    @Override
    public String getRequirementText() {
        return ((Requirement) this.sources().get(0)).getText();
    }

    @Override
    public boolean isRealized() {
        DerivableConstraint dc = condition.getDerivableConstraint();
        return dc == null || dc.isRealized();
    }

    @Override
    public FactType getFactType() {
        return target.getFactType();
    }

}
