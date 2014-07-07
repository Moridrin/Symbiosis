package equa.meta.objectmodel;

import java.io.Serializable;
import java.util.ArrayList;

import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;
import equa.meta.traceability.Source;

public abstract class Value extends ModelElement implements Serializable, Comparable<Value> {

    private static final long serialVersionUID = 1L;

    public Value() {
    }

    public Value(ParentElement parent, Source source) {
        super(parent, source);
    }

    public Value(ParentElement parent) {
        super(parent, new ArrayList<Source>());
    }

    /**
     *
     * @return the type of this value
     */
    public abstract Type getType();

    @Override
    public int compareTo(Value o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public boolean isManuallyCreated() {
        return false;
    }

}
