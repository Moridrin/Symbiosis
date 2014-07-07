/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.io.Serializable;

import equa.code.Language;

/**
 *
 * @author frankpeeters
 */
public interface ActualParam extends Serializable {

    String expressIn(Language l);

    String callString();
}
