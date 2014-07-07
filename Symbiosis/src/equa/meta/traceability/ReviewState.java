/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.traceability;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import equa.meta.ChangeNotAllowedException;
import equa.project.ProjectRole;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "disc")
public abstract class ReviewState implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    protected ExternalInput externalInput;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, mappedBy = "reviewState")
    protected Reviewable reviewable;
    @Column
    protected Impact reviewImpact;

    public ReviewState() {
        this.reviewImpact = Impact.NORMAL;
    }

    public ReviewState(ExternalInput externalInput, Reviewable reviewable) {
        this.externalInput = externalInput;
        this.reviewable = reviewable;
        reviewable.addSource(externalInput);
        this.reviewImpact = Impact.NORMAL;
    }

    public abstract boolean isRollBackable(ProjectRole projectRole);

    /**
     * reviewable model element changes based on source; if participant of
     * source is the owner the state will transit to APPROVED and
     * previousContent is stored as backup for instance in case of roll back
     *
     * @param participant
     * @param previousContyent
     */
    public abstract void change(ExternalInput source, String previousContent) throws ChangeNotAllowedException;

    /**
     * model element has to be removed; approval by responsible stakeholder is
     * needed
     */
    public abstract void remove(ExternalInput source) throws ChangeNotAllowedException;

    /**
     * proposed change in state (including adding or removal) will be withdrawn;
     * if previous change isn't done by participant, exception will be thrown
     */
    public abstract void rollBack(ProjectRole participant) throws ChangeNotAllowedException;

    /**
     * responsible stakeholder approves the current state of this model element;
     * but if participant isn't owner or his delegate, exception will be thrown
     */
    public abstract void approve(ExternalInput source) throws ChangeNotAllowedException;

    /**
     * stakeholder rejects proposed change in state but if partiocipant isn't
     * owner or his delegate, exception will be thrown
     */
    public abstract void reject(ExternalInput source) throws ChangeNotAllowedException;

    public void setReviewImpact(Impact impact) {
        reviewImpact = impact;
        if (reviewImpact == Impact.ZERO) {
            try {
                approve(new ExternalInput("approving is automatic because impact is defined as ZERO", reviewable.getCategory().getOwner()));
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(ReviewState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Impact getReviewImpact() {
        if (reviewable.isApproved()) {
            return Impact.ZERO;
        }
        return reviewImpact;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * DANGEROUS, do not use this method, because a review state should be
     * linked to one reviewable life time
     *
     * @param reviewable
     */
    void setReviewable(Reviewable reviewable) {
        this.reviewable = reviewable;
    }

    public Reviewable getReviewable() {
        return reviewable;
    }

    /**
     * DANGEROUS, do not use this method, because the source may not be changed
     * after creation
     *
     * @param source
     */
    void setExternalInput(ExternalInput source) {
        this.externalInput = source;
    }

    public ExternalInput getExternalInput() {
        return externalInput;
    }
    

}
