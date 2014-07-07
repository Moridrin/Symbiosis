/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.traceability.Source;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author frankpeeters
 */
public class CollectionTuple extends Tuple {

    private static final long serialVersionUID = 1L;
    private final List<Value> values;

    /**
     * creation of a singleton object belonging to ot, on base of source
     *
     * @param population
     * @param value
     * @param values
     * @param source
     */
    public CollectionTuple(Population population, BaseValue value, List<Value> values, Source source) {
        super(value, ((FactType)population.getParent()).roles.get(0),population, source);
        this.values = values;
    }

    @Override
    public String toString() {
        return ((CollectionTypeExpression)getType().getOTE()).makeExpression(values);
    }
   

    @Override
    public void remove() {
       getType().remove(this);
        try {
            super.remove();
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(CollectionTuple.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public CollectionType getType() {
        return (CollectionType) ((FactType) getParent().getParent()).getObjectType();
    }
    
    @Override
    public boolean equals(Object object){
        if (object instanceof CollectionTuple){
          CollectionTuple collectionTuple = (CollectionTuple)object;
          return  this.toString().equals(collectionTuple.toString());
        }
        return false;
    }
    
    public List<Value> values() {
        return Collections.unmodifiableList(values);
    }

}
