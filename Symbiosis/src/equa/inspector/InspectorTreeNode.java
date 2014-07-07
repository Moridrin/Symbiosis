/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.inspector;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/**
 *
 * @author FrankP based on demo of custom TreeModels in Core Java - Volume II
 */
public class InspectorTreeNode {

    private Class<?> type;
    private String name;
    private Object value;
    private ArrayList<SubNode> items;
    private static boolean extended = false;

    public static void setExtended(boolean extended) {
        InspectorTreeNode.extended = extended;
    }

    public InspectorTreeNode(Class<?> type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.items = new ArrayList<>();

        // find all object fields; we don't expand strings and null values
        if (type.isPrimitive() || type.equals(String.class)
                || value == null) {
            return;
        }

        if (value instanceof Object[]) {
            Class<?> componentType = type.getComponentType();
            scanArrayItems((Object[]) value, componentType);

        } else if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            scanArrayItems(map.entrySet().toArray(), Object.class);

        } else {
            Class<?> c = value.getClass();
            if (extended) {
                while (c != null) {
                    pickUpFields(c, value);
                    c = c.getSuperclass();
                }
            } else {
                if (c != null) {
                    pickUpFields(c, value);
                }
            }
        }
    }

    private void pickUpFields(Class<?> c, Object value) throws SecurityException {
        Field[] fs = c.getDeclaredFields();
        AccessibleObject.setAccessible(fs, true);
        // get all nonstatic fields
        for (Field f : fs) {
            if ((f.getModifiers() & Modifier.STATIC) == 0) {
                items.add(new ObjectNode(f, value));
            }
        }
    }

    private void scanArrayItems(Object[] array, Class<?> componentType) {
        for (int i = 0; i < array.length && array[i] != null; i++) {
            items.add(new ArrayNode(componentType, i + " " + array[i].toString(), array[i]));
        }
    }

    /**
     *
     * @return the value of this node
     */
    public Object getValue() {
        return value;
    }

    /**
     *
     * @param nr
     * @return the field with index nr; if this index doesn't exist, null will
     * be returned
     */
    public SubNode getItem(int nr) {
        if (nr < 0 || nr >= items.size()) {
            return null;
        }
        return items.get(nr);
    }

    /**
     *
     * @return the number of non-static fields
     */
    public int getItemCount() {
        return items.size();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(name);
        if (value == null) {
            str.append(" = undefined");
        } else if (type.equals(Calendar.class)) {
            str.append(
                    " = ");
            str.append(
                    ((Calendar) value).getTime().toString());
        } else if (type.isPrimitive() || type.equals(String.class)) {
            str.append(
                    " = ");
            str.append(value.toString());
        }
        return str.toString();
    }
}
