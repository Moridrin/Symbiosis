/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.List;

/**
 *
 * @author frankpeeters
 */
public interface IOperation {

    /**
     *
     * @param actualParams
     * @return call of this operation with actualParams
     */
    Call call(List<? extends ActualParam> actualParams);

    /**
     *
     * @return call of this operation and if necessary with a default list of
     * actual params
     */
    Call call();

    /**
     *
     * @param actualParams
     * @return call string of this operation with actualParams
     */
    String callString(List<? extends ActualParam> actualParams);

    /**
     *
     * @return call string of this operation and if necessary with a default
     * list of actual params
     */
    String callString();
}
