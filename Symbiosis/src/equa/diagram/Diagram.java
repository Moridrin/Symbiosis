/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram;

import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public abstract class Diagram implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;

    //for reasons of deserialization:
    protected Diagram() {
    }

    public Diagram(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
