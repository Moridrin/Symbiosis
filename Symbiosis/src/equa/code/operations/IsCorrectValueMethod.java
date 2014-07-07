/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.ImportType.ObjectEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.BaseValue;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Range;
import equa.meta.requirements.RuleRequirement;

/**
 * 
 * @author frankpeeters
 */
public class IsCorrectValueMethod extends Method implements IRelationalOperation, IBooleanOperation {

	private static final long serialVersionUID = 1L;
	private final Relation relation;

	public IsCorrectValueMethod(Relation relation, ObjectType parent) {
		super(parent, "isCorrectValue", null, relation.getParent());
		this.relation = relation;
		List<Param> params = new ArrayList<>();
		Param param = new Param(relation.name(), relation.targetType(), relation);
		params.add(param);
		setParams(params);
		returnType = new ReturnType(BaseType.BOOLEAN);
		setReturnType(returnType);
		setClassMethod(true);
	}

	@Override
	public IndentedList getCode(Language l) {
		// we get the constraints here
		ConstrainedBaseType cbt = (ConstrainedBaseType) relation.getOwner();
		// the value we need to check
		String p = getParams().get(0).getName();
		IndentedList list = new IndentedList();
		// standard operation header
		list.add(l.operationHeader(this));
		// this will return true when we have a hit.
		IndentedList returnTrue = new IndentedList();
		returnTrue.add(l.returnStatement("true"));
		// we iterate over the ranges and then do lower <= value <= upper
		Iterator<Range> ranges = cbt.getValueConstraint().ranges();
		while (ranges.hasNext()) {
			Range r = ranges.next();
			list.add(l.ifStatement(
					r.getLower() + l.operator(Operator.SMALLER_OR_EQUAL) + p + l.and() + p + l.operator(Operator.SMALLER_OR_EQUAL)
							+ r.getUpper(), returnTrue));
		}
		// we do an equals check on the values
		Iterator<BaseValue> values = cbt.getValueConstraint().values();
		while (values.hasNext()) {
			BaseValue bv = values.next();
			if (bv.getType().equals(BaseType.STRING)) {
				list.add(l.ifStatement(l.equalsStatement(p, l.stringSymbol() + bv.getName() + l.stringSymbol()), returnTrue));
			} else {
				list.add(l.ifStatement(l.equalsStatement(p, bv.getName()), returnTrue));
			}
		}
		// no hits : not a correct value
		list.add(l.returnStatement("false"));
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
		ConstrainedBaseType cbt = (ConstrainedBaseType) getParent();
		returnType.setSpec("true if value belongs to " + cbt.valuesString() + " otherwise false");
	}

	@Override
	public List<ImportType> getImports() {
		List<ImportType> list = new ArrayList<>();
		list.add(ObjectEquals);
		return list;
	}

	@Override
	public RuleRequirement getRuleRequirement() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
