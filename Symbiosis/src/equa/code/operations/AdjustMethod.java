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
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;

/**
 * 
 * @author frankpeeters
 */
public class AdjustMethod extends Method implements IActionOperation {

	private static final long serialVersionUID = 1L;
	private final Relation relation;

	public AdjustMethod(Relation relation, ObjectType ot) {
		super(ot, "adjust" + Naming.withCapital(relation.roleName()), null, relation.getParent());
		this.relation = relation;
		List<Param> params = new ArrayList<>();
		if (relation.targetType() instanceof ConstrainedBaseType) {
			ConstrainedBaseType cbt = (ConstrainedBaseType) relation.targetType();
			params.add(new Param("amount", cbt.getBaseType(), relation));
		} else {
			params.add(new Param("amount", relation.targetType(), relation));
		}
		addQualifiers(params, relation);
		setParams(params);
	}

	@Override
	public IndentedList getCode(Language l) {
		IndentedList list = new IndentedList();
		list.add(l.operationHeader(this));
		Relation r = getParams().get(0).getRelation();
		if (r.isMapRelation()) {
			// TODO Assumption only 1 qualifier in map relation
			list.add(l.adjustMap(relation, getParams().get(1).getName(), getParams().get(0).getName()));
		} else if (r.targetType() instanceof ConstrainedBaseType) {
			list.add(l.assignment(
					r.fieldName(),
					l.newInstance(r.targetType(), r.fieldName() + l.memberOperator() + l.getProperty("value") + " + "
							+ getParams().get(0).getName())));
		} else {
			list.add(l.assignment(relation.fieldName(), relation.fieldName() + " + " + getParams().get(0).getName()));
		}
		Relation inv = relation.inverse();
		if (inv != null && inv.isNavigable()) {
			
		}
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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void initSpec() {
		Operation property = getCodeClass().getOperation(relation.name());
		BinaryExpression newValue = new BinaryExpression(property.call(qualifierParams(relation)), Operator.PLUS, getParams().get(0));

		IFormalPredicate preCondition = null;
		if (!relation.isMandatory()) {
			IBooleanOperation isDefined = (IBooleanOperation) getCodeClass().getOperation("isDefined", relation);
			if (isDefined == null) {
				ActualParam undefined = relation.targetType().getUndefined();
				IBooleanOperation isEqual = getObjectModel().getIsEqualMethod();
				List<ActualParam> actualParams = new ArrayList<>();
				Call propertyCall = property.call(qualifierParams(relation));
				actualParams.add(propertyCall);
				actualParams.add(undefined);
				preCondition = new BooleanCall(isEqual, actualParams, true);
			} else {
				preCondition = new BooleanCall(isDefined, qualifierParams(relation), false);
			}
		}

		BooleanCall indicesAllowed = qualifiersCondition(relation);
		if (indicesAllowed != null) {
			if (preCondition != null) {
				preCondition = preCondition.conjunctionWith(indicesAllowed);
			} else {
				preCondition = indicesAllowed;
			}
		}
		if (preCondition != null) {
			setPreSpec(preCondition);
		}

		List<ActualParam> actualParams = new ArrayList<>();
		actualParams.add(newValue);
		if (relation.targetType() instanceof ObjectType) {
			ConstrainedBaseType cbt = (ConstrainedBaseType) relation.targetType();
			IBooleanOperation correctValue = (IBooleanOperation) cbt.getCodeClass().getOperation("isCorrectValue");
			BooleanCall bc = new BooleanCall(correctValue, actualParams, true);
			bc.setCalled((ObjectType) cbt.getCodeClass().getParent());
			IFormalPredicate predicate = bc;
			setEscape(predicate, new InformalPredicate(self() + " stays unchanged"));
		} else {
			BaseType bt = (BaseType) relation.targetType();
			if (bt.equals(BaseType.NATURAL)) {
				IBooleanOperation isNatural = getObjectModel().getIsNaturalMethod();
				IFormalPredicate predicate = new BooleanCall(isNatural, actualParams, true);
				setEscape(predicate, new InformalPredicate(self() + " stays unchanged"));
			}
		}

		setPostSpec(new InformalPredicate(relation.roleName() + " = self@Pre." + newValue.callString()));
	}

	@Override
	public List<ActorInputItem> inputItems() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
