/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.List;

import equa.actioncase.ActorInputItem;
import equa.code.CodeClass;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class PutMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    private final SubstitutionType st;

    public PutMethod(Relation relation, ObjectType ot, SubstitutionType st) {
        super(ot, "put" + Naming.withCapital(relation.name()), null, relation.getParent());
        this.relation = relation;
        this.st = st;

        List<Param> params = new ArrayList<>();

        List<Role> qualifiers = relation.qualifierRoles();
        for (Role role : qualifiers) {
            params.add(new Param(role.detectRoleName(), role.getSubstitutionType(), null));
        }
        if (st instanceof ObjectType) {
            ObjectType target = (ObjectType) st;
            CodeClass codeClassTarget = target.getCodeClass();
            if (codeClassTarget != null) {
                Constructor constructor = codeClassTarget.getConstructor();
                if (constructor.getAccess() != AccessModifier.PUBLIC) {
                    for (Param param : constructor.getParams()) {
                        if (!param.getRelation().isAutoIncr()
                                && !param.getRelation().equals(relation.inverse())) {

                            params.add(new Param(param.getName(), param.getType(), param.getRelation()));
                        }
                    }
                } else {
                    params.add(new Param(relation.name(), st, relation));
                }
            } else {
                params.add(new Param(relation.name(), st, relation));
            }
        } else {
            params.add(new Param(relation.name(), st, relation));
        }
        setParams(params);
    }

    @Override
    public IndentedList getCode(Language l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        StringBuilder sb = new StringBuilder();
        sb.append("@result is put in ").append(self()).append(".");
        sb.append(relation.getPluralName());
        sb.append(" at key ");
        //see assumption
        sb.append(getParams().get(0).getName());
        for (int i = 1; i < getParams().size() - 1; i++) {
            sb.append(",");
            sb.append(getParams().get(i).getName());
        }
        if (inverse != null && inverse.isNavigable()) {
            if (inverse.hasMultipleTarget()) {
                sb.append(" AND @result.contains(").append(self()).append(")");
            } else {
                sb.append(" AND @result knows ").append(self());
            }
        }

//        setPostSpec(new InformalPredicate(sb.toString()));
//        IFormalPredicate escapeCondition;
//        IBooleanOperation indexAllowed = getObjectModel().getIndexAllowedMethod();
//        List<ActualParam> actualParams = new ArrayList<>();
//        actualParams.add(indexParam);
//        IOperation count = getCodeClass().getOperation("count", relation);
//        BinaryExpression countIncr = new BinaryExpression(count.call(), Operator.PLUS, new ValueString("1", BaseType.INTEGER));
//        actualParams.add(countIncr);
//        escapeCondition = new BooleanCall(indexAllowed, actualParams, true);
//        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
//        if (maxCount != null) {
//            escapeCondition = escapeCondition.disjunctionWith(new BooleanCall(maxCount, false));
//        }
//
//        IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
//        setEscape(escapeCondition, escapeResult);
//
//        if (withConstructor) {
//
//            /* callString() should be called with actualParms; but there is a problem in 
//             * in case of navigable composition relation 
//             */
//            String constructorCall = target.getCodeClass().getConstructor().callString();
//            returnType.setSpec(constructorCall);
//        }
//   
//    
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
