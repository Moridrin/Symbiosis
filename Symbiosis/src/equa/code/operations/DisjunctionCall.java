/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

/**
 *
 * @author frankpeeters
 */
public class DisjunctionCall extends CompoundCall {

    private static final long serialVersionUID = 1L;

    public DisjunctionCall(IFormalPredicate operand1, IFormalPredicate operand2) {
        super(operand1, operand2);
    }

    @Override
    public String returnValue() {
        return operand1.returnValue() + " OR " + operand2.returnValue();
    }

}
