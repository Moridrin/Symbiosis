/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public class Escape implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IFormalPredicate condition;
    private final IPredicate result;

    public Escape(IFormalPredicate condition, IPredicate result) {
        this.condition = condition;
        this.result = result;
    }

    public IFormalPredicate getCondition() {
        return condition;
    }

    public IPredicate getResult() {
        return result;
    }
}
