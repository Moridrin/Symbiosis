/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.traceability;

import equa.meta.DuplicateException;
import equa.meta.requirements.Requirement;
import equa.project.ProjectRole;
import equa.project.StakeholderRole;

/**
 * Class to represent a particular and unique {@link Category}, which contains
 * several non-modifiable attributes. <br>
 * This class contains unconstrained overridden R[etrieve], unconstrained
 * overridden U[pdate] and unconstrained overridden D[elete] operations that
 * extend the {@link Category} class. This extension is a DRTO although it has a
 * DTO behavior (i.e., no logical constraints are done).
 *
 * @author frankpeeters
 */
public class SystemCategory extends Category {

    private static final long serialVersionUID = 1L;
    /**
     * Static {@link Category} of {@link Requirement}s.
     */
    public static SystemCategory SYSTEM = new SystemCategory();

    private SystemCategory() {
    }

    /**
     * Unconstrained overridden RETRIEVE operation.
     *
     * @return the static name of SystemCategory: <i>System</i>.
     */
    @Override
    public String getName() {
        return "System";
    }

    /**
     * Unconstrained overridden RETRIEVE operation.
     *
     * @return the static code of SystemCategory: <i>SYS</i>.
     */
    @Override
    public String getCode() {
        return "SYS";
    }

    /**
     * Unconstrained overridden UPDATE operation. This method overrides the
     * {@link Category#setCode(java.lang.String)} method: <code>code</code> is
     * ignored and no update is done on SystemCategory.
     *
     * @param code is ignored and no update is done on SystemCategory.
     * @throws DuplicateException is not thrown in this overridden method.
     */
    @Override
    public void setCode(String code) throws DuplicateException {
    }

    /**
     * Unconstrained overridden UPDATE operation. This method overrides the
     * {@link Category#setName(java.lang.String)} method: <code>name</code> is
     * ignored and no update is done on SystemCategory.
     *
     * @param name is ignored and no update is done on SystemCategory.
     */
    @Override
    public void setName(String name) {
    }

    /**
     * Unconstrained overridden RETRIEVE operation. This method overrides the
     * {@link Category#isOwner(equa.project.ProjectRole)} method:
     * <code>projectRole</code> is ignored and false is always returned.
     *
     * @param projectRole is ignored and false is always returned.
     * @return false is always returned.
     */
    @Override
    public boolean isOwner(ProjectRole projectRole) {
        return false;
    }

    /**
     * Unconstrained overridden UPDATE operation. This method overrides the
     * {@link Category#setOwner(equa.project.StakeholderRole, equa.project.StakeholderRole)}
     * method: <code>owner</code> and <code>newOwner</code> are ignored and no
     * update is done on SystemCategory.
     *
     * @param owner is ignored and no update is done on SystemCategory.
     * @param newOwner is ignored and no update is done on SystemCategory.
     */
    @Override
    public void setOwner(StakeholderRole owner, StakeholderRole newOwner) {
    }

    /**
     * Unconstrained overridden UPDATE operation. This method overrides the
     * {@link Category#setViceOwner(equa.project.StakeholderRole, equa.project.StakeholderRole)}
     * method: <code>owner</code> and <code>viceOwner</code> are ignored and no
     * update is done on SystemCategory.
     *
     * @param owner is ignored and no update is done on SystemCategory.
     * @param viceOwner is ignored and no update is done on SystemCategory.
     */
    @Override
    public void setViceOwner(StakeholderRole owner, StakeholderRole viceOwner) {
    }

    /**
     * Unconstrained overridden DELETE operation. This method overrides the
     * {@link Category#removeViceOwner(equa.project.StakeholderRole)} method:
     * <code>owner</code> is ignored and no removal is done on SystemCategory.
     *
     * @param owner is ignored and no removal is done on SystemCategory.
     */
    @Override
    public void removeViceOwner(StakeholderRole owner) {
    }

    /**
     * Unconstrained overridden RETRIEVE operation. The String representation of
     * SystemCategory is always "System [SYS]".
     *
     * @return the String "System [SYS]".
     */
    @Override
    public String toString() {
        return "System [SYS]";
    }

    /**
     * Unconstrained overridden UPDATE operation. This method overrides the
     * {@link Category#setOwner(equa.project.StakeholderRole)} method:
     * <code>owner</code> is ignored and no update is done on SystemCategory.
     *
     * @param owner is ignored and no update is done on SystemCategory.
     */
    @Override
    public void setOwner(StakeholderRole owner) {
    }

    /**
     * Unconstrained overridden UPDATE operation. This method overrides the {@link Category#setViceOwner(equa.project.StakeholderRole)
     * }
     * method: <code>viceOwner</code> is ignored and no update is done on
     * SystemCategory.
     *
     * @param viceOwner is ignored and no update is done on SystemCategory.
     */
    @Override
    public void setViceOwner(StakeholderRole viceOwner) {
    }
}
