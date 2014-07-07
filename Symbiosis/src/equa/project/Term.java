/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author frankpeeters
 */
@Entity
public abstract class Term implements ITerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public Term() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
