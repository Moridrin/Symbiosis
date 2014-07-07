/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code;

import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public class NameSpace implements Serializable {

    private static final long serialVersionUID = 1L;
    private NameSpace subN;
    private String name;

    public NameSpace(String rootName) {
        name = rootName;
    }

    public NameSpace addSubNameSpace(String name) {
        subN = new NameSpace(name);
        return this;
    }

    public NameSpace getSubNameSpace() {
        return subN;
    }

    public boolean hasSub() {
        return subN != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
