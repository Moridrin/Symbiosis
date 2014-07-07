package equa.meta.objectmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import equa.meta.requirements.RuleRequirement;

/**
 *
 * @author FrankP
 */
@Entity
public abstract class SetConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<Role> from;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<Role> towards;

    public SetConstraint() {
    }

    /**
     * from.size == towards.size AND from.size > 0 AND from-roles belong to the
     * same facttype and are all different AND towards-roles belong to the same
     * facttype and are all different AND the corresponding roles of from and
     * toward have the same subsititutiontype OR the substitutiontype of the
     * towards-role is a supertype of it
     *
     * @param from
     * @param towards
     * @param source
     */
    public SetConstraint(List<Role> from, List<Role> towards, RuleRequirement source) {
        super(from.get(0).getParent(), source);

        check(from, towards);

        this.from = from;
        this.towards = towards;
    }

    /**
     *
     * @return an iterator over the departing roles of this setconstraint; the
     * sequence of this iterator corresponds with the sequence of the
     * towardsRoles-iterator
     */
    public Iterator<Role> fromRoles() {
        return this.from.iterator();
    }

    /**
     *
     * @return an iterator over the arriving roles of this setcontraint; the
     * sequence of this iterator corresponds with the sequence of the
     * fromRoles-iterator
     */
    public Iterator<Role> towardsRoles() {
        return this.towards.iterator();
    }

    private void check(List<Role> from, List<Role> towards) {
        /* from.size == towards.size AND
         * from.size > 0 AND
         * from-roles belong to the same facttype and are all different AND
         * towards-roles belong to the same facttype and are all different AND
         * the corresponding roles of from and toward have the same subsititutiontype OR
         * the substitutiontype of the towards-role is a supertype of it
         * */
        if (from.size() != towards.size()) {
            throw new RuntimeException("from-roles and towards-role must have same size");
        }
        if (from.isEmpty()) {
            throw new RuntimeException("from-role may not be empty");
        }
        checkSameFactTypeAndDifference(from);
        checkSameFactTypeAndDifference(towards);
        checkMatch(from, towards);
    }

    /**
     * roles belong to the same facttype and are all different
     *
     * @param roles
     */
    private void checkSameFactTypeAndDifference(List<Role> roles)
            throws RuntimeException {
        ArrayList<Role> set = new ArrayList<>();
        Iterator<Role> it = roles.iterator();
        set.add(it.next());
        while (it.hasNext()) {
            Role role = it.next();
            if (set.contains(role)) {
                throw new RuntimeException("roles must be different");
            }
            set.add(role);
        }
    }

    /**
     * the corresponding roles of from and toward have the same
     * subsititutiontype OR the substitutiontype of the towards-role is a
     * supertype of it
     *
     */
    private void checkMatch(List<Role> from, List<Role> towards)
            throws RuntimeException {
        for (int i = 0; i < from.size(); i++) {
            ObjectType fr = (ObjectType) from.get(i).getSubstitutionType();
            ObjectType tw = (ObjectType) towards.get(i).getSubstitutionType();
            if (fr != tw || !fr.hasSuperType(tw)) {
                throw new RuntimeException("role " + i + " doesn't match");
            }
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
