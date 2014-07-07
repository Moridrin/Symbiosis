/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import equa.meta.traceability.Source;

/**
 *
 * @author FrankP
 */
@Entity
public class Singleton extends Tuple {

    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private SingletonObjectType ot;

    public Singleton() {
    }

    /**
     * creation of a singleton object belonging to ot, on base of source
     *
     * @param ot
     * @param source
     */
    public Singleton(SingletonObjectType ot, Source source) {
        super(ot.getFactType().getPopulation(), source);
        this.ot = ot;
    }

    @Override
    public String toString() {
        // a singleton doesn't have a FTE
        return ot.makeExpression(this);
    }

    @Override
    public void remove() {
    }

    @Override
    public Type getType() {
        return ot;
    }

}
