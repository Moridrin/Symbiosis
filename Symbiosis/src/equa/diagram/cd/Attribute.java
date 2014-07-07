package equa.diagram.cd;

import java.awt.Font;

import equa.diagram.DiagramComponent;
import equa.meta.classrelations.Relation;

/**
 *
 * @author frankpeeters
 */
public class Attribute extends DiagramComponent {

    // fields
    private static final long serialVersionUID = 1L;
    private Relation relation;

    // construction
    Attribute(Relation relation, AttributesBox container) {
        super(container);
        this.relation = relation;
        super.setValue(" " + relation.asAttribute() + " ");
        super.setVertex(true);
        setStyle("spacingLeft=2;align=left;fontColor=black;strokeColor=none;overflow=hidden");
    }

    @Override
    public Font getFont() {
        return container.getComponentFont();
    }

    public Relation getRelation() {
        return relation;
    }
}
