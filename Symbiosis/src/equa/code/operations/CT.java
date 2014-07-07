package equa.code.operations;

import java.util.ArrayList;
import java.util.List;

import equa.meta.objectmodel.Type;

public class CT implements STorCT {

    private static final long serialVersionUID = 1L;

    private final CollectionKind kind;
    private final STorCT type;

    /**
     * Constructor.
     *
     * @param kind of collection, see: {@link CollectionKind}.
     * @param type of object model element for a collection.
     */
    public CT(CollectionKind kind, STorCT type) {
        this.kind = kind;
        this.type = type;
    }

    /**
     *
     * @return kind of collection.
     */
    public CollectionKind getKind() {
        return this.kind;
    }

    /**
     *
     * @return type of object model element for a collection.
     */
    public STorCT getType() {
        return type;
    }

    /**
     *
     * @param t a type of object model element.
     * @return 1 value (if t is not a CollectionType), or kind comparison (if
     * types are equal) of type comparison (if types are not equal).
     */
    @Override
    public int compareTo(Type t) {
        if (!(t instanceof CT)) {
            return +1;
        }

        CT ct = (CT) t;
        if (type == ct.type) {
            return kind.compareTo(ct.kind);
        } else {
            return type.compareTo(ct.type);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     *
     * @return name of the kind followed by the name of the type.
     */
    @Override
    public String getName() {
        return kind.name() + "<" + type.getName() + ">";
    }

    @Override
    public boolean isPureFactType() {
        return false;
    }

    @Override
    public List<Param> transformToBaseTypes(Param param) {
        //interim "solution"
        List<Param> result = new ArrayList<>();
        result.add(param);
        return result;
    }
}
