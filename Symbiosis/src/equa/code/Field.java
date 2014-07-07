/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code;

import java.io.Serializable;

import equa.code.operations.AccessModifier;
import equa.code.operations.STorCT;
import equa.meta.classrelations.Relation;

/**
 * 
 * @author frankpeeters
 */
public class Field implements Comparable<Field>, Serializable {

	private static final long serialVersionUID = 1L;
	private AccessModifier accessModifier;
	private final STorCT type;
	private final String name;
	private boolean classField, immutable;
	private Relation relation;

	public Relation getRelation() {
		return relation;
	}

	public Field(Relation r) {
		if (!r.hasMultipleTarget()) {
			this.type = r.targetType();
		} else {
			this.type = r.collectionType();
		}
		this.name = r.fieldName();
		this.immutable = r.isFinal();
		this.relation = r;
		this.accessModifier = AccessModifier.PRIVATE;
	}

	public Field(STorCT type, String name) {
		this.type = type;
		this.name = name;
		this.accessModifier = AccessModifier.PRIVATE;
	}

	public STorCT getType() {
		return type;
	}

	public boolean isImmutable() {
		return immutable;
	}

	public String getName() {
		return name;
	}

	public AccessModifier getAccessModifier() {
		return accessModifier;
	}

	public void setAccessModifier(AccessModifier accessModifier) {
		this.accessModifier = accessModifier;
	}

	public boolean isClassField() {
		return classField;
	}

	public void setClassField(boolean classField) {
		this.classField = classField;
	}

	public IndentedList getCode(Language language, boolean withOrm, boolean isProtected) {
		if (isProtected && relation != null && !relation.hasMultipleTarget() && !relation.isSettable()) {
			Relation inverse = relation.inverse();
			if (inverse == null || !inverse.isNavigable()) {
				AccessModifier old = getAccessModifier();
				setAccessModifier(AccessModifier.PROTECTED);
				IndentedList result = language.field(this, withOrm);
				setAccessModifier(old);
				return result;
			}
		}
		return language.field(this, withOrm);

	}

	@Override
	public int compareTo(Field o) {
		return name.compareTo(o.name);
	}
}
