package equa.project;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discr", discriminatorType = DiscriminatorType.STRING)
public abstract class ProjectRole implements Comparable<ProjectRole>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long projectRoleId;
    @Column
    private RoleId id;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor. Initializes instance with a new {@link RoleId}.
     */
    public ProjectRole() {
        id = new RoleId();
    }

    /**
     *
     * @param id to set as {@link RoleId}.
     */
    void setId(RoleId id) {
        this.id = id;
    }

    public String getName() {
        return id.getName();
    }

    public String getRole() {
        return id.getRole();
    }

    /**
     *
     * @param name to set to this instance.
     */
    void setName(String name) {
        id.setName(name);
    }

    /**
     *
     * @param role to set to the role of the {@link RoleId} of this instance.
     */
    void setRole(String role) {
        id.setRole(role);
    }

    @Override
    public int compareTo(ProjectRole t) {
        int c = getName().toLowerCase().compareTo(t.getName().toLowerCase());
        if (c == 0) {
            return getRole().toLowerCase().compareTo(t.getRole().toLowerCase());
        } else {
            return c;
        }
    }

    public long getProjectRoleId() {
        return projectRoleId;
    }

    public void setProjectRoleId(long projectRoleId) {
        this.projectRoleId = projectRoleId;
    }

}
