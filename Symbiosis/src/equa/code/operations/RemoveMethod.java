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
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;

/**
 * @author frankpeeters§
 */
public class RemoveMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    public static final String NAME = "remove";

    public RemoveMethod(Relation relation, ObjectType ot) {
        super(ot, NAME, null, relation.getParent());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        SubstitutionType st = relation.targetType();

        if (relation.isMapRelation()) {
            name = NAME + Naming.withCapital(relation.name());
            if (relation.hasMultipleQualifiedTarget()) {
                params.add(new Param(Naming.withoutCapital(relation.name()), st, relation));
            }
        } else {
            if (relation.hasMultipleTarget()) {
                params.add(new Param(Naming.withoutCapital(relation.name()), st, relation));
            } else {
                name = NAME + Naming.withCapital(relation.name());
            }
        }

        params.addAll(qualifierParams(relation));
        setParams(params);

        if (!relation.isRemovable()) {
            setAccessModifier(AccessModifier.NAMESPACE);
        }

        returnType = new ReturnType(BaseType.STRING);
    }

    @Override
    public List<ImportType> getImports() {
        return Collections.emptyList();
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        //operation header
        list.add(l.operationHeader(this));
        Relation inv = relation.inverse();
        // if its a collection we call collection.remove(role);
        if (relation.hasMultipleTarget()) {
            list.add(l.remove(relation, getParams().get(0).getName()) + l.endLine());
            if (inv != null && inv.isNavigable()) {
//            	list.add(getParams().get(0).getName()+ ".remove()");
            }
        } else {
            //else we either set the boolean of the defined property to false.
            if (relation.targetType() instanceof BaseType && !relation.targetType().equals(BaseType.STRING)) {
                list.add(l.assignment(relation.fieldName() + "Defined", "false"));
                // or we set the field to null
            } else {
                list.add(l.assignment(relation.fieldName(), "null"));
            }
            if (inv != null && inv.isNavigable()) {
            }
        }
        //We return null to indicate that the object is removed.
        if (returnType.getType() != null) {
            list.add(l.returnStatement("null"));
        }
        list.add(l.bodyClosure());
        return list;
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
        StringBuilder postSpec = new StringBuilder();
        if (getParams().isEmpty()) {
            postSpec.append(relation.name()).append(" is removed from ").append(self());
        } else {
            postSpec.append(getParams().get(0).getName()).append(" is removed from ").append(self());
        }
        IFormalPredicate escapeCondition = null;

        IBooleanOperation minCount = (IBooleanOperation) getCodeClass().getOperation("minCount", relation);
        if (minCount != null) {
            escapeCondition = new BooleanCall(minCount, false);
        }

        if (relation.targetType() instanceof ObjectType) {
            ObjectType target = (ObjectType) relation.targetType();
            Operation isRemovable = target.getCodeClass().getOperation("isRemovableFrom");

            if (isRemovable != null && relation.isResponsible()/*&& inverse.isMultiple()*/) {
                List<ActualParam> actualParams1 = new ArrayList<>();
                Call isRemovableCall;
                ActualParam toRemove;
                if (getParams().isEmpty()) {
                    Operation property = getCodeClass().getOperation(relation.name(), relation);
                    toRemove = property.call();
                } else {
                    toRemove = getParams().get(0);
                }
                isRemovableCall = new Call(isRemovable, actualParams1).setCalled(toRemove);
                List<ActualParam> actualParams2 = new ArrayList<>();
                actualParams2.add(isRemovableCall);
                actualParams2.add(Null.NULL);
                IBooleanOperation isEqual = getObjectModel().getIsEqualMethod();
                BooleanCall isEqualCall = new BooleanCall(isEqual, actualParams2, true);
                if (escapeCondition != null) {
                    escapeCondition.disjunctionWith(isEqualCall);
                } else {
                    escapeCondition = isEqualCall;
                }
                getReturnType().setSpec("null if remove went well, otherwise the name of a property "
                        + " which refers currently to this child-object.");
                if (getParams().isEmpty()) {

                    Operation property = getCodeClass().getOperation(relation.name(), relation);
                    toRemove = property.call();
                    postSpec.append(" AND ").append(toRemove.callString()).append(" is removed from ").append(self());
                } else {
                    postSpec.append(" AND ").append(getParams().get(0).getName()).append(" is removed from ").append(self());
                }

            } else {
                returnType = new ReturnType(null);
            }
        } else {
            returnType = new ReturnType(null);
        }
        if (escapeCondition
                != null) {
            IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
            setEscape(escapeCondition, escapeResult);
        }

        setPostSpec(
                new InformalPredicate(postSpec.toString()));
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
