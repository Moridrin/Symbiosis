/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.TEMP1;

import java.util.ArrayList;
import java.util.Iterator;
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
 *
 * @author frankpeeters
 */
public class InsertMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    private boolean withConstructor;
    private ObjectType concreteOT;

    public InsertMethod(Relation relation, ObjectType ot) {
        super(ot, "insert" + Naming.withCapital(relation.name()), null, relation.getParent());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        this.concreteOT = ot;
        Relation inverse = relation.inverse();
        if (inverse != null && inverse.isMandatory()) {
            withConstructor = true;
            List<String> names = new ArrayList<>();
            ObjectType target = (ObjectType) relation.targetType();
            List<Param> candidates = target.getCodeClass().getConstructor().getParams();
            String paramName;
            for (Param candidate : candidates) {
                if (!target.isSingleton() && !candidate.getRelation().isAutoIncr()
                        && !candidate.getRelation().equals(relation.inverse())) {
                    paramName = detectUniqueName(candidate.getName(), names);
                    params.add(new Param(paramName, candidate.getType(), candidate.getRelation()));
                }
            }
        } else {
            params.add(new Param(relation.name(), relation.targetType(), relation));
        }
        addIndices(relation, params);

        setParams(params);
        if (withConstructor) {
            setReturnType(new ReturnType(relation.targetType()));
        } else {
            setReturnType(new ReturnType(null));
        }

    }

    @Override
    public IndentedList getCode(Language l) {
    	IndentedList list = new IndentedList();
    	list.add(l.operationHeader(this));
    	List<String> constructorParams = new ArrayList<>();
		Iterator<Param> it = concreteOT.getCodeClass().constructorParams();
		while (it.hasNext()) {
			Param p = it.next();
			String name = searchParamName(p.getRelation());
			if (name == null) {
				if (p.getType().equals(getParent())) {
					name = l.thisKeyword();
				} else {
					if (p.getRelation().isAutoIncr()) {
						name = l.autoIncr(p.getRelation().fieldName());
					} else {
						throw new RuntimeException("I don't know what to do.");
					}
				}
			}
			constructorParams.add(name);
		}
		list.add(l.createInstance(getReturnType().getType(), TEMP1, concreteOT.getName(), constructorParams.toArray(new String[0])));
        if (relation.isSeqRelation() || relation.isAutoIncr()) {
            list.add(l.add(relation.fieldName(), relation.collectionType().getKind(), TEMP1) + l.endLine());
            //if a value is added to the collection and the inverse relation is navigable, we have to register.
            if (relation.inverse().isNavigable() && relation.inverse().isCollectionReturnType()) {
                list.add(l.callMethod(TEMP1, RegisterMethod.NAME, l.thisKeyword()) + l.endLine());
            }
            list.add(l.returnStatement(TEMP1));
        } else {
            //if a value is added to the collection and the inverse relation is navigable, we have to register.
            if (relation.inverse().isNavigable() && relation.inverse().isCollectionReturnType()) {
                list.add(l.callMethod(TEMP1, RegisterMethod.NAME, l.thisKeyword()) + l.endLine());
            }
            //we add the item
            list.add(l.add(relation.fieldName(), relation.collectionType().getKind(), TEMP1) + l.endLine());
            //if a value is added to the collection, the new Object is returned
            list.add(l.returnStatement(TEMP1));
        }
    	list.add(l.bodyClosure());
    	return list;
    }
    
    private String searchParamName(Relation r) {
		for (Param p : getParams()) {
			if (p.getRelation().equals(r)) {
				return p.getName();
			}
		}
		return null;
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
        //assumption: qualifiers().size=1
        int size = getParams().size();
        Param indexParam = getParams().get(size - 1);
        SubstitutionType target = relation.targetType();
        Relation inverse = relation.inverse();
        StringBuilder sb = new StringBuilder();
        sb.append("@result is inserted in ").append(self()).append(".");
        sb.append(relation.getPluralName());
        sb.append(" at index ");
        //see assumption
        sb.append(indexParam.getName());
        if (inverse != null && inverse.isNavigable()) {
            if (inverse.hasMultipleTarget()) {
                sb.append(" AND @result.contains(").append(self()).append(")");
            } else {
                sb.append(" AND @result knows ").append(self());
            }
        }
        setPostSpec(new InformalPredicate(sb.toString()));
        IFormalPredicate escapeCondition;
        IBooleanOperation indexAllowed = getObjectModel().getIndexAllowedMethod();
        List<ActualParam> actualParams = new ArrayList<>();
        actualParams.add(indexParam);
        IOperation count = getCodeClass().getOperation("count", relation);
        BinaryExpression countIncr = new BinaryExpression(count.call(), Operator.PLUS, new ValueString("1", BaseType.INTEGER));
        actualParams.add(countIncr);
        escapeCondition = new BooleanCall(indexAllowed, actualParams, true);
        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
        if (maxCount != null) {
            escapeCondition = escapeCondition.disjunctionWith(new BooleanCall(maxCount, false));
        }

        IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
        setEscape(escapeCondition, escapeResult);

        if (withConstructor) {

            /* callString() should be called with actualParms; but there is a problem in 
             * in case of navigable composition relation 
             */
            String constructorCall = ((ObjectType) target).getCodeClass().getConstructor().callString();
            returnType.setSpec(constructorCall);
        }
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
