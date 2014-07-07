package equa.actioncase;

import java.util.List;

import equa.code.operations.BooleanCall;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;

/**
 *
 * @author frankpeeters
 */
public class ActorInputItem {

    private BaseType type;
    private String enteredValue;
    private ObjectType parentType;
    private String toolTip;

    public ActorInputItem(String name, BaseType type, String toolTip) {
        this.type = type;
        this.toolTip = toolTip;
    }

    public ActorInputItem(String name, BaseType type, String toolTip, ObjectType parent) {
        this(name, type, toolTip);
        this.parentType = parent;
    }

    /**
     *
     * @return the entered input by the actor, if the input is stil not entered
     * null will be returned
     */
    public String getEnteredValue() {
        return enteredValue;
    }

    /**
     *
     * @param value the entered value in behalf of this input item
     */
    public void setEnteredValue(String value) {
        enteredValue = value;
    }

    /**
     *
     * @return the type of the input entered by the actor
     */
    public BaseType getType() {
        return type;
    }

    /**
     *
     * @return the object type where this input item belongs to; could be null
     * if input item is a stand alone base type
     */
    public ObjectType getObjectType() {
        return parentType;
    }

    /**
     *
     * @return the default value of this input item, if such default value does
     * not exist, the empty string will be returned
     */
    public String getDefaultValue() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return true if the entered value satisfies the validation rules of this
     * input item, otherwise false
     */
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return the validation rules which the entered value has to satisfy
     */
    public List<BooleanCall> validationRules() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return a description of the input item
     */
    public String getToolTip() {
        return toolTip;
    }
}
