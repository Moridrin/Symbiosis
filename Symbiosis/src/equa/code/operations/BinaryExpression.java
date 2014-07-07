/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.code.Language;

/**
 * warning: non type safe binaryu expression
 *
 * @author frankpeeters
 */
public class BinaryExpression implements ActualParam {

    private static final long serialVersionUID = 1L;
    private ActualParam operand1;
    private ActualParam operand2;
    private Operator operator;

    public BinaryExpression(ActualParam operand1, Operator operator, ActualParam operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    @Override
    public String expressIn(Language l) {
        return operand1.expressIn(l) + l.operator(operator) + operand2.expressIn(l);
    }

    @Override
    public String callString() {
        return operand1.callString() + operator.getSign() + operand2.callString();
    }

    public ActualParam getOperand1() {
        return operand1;
    }

    public ActualParam getOperand2() {
        return operand2;
    }

    public Operator getOperator() {
        return operator;
    }

}
