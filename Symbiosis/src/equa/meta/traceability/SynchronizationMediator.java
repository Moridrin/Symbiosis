package equa.meta.traceability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author FrankP
 */
@Entity
public class SynchronizationMediator implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Source source;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "synch")
    private Collection<ToDo> toDos;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private ModelElement dependentModelElement;

    public SynchronizationMediator() {
    }

    /**
     * a node connected with source with zero todo's will be created
     *
     * @param source
     * @param dependentModelElement
     * @return
     */
    SynchronizationMediator(Source source, ModelElement dependentModelElement) {
        this.source = source;
        toDos = new ArrayList<>(2);
        this.dependentModelElement = dependentModelElement;
    }

    /**
     * change will be registered at this mediator
     *
     * @param descr
     * @param impact
     */
    void addToDo(String descr, Impact impact) {
        toDos.add(new ToDo(descr, impact, this));
        //     modelElement.needsReview();
    }

    /**
     * todo will be removed at this node
     *
     * @param toDo
     * @return true if this mediator doesn't keeps a toDo anymore
     */
    boolean removeToDo(ToDo toDo) {
        toDos.remove(toDo);
        return toDos.isEmpty();
    }

    /**
     *
     * @return an iterator on all todo's known by this mediator
     */
    public Iterator<ToDo> toDos() {
        return toDos.iterator();
    }

    /**
     *
     * @return the highest impact of to-dos
     */
    public Impact getImpactOfToDos() {
        Impact impact = Impact.ZERO;
        for (ToDo toDo : toDos) {
            if (toDo.getImpact().getOrder() > impact.getOrder()) {
                impact = toDo.getImpact();
            }
        }

        return impact;
    }

    /**
     *
     * @return the model element where this mediator mediates with
     */
    public ModelElement getDependentModelElement() {
        return dependentModelElement;
    }

    /**
     *
     * @return the source of the mediator
     */
    public Source getSource() {
        return source;
    }

    /**
     * If object is not an instance of SynchronizationMediator, false is
     * automatically returned.
     *
     * @param object that should be an instance of SynchronizationMediator.
     * @return true if the source of this mediator equals the source of the
     * object.
     */
    @Override
    public boolean equals(Object object) {
        SynchronizationMediator mediator = (SynchronizationMediator) object;
        return source.equals(mediator.source);
    }

   
    
     
    protected void removeForward() {
        dependentModelElement.removeForward(this);
    }
    
    void removeBackward() {
        source.removeBackward(this);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
