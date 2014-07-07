/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.traceability.ParentElement;
import equa.meta.traceability.Source;

/**
 *
 * @author frankpeeters
 */
public class AbstractValue extends Value {

    private static final long serialVersionUID = 1L;
    private final ObjectType type;
    private final String text;

    public AbstractValue(ParentElement parent, Source source, ObjectType abstract_type, String text) {
        super(parent, source);
        this.type = abstract_type;
        this.text = text;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public String getName() {
        return type.getName() + "_abstract";
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof AbstractValue) {
            return ((AbstractValue) object).text.equals(text);
        } else {
            return false;
        }
    }
}
