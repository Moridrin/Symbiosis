/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectType;

/**
 *
 * @author Maurice-Asus
 */
public class OTBox extends FTorOTBox {

    private static final long serialVersionUID = 1L;
    private mxCell _dock;
    private ObjectType _objectType;
    private ObjectRoleDiagram _controller;
    private transient ArrayList<InheritanceConnector> _inheritanceConnectors;
    private mxCell _typeExpressionCell;
    private double widthFullRoleBox;

    public OTBox(FactType factType, ObjectRoleDiagram controller) {
        super(factType, controller);

        _inheritanceConnectors = new ArrayList<InheritanceConnector>();
        _controller = controller;
        _objectType = factType.getObjectType();

        double widthConstraintBoxes = super.getRoleBoxes().get(0).getWidthConstraintBoxes();
        widthFullRoleBox = widthConstraintBoxes + super.maxLengthInPixels;

        setDock();
        addTypeExpression();
    }

    private void addTypeExpression() {
        String typeExpression = super.getFactType().getTypeExpression();
        String line = "";
        int countLines = 1;

        if (getWidthInPixels(typeExpression) > widthFullRoleBox) {
            line = WordUtils.wrap(typeExpression, getTextLengthFromPixels(typeExpression, widthFullRoleBox));
            countLines = line.split(System.getProperty("line.separator")).length;
        } else {
            line = typeExpression;
        }

        _typeExpressionCell = new mxCell(line);
        _typeExpressionCell.setGeometry(new mxGeometry(0, _dock.getGeometry().getHeight(), _dock.getGeometry().getWidth(), fontHeight * countLines));
        _typeExpressionCell.setStyle(getStyle(false));
        _typeExpressionCell.setVertex(true);
    }

    public mxCell getTypeExpressionCell() {
        return _typeExpressionCell;
    }

    private void setDock() {
        _dock = new mxCell();
        _dock.setStyle("rounded=1");
        _dock.setGeometry(new mxGeometry(10, 10, widthFullRoleBox + 30, (super.getRoleBoxes().size() * 20) + fontHeight + 30));
        _dock.setVertex(true);
        _dock.setId(getObjectType().getName());
    }

    public void SetSupertypes() {
        Set<ObjectType> superTypes = _objectType.allSupertypes();

        if (superTypes.size() > 0) {
            for (Iterator<ObjectType> it = superTypes.iterator(); it.hasNext();) {
                ObjectType ot = it.next();
                System.out.println(ot);
                for (FTorOTBox oTBox : _controller.getTypes()) {
                    if (oTBox instanceof OTBox) {
                        if (oTBox.getFactType().getObjectType().equals(ot)) {
                            // Nieuwe inherritance connector van deze naar de andere
                            _inheritanceConnectors.add(new InheritanceConnector(this, (OTBox) oTBox));
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the _objectType
     */
    public ObjectType getObjectType() {
        return _objectType;
    }

    public mxCell getDock() {
        return _dock;
    }

    /**
     * @return the _InherritanceConnector
     */
    public ArrayList<InheritanceConnector> getInheritanceConnector() {
        if (_inheritanceConnectors == null) {
            _inheritanceConnectors = new ArrayList<InheritanceConnector>();
            SetSupertypes();
        }
        return _inheritanceConnectors;
    }

    /**
     * @param InheritanceConnector the _InheritanceConnector to set
     */
    public void setInheritanceConnector(ArrayList<InheritanceConnector> InheritanceConnector) {
        this._inheritanceConnectors = InheritanceConnector;
    }

    public void setPosition(double x, double y, double width, double height) {
        this.setGeometry(new mxGeometry(x, y, width, height));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
    }
}
