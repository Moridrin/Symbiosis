/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 *
 * @author FrankP
 */
@Entity
public class UnidentifiedObject extends Value {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private UnidentifiedObjectType type;

    public UnidentifiedObject() {
    }

    /**
     * creation of an object without identification structure
     *
     * @param type
     */
    public UnidentifiedObject(UnidentifiedObjectType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnidentifiedObject) {
            return ((UnidentifiedObject) obj).type == type;
        } else {
            return false;
        }
    }

    /**
     *
     * @return the type of this value object
     */
    @Override
    public UnidentifiedObjectType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.makeExpression(null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return toString();
    }
}
