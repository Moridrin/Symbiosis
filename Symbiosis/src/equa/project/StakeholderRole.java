/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Stakeholder")
public class StakeholderRole extends ProjectRole { //implements IParticipant {

    private static final long serialVersionUID = 1L;

    public StakeholderRole() {
    }

    StakeholderRole(String name, String role) {
        setName(name);
        setRole(role);
    }

    @Override
    public String toString() {
        if (getRole().equalsIgnoreCase("stakeholder")) {
            return getName() + " [Stakeholder]";
        } else {
            return getName() + " [Stakeholder --> " + getRole() + "]";
        }
    }
}
