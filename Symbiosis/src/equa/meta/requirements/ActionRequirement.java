package equa.meta.requirements;

import equa.meta.ChangeNotAllowedException;
import equa.meta.objectmodel.ActionPermission;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author FrankP
 */
@Entity
@DiscriminatorValue("Action")
public class ActionRequirement extends Requirement {

    private static final long serialVersionUID = 1L;
    @ManyToOne
    private RequirementModel model;

    public ActionRequirement() {
    }

    /**
     * Constructor; entirely based on the constructor of the Requirement class.
     *
     * @param nr number of requirement.
     * @param cat (category) of requirement.
     * @param text of requirement.
     * @param source of requirement, which should be of external input.
     * @param parentof requirement(i.e., requirements model).
     */
    ActionRequirement(int nr, Category cat, String text,
            ExternalInput source, RequirementModel parent) {
        super(nr, cat, text, source, parent);
    }

    /**
     * @return 1, always.
     */
    @Override
    int order() {
        return 1;
    }

    @Override
    public boolean isRealized() {

        return false;
    }

    /**
     * If the dependents of this action-requirement-object have no instance of
     * DynamicRoleConstraint, null is returned.
     *
     * @return DynamicRoleConstraint, which is a dependent of this
     * action-requirement-object
     */
    public ActionPermission getRealizedConstraint() {
        List<ModelElement> modelElements = this.dependents();
        for (ModelElement modelElement : modelElements) {
            if (modelElement instanceof ActionPermission) {
                return (ActionPermission) modelElement;
            }
        }
        return null;
    }

    @Override
    public String getText() {
        ActionPermission actionPermission = getRealizedConstraint();
        if (actionPermission != null) {
            return actionPermission.getRequirementText();
        } else {
            return text;
        }
    }

    /**
     * The evaluation of the change of text is done only if the current text and
     * the input text are not String.equal(...).
     *
     * @param source of external input.
     * @param text of requirement.
     * @throws ChangeNotAllowedException if the (trimmed) text is empty.
     */
    @Override
    public void setText(ExternalInput source, String text) throws ChangeNotAllowedException {
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new ChangeNotAllowedException("text of requirement may not be empty");
        }

        if (!trimmedText.equals(this.text)) {
            getReviewState().change(source, getText());
            this.text = trimmedText;
        }
    }

    /**
     * @return "Action" as kind of requirement, always.
     */
    @Override
    public String getReqKind() {
        return "Action";
    }

    public RequirementModel getModel() {
        return model;
    }

    public void setModel(RequirementModel model) {
        this.model = model;
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return (requirement instanceof ActionRequirement);
            }

            @Override
            public String toString() {
                return "Action";
            }
        };
    }

    
    
}
