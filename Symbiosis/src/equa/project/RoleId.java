/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 *
 * @author frankpeeters
 */
@Embeddable
public class RoleId implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String role;

    /**
     * Constructor. Initializes instance with empty name and role.
     */
    public RoleId() {
    }

    /**
     *
     * @return name of this roleId
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name to set to this roleId
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return role of this roleId
     */
    public String getRole() {
        return role;
    }

    /**
     *
     * @param role to set to this roleId
     */
    public void setRole(String role) {
        this.role = role;
    }
}
