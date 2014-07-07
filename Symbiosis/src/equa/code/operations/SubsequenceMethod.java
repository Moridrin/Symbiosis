/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.ImportType.Collections;
import static equa.code.ImportType.Linq;

import java.util.ArrayList;
import java.util.List;

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
public class SubsequenceMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public SubsequenceMethod(Relation relation, ObjectType ot) {
        super(ot, "subseq" + Naming.withCapital(relation.name()), null, ot.getFactType());
        this.relation = relation;

        List<Param> params = new ArrayList<>();
        SubstitutionType indexType;
        if (relation.isSeqRelation()) {
            indexType = relation.qualifierRoles().get(0).getSubstitutionType();
        } else {
            indexType = BaseType.NATURAL;
        }
        params.add(new Param("i", indexType, null));
        params.add(new Param("j", indexType, null));
        setParams(params);
        returnType = new ReturnType(new CT(CollectionKind.LIST, relation.targetType()));
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        // we could have done checking of the parameters, but at least in Java,
        // this is handled by the subsequence method.
        list.add(l.returnStatement(l.unmodifiable(relation.collectionType(), l.subseq(relation, getParams().get(0).getName(), getParams().get(1).getName()))));
        list.add(l.bodyClosure());
        return list;
    }

    @Override
    public Relation getRelation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        String maxcount = null;
        if (getCodeClass().getOperation("count", relation) == null) {
            // frequency is constant
            maxcount = relation.getMaxFreq() + "";
        } else {
            maxcount = "count" + relation.getPluralName();
        }
        if (getParams().get(0).getType().equals(BaseType.NATURAL)) {
            returnType.setSpec("if 0 <= i and i <= j and j <= " + maxcount
                    + " then @result = the subsequence from i until j including, otherwise @result = null");
        } else {
            returnType.setSpec("if i <= j "
                    + " then @result = the subsequence from i until j including, otherwise @result = null");

        }

    }

    @Override
    public List<ImportType> getImports() {
        List<ImportType> list = new ArrayList<>();
        list.add(ImportType.List);
        list.add(ImportType.ArrayList);
        list.add(Collections);
        list.add(Linq);
        return list;
    }
}
