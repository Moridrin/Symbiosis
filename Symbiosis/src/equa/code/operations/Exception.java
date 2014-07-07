package equa.code.operations;

import java.io.Serializable;

public class Exception implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private IPredicate triggerSpec;

    /**
     *
     * @param name
     */
    public Exception(String name) {
        this.name = name;
        this.triggerSpec = new InformalPredicate("");
    }

    public String getName() {
        return this.name;
    }

    /**
     *
     * @return specification of the cause of the exception
     */
    public IPredicate getTriggerSpec() {
        return this.triggerSpec;
    }

    /**
     * setting of the specification of the cause of the exception
     *
     * @param triggerSpec
     */
    public void setTriggerSpec(IPredicate triggerSpec) {
        this.triggerSpec = triggerSpec;
    }
}
