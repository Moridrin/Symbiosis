package equa.meta.traceability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import equa.meta.ChangeNotAllowedException;

/**
 * Element of the object-model. This class extends is an extension of
 * {@link Source}.
 *
 * @author FrankP
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ModelElement extends Source {

    private static final long serialVersionUID = 1L;
    // TODO: Bespreken met Frank, kijken of we de interface kunnen vervangen.
    /*@VariableOneToOne (discriminatorColumn=@DiscriminatorColumn(name="MODEL_TYPE"),
     discriminatorClasses={
     @DiscriminatorClass(discriminator="M", value=equa.meta.Model.class), 
     @DiscriminatorClass(discriminator="F", value=equa.meta.objectmodel.FactType.class),
     @DiscriminatorClass(discriminator="O", value=equa.meta.objectmodel.ObjectType.class),
     @DiscriminatorClass(discriminator="R", value=equa.meta.objectmodel.Role.class)
     }, targetInterface = IParentModelElement.class)*/
    @Transient
    private ParentElement parent;
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "dependentModelElement")
    private List<SynchronizationMediator> sourceMediators;

    public ModelElement() {
    }

    /**
     * Constructor of a child model element due to a source of change; the time
     * of creation and last modification will be set to now.
     *
     * @param parent the parent for this model-element; model elements without
     * parent must be set with a null parent
     * @param source of changes for this model-element
     */
    public ModelElement(ParentElement parent, Source source) {
        this.parent = parent;
        sourceMediators = new ArrayList<>();
        this.addSource(source);
    }

    /**
     * Constructor of a child model element due to sources of changes; the time
     * of creation and last modification will be set to now.
     *
     * @param parent the parent for this model-element; model elements without
     * parent must be set with a null parent
     * @param sources of changes for this model-element
     */
    public ModelElement(ParentElement parent, List<Source> sources) {
        this.parent = parent;
        sourceMediators = new ArrayList<>();
        for (Source source : sources) {
            this.addSource(source);
        }
    }

    /**
     * @param me that is the model-element to use to create the (shallow) copy
     */
    protected ModelElement(ModelElement me) {
        //shallow copy:
        super(me);
        parent = me.parent;
        sourceMediators = me.sourceMediators;
    }

    /**
     * @return parent of this model-element
     */
    public ParentElement getParent() {
        return parent;
    }

    /**
     *
     * @return true, if all sources of this model-element are approved and
     * reliable, else false
     */
    public boolean isReliable() {
        for (SynchronizationMediator mediator : sourceMediators) {
            if (mediator.getImpactOfToDos() != Impact.ZERO) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param toDo that has been done, so it should be removed
     */
    public void settled(ToDo toDo) {
        toDo.remove();
        setModifiedAtToNow();
    }

    /**
     * @return a list with all sources of changes according to the
     * {@link SynchronizationMediator}'s of this model-element.
     */
    public List<Source> sources() {
        ArrayList<Source> sources = new ArrayList<>();
        if (sourceMediators != null) {
            for (SynchronizationMediator mediator : sourceMediators) {
                sources.add(mediator.getSource());
            }
        }
        return sources;
    }

    /**
     * @return the latest {@link Source} in this model-element.
     */
    public Source mostRecentSource() {
        Iterator<Source> it = sources().iterator();
        Source recent = it.next();
        while (it.hasNext()) {
            Source source = it.next();
            if (source.getModifiedAt().after(recent)) {
                recent = source;
            }
        }
        return recent;
    }

    /**
     *
     * @return the first relevant source
     */
    public Source creationSource() {
        return sourceMediators.get(0).getSource();
    }

    /**
     * Adding of a source (without to-dos)
     *
     * @param source
     */
    public final void addSource(Source source) {
        for (SynchronizationMediator mediator : sourceMediators) {
            if (mediator.getSource().equals(source)) {
                return;
            }
        }
        if (source == null) {
        	return; //JH
        }
        SynchronizationMediator mediator = source.addDependentModelElement(this);
        sourceMediators.add(mediator);
        setModifiedAtToNow();
    }

    /**
     * The synchronization-mediator that has source is located, for then remove
     * the mediator from the source and also from the sourceMediators of this
     * model-element.
     *
     * @param source of this model-element
     */
    public void removeSource(Source source) {
        for (SynchronizationMediator mediator : sourceMediators) {
            if (mediator.getSource().equals(source)) {
                removeBackward(mediator);
                sourceMediators.remove(mediator);
                setModifiedAtToNow();
                return;
            }
        }
    }

    public void remove() throws ChangeNotAllowedException {
        if (isLonely()) {
            // remove mediators which act like source mediator
            removeSourceMediators();
            getParent().remove(this);

        } else {
            removeDependentMediators();
            removeSourceMediators();
            // removing at parent will be considered via callback of removeBackward
            // because some model elements may not be removed unconditionally
        }

    }

    @Override
    protected void removeBackward(SynchronizationMediator mediator) {
        super.removeBackward(mediator);
        // move up to derived classes? :
        if (isLonely() && !isManuallyCreated()) {
            getParent().remove(this);
        }
        setModifiedAtToNow();
    }

    private void removeSourceMediators() {
        List<SynchronizationMediator> copy = new ArrayList<>(sourceMediators);
        for (SynchronizationMediator sourceMediator : copy) {
            sourceMediator.removeBackward();
        }
        sourceMediators.clear();
    }

    void removeForward(SynchronizationMediator sourceMediator) {
        if (!sourceMediators.contains(sourceMediator)) {
            System.out.println("mediator with source " + sourceMediator.getSource() + ";" + sourceMediator.getSource().getClass()
                    + " and dependent " + sourceMediator.getDependentModelElement() + ";" + sourceMediator.getDependentModelElement().getClass() + " without source");
        } else {
            sourceMediators.remove(sourceMediator);
            if (sourceMediators.isEmpty()) {
                try {
                    remove();
                    setModifiedAtToNow();
                } catch (ChangeNotAllowedException ex) {
                    Logger.getLogger(ModelElement.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                setModifiedAtToNow();
            }
        }
    }

    @Override
    public boolean isLonely() {
        for (SynchronizationMediator dependentMediator : mediators) {
            if (!dependentMediator.getDependentModelElement().equals(parent)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return name of the model-element
     */
    public abstract String getName();

    @Override
    public String toString() {
        return "model element with name " + getName();
    }

    @Override
    public abstract boolean equals(Object object);

}
