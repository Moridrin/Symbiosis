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
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.CollectionType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class SearchCollectionMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public SearchCollectionMethod(Relation relation, ObjectType ot) {
        // search based on values in collection

        super(ot, "getCollection" + Naming.withCapital(relation.name()), null, relation.getParent());
        this.relation = relation;
        List<Param> params = new ArrayList<>();

        CollectionType ct = (CollectionType) relation.targetType();
        SubstitutionType et = ct.getElementType();
        Param elementsParam;
        int maxSize = ct.maxSize();
        if (maxSize == -1 || maxSize > 2) {
            elementsParam = new Param(Naming.withoutCapital(et.getName() + "s"), new CT(CollectionKind.COLL, et), relation);
        } else {
            elementsParam = new Param(Naming.withoutCapital(et.getName()), et, relation);
        }
        params.add(elementsParam);
        setParams(params);
        returnType = new ReturnType(relation.targetType());
    }

    @Override
    public IndentedList getCode(Language l) {
    	throw new UnsupportedOperationException("Not supported yet.");
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
    }

    @Override
    public List<ImportType> getImports() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
