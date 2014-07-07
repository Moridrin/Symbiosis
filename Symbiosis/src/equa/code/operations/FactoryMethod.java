/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.TEMP1;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import static equa.code.operations.Operation.detectUniqueName;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class FactoryMethod extends Method {

    private static final long serialVersionUID = 1L;
    private final ObjectType concreteOT;
    private boolean autoIncr = false;

    public FactoryMethod(ObjectType concreteOT, ObjectType parent) {
        super(parent, "create" + concreteOT.getName(), null, concreteOT);
        this.concreteOT = concreteOT;
        List<Param> params = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<Param> candidates = concreteOT.getCodeClass().getConstructor().getParams();
        String paramName;
        for (Param candidate : candidates) {
            if (!candidate.getRelation().isAutoIncr()) {
                paramName = detectUniqueName(candidate.getName(), names);
                params.add(new Param(paramName, candidate.getType(), candidate.getRelation()));
            } else {
                autoIncr = true;
            }
        }
        setParams(params);
        setClassMethod(true);
        returnType = new ReturnType(concreteOT);
    }

    @Override
    public void initSpec() {

        /* callString() should be called with actualP{arsm; but there is a problem in 
         * in case of navigable composition relation 
         */
        String constructorCall = concreteOT.getCodeClass().getConstructor().callString();
        returnType.setSpec(constructorCall);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.add(l.operationHeader(this));
        // create a new object from the params.
        List<String> constructorParams = new ArrayList<>();
        Iterator<Param> it = concreteOT.getCodeClass().constructorParams();
        while (it.hasNext()) {
            Param p = it.next();
            String name = searchParamName(p.getRelation());
            if (name == null) {
                if (p.getRelation().isAutoIncr()) {
                    name = l.autoIncr(p.getRelation().fieldName());
                } else {
                    throw new RuntimeException("I don't know what to do.");
                }
            }
            constructorParams.add(name);
        }
        list.add(l.createInstance(getReturnType().getType(), TEMP1, concreteOT.getName(), constructorParams.toArray(new String[0])));

        list.add(l.returnStatement(TEMP1));
        list.add(l.bodyClosure());
        return list;
    }

    private String searchParamName(Relation r) {
        for (Param p : getParams()) {
            if (p.getRelation().equals(r)) {
                return p.getName();
            }
        }
        return null;
    }

    @Override
    public List<ImportType> getImports() {
        return Collections.emptyList();
    }

}
