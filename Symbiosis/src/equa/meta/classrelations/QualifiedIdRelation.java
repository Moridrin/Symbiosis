/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import java.util.List;

import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;

/**
 *
 * @author frankpeeters
 */
public class QualifiedIdRelation extends IdRelation {

    private static final long serialVersionUID = 1L;
    private List<Role> qualifiers;

    public QualifiedIdRelation(ObjectType owner, Role role, List<Role> qualifiers) {
        super(owner, role);
        this.qualifiers = qualifiers;
    }

    /**
     *
     * @return a list of concerning roles with respect to this qualified
     * relation
     */
    public List<Role> qualifiers() {
        return qualifiers;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

}
