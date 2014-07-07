/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.factbreakdown.ParentNode;
import equa.meta.ChangeNotAllowedException;
import equa.meta.MismatchException;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.traceability.Category;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;
import equa.meta.traceability.Reviewable;
import equa.meta.traceability.Source;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author FrankP
 */
@Entity
public class Population extends ParentElement implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<Tuple> tuples;

    public Population() {
    }

    /**
     * creation of an empty population in behalf of parent; expected size of
     * population for the future will get a default size
     *
     * @param defaultSize
     */
    Population(FactType parent, int defaultSize) {
        super(parent);
        tuples = new ArrayList<>(defaultSize);
    }

    /**
     *
     * @return an iterator over the population of tuples of the concerning
     * facttype
     */
    public Iterator<Tuple> tuples() {
        return tuples.iterator();
    }

    /**
     * a new tuple, if necessary, will be created; if this tuple doesn't equals
     * any other tuple with the same value, then the newly created tuple will be
     * registered.
     *
     * @param values values.length = number of roles of the concerning facttype
     * @param types types.length = number of roles of the concerning facttype;
     * concrete type of according value
     * @param source the source of values
     * @return the tuple which corresponds with values
     */
    Tuple addTuple(List<Value> values, List<Role> roles, List<SubstitutionType> types,
            FactRequirement source) {
        return addTupleIfNecessary(values, roles, types, source);
    }

    void addTuple(Tuple t) {
        if (!tuples.contains(t)) {
            tuples.add(t);
        }
    }

    private Tuple addTupleIfNecessary(List<Value> values, List<Role> roles,
            List<SubstitutionType> types, FactRequirement source) {
        Tuple tuple;
        int index = searchTuple(values, types);
        if (index == -1) {
            tuple = new Tuple(values, roles, this, source);
            tuples.add(tuple);

        } else {
            tuple = tuples.get(index);
            tuple.addSource(source);
        }
        return tuple;
    }

    private Tuple addTupleIfNecessary(List<Value> values, List<Role> roles,
            List<SubstitutionType> types, List<Source> sources) {
        Tuple tuple;
        int index = searchTuple(values, types);
        if (index == -1) {
            tuple = new Tuple(values, roles, this, sources);
            tuples.add(tuple);

        } else {
            tuple = tuples.get(index);
            for (Source source : sources) {
                tuple.addSource(source);
            }
        }
        return tuple;
    }

    Tuple addTuple(List<Value> values, List<Role> roles,
            FactRequirement source) {
        ArrayList<SubstitutionType> types = new ArrayList<>();
        for (Role role : roles) {
            types.add(role.getSubstitutionType());
        }

        return addTupleIfNecessary(values, roles, types, source);
    }

    Tuple addTuple(List<Value> values, List<Role> roles,
            List<Source> sources) {
        ArrayList<SubstitutionType> types = new ArrayList<>();
        for (Role role : roles) {
            types.add(role.getSubstitutionType());
        }

        return addTupleIfNecessary(values, roles, types, sources);
    }

    Tuple addTuple(Value value, ObjectRole or, List<Source> sources) {
        ArrayList<Value> values = new ArrayList<>();
        values.add(value);
        ArrayList<SubstitutionType> types = new ArrayList<>();
        ObjectType ot = or.getSubstitutionType();
        types.add(ot);
        int tupleIndex = this.searchTuple(values, types);
        Tuple tuple;
        if (tupleIndex == -1) {
            tuple = new Tuple(value, ot.getFactType().roles.get(0), this, sources);
        } else {
            tuple = tuples.get(tupleIndex);
        }
        return tuple;
    }

    CollectionTuple addCollectionTuple(List<Value> values, Source source) throws MismatchException {
        String collection = values.toString();
        CollectionType ct = (CollectionType) ((FactType) getParent()).getObjectType();
        CollectionTuple collectionTuple = null;
        for (Tuple tuple : tuples) {
            if (tuple.toString().equals(collection)) {
                collectionTuple = (CollectionTuple) tuple;
                collectionTuple.addSource(source);
            }
        }
        if (collectionTuple == null) {
            BaseValue bv;
            bv = new BaseValue(collection, BaseType.STRING);
            collectionTuple = new CollectionTuple(this, bv, values, source);
            tuples.add(collectionTuple);
        }

        RequirementModel rm = ((ObjectModel) getParent().getParent()).getProject().getRequirementModel();
        ct.addElements(collectionTuple, values, rm);
        return collectionTuple;
    }

    void addSingleton(Singleton singleton) {
        tuples.add(singleton);

    }

    int searchTuple(List<Value> values, List<SubstitutionType> types) {
        for (int i = 0; i < tuples.size(); i++) {
            if (tuples.get(i).hasSameValue(values, types)) {
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @param t
     * @return any tuple in the population of the concerning facttype which uses
     * t, if such a tuple doesn't exists null will be returned
     */
    Tuple anyTupleWhichUses(Tuple t) {
        for (Tuple tuple : tuples) {
            if (tuple.uses(t)) {
                return tuple;
            }
        }
        return null;
    }

    /**
     *
     * @param t
     * @return any tuple out of the population of the concerning facttype which
     * uses t, with respect to role with number nr if such a tuple doesn't
     * exists null will be returned
     */
    Tuple anyTupleWhichUses(Tuple t, int nr) {
        if (nr == -1) {
            throw new RuntimeException("facttype doesn't know role");
        }

        for (Tuple tuple : tuples) {
            if (tuple.getItem(nr).getValue() == t) {
                return tuple;
            }
        }
        return null;
    }

    /**
     * tuple with this nr will be removed from the list of tuples known at the
     * concerning facttype; if this tuple doesn't exist nothing changes; if this
     * tuple refers to an object which plays a role somewhere else, an exception
     * will be raised
     *
     * @param nr 0 <= nr < size of population of this facttype @throws
     * ChangeNotAllowedExcep tion if this tuple refers to an object which plays
     * a role somewhere else
     */
//    void removeTuple(int nr) throws ChangeNotAllowedException {
//        if (nr < 0 || nr >= tuples.size()) {
//            return;
//        }
//        if (parent.isObjectType()) {
//            if (parent.getObjectType().searchForUseOf(tuples.get(nr)) != null) {
//                throw new ChangeNotAllowedException("object with number " + nr
//                        + " is in use at population of " + parent.getName());
//            }
//        }
//        Tuple tuple = tuples.get(nr);
//        tuple.removeSources();
//        tuples.remove(nr);
//
//        publisher.inform(this, "tuple", null, tuple);
//    }
//    void removeTuples(int nr, Source source) throws ChangeNotAllowedException {
//        if (nr < 0 || nr >= tuples.size()) {
//            return;
//        }
//        if (parent.isObjectType()) {
//            if (parent.getObjectType().searchForUseOf(tuples.get(nr)) != null) {
//                throw new ChangeNotAllowedException("object with number " + nr
//                        + " is in use at population of " + parent.getName());
//            }
//        }
//        Tuple tuple = tuples.get(nr);
//        if (tuple.sources().contains(source)) {
//            tuple.removeSource(source);
//            if (tuple.sources().isEmpty()) {
//                tuples.remove(nr);
//            }
//            tuple.removeItems(source);
//        }
//
//        publisher.inform(this, "tuple", null, tuple);
//    }
    /**
     * all tuples will be thrown away; if this facttype behaves like an
     * objecttype then the population of the facttype where this objecttype
     * plays a role is cleared too
     */
    public void clearPopulation() {
        if (((FactType) getParent()).isObjectType()) {
            ((FactType) getParent()).getObjectType().resignTuplesFromAllRoles();
        }

        for (Tuple t : tuples) {
            try {
                t.remove();
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(Population.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        tuples.clear();

    }

    void changeBaseValueRole(int baseValueRoleNr, BaseType bt) {
        String btName = bt.getName();
        for (Tuple tuple : tuples) {
            try {
                bt.checkSyntaxis(tuple.getItem(baseValueRoleNr).getValue().toString());
            } catch (MismatchException ex) {
                ((BaseValue) tuple.getItem(baseValueRoleNr).getValue()).makeInvalid(btName);
            }
        }
    }

    /**
     *
     * @param ot
     * @return any tuple out of the population of this facttype which uses some
     * object of pop; if such a tuple doesn't exist null will be returned
     */
    Tuple usesTuplesOf(Population pop) {
        Iterator<Tuple> it = pop.tuples();
        while (it.hasNext()) {
            Tuple tuple = anyTupleWhichUses(it.next());
            if (tuple != null) {
                return tuple;
            }
        }
        return null;
    }

    int size() {
        return tuples.size();
    }

    void removeAssociationsWithSource(Source source, ObjectModel om)
            throws ChangeNotAllowedException {
        ArrayList<Tuple> toRemove = new ArrayList<>();
        List<Tuple> copyTuples = new ArrayList<>(tuples);
        for (Tuple tuple : copyTuples) {
            if (tuple.sources().contains(source)) {
                tuple.removeSource(source);
                if (tuple.sources().isEmpty()) {
                    toRemove.add(tuple);
                }
            }
        }
        tuples.removeAll(toRemove);
        for (Tuple tuple : toRemove) {
            tuple.removeAssociationsWithSource(source, om);
        }

    }

    Tuple getTuple(ParentNode node) {
        for (Tuple tuple : tuples) {
            if (tuple.toString().equalsIgnoreCase(node.getText())) {
                return tuple;
            }
        }
        return null;
    }

//    Tuple addCollectionTuple(List<Value> values, boolean sequence, FactRequirement source,
//            RequirementModel rm) throws MismatchException {
//        FactType ft = (FactType) this.getParent();
//        CollectionType ct = (CollectionType) ft.getObjectType();
//        CollectionTypeExpression cte = (CollectionTypeExpression) ct.getOTE();
//        String collectionString = cte.makeExpression(values);
//
//        for (Tuple tuple : tuples) {
//            if (tuple.toString().equals(collectionString)) {
//                tuple.addSource(source);
//                return tuple;
//            }
//        }
//
//        Tuple collection = new CollectionTuple(ft.getPopulation(), new BaseValue(ct.nextId(),BaseType.STRING), source);
//        tuples.add(collection);
//        ct.addElements(collection, values, rm);
//
//        return collection;
//    }
    void replaceTupleItems(int roleNr, ObjectRole or) {
        for (Tuple tuple : tuples) {
            tuple.replaceItem(roleNr, or);
        }
    }

    int getTupleIndex(Tuple aTuple) {
        return tuples.indexOf(aTuple);
    }

    String getFactTypeName() {
        return getParent().getName();
    }

    @Override
    public void remove(ModelElement member) {
        if (member instanceof Tuple) {
            tuples.remove(member);
        }
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        ArrayList<Tuple> copyTuples = new ArrayList<>(tuples);
        for (int i = 0; i < copyTuples.size(); i++) {
            copyTuples.get(i).remove();
        }
        super.remove();
    }

    Category getCategory() {
        Category def = ((ObjectModel) getParent().getParent()).getProject().getDefaultCategory();
        if (size() == 0) {
            return def;
        } else {

            if (tuples.get(0).sources().isEmpty()) {
                return def;
            }
            Source source = tuples.get(0).sources().get(0);
            if (source instanceof Reviewable) {
                return ((Reviewable) source).getCategory();
            }
            return def;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return "Population of " + getParent().getName();
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }
}
