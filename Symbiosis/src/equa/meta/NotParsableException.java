/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta;

import equa.meta.objectmodel.TypeExpression;

/**
 *
 * @author FrankP
 */
public class NotParsableException extends MismatchException {

    private static final long serialVersionUID = 1L;

    public NotParsableException(TypeExpression te, String message) {
        super(te, message);
    }

    public NotParsableException(TypeExpression te, String message, int mismatchPos) {
        super(te, message, mismatchPos);
    }
}
