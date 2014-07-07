/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.code.Language;
import equa.meta.objectmodel.BaseType;

/**
 *
 * @author frankpeeters
 */
public class ValueString implements ActualParam {

    private static final long serialVersionUID = 1L;
    private final String value;
    private final BaseType type;

    public ValueString(String value, BaseType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String expressIn(Language l) {
        if (type.equals(BaseType.STRING)) {
            return l.stringSymbol() + value + l.stringSymbol();
        } else {
            return value;
        }
    }

    @Override
    public String callString() {
        return value;
    }

    public BaseType getType() {
        return type;
    }

}
