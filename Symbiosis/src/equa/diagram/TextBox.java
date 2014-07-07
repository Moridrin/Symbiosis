/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram;

import com.mxgraph.model.mxCell;

/**
 *
 * @author frankpeeters
 */
public class TextBox extends mxCell {
    // fields

    private static final long serialVersionUID = 1L;

    public TextBox(String text) {
        setValue(text);
        setVertex(true);
    }
}
