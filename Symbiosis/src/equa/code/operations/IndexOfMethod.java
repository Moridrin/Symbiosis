/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class IndexOfMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    public final static String NAME_PREFIX = "indexOf";

    public IndexOfMethod(Relation relation, ObjectType parent) {
        super(parent, NAME_PREFIX + Naming.withCapital(relation.name()), null, relation.getParent());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        Param param = new Param(relation.name(), relation.targetType(), relation);
        params.add(param);
        setParams(params);
        if (relation.isSeqRelation()) {
            SubstitutionType indexType = relation.qualifierRoles().get(0).getSubstitutionType();
            if (indexType instanceof ConstrainedBaseType) {
                returnType = new ReturnType(indexType);
            } else {
                returnType = new ReturnType(BaseType.INTEGER);
            }
        } else {
            returnType = new ReturnType(BaseType.INTEGER);
        }
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        list.add(l.returnStatement(l.indexOf(relation, getParams().get(0).getName())));
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        if (returnType.getType() == BaseType.INTEGER) {
            returnType.setSpec("if " + relation.getPluralName()
                    + " includes " + getParams().get(0).getName()
                    + " the index which holds: "
                    + relation.getPluralName() + "(index) = " + getParams().get(0).getName()
                    + ", otherwise -1");
        } else {
            returnType.setSpec("if " + relation.getPluralName()
                    + " includes " + getParams().get(0).getName()
                    + " the index which holds: "
                    + relation.getPluralName() + "(index) = " + getParams().get(0).getName()
                    + ", otherwise null");

        }

    }

}
