/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta;

/**
 *
 * @author FrankP
 */
public class IncorrectNameException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param message
     */
    public IncorrectNameException(String message) {
        super(message);
    }
}
