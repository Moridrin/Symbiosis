/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * objects of this class wil be uses in extraordinary situations whereby objects
 * with multiple identification structures lacks some information about one of
 * those id's
 *
 * @author frankpeeters
 */
@Entity
class UnknownValue extends Value {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private SubstitutionType st;

    public UnknownValue() {
    }

    public UnknownValue(SubstitutionType st) {
        this.st = st;
    }

    @Override
    public Type getType() {
        return st;
    }

    @Override
    public String toString() {
        return "UNKNOWN";
    }

    @Override
    public String getName() {
        return "UNKNOWN";
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof UnknownValue) {
            return ((UnknownValue) object).st == st;
        } else {
            return false;
        }
    }

    public SubstitutionType getSt() {
        return st;
    }

    public void setSt(SubstitutionType st) {
        this.st = st;
    }
}
