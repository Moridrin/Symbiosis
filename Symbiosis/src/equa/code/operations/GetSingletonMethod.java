/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.ImportType.ObjectEquals;

import java.util.ArrayList;
import java.util.List;

import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.objectmodel.ObjectType;
import equa.meta.traceability.ModelElement;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class GetSingletonMethod extends Method {

    private static final long serialVersionUID = 1L;
    public static final String NAME = "getSingleton";

    public GetSingletonMethod(ObjectType parent, ModelElement source) {
        super(parent, NAME, new ArrayList<Param>(), source);
        setClassMethod(true);
        setReturnType(new ReturnType(parent));
    }

    @Override
    public IndentedList getCode(Language l) {
        // this is the field name
        String name = Naming.singletonName(getParent().getName());
        IndentedList list = new IndentedList();
        // standard operation header
        list.add(l.operationHeader(this));
        // if the field is null we have to create it first
        IndentedList ifTrue = new IndentedList();
        // here we assign a new instance to the field name
        ifTrue.add(l.assignment(name, l.callConstructor(getParent().getName())));
        // if field name equals null is the condition
        list.add(l.ifStatement(l.equalsStatement(name, "null"), ifTrue));
        // return the field
        list.add(l.returnStatement(name));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        String spec = "the singleton-object " + ((ObjectType) getParent()).getOTE().toString();
        returnType.setSpec(spec);
    }

    @Override
    public List<ImportType> getImports() {
        List<ImportType> list = new ArrayList<>();
        list.add(ObjectEquals);
        return list;
    }
}
