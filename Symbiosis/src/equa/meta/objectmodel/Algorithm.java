/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.io.Serializable;

import equa.code.IndentedList;
import equa.code.Language;

/**
 *
 * @author frankpeeters
 */
public class Algorithm implements Serializable {

    private static final long serialVersionUID = 1L;

    private Language language;
    private IndentedList code;
    private boolean editable, sealed;

    public Algorithm() {
        language = null;
        code = null;
        editable = true;
        sealed = false;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isEditable() {
        return editable;
    }
    
    public boolean isSealed() {
    	return sealed;
    }
    
    public void setSealed(boolean sealed) {
    	this.sealed = sealed;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public IndentedList getCode() {
        return code;
    }

    public void setCode(IndentedList code) {
        this.code = code;
    }
}
