/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.requirements;

/**
 *
 * @author frankpeeters
 */
public interface RequirementFilter {

    /**
     *
     * @param requirement
     * @return true if requirement satisfies this filter, otherwise false
     */
    boolean acccepts(Requirement requirement);

}
