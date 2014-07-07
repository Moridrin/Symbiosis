package equa.meta.objectmodel;

import java.io.Serializable;

public interface Type extends Comparable<Type>, Serializable {

    /**
     *
     * @return the name of this type
     */
    String getName();

    /**
     *
     * @return true if this type possesses a FTE and this type is not artificial
     * or generated.
     */
    boolean isPureFactType();
}
