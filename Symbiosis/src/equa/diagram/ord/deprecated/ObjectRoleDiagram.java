/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.mxgraph.swing.mxGraphComponent;

import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.SubstitutionType;
import fontys.observer.PropertyListener;

/**
 *
 * @author Maurice-Asus
 */
public class ObjectRoleDiagram implements Serializable, PropertyListener {

    static final long serialVersionUID = 1L;
    private String _name;
    private ArrayList<FTorOTBox> _boxes;
    private ObjectModel _om;
    private transient mxGraphComponent _graphComponent;
    private Color _fontColor = Color.black;
    private Integer _fontSize = 12; // font size in pixels.
    private ArrayList<FactType> _factTypes;
    private transient ObjectRoleDiagramPanel _panel;

    public Integer getFontSize() {
        return _fontSize;
    }

    public void setGraphComponent(mxGraphComponent _graphComponent) {
        this._graphComponent = _graphComponent;
    }

    public void setFontSize(Integer _fontSize) {
        this._fontSize = _fontSize;
    }

    public Color getFontColor() {
        return _fontColor;
    }

    public void setFontColor(Color fontColor) {
        this._fontColor = fontColor;
    }

    public mxGraphComponent getGraphComponent() {
        return _graphComponent;
    }

    public String getName() {
        return _name;
    }

    public ArrayList<FTorOTBox> getTypes() {
        return _boxes;
    }

    public ObjectModel getOm() {
        return _om;
    }

    public void setName(String name) {
        _name = name;
    }

    public ObjectRoleDiagram(String name, ObjectModel om, mxGraphComponent graphComponent, ObjectRoleDiagramPanel panel) {
        _graphComponent = graphComponent;
        _name = name;
        _boxes = new ArrayList<FTorOTBox>();
        _om = om;
        _panel = panel;

        _factTypes = new ArrayList<FactType>();

        initialize();
    }

    public ObjectRoleDiagram(String name, ObjectModel om, ArrayList<FactType> types, mxGraphComponent graphComponent, ObjectRoleDiagramPanel panel) {
        _graphComponent = graphComponent;
        _name = name;
        _boxes = new ArrayList<FTorOTBox>();
        _om = om;
        _panel = panel;

        _factTypes = types;

        initialize();
    }

    public void initialize() {
        _om.addListener(this, "newType");
        _om.addListener(this, "removedType");

        for (Iterator<FactType> it = _om.typesIterator(); it.hasNext();) {
            FactType factType = it.next();
            if (_factTypes.contains(factType)) {
                if (factType.isObjectType()) {
                    _boxes.add(new OTBox(factType, this));
                } else {
                    _boxes.add(new FTBox(factType, this));
                }
            }
        }

        setRelations();
    }

    /**
     * This property loops through all names and if the name is found The
     * property will be changed in the objectmodel and in the ordController
     *
     * @param name
     * @return if a property was changed or not.
     */
//    private void setPropertyValueByCell(mxCell cell) throws DuplicateException {
//        for (Iterator<FactType> it = _om.typesIterator(); it.hasNext();) {
//            FactType factType = it.next();
//            for (Iterator<Role> itr = factType.roles(); it.hasNext();) {
//                Role role = itr.next();
//                String totalName = factType.getName() + role.getRoleNameOrNr();
//
//                if (totalName.equals(cell.getId())) {
//                    factType.setRoleName(cell.getValue().toString(), role);
//                    cell.setId(totalName);
//                }
//            }
//        }
//    }
    private void setRelations() {
        for (FTorOTBox box : _boxes) {
            // Leg relaties van roleboxen naar ObjectTypes
            for (RoleBox roleBox : box.getRoleBoxes()) {
                if (roleBox instanceof ObjectRoleBox) {
                    SubstitutionType substitutionType = roleBox.getRole().getSubstitutionType();

                    if (substitutionType != null) {
                        OTBox otBox = getOTBoxByName(substitutionType.getName());

                        if (otBox != null) {
                            ObjectRoleBox objectRoleBox = (ObjectRoleBox) roleBox;
                            objectRoleBox.setRoleConnectorOTBox(otBox);
                        }
                    }
                }
            }
            if (box instanceof OTBox) {
                OTBox otBox = (OTBox) box;
                otBox.SetSupertypes();
            }
        }
    }

    private OTBox getOTBoxByName(String name) {
        for (FTorOTBox fTorOTBox : _boxes) {
            if (fTorOTBox.getFactType().isObjectType() == true) {
                if (fTorOTBox.getFactType().getObjectType().getName().equals(name)) {
                    return (OTBox) fTorOTBox;
                }
            }
        }

        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof FactType) {
            FactType ft = (FactType) evt.getNewValue();
            if (ft.isObjectType()) {
                if (ft.getObjectType().isValueType()) {
                    _boxes.clear();
                    initialize();
                    _panel.propertyChange(evt);
                }
            }
        }
    }

    @Override
    public String toString() {
        return _name;
    }
}
