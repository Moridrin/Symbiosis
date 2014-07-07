/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.FOREACH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;

/**
 * 
 * @author frankpeeters
 */
public class IsRemovableMethod extends Method implements IRelationalOperation {

	public static final long serialVersionUID = 1L;

	private final Relation relation;
	public final static String NAME = "isRemovable";

	public IsRemovableMethod(Relation relation, ObjectType ot) {
		super(ot, NAME, null, relation.getParent());
		this.relation = relation;
		List<Param> params = new ArrayList<Param>();
		// ObjectType parent = relation.getOwner();
		// params.add(new Param("parent",parent,relation));
		setParams(params);
		setReturnType(new ReturnType(BaseType.STRING));
		setAccessModifier(AccessModifier.NAMESPACE);
	}

	@Override
	public void initSpec() {
		returnType.setSpec("null if this object and all his compositional childs are removable,\n\t"
				+ "otherwise the name of a property of a Fan-class,\n\t" + "except " + relation.getOwner().getName()
				+ ", which equals or includes this " + self() + "\n\tor one his compositional childs now\n\t"
				+ "NB: Fan of X ::= a class with a navigable association from Fan to X (Fan != X)");
	}

	@Override
	public List<ImportType> getImports() {
		return Collections.emptyList();
	}

	@Override
	public IndentedList getCode(Language l) {
		IndentedList list = new IndentedList();
		list.add(l.operationHeader(this));
		for (Relation r : ((ObjectType) getParent()).fans()) {
			IndentedList ifTrue = new IndentedList();
			ifTrue.add(l.returnStatement(l.stringSymbol() + r.getOwner().getName() + "." + r.name() + l.stringSymbol()));
			// We have to ignore the parent.
			if (!relation.getOwner().equals(r.getOwner().getResponsible())) {
				String condition = "";
				if (r.isCollectionReturnType()) {
					// We need to do the contain check.
					List<ActualParam> params = new ArrayList<>();
					params.add(new This());
					Param otherObject = new Param(r.inverse().fieldName(), r.inverse().targetType(), r.inverse());
					condition = new Call(r.getOwner().getCodeClass().getOperation(ContainsMethod.NAME, r), params).setCalled(otherObject)
							.expressIn(l);
					list.add(l.ifStatement(condition, ifTrue));
				} else if (!r.inverse().isCollectionReturnType()) {
					// We have a relation to a single OT so we check if it is
					// this.
					condition = l.equalsStatement(r.inverse().fieldName() + l.memberOperator() + l.getProperty(r.fieldName()),
							l.thisKeyword());
					list.add(l.ifStatement(condition, ifTrue));
				} else {
					// we check if size == 0
					IndentedList forEachBody = new IndentedList();
					condition = l.equalsStatement(FOREACH + l.memberOperator() + l.getProperty(r.fieldName()),
							l.thisKeyword());
					forEachBody.add(l.ifStatement(condition, ifTrue));
					list.add(l.forEachLoop(r.inverse().targetType(), FOREACH, r.inverse().fieldName(), forEachBody));
				}
				
			}
		}
		// We return null to indicate it can be removed.
		list.add(l.returnStatement("null"));
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

}
