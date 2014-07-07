/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.inspector;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author FrankP
 */
public class ObjectNode implements SubNode {

    private Field field;
    private Object parent;

    public ObjectNode(Field f, Object parent) {
        field = f;
        this.parent = parent;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Object getValue() {
        try {
            return field.get(parent);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ObjectNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
