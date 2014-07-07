package equa.meta.objectmodel;

import java.util.List;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.Source;

/**
 *
 * @author FrankP
 */
public class MandatoryConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;

    private Role role;

    /**
     * creation of single mandatory constraint with respect to object-role,
     * based on source
     *
     * @param role
     * @param source
     */
    public MandatoryConstraint(Role role, RuleRequirement source) throws ChangeNotAllowedException {
        super(role.getParent(), source);

        if (role.getSubstitutionType().isValueType()) {
            throw new ChangeNotAllowedException("mandatory constraints are only "
                    + "allowed in case of a role played by an objecttype");
        }

        this.role = role;
        role.addConstraint(this);
    }

    @Override
    public String getName() {
        if (getFactType().withMoreMandarialConstraints()) {
            return getId();
        } else {
            return getAbbreviationCode();
        }
    }

    @Override
    public boolean clashesWith(MandatoryConstraint mc) {
        return false;
    }

    @Override
    public boolean clashesWith(FrequencyConstraint fc) {
        if (fc.getRole().equals(role)) {
            return fc.getMin() == 0;
        } else {
            return false;
        }
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        if (role != null) {

            role.deleteMandatoryConstraint();
            super.remove();
//            for (Source source : sources()) {
//                if (source instanceof Requirement) {
//                    ((Requirement) source).remove();
//                }
//            }

        }

    }

    MandatoryConstraint migrateTo(Role sourceRole, Role targetRole) {
        if (sourceRole == targetRole) {
            return this;
        }

        List<Source> sources = this.sources();
        try {

            MandatoryConstraint mc = new MandatoryConstraint(targetRole,
                    (RuleRequirement) sources.get(0));
            for (int i = 1; i < sources.size(); i++) {
                mc.addSource(sources.get(i));
            }
            return mc;
        } catch (ChangeNotAllowedException ex) {
            return null;
        }

    }

    @Override
    public String getAbbreviationCode() {
        return "m";
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent();
    }

    @Override
    public String getRequirementText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Every ");
        sb.append("<");
        sb.append(role.getSubstitutionType().getName());
        sb.append(">");
        sb.append(" cannot exist without a fact about ");
        sb.append(getFactType().getFactTypeString()).append(".");
        return sb.toString();
    }

    @Override
    public boolean isRealized() {
        return true;
    }
}
