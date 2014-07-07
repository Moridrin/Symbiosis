package equa.meta.traceability;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementFilter;
import equa.project.ProjectRole;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Added")
public class AddedState extends ReviewState {

    private static final long serialVersionUID = 1L;

    public AddedState() {
    }

    public AddedState(ExternalInput source, Reviewable reviewable) {
        super(source, reviewable);
    }

    @Override
    public void change(ExternalInput source, String previousContent)
            throws ChangeNotAllowedException {
        if (reviewable.isOwner(source.getFrom())) {
            reviewable.setReviewState(new ApprovedState(source, null, previousContent, reviewable));
        } else if (source.getFrom().equals(this.externalInput.getFrom())) {
            reviewable.addSource(source);
        } else {
            throw new ChangeNotAllowedException("change only allowed by owner or "
                    + " author");
        }
    }

    @Override
    public void approve(ExternalInput source) throws ChangeNotAllowedException {
        if (reviewable.isOwner(source.getFrom())) {
            reviewable.setReviewState(new ApprovedState(source, null, null, reviewable));
        } else {
            throw new ChangeNotAllowedException(source.getFrom().getName() + " isn't "
                    + "owner of " + reviewable.getName());
        }
    }

    @Override
    public void remove(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("removing of a reviewable modelelement"
                + " isn't allowed; please use a roll back");
    }

    @Override
    public void reject(ExternalInput rejection) throws ChangeNotAllowedException {
        if (reviewable.isOwner(rejection.getFrom())) {
            reviewable.setReviewState(new AddRejectedState(externalInput, rejection, reviewable));
        } else {
            throw new ChangeNotAllowedException(rejection.getFrom().getName() + " isn't "
                    + "owner of " + reviewable.getName());
        }
    }

    @Override
    public void rollBack(ProjectRole participant) throws ChangeNotAllowedException {
        if (isRollBackable(participant)) {
            reviewable.remove();
        } else {
            throw new ChangeNotAllowedException(participant.getName() + " isn't "
                    + "author of " + reviewable.getName());
        }
    }

    @Override
    public String toString() {
        return "ADD";
    }

    @Override
    public boolean isRollBackable(ProjectRole projectRole) {
        if (externalInput.getFrom()==null) return true;
        return externalInput.getFrom().getName().equalsIgnoreCase(projectRole.getName());
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getReviewState() instanceof AddedState;
            }

            @Override
            public String toString() {
                return "Added";
            }
        };
    }

}
