package equa.meta.requirements;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import equa.meta.ChangeNotAllowedException;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;

/**
 *
 * @author FrankP
 */
@Entity
@DiscriminatorValue("Quality")
public class QualityAttribute extends Requirement {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    private RequirementModel model;

    public QualityAttribute() {
    }

    /**
     * Constructor.
     *
     * @param nr number of requirement.
     * @param cat (category) of requirement.
     * @param text of requirement.
     * @param source of requirement, that is of external input.
     * @param parent of requirement, the requirements model.
     */
    QualityAttribute(int nr, Category cat, String text,
            ExternalInput source, RequirementModel parent) {
        super(nr, cat, text, source, parent);

    }

    /**
     * @return 4, always.
     */
    @Override
    int order() {
        return 4;
    }

    /**
     * @return N/A (i.e., undefined for this kind of requirement).
     * @throws UnsupportedOperationException this instance cannot be realized
     * yet.
     */
    @Override
    public boolean isRealized() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return false;
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
     * @return "QA", as kind of requirement, always.
     */
    @Override
    public String getReqKind() {
        return "QA";
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
                return (requirement instanceof QualityAttribute);
            }

            @Override
            public String toString() {
                return "QA";
            }
        };
    }

    @Override
    public boolean isManuallyCreated() {
        return true;
    }
}
