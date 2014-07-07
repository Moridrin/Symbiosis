/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.RESULT;
import static equa.code.ImportType.ObjectEquals;

import java.util.ArrayList;
import java.util.List;

import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.traceability.ModelElement;

/**
 *
 * @author frankpeeters
 */
public class HashCodeMethod extends Method {

    private static final long serialVersionUID = 1L;

    public HashCodeMethod(ObjectType parent, ModelElement source) {
        super(parent, "hashCode", new ArrayList<Param>(), source);
        setReturnType(new ReturnType(BaseType.INTEGER));
        setOverrideMethod(true);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        ObjectType ot = (ObjectType) getParent();
        list.add(l.declarationAndAssignment(BaseType.INTEGER, RESULT, "0"));
        for (Relation r : ot.identifyingRelations()) {
            list.add(l.assignment(RESULT, RESULT + " + 37 * " + l.hashCodeStatement(getCodeClass().getFieldNameOrProperty(l, r))));
        }
        list.add(l.returnStatement(RESULT));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        String spec = "a hashcode-value for this object";

        returnType.setSpec(spec);
    }

    @Override
    public List<ImportType> getImports() {
        List<ImportType> list = new ArrayList<>();
        list.add(ObjectEquals);
        return list;
    }
}
