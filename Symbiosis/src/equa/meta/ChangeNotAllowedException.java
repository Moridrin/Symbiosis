package equa.meta;

/**
 *
 * @author FrankP
 */
public class ChangeNotAllowedException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param message
     */
    public ChangeNotAllowedException(String message) {
        super(message);
    }
}
