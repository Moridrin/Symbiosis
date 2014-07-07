package equa.meta.objectmodel;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import equa.meta.MismatchException;

/**
 * lower and upper are basevalues belonging to the same basetype
 */
@Entity
public class Range implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private BaseValue lower;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private BaseValue upper;

    public Range() {
    }

    /**
     * creation of a range [lower..upper]
     *
     * @param lower lower is smaller then upper;
     * @param upper lower and upper belong to the same basetype
     */
    public Range(BaseValue lower, BaseValue upper) throws MismatchException {
        this.lower = lower;
        this.upper = upper;
        check(lower, upper);
    }

    /**
     *
     * @return the lower bound of this range
     */
    public BaseValue getLower() {
        return this.lower;
    }

    /**
     *
     * @return the upper bound of this range
     */
    public BaseValue getUpper() {
        return this.upper;
    }

    public int countAcceptedElements() {
        if (lower.getType().isNumber()) {
            int upperInt = Integer.parseInt(upper.toString());
            int lowerInt = Integer.parseInt(lower.toString());
            return upperInt - lowerInt + 1;
        } else {
            return -1;
        }
    }

    /**
     * lower must be smaller then upper and belonging to the same basetype
     *
     * @param lower
     * @param upper
     */
    public void changeBounds(BaseValue lower, BaseValue upper) throws MismatchException {
        check(lower, upper);
        this.lower = lower;
        this.upper = upper;
    }

    private void check(BaseValue lower, BaseValue upper) throws MismatchException {
        if (!lower.getType().equals(upper.getType())) {
            throw new MismatchException(null, "mismatch in types of bounds");
        }
        if (lower.compareTo(upper) >= 0) {
            throw new RuntimeException("lower has to be smaller than upper");
        }
    }

    /**
     *
     * @param value
     * @return true if value is part of this range else false
     */
    public boolean contains(BaseValue value) {
        if (value.getType() != lower.getType()) {
            return false;
        }
        return lower.compareTo(value) <= 0 && value.compareTo(upper) <= 0;
    }

    /**
     *
     * @param range
     * @return false if intersection of this object with range is empty else
     * true
     */
    public boolean hasOverlapWith(Range range) {
        return contains(range.lower) || contains(range.upper) || range.contains(lower) || range.contains(upper);
    }

    @Override
    public String toString() {
        return lower.toString() + ".." + upper.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Range) {
            Range range = (Range) object;
            return lower.equals(range.lower) && upper.equals(range.upper);
        } else {
            return false;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
