package equa.meta.objectmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import equa.meta.MismatchException;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.traceability.Category;
import equa.meta.traceability.SystemInput;

/**
 *
 * @author frankpeeters
 */
public class ElementsSetFactType extends ElementsFactType {

    private static final long serialVersionUID = 1L;

    ElementsSetFactType(String ftname, CollectionType ct, SubstitutionType st,
            String roleName, ObjectModel parent, FactRequirement source) {
        super(ftname, ct, parent, source);

        //initRoles
        roles = new ArrayList<>(2);
        Role role;
        role = new ObjectRole(ct, this);
        roles.add(role);
        role.setRoleName(ct.getName());
        if (st instanceof BaseType) {
            role = new BaseValueRole((BaseType) st, this);
        } else {
            role = new ObjectRole((ObjectType) st, this);
        }
        roles.add(role);
        role.setRoleName(roleName);

        //initFTE
        List<String> constants = new ArrayList<>();
        constants.add("");
        constants.add(" contains ");
        constants.add(".");

        fte = new ElementsFactTypeExpression(this, constants);
    }

    @Override
    public SubstitutionType getElementType() {
        return roles.get(1).getSubstitutionType();
    }

    @Override
    public Role getElementRole() {
        return roles.get(1);
    }

    @Override
    public List<Value> elementsOf(String collectionID) {
        Population pop = getPopulation();
        Iterator<Tuple> itTuples = pop.tuples();
        List<Value> elements = new ArrayList<>();
        while (itTuples.hasNext()) {
            Tuple tuple = itTuples.next();
            Tuple collection = (Tuple) tuple.getItem(0).getValue();
            String usedcollection = collection.getItem(0).getValue().toString();
            if (usedcollection.equals(collectionID)) {
                elements.add(tuple.getItem(1).getValue());
            }
        }
        return elements;
    }

    @Override
    void addFacts(Tuple collection, List<Value> elements,
            RequirementModel rm) throws MismatchException {
        List<SubstitutionType> types = new ArrayList<>();
        types.add(getCollectionType());
        SubstitutionType et = getElementType();
        types.add(et);

        SystemInput input = new SystemInput("Assignment of element to collection is needed.");
        String collectionString = collection.toString();
        for (Value value : elements) {
            FactRequirement source = rm.addFactRequirement(Category.SYSTEM,
                    collectionString
                    + " contains " + value.toString() + ".", input);
            List<Value> elementValues = new ArrayList<>();
            elementValues.add(collection);
            elementValues.add(value);
            getPopulation().addTuple(elementValues, roles, types, source);
        }
    }

}
