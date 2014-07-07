package equa.meta.objectmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import equa.code.operations.IPredicate;
import equa.meta.requirements.RuleRequirement;

/**
 *
 * @author FrankP
 */
@Entity
public class TupleConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private ArrayList<Role> roles;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private IPredicate predicate;

    public TupleConstraint() {
    }

    /**
     *
     * @param roles is not empty and all roles concern same facttype
     * @param predicate
     * @param source
     */
    public TupleConstraint(Set<Role> roles, IPredicate predicate, RuleRequirement source) {
        super(roles.iterator().next().getParent(), source);

        check(roles);
        this.roles = new ArrayList<>(2);
        this.roles.addAll(roles);
        this.predicate = predicate;
    }

    /**
     *
     * @return an iterator over the concerning roles of this constraint
     */
    public Iterator<Role> roles() {
        return roles.iterator();
    }

    /**
     *
     * @return the predicate of this constraint
     */
    public IPredicate getPredicate() {
        return this.predicate;
    }

    /**
     * changing of the predicate of this constraint
     *
     * @param predicate
     */
    public void setPredicate(IPredicate predicate) {
        this.predicate = predicate;
    }

    private void check(Set<Role> roles) {
        if (roles.isEmpty()) {
            throw new RuntimeException("roles may not be empty");
        }

        Iterator<Role> it = roles.iterator();
        FactType ft = it.next().getParent();
        while (it.hasNext()) {
            if (it.next().getParent() != ft) {
                throw new RuntimeException("all roles concern same facttype");
            }
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAbbreviationCode() {
        return "t";
    }

    @Override
    public FactType getFactType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getRequirementText() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isRealized() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
