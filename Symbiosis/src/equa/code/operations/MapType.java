/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.meta.objectmodel.Type;

/**
 *
 * @author frankpeeters
 */
public class MapType extends CT {

    private static final long serialVersionUID = 1L;
    private STorCT keyType;

    public MapType(STorCT keyType, STorCT valueType) {
        super(CollectionKind.MAP, valueType);
        this.keyType = keyType;
    }

    public STorCT getKeyType() {
        return keyType;
    }

    public STorCT getValueType() {
        return getType();
    }

    @Override
    public int compareTo(Type t) {
        if (!(t instanceof MapType)) {
            return +1;
        }

        MapType ht = (MapType) t;
        if (keyType == ht.keyType && getValueType() == ht.getValueType()) {
            return 0;
        } else {
            return +1;
        }
    }

    @Override
    public String getName() {
        return getKind().name() + "<" + getKeyType().getName()
                + "," + getValueType().getName() + ">";
    }
}
