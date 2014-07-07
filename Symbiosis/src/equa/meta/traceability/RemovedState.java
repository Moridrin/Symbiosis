package equa.meta.traceability;

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
@DiscriminatorValue("Removed")
public class RemovedState extends ReviewState {

    private static final long serialVersionUID = 1L;
    @OneToOne
    private ApprovedState previous;
    @Column
    private String previousContent;

    public RemovedState() {
    }

    public RemovedState(ExternalInput source, ApprovedState previous, String previousContent, Reviewable review) {
        super(source, review);
        this.previous = previous;
        this.previousContent = previousContent;
        setReviewImpact(Impact.NORMAL);
    }

    public ApprovedState getPreviousApprovedState() {
        return previous;
    }

    @Override
    public void change(ExternalInput source, String previousContent) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("change is not allowed in case of "
                + "REMOVED state");
    }

    @Override
    public void approve(ExternalInput source) throws ChangeNotAllowedException {
        if (reviewable.isOwner(source.getFrom())) {
            reviewable.remove();
        } else {
            throw new ChangeNotAllowedException("removal done by non owner");
        }
    }

    @Override
    public void remove(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("removal must be done by owner through approve");
    }

    @Override
    public void reject(ExternalInput rejection) {
        if (reviewable.isOwner(rejection.getFrom())) {
            reviewable.addSource(rejection);
            reviewable.setReviewState(new RemoveRejectedState(externalInput, this, previousContent, rejection, reviewable));
        }
    }

    @Override
    public void rollBack(ProjectRole participant) throws ChangeNotAllowedException {
        if (isRollBackable(participant)) {
            reviewable.setReviewState(new ApprovedState(externalInput, previous, previousContent, reviewable));
        } else {
            throw new ChangeNotAllowedException("roll back must be done by author");
        }
    }

    @Override
    public String toString() {
        return "REM";
    }

    @Override
    public boolean isRollBackable(ProjectRole projectRole) {
        return externalInput.getFrom().getName().equalsIgnoreCase(projectRole.getName());
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getReviewState() instanceof RemovedState;
            }

            @Override
            public String toString() {
                return "Removed";
            }
        };
    }
}
