/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code;

import static equa.code.CodeNames.SYSTEM_CLASS;
import equa.code.operations.AccessModifier;
import equa.code.operations.Constructor;
import equa.code.operations.IRelationalOperation;
import equa.code.operations.Operation;
import equa.code.operations.Param;
import equa.code.operations.Property;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.Algorithm;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;
import equa.meta.traceability.SystemInput;
import equa.util.Naming;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author FrankP
 */
public class CodeClass extends ParentElement implements ListModel<Operation>, Serializable {

    private static final long serialVersionUID = 1L;
    private final Set<Operation> operations;
    private final Set<Field> fields;
    private final NameSpace nameSpace;
    private final List<Relation> relations;
    private final EventListenerList listenerList;

    /**
     * creation of an empty class
     *
     * @param parent
     * @param relations
     */
    public CodeClass(ParentElement parent, List<Relation> relations) {
        super(parent);
        operations = new TreeSet<>(new FeatureComparator());
        fields = new TreeSet<>();
        this.nameSpace = ((ObjectModel) parent.getParent().getParent()).getProject().getRootNameSpace().addSubNameSpace("domain");
        listenerList = new EventListenerList();
        this.relations = relations;
    }

    public CodeClass(ParentElement parent) {
        super(parent);
        operations = new TreeSet<>(new FeatureComparator());
        fields = new TreeSet<>();
        this.nameSpace = ((ObjectModel) getParent()).getProject().getRootNameSpace().addSubNameSpace("system");
        listenerList = new EventListenerList();
        this.relations = new ArrayList<>();
    }

    public List<Relation> getRelations() {
        return Collections.unmodifiableList(relations);
    }

    public String getIndexMethodCall(String object, Language l) {
        for (Operation feature : operations) {
            if (feature.getName().equalsIgnoreCase(object) || feature.getName().equalsIgnoreCase("search" + object)) {
                return feature.call().expressIn(l);
            }
        }
        return "unknown";
    }

    @Override
    public void remove(ModelElement member) {
        if (member instanceof Operation) {
            operations.remove(member);
        }
    }

    @Override
    public String getName() {
        return "Behavior of " + getParent().getName();
    }

    @Override
    public boolean equals(Object object) {
        return object == this;
    }

    public void initSpecs() {
        for (Operation operation : operations) {
            operation.initSpec();
        }
        for (Operation operation : operations) {
            operation.initSpec();

        }
    }

    private class FeatureComparator implements Comparator<Operation>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Operation operation1, Operation operation2) {
            return operation1.compareTo(operation2);
        }
    }

    /**
     * @param feature
     * @return if this behavior already knows a feature with the same signature
     * then false will be returned otherwise true
     *
     */
    public boolean addOperation(Operation feature) {

        if (operations.contains(feature)) {
            feature.adaptName(this);
        }

        operations.add(feature);
        fireListChanged();
        return true;

    }

    public void addField(Field field) {
        fields.add(field);
    }

    /**
     * name of operations with a duplicate signature will be expanded with a
     * postfix-number
     */
    public void eliminateDuplicateSignatures() {

        Iterator<Operation> itFeatures = operations.iterator();
        if (itFeatures.hasNext()) {
            Operation focus = itFeatures.next();
            int postfix = 2;
            // map with features which have to be changed:
            HashMap<String, Operation> renameMap = new HashMap<>();

            while (itFeatures.hasNext()) {
                Operation next = itFeatures.next();
                if (next.compareTo(focus) == 0) {
                    // next and focus have the same signature
                    renameMap.put(next.getName() + postfix, next);
                    postfix++;
                } else {
                    if (postfix > 2) {
                        renameMap.put(focus.getName() + "1", focus);
                        postfix = 2;
                    }
                    focus = next;
                }
            }

            try {
                for (String featureName : renameMap.keySet()) {
                    renameOperation(renameMap.get(featureName), featureName);

                }
            } catch (DuplicateException ex) {
                Logger.getLogger(CodeClass.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     *
     * removing of feature out of this behavior
     *
     * @param feature
     */
    public void removeOperation(Operation feature) {
        throw new UnsupportedOperationException(("NOT SUPPORTED YET."));

    }

    public void removeOperation() {
        operations.clear();
    }

    /**
     * renaming of feature to newName
     *
     * @param feature is known and not a constructor
     * @param newName isIdentifier() (
     * @see #:Naming.isIdentifier())
     * @throws DuplicateException if the newName of this feature already is in
     * use at this class
     * @throws ChangeNotAllowedException if this feature concerns a constructor
     */
    public void renameOperation(Operation feature, String newName) throws DuplicateException {
        if (!Naming.isIdentifier(newName)) {
            throw new RuntimeException(("NAME DOESN'T") + (" SATISFY THE RULES OF AN IDENTIFIER"));
        }
        if (!operations.contains(feature)) {
            throw new RuntimeException("behavioral feature doesn't exist");
        }

        String oldName = feature.getName();
        operations.remove(feature);
        feature.rename(newName);
        if (operations.contains(feature)) {
            feature.rename(oldName);
            operations.add(feature);
            throw new DuplicateException("behavioral feature cannot renamed towards " + newName);
        }
        operations.add(feature);
    }

    /**
     *
     * @return an iterator over all behavioral features of the concerning class
     */
    public Iterator<Operation> getOperations() {
        return operations.iterator();
    }

    /**
     * Finds an operation
     *
     * @param name of the operation
     * @return Operation, or null if no Operation with this name is available.
     */
    public Operation getOperation(String name) {
        for (Operation o : operations) {
            if (o.getName().toLowerCase().equals(name.toLowerCase())) {
                return o;
            }
        }
        return null;
    }

    public Operation getOperation(String name, Class<? extends Operation> c) {
        for (Operation o : operations) {
            if (o.getName().equals(name) && o.getClass().equals(c)) {
                return o;
            }
        }
        return null;
    }

    public Constructor getConstructor() {
        return (Constructor) getOperation(getParent().getName());
    }

    public String getFieldNameOrProperty(Language l, Relation r) {
        Relation owner = r.getOwner().getResponsibleRelation();
        if (owner != null && owner.inverse().isSeqRelation()) {
            return l.getProperty(r.fieldName());
        } else {
            return l.getProperty(r.fieldName());
        }
    }

    /**
     * Finds a method
     *
     * @param name where this operation starts with
     * @param relation relation where this method is based on
     * @return operation with entered prefix and based on relation, if such
     * operation doesn't exist, null will be returned
     */
    public Operation getOperation(String name, Relation relation) {
        if (relation == null) {
            return null;
        }
        for (Operation operation : operations) {
            if (operation.getName().startsWith(name)) {
                if (operation instanceof IRelationalOperation) {
                    IRelationalOperation relOp = (IRelationalOperation) operation;
                    if (relOp.getRelation().equals(relation)) {
                        return operation;
                    }
                }
            }
        }
        return null;
    }

    public Property getProperty(Relation relation) {
        if (relation == null) {
            return null;
        }
        for (Operation operation : operations) {

            if (operation instanceof Property) {
                Property property = (Property) operation;
                if (property.getRelation().equals(relation)) {
                    return property;
                }
            }

        }
        return null;
    }

    public Set<Property> getIdentifyingProperties() {
        Set<Property> idProps = new HashSet<>();
        for (Operation operation : operations) {
            if (operation instanceof Property) {
                Property property = (Property) operation;
                if (property.getRelation() instanceof IdRelation) {
                    idProps.add(property);
                }
            }
        }
        return idProps;
    }

    public Iterator<Field> getFields() {
        return fields.iterator();
    }

    @Override
    public int getSize() {
       // ObjectType parent = (ObjectType) getParent();
        return operations.size() ;//+ parent.algorithms().size();
    }

    @Override
    public Operation getElementAt(int index) {

        if (index < operations.size()) {
            Iterator<Operation> it = getOperations();
            int i = 0;
            while (i < index) {
                i++;
                it.next();
            }
            return it.next();
        } 

        return null;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    // /**
    // *
    // * @return all relevant parameters of the generated constructor in
    // relation
    // * to objecttype within relation
    // */
    // public ArrayList<Param> constructorParams(Relation relation) {
    // Iterator<Param> iterator = null;
    // iterator = getConstructor().getParams().iterator();
    //
    // ArrayList<Param> params = new ArrayList<>();
    // if (iterator != null) {
    // while (iterator.hasNext()) {
    // Param param = iterator.next();
    // if (param.getType() instanceof CT) {
    // CT ct = (CT) param.getType();
    // //if (ct.getType() == objectType)
    // {
    // if (getParent() instanceof CollectionType) {
    // CollectionType parentCT = (CollectionType) getParent();
    // FrequencyConstraint fc = parentCT.getFrequencyConstraint();
    // if (fc != null) {
    // if (fc.getMin() == 2 && fc.getMax() == 2) {
    // params.add(new Param(Naming.withoutCapital(objectType.getName()),
    // objectType, param.getRelation()));
    // } else {
    // params.add(param);
    // }
    // }
    // }
    // } else {
    // params.add(param);
    // }
    // } else if (param.getType() != objectType
    // || (!relation.hasDefaultName()
    // && !param.getName().equals(relation.roleName()))) {
    // params.add(param);
    // }
    // }
    // }
    // return params;
    // }
    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    public void fireListChanged() {
        EventListener[] listeners = listenerList.getListeners(ListDataListener.class);
        for (EventListener l : listeners) {
            ((ListDataListener) l).contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
        }
    }

    public Iterator<Param> constructorParams() {
        Iterator<Param> iterator = null;
        for (Operation feature : operations) {
            if (feature instanceof Constructor) {
                iterator = ((Constructor) feature).getParams().iterator();
            }
        }
        return iterator;
    }

    /**
     * all constructors get namespace-access
     */
    public void encapsulateConstructor() {
        for (Operation feature : operations) {
            if (feature instanceof Constructor) {
                if (feature.getAccess() == AccessModifier.PUBLIC) {
                    feature.setAccessModifier(AccessModifier.NAMESPACE);
                }
            }
        }
    }

    public String getDirectory() {
        StringBuilder result = new StringBuilder();
        NameSpace nameSpace = this.nameSpace;
        do {
            result.append(nameSpace.getName().toLowerCase());
            result.append("/");
            nameSpace = nameSpace.getSubNameSpace();
        } while (nameSpace != null);
        return result.toString();
    }

    /**
     *
     * @return a set with the names of all public properties of this class
     */
    public Set<String> publicPropertyNames() {
        Set<String> properties = new TreeSet<>();

        for (Operation feature : operations) {
            if ((feature instanceof Property)) {
                if (feature.getAccess() == AccessModifier.PUBLIC && ((Property) feature).isPublicGetter()) {
                    properties.add(feature.getName());
                }
            }
        }
        return properties;
    }

    /**
     *
     * @return a set with all public properties of this class
     */
    public Set<Property> publicProperties() {
        Set<Property> properties = new TreeSet<>();
        ObjectType objectType = (ObjectType) getParent();
        for (ObjectType supertype : objectType.allSupertypes()) {
            for (Operation feature : supertype.getCodeClass().operations) {
                if ((feature instanceof Property)) {
                    if (feature.getAccess() == AccessModifier.PUBLIC && ((Property) feature).isPublicGetter()) {
                        properties.add((Property) feature);
                    }
                }
            }
        }
        for (Operation feature : operations) {
            if ((feature instanceof Property)) {
                if (feature.getAccess() == AccessModifier.PUBLIC && ((Property) feature).isPublicGetter()) {
                    properties.add((Property) feature);
                }
            }
        }
        return properties;
    }

    public boolean hasUnspecifiedOperation() {
        if (getParent() instanceof ObjectType) {
            Map<OperationHeader, Algorithm> algos = ((ObjectType) getParent()).algorithms();
            for (Algorithm a : algos.values()) {
                if (!a.isSealed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFinal() {
        if (getParent() instanceof ObjectModel) {
            return true;
        }
        return !hasUnspecifiedOperation() && !((ObjectType) getParent()).subtypes().hasNext();
    }

    public String getCode(Language l, boolean withOrm) {
        Set<ImportType> imports = new TreeSet<>();
        IndentedList list = new IndentedList();
        Iterator<Field> fields = getFields();
        while (fields.hasNext()) {
            Field f = fields.next();
            if (f.getRelation() != null) {
                imports.addAll(l.imports(f.getRelation()));
            }
        }
        Iterator<Operation> operations = getOperations();
        while (operations.hasNext()) {
            Operation o = operations.next();
            try {
                imports.addAll(o.getImports());
            } catch (Exception e) {

            }
        }

        list.add(l.nameSpaceAndImports(nameSpace, imports));
        if (getParent() instanceof ObjectType) {
            list.add(l.classHeader(AccessModifier.PUBLIC, (ObjectType) getParent(), false, withOrm));
        } else if (getParent() instanceof ObjectModel) {
            list.add(l.systemClassHeader());
        }

        fields = getFields();
        if (fields.hasNext()) {
            list.add("");
            do {
                Field f = fields.next();
                list.add(f.getCode(l, withOrm, hasUnspecifiedOperation()));
            } while (fields.hasNext());
        }
        operations = getOperations();
        while (operations.hasNext()) {
            Operation o = operations.next();
            list.add("");
            list.add(o.getCode(l));
        }
        if (getParent() instanceof ObjectType) {
            ObjectType ot = (ObjectType) getParent();
            Map<OperationHeader, Algorithm> algorithms = ot.algorithms();
            for (Entry<OperationHeader, Algorithm> e : algorithms.entrySet()) {
                Algorithm a = e.getValue();
                list.add("");
                if (a.isSealed()) {
                    list.add(a.getCode());
                } else {
                    list.add(l.operationTemplate(e.getKey()));
                }
            }
        }
        list.add(l.classClosure(), false);
        list.add(l.nameSpaceEnd());
        return list.toString();
    }

    public String getCodeForSpecification(Language l) {
        ObjectType ot = (ObjectType) getParent();
        IndentedList list = new IndentedList();
        list.add(l.docStart());
        list.add(l.docLine("Please use the indenting and new line rules for your language."));
        list.add(l.docLine("Do not put line breaks where the line is not finished."));
        list.add(l.docEnd());
        list.add(l.nameSpaceStart(nameSpace));
        list.add("");
        list.add(l.classHeader(AccessModifier.PUBLIC, ot, true, false));
        list.add("");
        list.add(l.constructorTemplate(getConstructor()));
        Map<OperationHeader, Algorithm> algos = ot.algorithms();
        for (Entry<OperationHeader, Algorithm> e : algos.entrySet()) {
            if (e.getValue().isEditable()) {
                list.add("");
                if (e.getValue().getCode() == null) {
                    list.add(l.operationTemplate(e.getKey()));
                } else {
                    list.add(e.getValue().getCode());
                }
            }
        }
        list.add(l.classClosure(), false);
        list.add(l.nameSpaceEnd());
        return list.toString();
    }

    public String getClassName() {
        if (getParent() instanceof ObjectType) {
            return getParent().getName();
        } else {
            return SYSTEM_CLASS;
        }
    }
}
