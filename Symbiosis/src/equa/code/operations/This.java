/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.code.Language;

/**
 *
 * @author frankpeeters
 */
public class This implements ActualParam {

    private static final long serialVersionUID = 1L;

    @Override
    public String expressIn(Language l) {
        return l.thisKeyword();
    }

    @Override
    public String callString() {
        return "this";
    }

}
