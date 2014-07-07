/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.inspector;

/**
 *
 * @author FrankP
 */
public class ArrayNode implements SubNode {

    private Class<?> type;
    private String name;
    private Object value;

    public ArrayNode(Class<?> type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
