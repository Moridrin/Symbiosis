/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.List;

import equa.meta.classrelations.QualifierRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class IndexedProperty extends Property {

    private static final long serialVersionUID = 1L;
    private final List<Param> params;

    public IndexedProperty(Relation relation, ObjectType ot) {
        super(relation, ot);

        List<Role> qualifiers = relation.qualifierRoles();
        params = new ArrayList<>();
        for (Role role : qualifiers) {
            params.add(new Param(role.detectRoleName(), role.getSubstitutionType(), new QualifierRelation(ot, role)));
        }

    }
    
    public List<Param> getParams() {
    	return params;
    }

    @Override
    public void initSpec() {
        StringBuilder returnSpec = new StringBuilder();
        if (relation.hasMultipleQualifiedTarget()) {
            if (relation.getMinFreq() == relation.getMaxFreq()) {
                returnSpec.append("a collection with the ").append(Integer.toString(relation.getMinFreq())).
                        append(" ").append(callString()).append(" of ").append(Naming.withoutCapital(getParent().getName()));
            } else {
                returnSpec.append("a collection with all ").append(callString()).append(" of ").append(Naming.withoutCapital(getParent().getName()));
            }
        } else {
            returnSpec.append("the ").append(callString()).append(" of this ").append(Naming.withoutCapital(getParent().getName()));
        }

        //StringBuilder postSpec = new StringBuilder();
        //postSpec.append("The ").append(getName()).append(" of this ").append(Naming.withoutCapital(parent.getName()));
        if (relation.isDerivable()) {
            returnSpec.append("; the value is derived conform the rule: \"").append(relation.getDerivableText()).append("\"");
        }

        if (!relation.isMandatory() && !relation.hasMultipleQualifiedTarget()) {
            if (relation.targetType().getUndefinedString() == null && !relation.targetType().equals(BaseType.BOOLEAN)) {
//                IBooleanOperation isdefined = (IBooleanOperation) ((ObjectType) parent).getCodeClass().getOperation("isDefined", relation);
//                BooleanCall undefinedCall = ((BooleanCall) isdefined.call()).withNegation();
//                setPreSpec(undefinedCall);
            } else if (!relation.targetType().equals(BaseType.BOOLEAN)) {
                returnSpec.append("; @result could be undefined, " + "in that case ").append(relation.targetType().getUndefinedString()).append(" will be returned");
            }
        }
        returnType.setSpec(returnSpec.toString());
    }

    @Override
    public String callString(List<? extends ActualParam> actualParams) {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append("(");
        if (!actualParams.isEmpty()) {
            sb.append(actualParams.get(0).callString());
            for (int i = 1; i < actualParams.size(); i++) {
                sb.append(",");
                sb.append(actualParams.get(i).callString());
            }
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String callString() {
        return callString(params);
    }

    @Override
    public String toString() {
        StringBuilder propString = new StringBuilder();
        propString.append(getAccess().getAbbreviation());
        propString.append(" ");
        propString.append(getName());
        propString.append("(");
        propString.append(params.get(0).getName()).append(" : ").append(params.get(0).getType().getName());
        for (int i = 1; i < params.size(); i++) {
            propString.append(",");
            propString.append(params.get(i).getName()).append(" : ").append(params.get(i).getType().getName());
        }
        propString.append(") : ");
        propString.append(returnType.toString());

        if (getter.isPresent()) {
            if (setter.isPresent()) {
                propString.append(" {").append(getter.getAccessString()).append("get, ").append(setter.getAccessString()).append("set}");
            } else {
                propString.append(" {").append(getter.getAccessString()).append("get}");
            }
        } else if (setter.isPresent()) {
            propString.append(" {").append(setter.getAccessString()).append("set}");
        }

        return propString.toString();
    }

}
