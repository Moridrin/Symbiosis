/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

/**
 *
 * @author frankpeeters
 */
public interface ObjectModelRealization {

    /**
     *
     * @return the fact type which realizes the concerning requirement
     */
    FactType getFactType();

    /**
     *
     * @return the generated text of the concerning requirement
     */
    String getRequirementText();
}
