/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.traceability;

import java.util.ArrayList;

/**
 *
 * @author frankpeeters
 */
public abstract class ParentElement extends ModelElement {

    private static final long serialVersionUID = 1L;

    public ParentElement(ParentElement parent, Source source) {
        super(parent, source);
    }

    public ParentElement(ParentElement parent) {
        super(parent, new ArrayList<Source>());
    }

    public ParentElement() {
        this(null);
    }

    public abstract void remove(ModelElement member);
    
    @Override
    public boolean isManuallyCreated() {
        return false;
    }

}
