/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.Iterator;

/**
 *
 * @author frankpeeters
 */
public interface IFormalPredicate extends IPredicate {

    DisjunctionCall disjunctionWith(BooleanCall call);

    ConjunctionCall conjunctionWith(BooleanCall call);

    boolean isNegated();

    Iterator<BooleanCall> operands();
}
