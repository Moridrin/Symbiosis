package equa.diagram.cd;

import java.awt.Font;

import equa.diagram.DiagramComponent;

public class OperationsBoxItem extends DiagramComponent {

    // fields
    private static final long serialVersionUID = 1L;

    // construction
    OperationsBoxItem(equa.code.operations.Operation feature, OperationsBox container) {
        super(container);
        super.setVertex(true);
        super.setValue(" " + feature.getNameParamTypesAndReturnType() + " ");
        if (feature.isClassMethod()) {
            setStyle("spacingLeft=2;align=left;fontColor=black;strokeColor=none;fontStyle=4;overflow=hidden");
        } else {
            setStyle("spacingLeft=2;align=left;fontColor=black;strokeColor=none;overflow=hidden");
        }
    }

    @Override
    public Font getFont() {
        return container.getComponentFont();
    }
}
