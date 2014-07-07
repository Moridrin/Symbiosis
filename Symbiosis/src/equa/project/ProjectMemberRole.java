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
@Entity(name = "ProjectMember")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("projectMember")
public class ProjectMemberRole extends ProjectRole {//implements IParticipant {

    private static final long serialVersionUID = 1L;

    public ProjectMemberRole() {
    }

    ProjectMemberRole(String name, String role) {
        setName(name);
        setRole(role);
    }

    @Override
    public String toString() {
        if (getRole().equalsIgnoreCase("projectmember") || getRole().equalsIgnoreCase("project member")) {
            return getName() + " [ProjectMember]";
        } else {
            return getName() + " [ProjectMember --> " + getRole() + "]";
        }
    }
}
