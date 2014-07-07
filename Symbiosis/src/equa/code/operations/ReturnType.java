package equa.code.operations;

import java.io.Serializable;

public class ReturnType implements Serializable {

    private static final long serialVersionUID = 1L;
    private STorCT type;
    private String spec;

    /**
     *
     * @param type
     */
    public ReturnType(STorCT type) {
        this.type = type;
        this.spec = "";
    }

    /**
     *
     * @return the type of the return value
     */
    public STorCT getType() {
        return type;
    }

    /**
     * setting of the type of the return value
     *
     * @param type
     */
    public void setType(STorCT type) {
        this.type = type;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getSpec() {
        return spec;

    }

    @Override
    public String toString() {
        if (type == null) {
            return "void";
        } else {
            return type.toString();
        }
    }
}
