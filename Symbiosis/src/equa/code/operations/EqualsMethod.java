/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.TEMP1;
import static equa.code.ImportType.ObjectEquals;

import java.util.ArrayList;
import java.util.List;

import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ModelElement;

/**
 *
 * @author frankpeeters
 */
public class EqualsMethod extends Method implements IBooleanOperation {

    private static final long serialVersionUID = 1L;

    public EqualsMethod(ObjectType parent, ModelElement source) {
        super(parent, "equals", null, source);
        List<Param> params = new ArrayList<>();
        params.add(new Param("object", BaseType.OBJECT, null));
        setParams(params);
        setReturnType(new ReturnType(BaseType.BOOLEAN));
        setOverrideMethod(true);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
//        getParams().clear();
//        //@reason c#, there is object a keyword
//        params.add(new Param(l.nonObjectKeyword(), BaseType.OBJECT, null));
        // standard operation header
        list.add(l.operationHeader(this));
        ObjectType ot = (ObjectType) getParent();
        // we will have to return false every time a check fails
        IndentedList bodyIf = new IndentedList();
        bodyIf.add(l.returnStatement("false"));
        // check if the instance is correct
        list.add(l.ifStatement(l.negate(l.checkType(getParams().get(0).getName(), ot)), bodyIf));
        // then we can cast to the correct instance
        list.add(l.cast(ot, TEMP1, getParams().get(0).getName()));
        // for every relation that is part of the id we invoke the equals statement
        for (Relation r : ot.identifyingRelations()) {
            list.add(l.ifStatement(l.negate(l.equalsStatement(getCodeClass().getFieldNameOrProperty(l, r), TEMP1 + l.memberOperator() + l.getProperty(r.fieldName()))), bodyIf));
        }
        // if there is not once returned false, we can return true.
        list.add(l.returnStatement("true"));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        String returnSpec;
        if (((FactType) getParent().getParent()).isValueType()) {
            returnSpec = "if all properties of " + self() + " are equal to "
                    + " that of object then result = true else result = false";
        } else {
            returnSpec = "if all identifying properties of " + self()
                    + " are equal to that of object then result = true else result = "
                    + "false";
        }
        returnType.setSpec(returnSpec);
    }

    @Override
    public List<ImportType> getImports() {
        List<ImportType> list = new ArrayList<>();
        list.add(ObjectEquals);
        return list;
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
