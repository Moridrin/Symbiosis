/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.beans.PropertyChangeEvent;

import org.apache.commons.lang3.text.WordUtils;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

import equa.meta.objectmodel.FactType;

/**
 *
 * @author Maurice-Asus
 */
public class FTBox extends FTorOTBox {

    private static final long serialVersionUID = 1L;
    private mxCell _typeExpressionCell;
    private double widthFullRoleBox;

    public FTBox(FactType factType, ObjectRoleDiagram controller) {
        super(factType, controller);
        double widthConstraintBoxes = super.getRoleBoxes().get(0).getWidthConstraintBoxes();
        widthFullRoleBox = widthConstraintBoxes + super.maxLengthInPixels;
        addTypeExpression();
    }

    public void setPosition(double x, double y, double width, double height) {
        this.setGeometry(new mxGeometry(x, y, width, height));
    }

    private void addTypeExpression() {
        String typeExpression = super.getFactType().getTypeExpression();
        String line;
        int countLines = 1;

        if (getWidthInPixels(typeExpression) > widthFullRoleBox) {
            line = WordUtils.wrap(typeExpression, getTextLengthFromPixels(typeExpression, widthFullRoleBox));
            countLines = line.split(System.getProperty("line.separator")).length;
        } else {
            line = typeExpression;
        }

        _typeExpressionCell = new mxCell(line);
        _typeExpressionCell.setGeometry(new mxGeometry(0, 30 + super.getRoleBoxes().size() * 20, widthFullRoleBox, fontHeight * countLines));
        _typeExpressionCell.setStyle(getStyle(false));
        _typeExpressionCell.setVertex(true);
    }

    public mxCell getTypeExpressionCell() {
        return _typeExpressionCell;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
    }
}
