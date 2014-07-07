/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.QualifierRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.requirements.RuleRequirement;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class IsDefinedMethod extends Method implements IBooleanOperation, IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public IsDefinedMethod(Relation relation, ObjectType ot) {
        super(ot, "isDefined" + Naming.withCapital(relation.name()), null, relation.getParent());
        this.relation = relation;

        List<Role> qualifiers = relation.qualifierRoles();
        List<Param> params = new ArrayList<>();
        for (Role role : qualifiers) {
            params.add(new Param(role.detectRoleName(), role.getSubstitutionType(), new QualifierRelation(ot, role)));
        }
        setParams(params);

        setReturnType(new ReturnType(BaseType.BOOLEAN));
    }

    @Override
    public void initSpec() {

        returnType.setSpec("true if " + relation.name() + " has a defined value"
                + " otherwise false");
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String returnValue() {
        return returnType.getSpec();
    }

    @Override
    public List<ImportType> getImports() {
        return Collections.emptyList();
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        list.add(l.returnStatement(relation.fieldName() + "Defined"));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        return new Field(BaseType.BOOLEAN, getName());
    }
}
