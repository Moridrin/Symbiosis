package equa.actioncase;

import java.util.List;

import equa.code.operations.IActionOperation;

/**
 *
 * @author frankpeeters
 * @param <T>
 */
public abstract class ActorInput<T extends IActionOperation> {

    /**
     *
     * @param operation
     * @return the required input items of this operation
     */
    public List<ActorInputItem> inputItems(T operation) {
        return operation.inputItems();
    }

    /**
     *
     * @param operation
     * @return true if all input item results are valid, otherwise false
     */
    public boolean isValid(T operation) {
        for (ActorInputItem item : inputItems(operation)) {
            if (!item.isValid()) {
                return false;
            }
        }
        return true;
    }

}
