package equa.diagram.cd;
//SR

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import equa.code.operations.Operation;
import equa.diagram.Diagram;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.project.Project;

/**
 *
 * @author Frank Peeters
 */
@Entity
public class ClassDiagram extends Diagram implements Comparable<ClassDiagram> {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    //@OneToMany
    //@MapKeyColumn (name = "ObjectType")
    private HashMap<ObjectType, ClassBox> classBoxes;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Project project;

    public ClassDiagram() {
    }

    public ClassDiagram(String name, ObjectModel om) {
        super(name);
        Collection<ObjectType> selection = new ArrayList<>();
        for (FactType ft : om.types()) {
            if (ft.isClass() && !ft.isValueType()) {
                selection.add(ft.getObjectType());
            }
        }
        initClassBoxes(selection);
        relateAndFillClassBoxes();

    }

    public ClassDiagram(String name, Collection<ObjectType> selection) {
        super(name);
        initClassBoxes(selection);
        relateAndFillClassBoxes();

    }

    public Iterator<ClassBox> classBoxes() {
        return classBoxes.values().iterator();
    }

    private void initClassBoxes(Collection<ObjectType> selection) {
        classBoxes = new HashMap<>();
        for (ObjectType ot : selection) {
            classBoxes.put(ot, new ClassBox(ot));
        }
    }

    public final void relateAndFillClassBoxes() {
        for (ClassBox cb : classBoxes.values()) {
            relateAndFillClassBox(cb);
        }
    }

    void fold(ObjectType ot) {
        for (ClassBox cb : classBoxes.values()) {
            cb.clear();
        }
        classBoxes.remove(ot);
        relateAndFillClassBoxes();
    }

    ClassBox unfold(ObjectType ot) {
        for (ClassBox cb : classBoxes.values()) {
            cb.clear();
        }
        ClassBox cb = new ClassBox(ot);
        classBoxes.put(ot, cb);
        relateAndFillClassBoxes();
        return cb;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(ClassDiagram t) {
        return getName().compareTo(t.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassDiagram) {
            return compareTo((ClassDiagram) obj) == 0;
        } else {
            return false;
        }
    }

    private void relateAndFillClassBox(ClassBox cb) {
        ObjectType ot = cb.getObjectType();

        // Set outgoing associations and attributes
        for (Relation relation : ot.relations()) {
            if (!relation.isDerivable()) {
                if (relation.targetType() instanceof BaseType) {
                    if (relation.isMapRelation()) {
                    } else {
                        classBoxes.get(ot).getAttributesBox().addAttribute(relation);
                    }
                } else {
                    ObjectType target = (ObjectType) relation.targetType();
                    Relation inverse = relation.inverse();
                    if (classBoxes.containsKey(target)) {
                        if ((inverse == null || !inverse.isComposition())) {
                            classBoxes.get(ot).addAssociationConnector(classBoxes.get(target), relation);
                        }
                    } else {
                        classBoxes.get(ot).getAttributesBox().addAttribute(relation);
                    }
                }
            }
        }
// Set GeneralizationConnectors
        Iterator<ObjectType> itSuperTypes = ot.supertypes();
        while (itSuperTypes.hasNext()) {
            ObjectType supertype = itSuperTypes.next();
            ClassBox superClass = classBoxes.get(supertype);
            if (superClass != null) {
                cb.addGeneralizationConnector(superClass);
            }
        }

        // Set operations
        Iterator<Operation> itFeatures = ot.getCodeClass().getOperations();
        while (itFeatures.hasNext()) {
            classBoxes.get(ot).getOperationsBox().addOperation(itFeatures.next());
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
