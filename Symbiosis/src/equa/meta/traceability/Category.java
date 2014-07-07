package equa.meta.traceability;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import equa.meta.DuplicateException;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementFilter;
import equa.project.Project;
import equa.project.ProjectRole;
import equa.project.StakeholderRole;

/**
 * Class to represent a Category of a {@link Requirement}s that belong to a
 * {@link Project}. <br>
 * This class contains constrained/unconstrained (overridden) R[etrieve],
 * constrained/unconstrained U[pdate] and constrained D[elete] operations. The
 * behavior is not precisely of a typical DTO, as this class includes
 * constraints implemented as logic in some retrieval or update operations.
 * Moreover, delete operation(s) are included. Thus, this behavior is proposed
 * as Data Regulated Transfer Object (DRTO).
 *
 * @author FrankP
 */
@Entity
public class Category implements Comparable<Category>, Serializable, RequirementFilter {

    private static final long serialVersionUID = 1L;

    /**
     * Static Category of requirements, see {@link SystemCategory}.
     */
    public static Category SYSTEM = SystemCategory.SYSTEM;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)//, generator = "cat_seq")
    private long id;
    @Column
    private String name;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Project project;
    @Column
    private String code;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, optional = true)
    //@PrimaryKeyJoinColumn(referencedColumnName = "name")
    private StakeholderRole owner;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, optional = true)
    //@PrimaryKeyJoinColumn(referencedColumnName = "name")
    private StakeholderRole viceOwner;

    /**
     * DEFAULT CONSTRUCTOR; creates a new instance of Category without
     * modification of any of its attributes.
     */
    public Category() {
    }

    /**
     * CONSTRUCTOR; code and name of the Category are assigned. The owner and
     * vice-owner are specified as <b>null</b>.
     *
     * @param code for the Category.
     * @param name for the Category.
     */
    public Category(String code, String name, Project project) {
        this.code = code;
        this.name = name;
        this.project = project;
        this.owner = null;
        this.viceOwner = null;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return name of this Category.
     */
    public String getName() {
        return name;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return code of this category.
     */
    public String getCode() {
        return code;
    }

    /**
     * Constrained UPDATE operation. First, this method checks if the
     * <code>code</code> is already in use by another Category. If the
     * <code>code</code> is not already used, it is assigned to this Category,
     * otherwise a {@link DuplicateException} is thrown.
     *
     * @param code to set to this Category.
     * @throws DuplicateException if the <code>code</code> is already used.
     */
    public void setCode(String code) throws DuplicateException {
        if (project.getCategory(code) != null) {
            throw new DuplicateException("there exists already a category with code " + code);
        }
        this.code = code;
    }

    /**
     * Constrained UPDATE operation. First, this method checks if the
     * <code>name</code> is empty. If the <code>name</code> is not empty, it is
     * assigned to this Category, otherwise a {@link RuntimeException} is
     * thrown. setting of the category-name; no check on unique names
     *
     * @param name that should not be empty.
     * @throws RuntimeException if the name is empty.
     */
    public void setName(String name) {
        if (name.isEmpty()) {
            throw new RuntimeException("name may not be empty");
        }
        this.name = name;
    }

    /**
     * Constrained RETRIEVE operation. First, if the owner and vice owner of
     * this Category are <b>null</b>, false is automatically returned.
     * Otherwise, the name of <code>projectRole</code> is compared with the
     * owner's name. If the names match, true is returned. If the names do not
     * match, the name of <code>projectRole</code> is compared with the vice
     * owner's name. If the names match true is returned, otherwise false is
     * returned.
     *
     * @param projectRole that should not be <b>null</b>. If it is <b>null</b>,
     * false is returned.
     * @return true if participant owns this category
     */
    public boolean isOwner(ProjectRole projectRole) {
        if (getOwner() == null && getViceOwner() == null) {
            return false;
        }
        if (projectRole != null) {
            if (projectRole.getName().equalsIgnoreCase(getOwner().getName())) {
                return true;
            } else {
                if (getViceOwner() == null) {
                    return false;
                } else {
                    return projectRole.getName().equalsIgnoreCase(getViceOwner().getName());
                }
            }
        }
        return false;
    }

    /**
     * Constrained UPDATE operation. First, this method checks if the current
     * owner of this Category is either <b>null<\b> or <code>owner</code>. If
     * any of those two cases is true, <code>newOwner</code> becomes the new
     * owner of this Category. Otherwise, no update is applied.
     *
     * @param owner of this Category, which is going to be replaced.
     * @param newOwner of this Category.
     */
    public void setOwner(StakeholderRole owner, StakeholderRole newOwner) {
        if (this.owner == null || isOwner(owner)) {
            this.owner = newOwner;
        }
    }

    /**
     * Constrained UPDATE operation. First, this method checks if
     * <code>owner</code> is actually the owner of this Category. If it is true,
     * <code>viceOwner</code> becomes the vice owner of this Category.
     * Otherwise, no update is applied.
     *
     * @param owner of this Category.
     * @param viceOwner of this Category.
     */
    public void setViceOwner(StakeholderRole owner, StakeholderRole viceOwner) {
        if (isOwner(owner)) {
            this.viceOwner = viceOwner;
        }
    }

    /**
     * Constrained DELETE operation. First, this method checks if
     * <code>owner</code> is actually the owner of this Category. If it is true,
     * <b>null</b> becomes the vice owner. Otherwise, no removal is applied.
     *
     * @param owner of this category.
     */
    public void removeViceOwner(StakeholderRole owner) {
        if (isOwner(owner)) {
            setViceOwner(null);
        }
    }

    /**
     * Constrained overridden RETRIEVE operation. The String representation of
     * this Category is generated with the name of this Category, the name of
     * its owner. If the name of the owner is undefined, "Owner undefined" is
     * used to represent that name.
     *
     * @return String with the name of this Category and the name of its owner.
     */
    @Override
    public String toString() {
        String s
                = getName() + " [" + code;
        if (owner != null) {
            s += ", " + owner.getName() + "]";
        } else {
            s += ", Owner undefined]";
        }
        return s;
    }

    /**
     * Unconstrained overridden RETRIEVE operation. Returns the comparison of
     * the code of this Category with the code of <code>category</code>, which
     * is performed with the {@link String#compareTo(java.lang.String)} method.
     *
     * @param category to be compared with this Category.
     * @return lower case comparison of codes with
     * {@link String#compareTo(java.lang.String)}.
     */
    @Override
    public int compareTo(Category category) {
        return getCode().toLowerCase().compareTo(category.getCode().toLowerCase());
    }

    /**
     * Constrained overridden RETRIEVE operation. First, only a not <b>null</b>
     * <code>other</code> Object is casted as a Category instance. Afterwards,
     * the code of <code>other</code> Category is compared with the code of this
     * Category. If these codes match, true is returned. In any other case,
     * false is returned.
     *
     * @param other object, which should not be <b>null</b>.
     * @return true if the codes match.
     */
    @Override
    public boolean equals(Object other) {
        if (other != null) {
            Category c = (Category) other;
            if (c.getCode() == this.getCode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return owner of this Category.
     */
    public StakeholderRole getOwner() {
        return owner;
    }

    /**
     * Constrained UPDATE operation. First, this method checks if the owner of
     * this category is <b>null</b>. The <code>ownerCandidate</code> is set as
     * the owner of this Category only if the current owner is <b>null</b>.
     *
     * @param ownerCandidate for this Category.
     * @throws RuntimeException if the owner of this Category is already known.
     */
    public void setOwner(StakeholderRole ownerCandidate) {
        if (this.owner != null) {
            throw new RuntimeException("owner is already known");
        }
        this.owner = ownerCandidate;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return viceOwner of this Category.
     */
    public StakeholderRole getViceOwner() {
        return viceOwner;
    }

    /**
     * Constrained UPDATE operation. First, this method checks if the vice owner
     * of this category is <b>null</b>. The <code>viceOwner</code> is set as the
     * vice owner of this Category only if the current vice owner is
     * <b>null</b>.
     *
     * @param viceOwner for this category.
     */
    public void setViceOwner(StakeholderRole viceOwner) {
        if (this.viceOwner != null) {
            throw new RuntimeException("vice owner is already known");
        }
        this.viceOwner = viceOwner;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return the {@link Project} of this Category.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Unconstrained UPDATE operation.
     *
     * @param project of this Category.
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Unconstrained RETRIEVE operation.
     *
     * @return id of this Category.
     */
    public long getId() {
        return id;
    }

    /**
     * Unconstrained UPDATE operation.
     *
     * @param id of this Category.
     */
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean acccepts(Requirement requirement) {
        return requirement.getCategory().equals(this);
    }
}
