/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.code.Field;
import equa.meta.classrelations.Relation;

/**
 *
 * @author frankpeeters
 */
public interface IRelationalOperation {

    /**
     *
     * @return the relation where this operation is derived from
     */
    Relation getRelation();

    /**
     *
     * @return the field where this operation is related to; the field could be
     * null in case of a derivable relation
     */
    Field getField();
}
