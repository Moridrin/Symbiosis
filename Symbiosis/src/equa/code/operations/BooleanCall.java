/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.Iterator;
import java.util.List;

import equa.code.Language;
import equa.meta.requirements.RuleRequirement;

/**
 *
 * @author frankpeeters
 */
public class BooleanCall extends Call implements IFormalPredicate {

    private static final long serialVersionUID = 1L;
    private final boolean negation;

    public BooleanCall(IBooleanOperation operation, List<? extends ActualParam> actualParams,
            boolean negation) {
        super((Operation) operation, actualParams);
        this.negation = negation;
    }

    public BooleanCall(IBooleanOperation operation, boolean negation) {
        super((Operation) operation);
        this.negation = negation;
    }

    public String violationMessage() {
        StringBuilder sb = new StringBuilder();

        if (negation) {
            sb.append("[");
        } else {
            sb.append("Rule [");
        }

        RuleRequirement rule;
        IBooleanOperation operation = (IBooleanOperation) getOperation();
        rule = operation.getRuleRequirement();
        sb.append(rule.getText());

        if (negation) {
            sb.append("] is not allowed.");
        } else {
            sb.append("] has been violated.");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return returnValue();
    }

    public String withoutNegationString() {
        return getOperation().callString(getActualParams());
    }

    public BooleanCall withNegation() {
        return new BooleanCall((IBooleanOperation) getOperation(), getActualParams(), !negation);
    }

    @Override
    public boolean isNegated() {
        return negation;
    }

    @Override
    public String returnValue() {
        StringBuilder sb = new StringBuilder();
        if (negation) {
            sb.append("NOT (");
        }
        sb.append(getOperation().callString(getActualParams()));

        if (negation) {
            sb.append(")");
        }
        return sb.toString();

    }

    @Override
    public Iterator<BooleanCall> operands() {

        return new Iterator<BooleanCall>() {

            private BooleanCall iterator = BooleanCall.this;

            @Override
            public boolean hasNext() {
                return iterator != null;
            }

            @Override
            public BooleanCall next() {
                iterator = null;
                return BooleanCall.this;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public DisjunctionCall disjunctionWith(BooleanCall call) {
        return new DisjunctionCall(this, call);
    }

    @Override
    public ConjunctionCall conjunctionWith(BooleanCall call) {
        return new ConjunctionCall(this, call);
    }

    @Override
    public String expressIn(Language l) {
        String s = super.expressIn(l);
        if (negation) {
            return l.negate(s);
        } else {
            return s;
        }
    }

}
