package equa.diagram.cd;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;

import equa.diagram.DiagramComponent;
import equa.diagram.DiagramContainer;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;

/**
 *
 * @author Frank Peeters
 */
public class ClassBox extends DiagramContainer {

    // fields
    private static final long serialVersionUID = 1L;

    private ObjectType objectType;
    private ArrayList<AssociationConnector> associationConnectors;
    private ArrayList<GeneralizationConnector> generalizationConnectors;
    private AttributesBox attributesBox;
    private OperationsBox operationsBox;
    private Font font;
    private Font component_font;

    // construction
    public ClassBox(ObjectType objectType) {
        super(null);
        this.objectType = objectType;

        associationConnectors = new ArrayList<>();
        generalizationConnectors = new ArrayList<>();

        attributesBox = new AttributesBox(this);
        attributesBox.setEastMarge(5);
        attributesBox.setWestMarge(1);
        operationsBox = new OperationsBox(this);
        operationsBox.setEastMarge(5);
        operationsBox.setWestMarge(1);

        if (objectType.getFactType().isEnum()) {
            setValue(" <<enum>> " + objectType.getName() + " ");
        } else {
            setValue(" " + objectType.getName() + " ");
        }
        setVertex(true);
        setVisible(true);
        if (objectType.isAbstract()) {
            setStyle("fontFamily=Verdana;fontSize=14;fontStyle=3;fontColor=white;verticalAlign=top;align=center;fillColor=blue;overflow=hidden");
            font = new Font("Verdana", Font.ITALIC & Font.BOLD, 14);
        } else {
            setStyle("fontFamily=Verdana;fontSize=14;fontStyle=1;fontColor=white;verticalAlign=top;align=center;fillColor=blue;overflow=hidden");
            font = new Font("Verdana", Font.BOLD, 14);
        }
        component_font = new Font("Verdana", Font.PLAIN, 10);

        operationsBox.setCollapsed(true);
    }

    // properties
    public ObjectType getObjectType() {
        return objectType;
    }

    public Iterator<AssociationConnector> getAssociationConnectors() {
        return associationConnectors.iterator();
    }

    public Iterator<GeneralizationConnector> getGeneralizationConnectors() {
        return generalizationConnectors.iterator();
    }

    public AttributesBox getAttributesBox() {
        return attributesBox;
    }

    public OperationsBox getOperationsBox() {
        return operationsBox;
    }

    public void addAssociationConnector(ClassBox targetBox, Relation relation) {
        associationConnectors.add(new AssociationConnector(this, targetBox, relation));
    }

    public void removeAssociationConnector(AssociationConnector connector) {
        associationConnectors.remove(connector);
    }

    public void addGeneralizationConnector(ClassBox generalization) {
        generalizationConnectors.add(new GeneralizationConnector(this, generalization));
    }

    void removeInheritanceConnector(GeneralizationConnector connector) {
        generalizationConnectors.remove(connector);
    }

    public int getRank(AssociationConnector ac) {
        int rank = 0;
        for (AssociationConnector associationConnector : associationConnectors) {
            if (associationConnector.isInConflictWith(ac)) {
                rank++;
            }
        }
        return rank;
    }

    @Override
    protected DiagramComponent component(int i) {
        if (i == 0) {
            return attributesBox;
        } else if (i == 1) {
            return operationsBox;
        } else {
            throw new RuntimeException("component " + i + " doesn't exist");
        }
    }

    @Override
    protected int size() {
        return 2;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public Font getComponentFont() {
        return component_font;
    }

    void removeInverse(Relation inverse) {
        for (AssociationConnector connector : associationConnectors) {
            if (connector.getRelation().equals(inverse)) {
                associationConnectors.remove(connector);
                return;
            }
        }
    }

    void clear() {
        associationConnectors.clear();
        generalizationConnectors.clear();
        attributesBox.clear();
        operationsBox.clear();
    }

    int getChilds() {
        return getChildCount() + attributesBox.getChildCount() + operationsBox.getChildCount();
    }
}
