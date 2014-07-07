package equa.meta;

import equa.meta.objectmodel.TypeExpression;

/**
 *
 * @author FrankP
 */
public class MismatchException extends Exception {

    private static final long serialVersionUID = 1L;
    private final int mismatchPosition;
    private final TypeExpression te;
    

    /**
     *
     * @param message
     */
    public MismatchException(TypeExpression te, String message) {
        super(message);
        this.te = te;
        mismatchPosition = -1;
    }

    public MismatchException(TypeExpression te,String message, int mismatchPosition) {
        super(message);
        this.te =te;
        this.mismatchPosition = mismatchPosition;
    }

    public int getMismatchPosition() {
        return mismatchPosition;
    }
    
    public TypeExpression getTypeExpression() {
        return te;
    }
}
