package equa.actioncase;

import java.util.List;

import equa.code.operations.BooleanCall;
import equa.code.operations.IActionOperation;

/**
 *
 * @author frankpeeters
 * @param <T>
 */
public interface IActionCase<T extends IActionOperation> {

    /**
     * executes the action case; if @return.isEmpty() the postcondition will be
     * realized otherwise the state of the system will be unchanged
     *
     * @param input required information entered by actor; input.isValid()
     * @return the list with detected violations while executing this action
     * case
     */
    List<BooleanCall> execute(ActorInput<T> input);

    /**
     *
     * @return the name of this action case
     */
    String getName();

    /**
     *
     * @return a list with al possible violations within the normal flow of this
     * action case
     */
    List<BooleanCall> getViolations();

    /**
     *
     * @return the description of every ordered step in the normal flow of this
     * action case
     */
    List<String> getNormalFlow();

    @Override
    String toString();

//    /**
//     * 
//     * @return the postcondition of the normal flow
//     */
//    Predicate getPostCondition();
//    
//    /**
//     * precondition: withResult()
//     * @return the result of the execution of the normal flow; if any violation
//     * has been detected, null will be returned
//     */
//    ReturnType getResult();
//    
//    /**
//     * 
//     * @return true if Operation is defined with a return value, otherwise false 
//     */
//    boolean withResult();
}
