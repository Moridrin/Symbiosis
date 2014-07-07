/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.List;

import equa.actioncase.ActorInputItem;

/**
 *
 * @author frankpeeters
 */
public interface IActionOperation extends IRelationalOperation {

    /**
     *
     * @return the required input items of this operation
     */
    List<ActorInputItem> inputItems();
}
