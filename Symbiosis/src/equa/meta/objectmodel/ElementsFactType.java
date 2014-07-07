/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.List;

import equa.meta.ChangeNotAllowedException;
import equa.meta.MismatchException;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author frankpeeters
 */
public abstract class ElementsFactType extends FactType {

    private static final long serialVersionUID = 1L;
    private CollectionType collectionType;

    public ElementsFactType(String ftname, CollectionType ct, ObjectModel parent, FactRequirement source) {
        super(ftname, parent, source);
        collectionType = ct;
    }

    public abstract SubstitutionType getElementType();

    public abstract Role getElementRole();

    public CollectionType getCollectionType() {
        return collectionType;
    }

    public abstract List<Value> elementsOf(String collectionID);

    abstract void addFacts(Tuple collection, List<Value> values,
            RequirementModel rm) throws MismatchException;

    @Override
    public ObjectRole mergeRoles(int[] mapping, ObjectType ot) throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("roles of elements fact type cannot be merged");

    }

    @Override
    public void objectify(List<String> constants, List<Integer> roleNumbers)
            throws ChangeNotAllowedException {
        throw new ChangeNotAllowedException("roles of elements fact type cannot be objectified");
    }

    @Override
    public void objectify() {
    }


    @Override
    public boolean isMissingResponsibleRole() {
        if (collectionType.getFactType().isMutable()) {
            boolean result = false;
            for (Role role : roles) {
                if (role.isResponsible() || (role.isNavigable() && !role.isMultiple() && role.isMandatory())) {
                    return false;
                } else if (role.isNavigable() && role.isMultiple() && !(role.getSubstitutionType() instanceof CollectionType)) {
                    result = true;
                }
            }
            return result;
        } else {
            return false;
        }
    }

    void removeTuplesOf(CollectionTuple collection) {
        Population pop = getPopulation();
        Iterator<Tuple> itTuples = pop.tuples();
        List<Tuple> toRemove = new ArrayList<>();
        while (itTuples.hasNext()) {
            Tuple tuple = (Tuple) itTuples.next().getItem(0).getValue();
            if (tuple.equals(collection)) {
                toRemove.add(tuple);
            }
        }
    
        for (Tuple t : toRemove) {
            pop.remove(t);
        }
    }
}
