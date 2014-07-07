/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta;

/**
 *
 * @author frankpeeters
 */
public class ReflexiveException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param message
     */
    public ReflexiveException(String message) {
        super(message);
    }
}
