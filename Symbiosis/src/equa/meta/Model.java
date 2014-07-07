package equa.meta;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import equa.meta.objectmodel.ObjectModel;
import equa.meta.requirements.RequirementModel;
import equa.meta.traceability.ParentElement;
import equa.project.Project;

/**
 * Abstract class to represent a model; this class should be extended by the
 * sub-models, e.g., {@link ObjectModel}, {@link RequirementModel}. <br>
 * This class contains unconstrained R[etrieve] and unconstrained U[pdate]
 * operation(s), deriving a typical behavior of a Data Transfer Object (DTO).
 * Thus, no business logic is involved. However, the possibility to start the
 * life-cycle of a Model instance with the constructor {@link #Model()},
 * suggests a variation of behavior: the variation of using
 * {@link #setParent(equa.project.Project)}
 * <i>right after</i> the constructor {@link #Model()}. If the constructor
 * {@link #Model(equa.project.Project)} is used, no variation is needed in the
 * behavior of this DTO.
 *
 * @author EQuA
 */
@MappedSuperclass
public abstract class Model extends ParentElement implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToOne
    protected Project project;

    /**
     * DEFAULT CONSTRUCTOR; WARNING: the {@link Project} of the Model remains
     * undefined. The {@link #setParent(equa.project.Project)} method should be
     * used right after this constructor for an adequate behavior of the Model
     * instance.
     */
    public Model() {
    }

    /**
     * CONSTRUCTOR. The {@link Project} is received as the parent of this model.
     *
     * @param parent {@link Project}.
     */
    public Model(Project parent) {
        this.project = parent;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the parent {@link Project} of this model.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Unconstrained UPDATE operation.
     *
     * @param parent {@link Project} of this model.
     */
    public void setParent(Project parent) {
        this.project = parent;
    }
}
