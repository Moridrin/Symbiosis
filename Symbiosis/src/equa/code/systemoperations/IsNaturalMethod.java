/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.systemoperations;

import java.util.ArrayList;
import java.util.List;

import equa.code.CodeClass;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.operations.IBooleanOperation;
import equa.code.operations.Method;
import equa.code.operations.Operator;
import equa.code.operations.Param;
import equa.code.operations.ReturnType;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ModelElement;

/**
 *
 * @author frankpeeters
 */
public class IsNaturalMethod extends Method implements IBooleanOperation {

    public static final long serialVersionUID = 1L;

    public IsNaturalMethod(ObjectModel om, ModelElement source) {
        super(om, "isNatural", null, source);
        List<Param> params = new ArrayList<>();
        params.add(new Param("value", BaseType.INTEGER, null));
        setParams(params);
        returnType = new ReturnType(BaseType.BOOLEAN);
    }

    @Override
    public void initSpec() {
        returnType.setSpec("value is not negative");
    }

    @Override
    public List<ImportType> getImports() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        list.add(l.returnStatement(getParams().get(0).getName() + l.operator(Operator.GREATER_OR_EQUAL) + "0"));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CodeClass getCodeClass() {
        return ((ObjectModel) getParent()).getCodeClass();
    }

}
