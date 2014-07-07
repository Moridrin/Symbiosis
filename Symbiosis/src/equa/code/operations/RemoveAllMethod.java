/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.actioncase.ActorInputItem;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class RemoveAllMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public RemoveAllMethod(Relation relation, ObjectType ot) {
        super(ot, "removeAll" + Naming.withCapital(relation.getPluralName()), new ArrayList<Param>(), relation.getParent());
        this.relation = relation;

        returnType = new ReturnType(BaseType.STRING);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        list.add(l.clear(relation));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public List<ImportType> getImports() {
        return Collections.emptyList();
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {

        Relation inverse = relation.inverse();
        StringBuilder postSpec = new StringBuilder();
        postSpec.append("self->collect(").append(relation.name()).append(")->isEmpty()");

        IFormalPredicate escapeCondition = null;

        IBooleanOperation minCount = (IBooleanOperation) getCodeClass().getOperation("minCount", relation);
        if (minCount != null) {
            escapeCondition = new BooleanCall(minCount, false);
        }

        Operation isRemovable=null;
        if (relation.targetType() instanceof ObjectType) {
            ObjectType target = (ObjectType) relation.targetType();
            isRemovable = target.getCodeClass().getOperation("isRemovableFrom");
        }

        if (isRemovable != null && inverse.hasMultipleTarget()) {
//            List<ActualParam> actualParams1 = new ArrayList<>();
//            actualParams1.add(new This());
//            Call isRemovableCall = new Call(isRemovable, actualParams1);
//            List<ActualParam> actualParams2 = new ArrayList<>();
//            actualParams2.add(isRemovableCall);
//            actualParams2.add(new Null());
//            IBooleanOperation isEqual = getObjectModel().getIsEqualMethod();
//            BooleanCall isEqualCall = new BooleanCall(isEqual, actualParams2, true);
//            if (escapeCondition != null) {
//                escapeCondition.disjunctionWith(isEqualCall);
//            } else {
//                escapeCondition = isEqualCall;
//            }
//            getReturnType().setSpec("null if remove went well, otherwise the name of a property "
//                    + " which refers (indirectly) at this moment to this child-object.");
//
//            postSpec.append(" AND ").append(self()).append(" is removed from ").append(getParams().get(0).getName());

        } else {
            returnType = new ReturnType(null);
        }
        if (escapeCondition != null) {
            IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
            setEscape(escapeCondition, escapeResult);
        }
        setPostSpec(new InformalPredicate(postSpec.toString()));

    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
