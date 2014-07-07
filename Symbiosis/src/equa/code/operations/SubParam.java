/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class SubParam extends Param {

    private static final long serialVersionUID = 1L;
    private Param parent;

    public SubParam(String name, STorCT type, Relation relation, Param parent) {
        super(name, type, relation);
        this.parent = parent;
    }

    public Param getParent() {
        return parent;
    }

    public Param getRoot() {
        Param root = parent;
        while (root instanceof SubParam) {
            root = ((SubParam) root).parent;
        }
        return root;
    }

    public void setNewRoot(SubParam sp) {
        Param root = this;
        while (root instanceof SubParam && ((SubParam) root).parent instanceof SubParam) {
            root = ((SubParam) root).parent;
        }
        ((SubParam) root).parent = sp;
    }

    @Override
    public String getName() {
        Param root = getRoot();
        if (root.getName().equalsIgnoreCase(super.getName())
                || getType() instanceof ConstrainedBaseType) {
            return root.getName();
        } else {
            return root.getName() + Naming.withCapital(super.getName());
        }
    }

    public String getShortName() {
        return super.getName();
    }

    @Override
    public String expressIn(Language l) {
        return parent.expressIn(l) + l.memberOperator() + l.getProperty(super.getName());
    }
}
