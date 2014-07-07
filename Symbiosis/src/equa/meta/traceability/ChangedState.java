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
@DiscriminatorValue("Changed")
public class ChangedState extends ReviewState {

    private static final long serialVersionUID = 1L;
    @OneToOne
    private ApprovedState previous;
    @Column
    private String previousContent;

    public ChangedState() {
    }

    ChangedState(ExternalInput source, ApprovedState previous, String previousContent, Reviewable review) {
        super(source, review);
        this.previous = previous;
        this.previousContent = previousContent;
        setReviewImpact(Impact.NORMAL);
    }

    public ApprovedState getPreviousApprovedState() {
        return previous;
    }

    public Object getPreviousContent() {
        return previousContent;
    }

    @Override
    public void change(ExternalInput source, String previousContent)
            throws ChangeNotAllowedException {
        if (reviewable.isOwner(source.getFrom())) {
            reviewable.setReviewState(new ApprovedState(source, previous, previousContent, reviewable));
        } else if (source.getFrom().equals(this.externalInput.getFrom())) {
            reviewable.addSource(externalInput);
            reviewable.setReviewState(new ChangedState(source, previous, previousContent, reviewable));
        } else {
            throw new ChangeNotAllowedException("change is only allowed by owner or author of previous change");
        }
    }

    @Override
    public void approve(ExternalInput source) throws ChangeNotAllowedException {
        if (reviewable.isOwner(source.getFrom())) {
            reviewable.addSource(externalInput);
            reviewable.setReviewState(new ApprovedState(source, previous, previousContent, reviewable));
        } else {
            throw new ChangeNotAllowedException("approval must be done by owner of the concerning category");
        }
    }

    @Override
    public void remove(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("remove is not allowed in Changed-state");
    }

    @Override
    public void reject(ExternalInput rejection) throws ChangeNotAllowedException {
        if (reviewable.isOwner(rejection.getFrom())) {
            reviewable.addSource(rejection);
            reviewable.setReviewState(new ChangeRejectedState(externalInput, rejection, this, previousContent, reviewable));
        } else {
            throw new ChangeNotAllowedException("rejection must be done by owner of the concerning category");
        }
    }

    @Override
    public void rollBack(ProjectRole participant) throws ChangeNotAllowedException {
        if (isRollBackable(participant)) {
            reviewable.replaceBy(previousContent);
            reviewable.setReviewState(new ApprovedState(previous.getExternalInput(), previous.getPreviousApprovedState(), previous.getPreviousContent(), reviewable));
        } else {
            throw new ChangeNotAllowedException("roll back is only allowed by author of change");
        }
    }

    @Override
    public String toString() {
        return "CHA";
    }

    @Override
    public boolean isRollBackable(ProjectRole projectRole) {
        return projectRole.getName().equalsIgnoreCase(externalInput.getFrom().getName());
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getReviewState() instanceof ChangedState;
            }

            @Override
            public String toString() {
                return "Changed";
            }
        };
    }
}
