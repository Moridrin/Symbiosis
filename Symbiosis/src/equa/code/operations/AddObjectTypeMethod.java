/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.TEMP1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import equa.actioncase.ActorInputItem;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.systemoperations.UnknownMethod;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;

/**
 *
 * @author frankpeeters
 */
public class AddObjectTypeMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    private final ObjectType concreteOT;
    private boolean autoIncr = false;

    public AddObjectTypeMethod(Relation relation, ObjectType concreteOT, ObjectType parent) {
        super(parent, "add" + concreteOT.getName(), null, relation.getParent());
        this.relation = relation;
        this.concreteOT = concreteOT;
        List<Param> params = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<Param> candidates = concreteOT.getCodeClass().getConstructor().getParams();
        String paramName;
        //ObjectType target = (ObjectType) relation.targetType();
        for (Param candidate : candidates) {
            if (/**!target.isSingleton() && **/!candidate.getRelation().isAutoIncr()
                    && !candidate.getRelation().equals(relation.inverse())) {
                paramName = detectUniqueName(candidate.getName(), names);
                params.add(new Param(paramName, candidate.getType(), candidate.getRelation()));
            } else if (candidate.getRelation().isAutoIncr()) {
                autoIncr = true;
            }
        }
        setParams(params);
        returnType = new ReturnType(concreteOT);
    }

    @Override
    public void initSpec() {
        Relation inverse = relation.inverse();
        StringBuilder sb = new StringBuilder();
        sb.append("@result is added to ").append(self()).append(".");
        sb.append(relation.getPluralName());
        if (inverse != null && inverse.isNavigable()) {
            if (inverse.hasMultipleTarget()) {
                sb.append(" AND @result.contains(").append(self()).append(")");
            } else {
                sb.append(" AND @result knows ").append(self());
            }
        }
        setPostSpec(new InformalPredicate(sb.toString()));

        IFormalPredicate escapeCondition;
        //List<Param> idParams = getSearchParams();
        List<Param> idParams = idParams();
        if (idParams.isEmpty()) {
            escapeCondition = null;
        } else {
            Method search = (Method) getCodeClass().getOperation("get", relation);
            Call searchCall = search.call(idParams);
            UnknownMethod unknown = getObjectModel().getUnknownMethod();
            List<ActualParam> actualParams = new ArrayList<>();
            actualParams.add(searchCall);
            escapeCondition = new BooleanCall(unknown, actualParams, true);
        }
        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
        if (maxCount != null) {
            if (escapeCondition != null) {
                escapeCondition = escapeCondition.disjunctionWith(new BooleanCall(maxCount, false));
            }
        }
        if (escapeCondition != null) {
            IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
            setEscape(escapeCondition, escapeResult);
        }

        /* callString() should be called with actualP{arsm; but there is a problem in 
         * in case of navigable composition relation 
         */
        String constructorCall = concreteOT.getCodeClass().getConstructor().callString();
        returnType.setSpec(constructorCall);
    }

    private List<Param> idParams() {
        List<Param> idParams = new ArrayList<>();
        List<Relation> idRelations = concreteOT.identifyingRelations();
        for (Param param : getParams()) {
            if (idRelations.contains(param.getRelation())) {
                idParams.add(param);
            }
        }
        return idParams;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        // create a new object from the params.
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
        if (relation.isSeqRelation() || autoIncr) {
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
    public List<ImportType> getImports() {
        return Collections.emptyList();
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
