/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import equa.meta.objectmodel.Type;

/**
 *
 * @author FrankP
 */
public abstract class ValueNode extends ExpressionNode implements Comparable<ValueNode> {

    private static final long serialVersionUID = 1L;
    private String typeName;

    public ValueNode(ParentNode parent, String typeName) {
        super(parent);
        setTypeName(typeName);
    }

    public String getTypeName() {
        return typeName;
    }

    final void setTypeName(String typeName) {
        String typeNameTrimmed = typeName.trim();

        if (typeNameTrimmed.isEmpty()) {
            throw new RuntimeException("type name cannot be empty");
        }
        this.typeName = typeNameTrimmed;
    }

    public abstract Type getDefinedType();

    public abstract String getTypeDescription();

    @Override
    public int compareTo(ValueNode o) {
        return this.toString().compareTo(o.toString());
    }

}
