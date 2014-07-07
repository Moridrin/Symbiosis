/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import equa.meta.MismatchException;
import equa.meta.NotParsableException;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.FactTypeRelation;
import equa.meta.classrelations.ObjectTypeRelation;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RequirementModel;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class CollectionType extends ObjectType implements Serializable {

    private static final long serialVersionUID = 1L;
    private ElementsFactType elementsFT;

    CollectionType(FactType parent, String begin, String separator, String end) {
        super(parent, new CollectionTypeExpression(parent, Naming.withoutCapital(begin), separator, end));
        this.elementsFT = null;
    }

    public SubstitutionType getElementType() {
        return elementsFT.getElementType();
    }

    void setElementsFactType(ElementsFactType eft) {
        elementsFT = eft;
    }

    public int maxSize() {
        FrequencyConstraint fc = elementsFT.roles.get(0).getFrequencyConstraint();
        if (fc == null) {
            return -1;
        } else {
            return fc.getMax();
        }
    }

    public FrequencyConstraint getFrequencyConstraint() {
        return elementsFT.roles.get(0).getFrequencyConstraint();

    }

    public boolean mayBeEmpty() {
        return !elementsFT.roles.get(0).isMandatory();
    }

    @Override
    public boolean isComparable() {
        return true;
    }

    @Override
    public String getKind() {
        if (isSequence()){
            return "SQ";
        }
        return "ST";
    }

    public Role getElementRole() {
        return elementsFT.getElementRole();
    }

    public List<Value> elementsOf(String collection) {
        return elementsFT.elementsOf(collection);
    }

    void addElements(Tuple collection, List<Value> elements,
            RequirementModel rm) throws MismatchException {
        elementsFT.addFacts(collection, elements, rm);
    }

    public boolean isSequence() {
        return elementsFT instanceof ElementsSequenceFactType;
    }

    CollectionTuple addCollectionTuple(List<Value> values, FactRequirement source) throws MismatchException {
        FactType ft = getFactType();
        CollectionTuple collection = ft.getPopulation().addCollectionTuple(values, source);
        this.addElements(collection, values, source.getModel());
        return collection;
    }

    @Override
    public Value parse(String expression, boolean otherOptions, FactRequirement source)
            throws MismatchException {
        CollectionTypeExpression ote = (CollectionTypeExpression) getOTE();
        if (!ote.isParsable()) {
            throw new NotParsableException(ote, "OTE OF " + getName() + " NOT PARSABLE");
        }

        ParseResult result = ote.parse(expression, otherOptions, source);
        List<Value> elements = result.getValues();
        addSource(source);

        return addCollectionTuple(elements, source);
    }

    @Override
    public void setValueType(boolean valueType) {
        if (this.valueType == valueType) {
            return;
        }

        if (valueType) {
            this.valueType = true;
            for (Role role : plays) {
                if (!role.isDerivable() && !(role.getParent() instanceof ElementsFactType)) {
                    role.setNavigable(false);
                }
            }
        } else {
            this.valueType = false;
        }

        getFactType().fireListChanged();

        publisher.inform(this, "valueType", null, isValueType());

    }

    List<Relation> playsRoleRelations() {
        ArrayList<Relation> list = new ArrayList<>();

        for (Role role : plays) {
            if (!(role.getParent() instanceof ElementsFactType) && role.isNavigable() && !role.isQualifier()) {
                Relation relation;
                if (role.getParent().isClass()) {
                    relation = new ObjectTypeRelation(this, role);
                } else if (role.getParent().nonQualifierSize() == 1) {
                    relation = new BooleanRelation(this, role);
                } else {
                    relation = new FactTypeRelation(this, role);
                }
                list.add(relation);
            }
        }
        return list;
    }

    @Override
    public void remove() {
        elementsFT.remove();
        elementsFT = null;
        super.remove();
    }

    void remove(CollectionTuple tuple) {
        if (elementsFT != null) {
            elementsFT.removeTuplesOf(tuple);
        }
        getFactType().getPopulation().remove(tuple);
    }

}
