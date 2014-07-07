package equa.meta.requirements;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import equa.meta.ChangeNotAllowedException;
import equa.meta.traceability.AddRejectedState;
import equa.meta.traceability.AddedState;
import equa.meta.traceability.ApprovedState;
import equa.meta.traceability.Category;
import equa.meta.traceability.ChangeRejectedState;
import equa.meta.traceability.ChangedState;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.Impact;
import equa.meta.traceability.RemoveRejectedState;
import equa.meta.traceability.RemovedState;
import equa.meta.traceability.ReviewState;
import equa.meta.traceability.Reviewable;
import equa.project.Project;
import equa.project.ProjectRole;

/**
 *
 * @author FrankP
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Type", discriminatorType = DiscriminatorType.STRING)
public abstract class Requirement extends Reviewable
        implements Comparable<Requirement> {

    private static final long serialVersionUID = 1L;
    @Column(name = "nr")
    private int nr;
    @Column(name = "text")
    protected String text;
    @Column(name = "verifyMethod")
    private VerifyMethod verifyMethod;
    @Column(name = "chanceOfFailure")
    private ChanceOfFailure chanceOfFailure;
    @Column(name = "impact")
    private Impact impact;
    @Column(name = "urgency")
    private UrgencyKind urgency;
    @Column(name = "moscow")
    private MoSCoW moscow;


    public Requirement() {
    }


    /**
     * creation of a requirement with chance of failure LOW and impact NORMAL;
     * other aspects are undefined
     *
     * @param nr number of this requirement
     * @param cat category of this requirement
     * @param text non empty
     * @param source of requirement, which is of external input
     * @param parent of requirement, which is the requirements model
     */
    Requirement(int nr, Category cat, String text,
            ExternalInput source, RequirementModel parent) {
        super(parent, cat, source);
        if (text.isEmpty()) {
            throw new RuntimeException("text of a requirement can not be empty");
        }
        chanceOfFailure = ChanceOfFailure.UNDEFINED;
        this.text = text.trim();
        verifyMethod = VerifyMethod.UNDEFINED;
        this.nr = nr;
        impact = Impact.NORMAL;
        urgency = UrgencyKind.UNDEFINED;
        moscow = MoSCoW.UNDEFINED;
    }

    public String getJustification() {
        return getReviewState().getExternalInput().getJustification();
    }

    public void setJustification(String justification) {
        ExternalInput externalInput = getReviewState().getExternalInput();
        externalInput.setJustification(justification);
    }


    /**
     * @param impact for this requirement.
     */
    public void setImpact(Impact impact) {
        this.impact = impact;
    }

    /**
     * @param urgency for this requirement.
     */
    public void setUrgency(UrgencyKind urgency) {
        this.urgency = urgency;
    }

    /**
     * @param impact for this requirement.
     */
    public Impact getImpact() {
        return impact;
    }

    /**
     * @return the urgency of this requirement.
     */
    public UrgencyKind getUrgency() {
        return urgency;
    }

    /**
     * @return the moscow of this requirement.
     */
    public MoSCoW getMoSCoW() {
        return moscow;
    }

    /**
     * @param moscow for this requirement.
     */
    public void setMoSCoW(MoSCoW moscow) {
        this.moscow = moscow;
    }

    /**
     *
     * @return product of chance of failure and impact
     */
    public int getRisk() {
        if (getChanceOfFailure() == ChanceOfFailure.UNDEFINED || getImpact() == Impact.UNDEFINED) {
            return -1;
        }
        return getChanceOfFailure().ordinal() * getImpact().ordinal();
    }

    /**
     *
     * @return the text of this requirement
     */
    public String getText() {
        return this.text;
    }

    /**
     * changing of the text of this requirement based on source; subclass
     * decides if change is acceptaple, this depends on ownership and authorship
     *
     * @param text
     * @param source
     * @throws ChangeNotAllowedException if text is empty or in other way not
     * acceptable
     */
    public abstract void setText(ExternalInput source, String text)
            throws ChangeNotAllowedException;

    /**
     *
     * @return the risk of realisation of this requirement
     */
    public ChanceOfFailure getChanceOfFailure() {
        return chanceOfFailure;
    }

    /**
     *
     * @return the identification: the category code followed by the number of
     * the requirement, seperated by a dot
     *
     */
    public String getId() {
        return getCategory().getCode() + "." + nr;
    }

    /**
     *
     * @return the number of this requirement
     */
    public int getNr() {
        return nr;
    }

    /**
     * @param nr the number of requirement to set on this requirement.
     */
    void setNr(int nr) {
        this.nr = nr;
    }

    /**
     *
     * @return the method of verifying this requirement
     */
    public VerifyMethod getVerifyMethod() {
        return verifyMethod;
    }

    /**
     * changing of the risk of this requirement
     *
     * @param risk
     */
    public void setChanceOfFailure(ChanceOfFailure risk) {
        this.chanceOfFailure = risk;
    }

    /**
     * changing of the verifyMethod of this requirement
     *
     * @param verifyMethod
     */
    public void setVerifyMethod(VerifyMethod verifyMethod) {
        this.verifyMethod = verifyMethod;
    }

    /**
     * @return true if the requirement is realized.
     */
    public abstract boolean isRealized();

    public boolean isApprovable(ProjectRole projectRole) {
        ReviewState rs = getReviewState();
        if (getCategory().isOwner(projectRole)
                && (rs instanceof AddedState
                || rs instanceof ChangedState
                || rs instanceof RemovedState)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isChangeable(ProjectRole projectRole) {
        ReviewState rs = getReviewState();
        ExternalInput ei = rs.getExternalInput();
        if (ei.getFrom()==null) return true;
        if (rs instanceof ApprovedState
                || (ei.getFrom().getName().equalsIgnoreCase(projectRole.getName())
                && (rs instanceof AddedState || rs instanceof ChangedState))) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRemovable(ProjectRole projectRole) {
        ReviewState rs = getReviewState();
        if (rs instanceof ApprovedState) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRejectable(ProjectRole projectRole) {
        return isApprovable(projectRole);
    }

    public boolean isRollBackable(ProjectRole projectRole) {
        ReviewState rs = getReviewState();
        return rs.isRollBackable(projectRole);
    }

    /**
     *
     * @return every subtype of requirements has is own order: action=1; fact=2;
     * rule=3; quality attribute=4
     */
    abstract int order();

    /**
     * Comparison criteria between two requirements, based on the metrics:
     * order, category and number of requirement.
     *
     * @param req to compare with this requirement.
     * @return ordering on base of order() --> category --> nr
     */
    @Override
    public int compareTo(Requirement req) {
        int orderCompare = order() - req.order();
        if (orderCompare != 0) {
            return orderCompare;
        }

        int catCompare = getCategory().compareTo(req.getCategory());
        if (catCompare != 0) {
            return catCompare;
        }
        return getNr() - req.getNr();
    }

    /**
     * Before replacing the (text) content of this requirement, a review of
     * content in other requirements is performed to avoid the duplication of
     * content.
     *
     * @param content which should have the text that replaces the text of this
     * requirement.
     * @throws ChangeNotAllowedException if content is found in another
     * requirement
     */
    @Override
    protected void replaceBy(String content) throws ChangeNotAllowedException {
        Requirement req = ((RequirementModel) getParent()).searchFor(content);
        if (req != null) {
            throw new ChangeNotAllowedException("replace with " + content + " results in redundant requirement (see: " + req.getId());
        }

        this.text = content;
    }


    /**
     * @return the Id of this requirement.
     */
    @Override
    public String getName() {
        return getId();
    }

    /**
     * @return the kind of this requirement.
     */
    public abstract String getReqKind();

    @Override
    public String toString() {
        //String formatstring = "%s%5s%8s: %s";
        String formatstring = "%s [%s, %s, %s]";
        return String.format(formatstring, getText(), getId(), getReqKind(), getReviewState().toString());
    }

    /**
     * If member is not an instance of Requirement, false is automatically
     * returned.
     *
     * @param member that should be an instance of Requirement.
     * @return true if the comparisson criteria (see: compareTo(Requirement
     * req)) returns 0
     */
    @Override
    public boolean equals(Object member) {
        if (member instanceof Requirement) {
            Requirement requirement = (Requirement) member;
            return this.compareTo(requirement) == 0;
        } else {
            return false;
        }
    }

    public boolean isTrue(String filter) {
        switch (filter) {
            case "Added":
                return getReviewState() instanceof AddedState;
            case "AddRejected":
                return getReviewState() instanceof AddRejectedState;
            case "Approved":
                return getReviewState() instanceof ApprovedState;
            case "Changed":
                return getReviewState() instanceof ChangedState;
            case "ChangeRejected":
                return getReviewState() instanceof ChangeRejectedState;
            case "Removed":
                return getReviewState() instanceof RemovedState;
            case "RemoveRejected":
                return getReviewState() instanceof RemoveRejectedState;
            case "Action":
                return this instanceof ActionRequirement;
            case "Fact":
                return this instanceof FactRequirement;
            case "Rule":
                return this instanceof RuleRequirement;
            case "QA":
                return this instanceof QualityAttribute;
            case "UNDEFINED":
                return this.getReviewState().getReviewImpact().equals(Impact.UNDEFINED);
            case "ZERO":
                return this.getReviewState().getReviewImpact().equals(Impact.ZERO);
            case "LIGHT":
                return this.getReviewState().getReviewImpact().equals(Impact.LIGHT);
            case "NORMAL":
                return this.getReviewState().getReviewImpact().equals(Impact.NORMAL);
            case "SERIOUS":
                return this.getReviewState().getReviewImpact().equals(Impact.SERIOUS);
            case "Realized":
                return isRealized();
            case "Owner":
                RequirementModel rm = (RequirementModel) getParent();

                ProjectRole currentUser = rm.getProject().getCurrentUser();
                return this.getCategory().isOwner(currentUser);
            default:
                Project project = ((RequirementModel) getParent()).getProject();
                return getCategory().equals(project.getCategory(filter));
        }
    }

}
