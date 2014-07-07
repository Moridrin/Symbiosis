package equa.meta.traceability;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementFilter;
import equa.project.ProjectRole;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("ChangeRejected")
public class ChangeRejectedState extends ReviewState {

    private static final long serialVersionUID = 1L;
    @OneToOne
    private ChangedState previous;
    @Column
    private String previousContent;
    @ManyToOne
    private ExternalInput rejection;

    public ChangeRejectedState() {
    }

    ChangeRejectedState(ExternalInput source, ExternalInput rejection, ChangedState previous, String approvedContent, Reviewable review) {
        super(source, review);
        this.rejection = rejection;
        this.previous = previous;
        this.previousContent = approvedContent;
        setReviewImpact(Impact.SERIOUS);
    }

    public String getJustification() {
        return rejection.getJustification();
    }

    @Override
    public void change(ExternalInput source, String previousContent)
            throws ChangeNotAllowedException {

        throw new ChangeNotAllowedException("change is not allowed in CHANGE_REJECTED state");
    }

    @Override
    public void approve(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("approval is not allowed in CHANGE_REJECTED state");
    }

    @Override
    public void remove(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("removal is not allowed in CHANGE_REJECTED state");
    }

    @Override
    public void reject(ExternalInput source) {
    }

    @Override
    public void rollBack(ProjectRole participant) throws ChangeNotAllowedException {
        if (reviewable.isOwner(participant)) {
            reviewable.setReviewState(new ChangedState(externalInput,
                    previous.getPreviousApprovedState(), previousContent, reviewable));
        } else if (participant.getName().equalsIgnoreCase(externalInput.getFrom().getName())) {
            reviewable.replaceBy(previousContent);
            reviewable.setReviewState(previous.getPreviousApprovedState());
        } else {
            throw new ChangeNotAllowedException("roll back is only allowed by author or owner");
        }
    }

    @Override
    public String toString() {
        return "CHA_REJ";
    }

    @Override
    public boolean isRollBackable(ProjectRole projectRole) {
        return reviewable.isOwner(projectRole)
                || projectRole.getName().equalsIgnoreCase(externalInput.getFrom().getName());

    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getReviewState() instanceof ChangeRejectedState;
            }

            @Override
            public String toString() {
                return "ChangeRejected";
            }
        };
    }
}
