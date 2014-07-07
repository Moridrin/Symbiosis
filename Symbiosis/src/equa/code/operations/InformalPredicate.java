package equa.code.operations;

public class InformalPredicate implements IPredicate {

    private static final long serialVersionUID = 1L;
    private String text;

    /**
     * Constructor of informal specification.
     *
     * @param text
     */
    public InformalPredicate(String text) {
        this.text = text;
    }

    /**
     *
     * @return text of this informal specification.
     */
    @Override
    public String toString() {
        return text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String returnValue() {
        return text;
    }
}
