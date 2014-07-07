/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.Iterator;

/**
 *
 * @author frankpeeters
 */
public abstract class CompoundCall implements IFormalPredicate {

    private static final long serialVersionUID = 1L;

    protected final IFormalPredicate operand1, operand2;

    public CompoundCall(IFormalPredicate operand1, IFormalPredicate operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public String toString() {
        return returnValue();
    }

    @Override
    public Iterator<BooleanCall> operands() {
        return new Iterator<BooleanCall>() {

            private Iterator<BooleanCall> iterator = operand1.operands();
            private boolean usesOperand1 = true;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public BooleanCall next() {
                BooleanCall result = iterator.next();
                if (!iterator.hasNext() && usesOperand1) {
                    usesOperand1 = false;
                    iterator = operand2.operands();
                }
                return result;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public boolean isNegated() {
        return false;
    }

    @Override
    public DisjunctionCall disjunctionWith(BooleanCall call) {
        return new DisjunctionCall(this, call);

    }

    @Override
    public ConjunctionCall conjunctionWith(BooleanCall call) {
        return new ConjunctionCall(this, call);
    }
}
