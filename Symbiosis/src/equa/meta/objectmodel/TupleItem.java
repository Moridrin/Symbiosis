package equa.meta.objectmodel;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 *
 * @author FrankP
 */
@Entity
public class TupleItem implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    private Role role;
    @OneToOne
    private Value value;

    public TupleItem() {
    }

    /**
     *
     * @param value
     * @param role
     */
    TupleItem(Value value, Role role) {
        this.role = role;
        this.value = value;
    }

    /**
     *
     * @return the object expression of this tuple item
     */
    String substitutionExpression() {
        return getType().makeExpression(value);
    }

    /**
     *
     * @return the value of this tupleItem
     */
    public Value getValue() {
        return value;
    }

    /**
     *
     * @return the role where this tuple item refers to
     */
    public Role getRole() {
        return role;
    }

    /**
     *
     * @return the type of this item
     */
    public SubstitutionType getType() {
        if (value.getType() instanceof FactType) {
            return ((FactType) value.getType()).getObjectType();
        }
        return (SubstitutionType) value.getType();
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(((TupleItem) obj).value);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
