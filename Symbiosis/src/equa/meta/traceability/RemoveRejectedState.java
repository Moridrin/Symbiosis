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
@DiscriminatorValue("RemoveRejected")
public class RemoveRejectedState extends ReviewState {

    private static final long serialVersionUID = 1L;
    @ManyToOne
    private ExternalInput rejection;
    @OneToOne
    private RemovedState previous;
    @Column
    private String previousContent;

    public RemoveRejectedState() {
    }

    RemoveRejectedState(ExternalInput source, RemovedState previous, String previousContent, ExternalInput rejection, Reviewable review) {
        super(source, review);
        this.rejection = rejection;
        this.previous = previous;
        this.previousContent = previousContent;
        setReviewImpact(Impact.SERIOUS);
    }

    public String getJustification() {
        return rejection.getJustification();
    }

    @Override
    public void change(ExternalInput source, String previousContent) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("change not allowed in case of REMOVE_REJECTED");
    }

    @Override
    public void approve(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("approve not allowed in case of REMOVE_REJECTED");
    }

    @Override
    public void remove(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("remove not allowed in case of REMOVE_REJECTED");
    }

    @Override
    public void reject(ExternalInput source) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("reject not allowed in case of REMOVE_REJECTED");
    }

    @Override
    public void rollBack(ProjectRole participant) throws ChangeNotAllowedException {
        if (participant.getName().equalsIgnoreCase(externalInput.getFrom().getName())) {
            reviewable.setReviewState(new ApprovedState(externalInput,
                    previous.getPreviousApprovedState(), previousContent, reviewable));
        } else if (reviewable.isOwner(participant)) {
            reviewable.setReviewState(previous);
        } else {
            throw new ChangeNotAllowedException("roll Back not allowed by participant "
                    + participant.getName() + " in case of REMOVE_REJECTED");
        }
    }

    @Override
    public String toString() {
        return "REM_REJ";
    }

    @Override
    public boolean isRollBackable(ProjectRole projectRole) {
        return projectRole.getName().equalsIgnoreCase(externalInput.getFrom().getName())
                || reviewable.isOwner(projectRole);
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return requirement.getReviewState() instanceof RemoveRejectedState;
            }

            @Override
            public String toString() {
                return "RemoveRejected";
            }
        };
    }
}
