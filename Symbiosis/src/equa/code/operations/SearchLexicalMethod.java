/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.FOREACH;
import static equa.code.CodeNames.TEMP1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class SearchLexicalMethod extends SearchMethod {

    private static final long serialVersionUID = 1L;

    public SearchLexicalMethod(Relation relation, ObjectType concreteTarget, ObjectType parent) {
        super(relation, concreteTarget, parent);

        if (relation.targetType().equals(concreteTarget)) {
            name = "search" + Naming.withCapital(relation.name());
        } else {
            name = "search" + concreteTarget.getName();
        }
        List<Param> params = new ArrayList<>();
        Iterator<Role> itRoles = concreteTarget.getFactType().roles();
        // searching for set of identifying roles without this very role
        while (itRoles.hasNext()) {
            Role idRole = itRoles.next();
            if (!relation.refersTo(idRole)) {
                Param param = new Param(idRole.detectRoleName(), idRole.getSubstitutionType(), new IdRelation(relation.getOwner(), idRole));
                params.add(param);
            }
        }
        params = transformToBaseTypes(params);
        setParams(params);
        returnType = new ReturnType(concreteTarget);

    }

    private List<Param> transformToBaseTypes(List<Param> params) {
        List<Param> result = new ArrayList<>();
        for (Param param : params) {
            result.addAll(param.getType().transformToBaseTypes(param));
        }
        return result;
    }

    @Override
    public void initSpec() {
        StringBuilder returnSb = new StringBuilder();
        returnSb.append("let selection : Set(").append(getParent().getName()).append(") = ");
        returnSb.append(getRelation().name());
        returnSb.append("->select(");
        returnSb.append(paramCallList());
        returnSb.append(") AND \n\tif selection->isEmpty() \n\t"
                + "then @result = null else @result = selection->asSequence()->first()\n\t"
                + "endif\"");

        returnType.setSpec(returnSb.toString());

    }

    @Override
    public IndentedList getCode(Language l) {
        boolean supertype = false;
        IndentedList list = new IndentedList();
        // operation header
        list.add(l.operationHeader(this));
        // we create a body for the if statement which will be in the foreach
        // loop.
        // this is just to return the found object
        IndentedList bodyIf = new IndentedList();
        IndentedList bodyIf2 = new IndentedList();
        IndentedList bodyForEach = new IndentedList();
        String sReturn = FOREACH;
        // if we have a supertype in the collection, we need to cast.
        if (getReturnType().getType() instanceof ObjectType) {
            ObjectType ot = (ObjectType) getReturnType().getType();
            if (ot.supertypes().hasNext()) {
                supertype = true;
                sReturn = TEMP1;
            }
        }
        // now we have to make the condition for the if statement
        // we add all params to it and check if they are equal
        Iterator<Param> params = getParams().iterator();
        String statement = "";
        while (params.hasNext()) {
            Param p = params.next();
            String pName = p.getName();
            if (p instanceof SubParam) {
                SubParam sp = (SubParam) p;
                Param root = sp.getRoot();
                Param newRoot = new Param(sReturn, sp.getType(), sp.getRelation());
                SubParam sRoot = new SubParam(root.getName(), root.getType(), root.getRelation(), newRoot);
                sp.setNewRoot(sRoot);
            }
            statement += l.equalsStatement(p.expressIn(l), pName);
            if (params.hasNext()) {
                statement += l.and();
            }
        }
        bodyIf.add(l.returnStatement(sReturn));
        //statement if2 checks if instance is the same as the one we want to return
        bodyIf2.add(l.cast(getReturnType().getType(), TEMP1, FOREACH));
        //then we can check the values and return the variable
        bodyIf2.add(l.ifStatement(statement, bodyIf));
        //if we have a supertype we need to check instance etc.
        // else we can just check the values and return
        if (supertype) {
            bodyForEach.add(l.ifStatement(l.checkType(FOREACH, getReturnType().getType()), bodyIf2));
        } else {
            bodyForEach.add(l.ifStatement(statement, bodyIf));
        }
        //here we loop over the elements in the collection, it may be a supertype of the one we want to return
        list.add(l.forEachLoop(getRelation().targetType(), FOREACH, getRelation().fieldName(), bodyForEach));
        list.add(l.returnStatement("null"));
        list.add(l.bodyClosure());
        return list;

        /*
         * foreach loop {
         * 		if2 (only when supertype exists) {
         * 			if {
         * 			
         * 			}
         * 		}
         * }
         */
    }

}
