package equa.diagram.cd;

import com.mxgraph.model.mxCell;

import equa.diagram.Connector;
import equa.diagram.DiagramComponent;
import equa.meta.classrelations.Relation;

public class AssociationConnector extends mxCell implements Connector {

    // fields
    private static final long serialVersionUID = 1L;
    private ClassBox fromClassBox;
    private ClassBox toClassBox;
    private Relation relation;
    private String roleText;
    private int rank;

    AssociationConnector(ClassBox from, ClassBox to, Relation relation) {
        this.fromClassBox = from;
        this.toClassBox = to;
        this.relation = relation;

        if (relation.hasDefaultName()) {
            roleText = relation.multiplicity();
        } else {
            roleText = relation.multiplicity() + "\n" + relation.name();
        }
        if (relation.isComposition()) {
            Relation inverse = relation.inverse();
            if (inverse == null || !inverse.isNavigable()) {
                setStyle("startArrow=diamond;endArrow=open;strokeWidth=1;startSize=14;endSize=10; edgeStyle=topToBottomEdgeStyle");
            } else {
                setStyle("startArrow=diamond;endArrow=none;strokeWidth=1;startSize=14;endSize=10; edgeStyle=topToBottomEdgeStyle");
            }
        } else {
            //setStyle("startArrow=none;endArrow=open;strokeWidth=1;endSize=10; edgeStyle=topToBottomEdgeStyle");
            setStyle("startArrow=none;endArrow=open;strokeWidth=1;endSize=10; edgeStyle=sideToSideEdgeStyle");
        }
        rank = fromClassBox.getRank(this);
    }

    public ClassBox getFromClassBox() {
        return fromClassBox;
    }

    public String getRoleText() {
        return roleText;
    }

    public ClassBox getToClassBox() {
        return toClassBox;
    }

    public DiagramComponent getStart() {
        if (rank == 0) {
            return fromClassBox;
        } else if (rank == 1) {
            return fromClassBox.getOperationsBox();
        } else {
            return fromClassBox.getAttributesBox();
        }
    }

    public DiagramComponent getDestination() {
        if (rank == 0) {
            return toClassBox;
        } else if (rank == 1) {
            return toClassBox.getOperationsBox();
        } else {
            return toClassBox.getAttributesBox();
        }
    }

    boolean isInConflictWith(AssociationConnector ac) {
        if (ac.equals(this)) {
            return false;
        }
        if (ac.relation.equals(relation.inverse())) {
            return false;
        }

        if (fromClassBox.equals(ac.fromClassBox)) {
            return toClassBox.equals(ac.toClassBox);
        } else if (fromClassBox.equals(ac.toClassBox)) {
            return toClassBox.equals(ac.fromClassBox);
        } else {
            return false;
        }
    }

    public Relation getRelation() {
        return relation;
    }

    public int getRank() {
        return rank;
    }
}
