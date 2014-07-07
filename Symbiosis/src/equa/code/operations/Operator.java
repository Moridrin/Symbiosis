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
public enum Operator {

    PLUS("+"),
    MINUS("-"),
    SMALLER("<"),
    SMALLER_OR_EQUAL("<="),
    GREATER_OR_EQUAL(">=");

    private final String sign;

    Operator(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }
}
