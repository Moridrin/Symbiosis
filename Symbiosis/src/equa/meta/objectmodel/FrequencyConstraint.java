package equa.meta.objectmodel;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.RuleRequirement;

/**
 * (1 <= min < max) or (1 < min <= max)
 */
@Entity
public class FrequencyConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Role role;
    @Column(name = "minValue")
    private int min;
    @Column(name = "maxValue")
    private int max;

    public FrequencyConstraint() {
    }

    /**
     * the min and max frequency some SubstitutionValue must play with respect
     * to roles; (0 <= min <= max) and (2 <= max)
     *
     *
     * @param role
     * @param min
     * @param max
     * @param source
     */
    FrequencyConstraint(Role role, int min, int max, RuleRequirement source)
            throws ChangeNotAllowedException {
        super(role.getParent(), source);

        if (!((0 <= min && min <= max) && (2 <= max))) {
            throw new RuntimeException("(0 <= min <= max) and (max >= 2)");
        }
        this.role = role;
        this.min = min;
        this.max = max;
    }

    public Role getRole() {
        return role;
    }

    /**
     *
     * @return the minimum frequency
     */
    public int getMin() {
        return this.min;
    }

    /**
     *
     * @return the maximum frequency
     */
    public int getMax() {
        return this.max;
    }

    @Override
    public boolean clashesWith(UniquenessConstraint uc) {
        return uc.clashesWith(this);
    }

    @Override
    public boolean clashesWith(MandatoryConstraint mc) {
        return mc.clashesWith(this);
    }

    @Override
    public String getAbbreviationCode() {
        return "f";
    }

    @Override
    public String getDescription() {
        return "f:" + range();
    }

    public String range() {
        String range;
        if (min == max) {
            range = min + "";
        } else {
            range = min + ".." + max;
        }
        return range;
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        role.removeOtherConstraint(this);
        super.remove();
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent();
    }

    @Override
    public String getRequirementText() {
        return "The value with respect to the <" + getRole().getNamePlusType() + ">-role " + " within " + getFactType().getFactTypeString()
                + " must occcur " + range() + " times.";
    }

    @Override
    public boolean isRealized() {
        return true;
    }
}
