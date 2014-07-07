/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.RESULT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.traceability.ModelElement;

/**
 *
 * @author frankpeeters
 */
public class ToStringMethod extends Method {

    private static final long serialVersionUID = 1L;

    public ToStringMethod(ObjectType parent, ModelElement source) {
        super(parent, "toString", new ArrayList<Param>(), source);
        ReturnType returnType = new ReturnType(BaseType.STRING);
        setReturnType(returnType);
        setOverrideMethod(true);
    }

    @Override
    public IndentedList getCode(Language l) {
        TypeExpression ote = ((ObjectType) getParent()).getOTE();
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        //get constants
        Iterator<String> constants = ote.constants();
        list.add(l.declarationAndAssignment(BaseType.STRING, RESULT, l.stringSymbol() + l.stringSymbol()));
        int i = 0;
        //add the first constant 
        String const1 = constants.next();
        list.add(l.assignment(RESULT, l.concatenate(RESULT, l.stringSymbol() + const1 + l.stringSymbol())));
        while (constants.hasNext()) {
            //for each other constant, first add the role then add the constant.
            String constant = constants.next();
            String s = l.getProperty(ote.getParent().getRole(ote.getRoleNumber(i)).relatedRoleName((ObjectType) getParent()));
            s = l.concatenate(s, l.stringSymbol() + constant + l.stringSymbol());
            s = l.concatenate(RESULT, s);
            list.add(l.assignment(RESULT, s));
            i++;
        }
        //return the created string
        list.add(l.returnStatement(RESULT));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        returnType.setSpec(((ObjectType) getParent()).getOTE().toString());

    }

    @Override
    public List<ImportType> getImports() {
        return Collections.emptyList();
    }
}
