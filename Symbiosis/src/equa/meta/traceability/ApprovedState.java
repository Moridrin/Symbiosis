package equa.meta.traceability;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementFilter;
import equa.project.ProjectRole;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Approved")
public class ApprovedState extends ReviewState {

    private static final long serialVersionUID = 1L;
    @OneToOne
    private ApprovedState previousApprovedState;
    @Column
    private String previousContent;

    public ApprovedState() {
        //this(null, null, null, null);
    }

    public ApprovedState(ExternalInput source, ApprovedState previous, String content, Reviewable reviewable) {
        super(source, reviewable);
        this.previousApprovedState = previous;
        this.previousContent = content;
    }

    /**
     *
     * @return the previous approved content, if existing, otherwise null;
     */
    public String getPreviousContent() {
        return previousContent;
    }

    /**
     *
     * @return the previous approved state, if existing, otherwise null;
     */
    public ApprovedState getPreviousApprovedState() {
        return previousApprovedState;
    }

    @Override
    public void setReviewImpact(Impact impact) {
    }

    @Override
    public void change(ExternalInput source, String previousContent) throws ChangeNotAllowedException {
        if (reviewable.isOwner(source.getFrom())) {
            reviewable.setReviewState(new ApprovedState(source, this, previousContent, reviewable));
        } else {
            ChangedState state = new ChangedState(source, this, previousContent, reviewable);
            reviewable.setReviewState(state);
        }
    }

    @Override
    public void approve(ExternalInput source) {
    }

    @Override
    public void remove(ExternalInput source) {
        if (reviewable.isOwner(source.getFrom())) {
            try {
                reviewable.remove();
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(ApprovedState.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            reviewable.setReviewState(new RemovedState(source, this, reviewable.toString(), reviewable));
            reviewable.removeDependentMediators();
        }
    }

    @Override
    public void reject(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("reject not allowed in case of APPROVED");
    }

    @Override
    public void rollBack(ProjectRole participant) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("roll back not allowed in case of APPROVED");
    }

    @Override
    public String toString() {
        return "APP";
    }

    @Override
    public boolean isRollBackable(ProjectRole projectRole) {
        return false;
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getReviewState() instanceof ApprovedState;
            }

            @Override
            public String toString() {
                return "Approved";
            }
        };
    }

}
