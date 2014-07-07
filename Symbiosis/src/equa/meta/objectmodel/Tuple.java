package equa.meta.objectmodel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.FactRequirement;
import equa.meta.traceability.Source;
import equa.util.Naming;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 *
 * @author FrankP
 */
@Entity
public class Tuple extends Value implements ObjectModelRealization {

    private static final long serialVersionUID = 1L;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<TupleItem> items;

    public Tuple() {
        items = new ArrayList<>();
    }

    /**
     * substitionvalues matches with substitutiontypes of roles; use this
     * constructor in case of abstract roles
     *
     * a new Tuple has been created and returned
     *
     * @param values values.length == roles.size()
     * @param roles
     * @param source
     */
    Tuple(List<Value> values, List<Role> roles,
            Population parent, FactRequirement source) {
        super(parent, source);
        items = new ArrayList<>();
        int i = 0;
        for (Role role : roles) {
            items.add(new TupleItem(values.get(i), role));
            i++;
        }
    }

    Tuple(List<Value> values, List<Role> roles,
            Population parent, List<Source> sources) {
        super(parent, sources.get(0));
        items = new ArrayList<>();
        int i = 0;
        for (Role role : roles) {
            items.add(new TupleItem(values.get(i), role));
            i++;
        }
        for (int j = 1; j < sources.size(); j++) {
            addSource(sources.get(j));
        }
    }

    /**
     * in behalf of singleton
     *
     * @param source
     */
    Tuple(Population parent, Source source) {
        super(parent, source);
        items = new ArrayList<>(0);
    }

    Tuple(Value value, Role idRole, Population parent, Source source) {
        super(parent, source);
        items = new ArrayList<>();

        items.add(new TupleItem(value, idRole));
       
    }
    
    Tuple(Value value, Role idRole, Population parent, List<Source> sources) {
        super(parent, sources.get(0));
        items = new ArrayList<>();

        items.add(new TupleItem(value, idRole));
        for (int i=1; i<sources.size(); i++){
            sources.add(sources.get(i));
        }
       
    }

    /**
     *
     * @param i 0<= i < size of facttype of the population where this tuple
     * belongs to @return the i-th tuple-item of this tuple
     */
    public TupleItem getItem(int i) {
        return items.get(i);
    }

    /**
     *
     * @return an iterator over all the items of this tuple
     */
    public Iterator<TupleItem> items() {
        return items.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple tuple = (Tuple) obj;
            for (int i = 0; i < items.size(); i++) {
                if (!items.get(i).equals(tuple.items.get(i))
                        && !(items.get(i).getValue() instanceof UnknownValue)
                        && !(tuple.items.get(i).getValue() instanceof UnknownValue)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @return the object expression if the parent of this tuple possesses a FTE
     * otherwise the fact expression
     */
    @Override
    public String toString() {
        if (getType() instanceof ObjectType) {
            return ((ObjectType) getType()).makeExpression(this);
        } else {
            return Naming.withCapital(((FactType) getType()).makeExpression(this));
        }
    }

    boolean hasSameValue(List<Value> values, List<SubstitutionType> types
    ) {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).getType().equals(types.get(i))
                    || !items.get(i).getValue().equals(values.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param t
     * @param r
     * @return true if t is used by one of the tuple items else false
     */
    boolean uses(Tuple t) {
        for (TupleItem item : items) {
            if (item.getValue() == t) {
                return true;
            }
        }
        return false;
    }

    void removeItems(Source source
    ) {
        for (TupleItem item : items) {
            Value value = item.getValue();
            if (value instanceof Tuple) {
                Tuple tuple = (Tuple) value;
                if (tuple.sources().contains(source)) {
                    tuple.removeSource(source);
                    if (tuple.sources().isEmpty()) {
                        if (!tuple.items.isEmpty()) {
                            //tuple.items.get(0).getRole().getParent().getPopulation()
                        }
                    }
                }
            }
        }
    }

    @Override
    public Type getType() {
        if (items.isEmpty()) {
            throw new UnsupportedOperationException("todo");
        } else {
            FactType ft = items.get(0).getRole().getParent();
            if (ft.isObjectType()) {
                return ft.getObjectType();
            } else {
                return ft;
            }
        }
    }

    void removeAssociationsWithSource(Source source, ObjectModel om)
            throws ChangeNotAllowedException {
        for (TupleItem item : items) {
            SubstitutionType st = item.getType();
            if (st instanceof ObjectType) {
                ((ObjectType) st).getFactType().removeAssociationsWithSource(source, om);
            }
        }
    }

    void merge(int[] mapping, TupleItem newObjectTuple
    ) {
        for (int i = mapping.length - 1; i >= 0; i--) {
            if (mapping[i] >= 0) {
                items.remove(i);
            }
        }
        items.add(newObjectTuple);
    }

    void replaceItem(int roleNr, ObjectRole or
    ) {
        ConstrainedBaseType cbt = (ConstrainedBaseType) or.getSubstitutionType();
        TupleItem item = items.get(roleNr);
        Tuple tuple = cbt.getFactType().getPopulation().addTuple(item.getValue(),
                or, this.sources());
        items.set(roleNr, new TupleItem(tuple, or));
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        for (TupleItem item : items) {
            if (item.getValue() instanceof Tuple) {
                Tuple subtuple = (Tuple) item.getValue();
                subtuple.remove();
            }
        }
        super.remove();
        ((Population) getParent()).remove(this);
    }

    /**
     *
     * @return the number of this tuple within his population; numbering starts
     * at 1;
     */
    public int getNr() {
        return ((Population) getParent()).getTupleIndex(this) + 1;
    }

    @Override
    public String getName() {
        return "tuple " + getNr() + " of " + ((Population) getParent()).getFactTypeName();
    }

    void deobjectifyItem(int index, List<Role> idRolesNew) {
        TupleItem item = items.get(index);
        Tuple otValue = (Tuple) item.getValue();
        int i = index;
        items.remove(i);
        int j = 0;
        for (TupleItem otItem : otValue.items) {
            items.add(i, new TupleItem(otItem.getValue(),idRolesNew.get(j)));
            i++;
            j++;
        }
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent().getParent();
    }

    @Override
    public String getRequirementText() {
        return toString();
    }
}
