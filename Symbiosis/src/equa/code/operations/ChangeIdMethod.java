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
import equa.meta.classrelations.ObjectTypeRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.util.Naming;

/**
 * 
 * @author frankpeeters
 */
public class ChangeIdMethod extends Method implements IActionOperation {

	private static final long serialVersionUID = 1L;
	private final Relation relation;

	public ChangeIdMethod(Relation relation, ObjectType ot, List<Role> otherRoles) {
		super(relation.getOwner(), "changeId" + Naming.withCapital(relation.roleName()), null, relation.getParent());
		this.relation = relation;

		List<Param> params = new ArrayList<>();
		List<String> names = new ArrayList<>();

		params.add(new Param(Naming.withoutCapital(relation.roleName()), ot, relation));
		String paramName;
		for (Role role : otherRoles) {
			paramName = detectUniqueName(Naming.withoutCapital(role.detectRoleName()), names);
			params.add(new Param(paramName, role.getSubstitutionType(), new ObjectTypeRelation(relation.getOwner(), role)));
		}

		setParams(params);

		returnType = new ReturnType(BaseType.BOOLEAN);

	}

	@Override
	public IndentedList getCode(Language l) {
		IndentedList list = new IndentedList();
		 list.add(l.operationHeader(this));
		 for (Param p : getParams().subList(1, getParams().size())) {
			 list.add(l.setProperty(getParams().get(0).getName(), p.getName(), p.getName()));
		 }
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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void initSpec() {
        Operation get = getCodeClass().getOperation("get", relation);
		List<ActualParam> actualParams = new ArrayList<ActualParam>(getParams());
		actualParams.remove(getParams().get(0));
		Call getCall = new Call(get, actualParams);
		IBooleanOperation unknown = getObjectModel().getUnknownMethod();
		List<ActualParam> actualParams2 = new ArrayList<>();
		actualParams2.add(getCall);
		BooleanCall notUnknownCall = new BooleanCall(unknown, actualParams2, false);
		IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
		IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
		if (maxCount != null) {
			setEscape(new DisjunctionCall(notUnknownCall, new BooleanCall(maxCount, true)), escapeResult);
		} else {
			setEscape(notUnknownCall, escapeResult);
		}

		setPostSpec(new InformalPredicate("Identifying properties are set with actual parameter values"));

		returnType.setSpec("true if change of identifying properties has been accepted, otherwise false");
	}

	@Override
	public List<ImportType> getImports() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<ActorInputItem> inputItems() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
