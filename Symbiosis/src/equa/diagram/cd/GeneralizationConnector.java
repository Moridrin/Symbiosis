package equa.diagram.cd;

import com.mxgraph.model.mxCell;

import equa.diagram.Connector;

public class GeneralizationConnector extends mxCell implements Connector {

    // fields
    private static final long serialVersionUID = 1L;
    private ClassBox subClass;
    private ClassBox superClass;

    GeneralizationConnector(ClassBox subClass, ClassBox superClass) {
        this.subClass = subClass;
        this.superClass = superClass;
        setStyle("startArrow=none;endArrow=block;strokeWidth=1;endSize=15;edgeStyle=topToBottomEdgeStyle");
    }

    // properties
    public ClassBox getSubClass() {
        return subClass;
    }

    public ClassBox getSuperClass() {
        return superClass;
    }
}
