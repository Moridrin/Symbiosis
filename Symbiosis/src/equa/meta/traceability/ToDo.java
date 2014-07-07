package equa.meta.traceability;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author FrankP
 */
@Entity
public class ToDo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String description;
    @Column
    private Impact impact;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private SynchronizationMediator synch;

    public ToDo() {
    }

    /**
     * creation of a to-do with given description and impact
     *
     * @param descr
     * @param impact
     */
    public ToDo(String descr, Impact impact, SynchronizationMediator synch) {
        this.description = descr;
        this.impact = impact;
        this.synch = synch;
    }

    /**
     *
     * @return the description of this to-do
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * setting of the description iof this to-do
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return the impact of this to-do
     */
    public Impact getImpact() {
        return this.impact;
    }

    /**
     * setting of the impact of this to-do
     *
     * @param impact
     */
    public void setImpact(Impact impact) {
        this.impact = impact;
    }

    boolean remove() {
        return synch.removeToDo(this);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
