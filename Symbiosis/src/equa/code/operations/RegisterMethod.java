/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;

/**
 *
 * @author frankpeeters
 */
public class RegisterMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public static final String NAME = "register";

    public RegisterMethod(Relation relation, ObjectType parent) {
        super(parent, NAME, null, relation.getParent());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        ObjectType concreteObjectType = (ObjectType) relation.targetType();
        params.add(new Param(relation.name(), concreteObjectType, relation));
        addQualifiers(params, relation);
        setParams(params);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        list.add(l.add(relation.fieldName(), relation.collectionType().getKind(), getParams().get(0).getName()) + l.endLine());
        list.add(l.bodyClosure());

        /*
         * void registerRole(ST role) {
         * 		collection.add(role);
         * } 
         */
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initSpec() {
        IFormalPredicate escapeCondition = null;

        IBooleanOperation contains = (IBooleanOperation) getCodeClass().getOperation("contains", relation);
        if (contains != null) {
            List<ActualParam> actualParams = new ArrayList<>();
            actualParams.add(getParams().get(0));
            escapeCondition = new BooleanCall(contains, actualParams, false);
        }

        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
        if (maxCount != null) {
            BooleanCall maxCall = new BooleanCall(maxCount, false);

            if (escapeCondition == null) {
                escapeCondition = maxCall;
            } else {
                escapeCondition.disjunctionWith(maxCall);
            }
        }

        if (escapeCondition != null) {
            IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
            setEscape(escapeCondition, escapeResult);
        }

        IPredicate postSpec = new InformalPredicate("collect(" + relation.name() + ") = collect("
                + relation.name() + "@Pre)->including(" + relation.name() + ")");
        setPostSpec(postSpec);
    }
}
