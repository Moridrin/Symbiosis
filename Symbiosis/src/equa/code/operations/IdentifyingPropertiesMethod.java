/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.RESULT;
import static equa.code.ImportType.Set;
import static equa.code.ImportType.SortedSet;

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
public class IdentifyingPropertiesMethod extends Method {

    private static final long serialVersionUID = 1L;

    public IdentifyingPropertiesMethod(ObjectType parent, ModelElement source) {
        super(parent, "identifyingProperties", new ArrayList<Param>(), source);
        setAccessModifier(AccessModifier.NAMESPACE);
        returnType = new ReturnType(new CT(CollectionKind.SET, BaseType.STRING));
        setClassMethod(true);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        // standard operation header
        list.add(l.operationHeader(this));
        // we create a Set as defined in the specifications
        list.add(l.declarationAndAssignment(returnType.getType(), RESULT, l.assignCollection(returnType.getType())));
        // every field that is public will be added as a string to this set.

        for (Relation r : ((ObjectType) getParent()).identifyingRelations()) {
            list.add(l.add(RESULT, ((CT) returnType.getType()).getKind(), l.stringSymbol() + r.name() + l.stringSymbol()) + l.endLine());
        }
        // return the set (it is a new one every time so we can give a direct reference)
        list.add(l.returnStatement(RESULT));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        returnType.setSpec("the name of all identifying "
                + "properties of an object of this class");
    }

    @Override
    public List<ImportType> getImports() {
        List<ImportType> list = new ArrayList<>();
        list.add(Set);
        list.add(SortedSet);
        return list;
    }
}
