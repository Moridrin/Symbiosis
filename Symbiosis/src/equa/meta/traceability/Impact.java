package equa.meta.traceability;

import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementFilter;

/**
 *
 * @author FrankP
 */
public enum Impact implements RequirementFilter {

    UNDEFINED(-1),
    /**
     *
     */
    ZERO(0),
    /**
     *
     */
    LIGHT(1),
    /**
     *
     */
    NORMAL(2),
    /**
     *
     */
    SERIOUS(3);

    private int order;

    private Impact(int order) {
        this.order = order;
    }

    /**
     *
     * @return the number of the constant
     */
    public int getOrder() {
        return order;
    }

    @Override
    public boolean acccepts(Requirement requirement) {
        return requirement.getReviewState().getReviewImpact().equals(this);
    }

}
