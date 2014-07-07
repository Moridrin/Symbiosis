/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.traceability;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import equa.project.ProjectRole;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ExternalInput extends Source {

    private static final long serialVersionUID = 1L;
    @Column
    private String justification;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private ProjectRole from;

    public ExternalInput() {
    }

    /**
     * Constructor of external input source.
     *
     * @param justification of this external input.
     * @param from which participant of the project.
     */
    public ExternalInput(String justification, ProjectRole from) {
        if (justification == null) {
            this.justification = "";
        } else {
            this.justification = justification;
        }
        this.from = from;
    }

    /**
     * @return the justification of this external input.
     */
    public String getJustification() {
        return justification;
    }

    /**
     * @param justification of this external input.
     */
    public void setJustification(String justification) {
        this.justification = justification;
    }

    /**
     * @return the participant who provides this external input.
     */
    public ProjectRole getFrom() {
        return from;
    }

    @Override
    public String toString() {
        return justification + " by " + from.getName();
    }

    /**
     * If obj is not an instance of ExternalInput, false is automatically
     * returned.
     *
     * @param obj that should be an instance of ExternalInput.
     * @return true if the justification and project member are the same.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExternalInput) {
            ExternalInput externalInput = (ExternalInput) obj;
            return Objects.equals(externalInput.justification, justification) && Objects.equals(externalInput.from, from);
        } else {
            return false;
        }
    }

    @Override
    public boolean isManuallyCreated() {
        return true;
    }
}
