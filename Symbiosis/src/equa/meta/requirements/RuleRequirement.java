package equa.meta.requirements;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import equa.meta.ChangeNotAllowedException;
import equa.meta.objectmodel.Constraint;
import equa.meta.objectmodel.ObjectModelRealization;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.Impact;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.SystemInput;

/**
 *
 * @author FrankP
 */
@Entity
@DiscriminatorValue("Rule")
public class RuleRequirement extends Requirement {

    private static final long serialVersionUID = 1L;
    @ManyToOne
    private RequirementModel model;

    public RuleRequirement() {
    }

    /**
     * Constructor.
     *
     * @param nr number of requirement.
     * @param cat (category of requirement).
     * @param text of requirement.
     * @param source of requirement, of external input.
     * @param parent of requirement, the requirements model.
     */
    RuleRequirement(int nr, Category cat, String text,
            ExternalInput source, RequirementModel parent) {
        super(nr, cat, text, source, parent);
    }

    /**
     * @return 3, always
     */
    @Override
    int order() {
        return 3;
    }

    @Override
    public boolean isRealized() {
        if (isSystemRule()) {
            return true;
        }
        return getRealizedModelElement() != null;
    }

    public Constraint getRealizedModelElement() {
        
        for (ModelElement dependent : dependents()) {
            if (dependent instanceof Constraint) {
                Constraint constraint = (Constraint) dependent;
                if (constraint.isRealized()) {
                    return constraint;
                }
            }
        }
        return null;
    }

    @Override
    public String getText() {
        for (ModelElement modelElement : dependents()) {
            if (modelElement instanceof ObjectModelRealization) {
                return ((ObjectModelRealization) modelElement).getRequirementText();
            }
        }
        return text;
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
        if (isSystemRule()) {
            throw new ChangeNotAllowedException("system driven rules can not be changed");
        }
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
     * @return "Rule" as kind of requirement, always.
     */
    @Override
    public String getReqKind() {
        return "Rule";
    }

    public RequirementModel getModel() {
        return model;
    }

    public void setModel(RequirementModel model) {
        this.model = model;
    }

    private boolean isSystemRule() {
        return getReviewState().getExternalInput() instanceof SystemInput;
    }

    @Override
    public void setJustification(String justification) {
        if (isSystemRule()) {
            return;
        }
        ExternalInput externalInput = getReviewState().getExternalInput();
        externalInput.setJustification(justification);
    }

    @Override
    public void setImpact(Impact impact) {
        if (isSystemRule()) {
            return;
        }
        super.setImpact(impact);
    }

    @Override
    public void setUrgency(UrgencyKind urgency) {
        if (isSystemRule()) {
            return;
        }
        super.setUrgency(urgency);
    }

    @Override
    public void setMoSCoW(MoSCoW moscow) {
        if (isSystemRule()) {
            return;
        }
        super.setMoSCoW(moscow);
    }

    @Override
    public void setChanceOfFailure(ChanceOfFailure risk) {
        if (isSystemRule()) {
            return;
        }
        super.setChanceOfFailure(risk);
    }

    @Override
    public void setVerifyMethod(VerifyMethod verifyMethod) {
        if (isSystemRule()) {
            return;
        }
        super.setVerifyMethod(verifyMethod);
    }

    @Override
    public void setCategory(Category cat) {
        if (isSystemRule()) {
            return;
        }
        super.setCategory(cat);
    }

    public static RequirementFilter getRequirementFilter() {
        return new RequirementFilter() {

            @Override
            public boolean acccepts(Requirement requirement) {
                return (requirement instanceof RuleRequirement);
            }

            @Override
            public String toString() {
                return "Rule";
            }
        };
    }

}
