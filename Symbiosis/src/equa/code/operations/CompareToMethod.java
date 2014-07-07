/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.List;

import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.traceability.ModelElement;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class CompareToMethod extends Method {

    private static final long serialVersionUID = 1L;

    public CompareToMethod(ObjectType parent, ModelElement source) {
        super(parent, "compareTo", null, source);
        List<Param> params = new ArrayList<>();
        params.add(new Param(Naming.withoutCapital(getName()), parent, null));
        setParams(params);
        setReturnType(new ReturnType(BaseType.INTEGER));
    }

    @Override
    public IndentedList getCode(Language l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        String actualParam = getParams().get(0).callString();
        returnType.setSpec("if self equals " + actualParam
                + " 0 will be returned else if self is smaller than " + actualParam
                + " -1 will be returned else +1 will be returned");
    }

    @Override
    public List<ImportType> getImports() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
