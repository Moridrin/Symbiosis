/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta;

/**
 * Text messages in {@link Model}s; a Message instance could be perceived as an
 * error message. <br>
 * This class only contains unconstrained R[etrieve] operations, deriving a
 * typical behavior of a Data Transfer Object (DTO). Thus, no business logic is
 * involved.
 *
 * @author frankpeeters
 */
public class Message {

    private String text;
    private boolean isError;

    /**
     * CONSTRUCTOR; prepares all the attributes of a Message.
     *
     * @param text in this Message.
     * @param isError classification for this Message.
     */
    public Message(String text, boolean isError) {
        this.text = text;
        this.isError = isError;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return text in this Message.
     */
    public String getText() {
        return text;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return true if this Message is classified as an error message.
     */
    public boolean isError() {
        return isError;
    }

}
