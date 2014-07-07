package equa.code.operations;

import equa.code.Language;

/**
 *
 * @author frankpeeters
 */
public class Null implements ActualParam {

    private static final long serialVersionUID = 1L;

    public static final Null NULL = new Null();

    private Null() {
    }

    @Override
    public String expressIn(Language l) {
        return "null";
    }

    @Override
    public String callString() {
        return "null";
    }

}
