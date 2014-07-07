/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;
import equa.meta.requirements.RuleRequirement;

/**
 *
 * @author frankpeeters
 */
public class BooleanProperty extends Property implements IBooleanOperation {

    private static final long serialVersionUID = 1L;
    private final RuleRequirement rule;

    public BooleanProperty(Relation relation, ObjectType ot, RuleRequirement rule) {
        super(relation, ot);
        this.rule = rule;
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        return rule;
    }

    @Override
    public String returnValue() {
        return getReturnType().getSpec();
    }

    @Override
    public BooleanCall call() {
        return new BooleanCall(this, false);
    }

}
