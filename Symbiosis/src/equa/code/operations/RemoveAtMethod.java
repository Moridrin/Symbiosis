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
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class RemoveAtMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public RemoveAtMethod(Relation relation, ObjectType ot) {
        super(ot, "remove" + Naming.withCapital(relation.name()), null, relation.getParent());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        params.add(new Param("index", BaseType.NATURAL, relation));
        setParams(params);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        // standard operation header
        list.add(l.operationHeader(this));
        // we call the removeAt, this is only defined for sequenceRelation.
        list.add(l.removeAt(relation.fieldName(), getParams().get(0).getName()));
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
//    
//        Predicate postSpec = new FormalSpec(Operation.collectionCondition(relation, true, false));
//        setPostSpec(postSpec);
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
