package equa.meta.objectmodel;

import java.util.List;

import javax.persistence.Entity;

import equa.meta.requirements.RuleRequirement;

/**
 *
 * @author FrankP
 */
@Entity
public class SubsetConstraint extends SetConstraint {

    private static final long serialVersionUID = 1L;

    public SubsetConstraint() {
    }

    /**
     * @see SetConstraint
     * @param from
     * @param towards
     * @param source
     */
    public SubsetConstraint(List<Role> from, List<Role> towards, RuleRequirement source) {
        super(from, towards, source);
    }

    @Override
    public String getAbbreviationCode() {
        return "s";
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
