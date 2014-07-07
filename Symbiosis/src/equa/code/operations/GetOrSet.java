/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public class GetOrSet implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean present;
    private AccessModifier access;
    private String spec;

    /**
     * Constructor. present=true and the access modifier is public by default.
     */
    public GetOrSet() {
        this.present = true;
        this.access = AccessModifier.PUBLIC;
        this.spec = "";
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getSpec() {
        return spec;

    }

    /**
     * if present=true then GetOrSet is active otherwise inactive
     *
     * @param present
     */
    public void setPresent(boolean present) {
        this.present = present;
    }

    /**
     *
     * @param access modifier, see: {@link AccessModifier}.
     */
    public void setAccessModifier(AccessModifier access) {
        this.access = access;
    }

    /**
     *
     * @return if the GetOrSet is active (true) or inactive (false).
     */
    public boolean isPresent() {
        return present;
    }

    /**
     *
     * @return the access modifier, see: {@link AccessModifier}.
     */
    public AccessModifier getAccessModifier() {
        return access;
    }

    /**
     *
     * @return the abbreviation of the access modifier
     */
    public String getAccessString() {
        return access.getAbbreviation();
    }

    /**
     *
     * @return if the access modifier is not public, the abbreviation of the
     * access modifier is returned.
     */
    public String getAccessStringNonPublic() {
        if (access.equals(AccessModifier.PUBLIC)) {
            return "";
        } else {
            return access.getAbbreviation();
        }
    }
}
