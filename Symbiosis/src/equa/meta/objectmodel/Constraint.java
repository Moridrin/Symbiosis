/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.requirements.Requirement;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;

/**
 *
 * @author frankpeeters
 */
public abstract class Constraint extends ModelElement implements ObjectModelRealization {

    private static final long serialVersionUID = 1L;

    public Constraint() {
    }

    public Constraint(ParentElement parent, Requirement rule) {
        super(parent, rule);
    }

    /**
     *
     * most of the constraints are coded with an abbreviation followed by a
     * number;
     *
     * @return the abbreviationcode of the concrete constraint; if no
     * abbreviationcode exists null will be returned
     */
    public abstract String getAbbreviationCode();

    public String getDescription() {
        return getName();
    }

    /**
     *
     * @return true if this constraint does have a formal definition, otherwise
     * false
     */
    public abstract boolean isRealized();

    @Override
    public boolean isManuallyCreated() {
        return false;
    }
}
