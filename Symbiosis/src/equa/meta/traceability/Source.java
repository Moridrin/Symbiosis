package equa.meta.traceability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class represents the source of changes in the object- model; this source
 * includes the start of the changes, the last update of the changes and the
 * dependability between model elements related to the changes. To mediate these
 * changes, the {@link SynchronizationMediator} is utilized.
 *
 * @author
 */
@Entity
public abstract class Source implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sourceId;
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar createdAt;
    @Column(name = "ModifiedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar modifiedAt;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "dependentModelElement")
    protected List<SynchronizationMediator> mediators;

    /**
     * Constructor. The creation date-time and the modification date-time are
     * assigned by default with the same value, which is the date-time of the
     * call to this constructor. A list of mediators is prepared for future
     * usage.
     */
    public Source() {
        createdAt = new GregorianCalendar();
        modifiedAt = createdAt;
        mediators = new ArrayList<>();
    }

    /**
     * Creates a (shallow) copy of Source based on another instance of Source.
     *
     * @param source , its attributes are used assigned to the copy.
     */
    protected Source(Source source) {
        //shallow copy
        createdAt = source.createdAt;
        modifiedAt = source.modifiedAt;
        mediators = source.mediators;
    }

    /**
     * @return the date and time of the creation of this source-object.
     */
    public Calendar getCreatedAt() {
        return createdAt;
    }

    /**
     * @return the date and time of the last change on this source-object.
     */
    public Calendar getModifiedAt() {
        return modifiedAt;
    }

    /**
     * Assigns the current date and time to the modification date and time of
     * this source-object.
     */
    protected void setModifiedAtToNow() {
        modifiedAt = new GregorianCalendar();
    }

    /**
     * This source has been changed; descr describes the accomplished change;
     * impact gives an indication of the seriousness of the change on this
     * source; it results in a todo at all synchronization nodes of this source;
     * modifiedAt will be adjusted to now
     *
     * @param descr describes the accomplished change
     * @param impact gives an indication of the seriousness of the change on
     * this source
     */
    public void publishChange(String descr, Impact impact) {
        for (SynchronizationMediator mediator : mediators) {
            mediator.addToDo(descr, impact);
        }
        // @TODO: usage of setModifiedAtToNow() here?
    }

    public boolean isLonely() {

        return mediators.isEmpty();
    }

    /**
     * Creation of a new mediator (without to-dos) which points at modelElement
     *
     * @param modelElement to be dependent of this source.
     */
    SynchronizationMediator addDependentModelElement(ModelElement modelElement) {
//        for (SynchronizationMediator mediator : mediators){
//            if (mediator.getDependentModelElement().equals(modelElement)){
//                return mediator;
//            }
//        }
        SynchronizationMediator mediator = new SynchronizationMediator(this, modelElement);
        mediators.add(mediator);
        setModifiedAtToNow();
        return mediator;
    }

    /**
     * The list of dependents is obtained with the support of the
     * SynchronizationMediator objects of this source-object.
     *
     * @return the list of (dependent) ModelElement instances.
     */
    public List<ModelElement> dependents() {
        ArrayList<ModelElement> dependents = new ArrayList<ModelElement>();
        for (SynchronizationMediator mediator : mediators) {
            dependents.add(mediator.getDependentModelElement());
        }
        return dependents;
    }

    /**
     * @return id of this source.
     */
    public long getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId to set in this source.
     */
    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    public abstract boolean isManuallyCreated();


    protected void removeDependentMediators() {
        List<SynchronizationMediator> copy = new ArrayList<>(mediators);
        for (SynchronizationMediator mediator : copy)  {
            mediator.removeForward();
        }
        mediators.clear();
        setModifiedAtToNow();
    }

    protected void removeBackward(SynchronizationMediator dependentMediator) {
        if (!mediators.contains(dependentMediator)) {
            System.out.println("remove mediator " + dependentMediator.getSource().getClass());
        } else {
            mediators.remove(dependentMediator);
            setModifiedAtToNow();
        }
    }

}
