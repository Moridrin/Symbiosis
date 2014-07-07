/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.traceability;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import equa.meta.ChangeNotAllowedException;
import equa.project.ProjectRole;
import equa.project.StakeholderRole;

/**
 *
 * @author frankpeeters
 */
//@MappedSuperclass
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Reviewable extends ModelElement {

    //@Id
    //@GeneratedValue (strategy = GenerationType.IDENTITY)
    //private long reviewableId;
    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ReviewState reviewState;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Category cat;
    private boolean generated;

    public Reviewable() {
    }

    public Reviewable(ParentElement parent, Category cat, ExternalInput source) {
        super(parent, source);
        this.cat = cat;
        this.generated = true;
        initReviewState(source);
    }

    /**
     *
     * @return the state of this model element; attention: reliability of this
     * state must be informed through the call of isReliable()
     */
    public ReviewState getReviewState() {
        return reviewState;
    }

    public boolean isApproved() {
        return reviewState instanceof ApprovedState;
    }

    public Category getCategory() {

        return cat;
    }

    public void setCategory(Category cat) {
        this.cat = cat;
    }

    public boolean isOwner(ProjectRole participant) {
        return cat.isOwner(participant);
    }
    
    public void setManuallyCreated(boolean manual){
        generated = !manual;
    }

    private boolean basedOnStakeholderInput() {
        for (Source source : sources()) {
            if (source instanceof ExternalInput) {
                if (((ExternalInput) source).getFrom() instanceof StakeholderRole) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initReviewState(ExternalInput source) {
        if (cat.isOwner(source.getFrom()) || cat.equals(Category.SYSTEM)) {
            reviewState = new ApprovedState(source, null, null, this);
        } else {
            reviewState = new AddedState(source, this);
        }
    }

    /**
     * at this model element content should be replaced
     *
     * @param content approved content
     */
    protected abstract void replaceBy(String content) throws ChangeNotAllowedException;

    void setReviewState(ReviewState reviewState) {
        this.reviewState = reviewState;
    }

    @Override
    public boolean isManuallyCreated() {
        return !generated;
    }
}
