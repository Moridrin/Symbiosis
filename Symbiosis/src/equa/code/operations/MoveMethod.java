/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
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
public class MoveMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public MoveMethod(Relation relation, ObjectType parent) {
        super(parent, "move" + Naming.withCapital(relation.name()), null, relation.getParent());
        this.relation = relation;
        ObjectType objectType = (ObjectType) relation.targetType();
        List<Param> params = new ArrayList<>();
        params.add(new Param(relation.roleName(), objectType, relation));
        params.add(new Param("destination", parent, relation));
        setParams(params);
        returnType = new ReturnType(BaseType.BOOLEAN);
    }

    @Override
    public IndentedList getCode(Language l) {
    	IndentedList list = new IndentedList();
    	list.add(l.operationHeader(this));
    	list.add(l.remove(relation, getParams().get(0).getName()) + l.endLine());
    	list.add(l.add(getParams().get(1).getName() + l.memberOperator() + relation.fieldName(), relation.collectionType().getKind(), getParams().get(0).getName()) + l.endLine());
    	list.add(l.returnStatement("true"));
    	list.add(l.bodyClosure());
    	return list;
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

        StringBuilder post = new StringBuilder();
        post.append("self.collect(");
        post.append(relation.name());
        post.append(") = self@Pre->collect(");
        post.append(relation.name()).append(")->excluding(");
        post.append(relation.name());
        post.append(")");
        post.append("AND destination.collect(");
        post.append(relation.name());
        post.append(") = destination@Pre->collect(");
        post.append(relation.name());
        post.append(")->including(");
        post.append(relation.name());
        post.append(")");
        setPostSpec(new InformalPredicate(post.toString()));

        IBooleanOperation isEqual = getObjectModel().getIsEqualMethod();
        {
            List<ActualParam> actualParams = new ArrayList<>();
            actualParams.add(new This());
            actualParams.add(getParams().get(0));
            BooleanCall isEqualCall = new BooleanCall(isEqual, actualParams, true);
            setPreSpec(isEqualCall);
        }

        // remove is necessary:
        IBooleanOperation contains = (IBooleanOperation) (Method) getCodeClass().getOperation("contains", relation);
        List<ActualParam> actualParams1 = new ArrayList<>();
        actualParams1.add(getParams().get(0));

        BooleanCall escapeCondition = new BooleanCall(contains, actualParams1, false);
        List<ActualParam> actualParams2 = new ArrayList<>();
        actualParams2.add(getParams().get(0));
        BooleanCall containsCall = new BooleanCall(contains, actualParams2, true);
        containsCall.setCalled(getParams().get(1));
        escapeCondition.disjunctionWith(containsCall);

        // remove is necessary AND remove is allowed:
        IBooleanOperation minCount = (IBooleanOperation) getCodeClass().getOperation("minCount", relation);
        if (minCount != null) {
            escapeCondition.disjunctionWith(new BooleanCall(minCount, false));

        }

        // remove is necessary AND remove is allowed AND destination.register is allowed:
        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
        if (maxCount != null) {
            escapeCondition.disjunctionWith((BooleanCall) new BooleanCall(maxCount, false).setCalled(getParams().get(1)));
        }

        IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
        setEscape(escapeCondition, escapeResult);

    }

    @Override
    public List<ImportType> getImports() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
