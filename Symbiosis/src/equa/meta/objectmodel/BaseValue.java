package equa.meta.objectmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import equa.meta.MismatchException;

/**
 *
 * @author FrankP
 */
@Entity
public class BaseValue extends Value implements Comparable<Value> {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String value;

    public BaseValue() {
    }

    /**
     * creation of basevalue with given value and type
     *
     * @param value not empty
     * @param type
     */
    public BaseValue(String value, BaseType type) throws MismatchException {
        super(type);
        if (value.equals("")) {
            throw new MismatchException(null, "base values are never empty");
        }
        type.checkSyntaxis(value);
        this.value = value;

    }

    /**
     *
     * @return the type of this basevalue
     */
    @Override
    public BaseType getType() {
        return (BaseType) getParent();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseValue) {
            BaseValue bv = (BaseValue) obj;
            return value.compareTo(bv.value) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Value v) {

        BaseValue bv = (BaseValue) v;
        if (getType().isNumber()) {
            return Integer.parseInt(this.value) - Integer.parseInt(bv.value);
        } else if (getType().equals(BaseType.REAL)) {
            double d1 = Double.parseDouble(value);
            double d2 = Double.parseDouble(bv.value);
            if (d1 < d2) {
                return -1;
            }
            if (d1 > d2) {
                return +1;
            }
            return 0;
        } else if (getType().equals(BaseType.CHARACTER)) {
            return value.charAt(0) - bv.value.charAt(0);
        } else if (getType().equals(BaseType.STRING)) {
            return value.compareTo(bv.value);
        } else {
            return value.compareTo(bv.value);
        }

    }

    @Override
    public String toString() {
        return value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return value;
    }

    void makeInvalid(String btName) {
        value += "(INVALID " + btName + ")";
    }
}
