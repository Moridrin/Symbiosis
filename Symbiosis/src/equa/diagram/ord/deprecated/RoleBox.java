/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

import equa.meta.objectmodel.Constraint;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.StaticConstraint;
import equa.meta.objectmodel.UniquenessConstraint;
import fontys.observer.PropertyListener;

/**
 *
 * @author Maurice-Asus
 */
public class RoleBox extends mxCell implements PropertyListener {

    private static final long serialVersionUID = 1L;
    private Role _role;
    private ObjectRoleDiagram _controller;
    private mxCell uniqueness;
    private mxCell getset;
    private mxCell mandatory;
    private mxCell roleBoxParentCell;
    private double widthMandatory;
    private double widthGetset;
    private double widthUniqueness;

    public mxCell getRoleBoxParentCell() {
        return roleBoxParentCell;
    }

    public RoleBox(Role role, ObjectRoleDiagram controller) {
        roleBoxParentCell = new mxCell();
        _role = role;
        _controller = controller;
        String roleName = role.getRoleName() + ":" + role.getSubstitutionType().getName();
        this.setValue(roleName);
        this.setStyle(getStyle());
        this.setVertex(true);

        mandatory = new mxCell();
        widthMandatory = 10;
        mandatory.setId("Mandatory" + _role.getRoleNameOrNr());

        uniqueness = new mxCell();
        mandatory.setId("uniqueness");
        widthUniqueness = 5;
        setPositionUniqueness(0, 0, widthUniqueness, 20);
        uniqueness.setVertex(true);

        getset = new mxCell();
        widthGetset = 15;
        setPositionGetset(widthUniqueness, 0, widthGetset, 20);
        getset.setId("Getset" + _role.getRoleNameOrNr());
        getset.setVertex(true);

        _role.addListener(this, "name");
        _role.addListener(this, "nr");
        _role.addListener(this, "mandatory");
        _role.addListener(this, "composition");
        _role.addListener(this, "frozenId");
        _role.addListener(this, "settable");
        _role.addListener(this, "addable");
        _role.addListener(this, "removable");
        _role.addListener(this, "navigable");
        _role.addListener(this, "qualifier");
        _role.addListener(this, "hidden");
        _role.addListener(this, "frozen");
        _role.addListener(this, "hiddenId");

    }

    public Double getWidthConstraintBoxes() {
        double width = widthGetset + widthMandatory + widthUniqueness;
        return width;
    }

    public void addRoleBox(ArrayList<String> ucConstraints, double begin, double maxLengthInPixels) {
        addParentUniqueness(roleBoxParentCell);

        roleBoxParentCell.insert(getset);
        getset.setId("getset" + getRole().getRoleNameOrNr());

        if (getRole().isMandatory()) {
            mandatory.setStyle("ellipse;fillColor=#000000;gradientColor=#000000;lineColor=#000000");
            mandatory.setVertex(true);
        }

        for (Iterator<StaticConstraint> it = getRole().constraints(); it.hasNext();) {
            Constraint constraint = it.next();

            if (constraint instanceof UniquenessConstraint) {
                UniquenessConstraint uc = (UniquenessConstraint) constraint;
                if (!ucConstraints.contains(uc.getId())) {
                    ucConstraints.add(uc.getId());
                }

                uniqueness.setStyle("fillColor=#0018FF;gradientColor=#0018FF");
                setPositionUniqueness(begin, 0, widthUniqueness, 20);
            }
        }
        begin += widthUniqueness;

        String getsetS = "";
        if (getRole().isNavigable()) {
            getsetS = "<";
        }
        if (getRole().isSettable()) {
            getsetS += ">";
        }

        setValueGetset(getsetS);
        setPositionGetset(begin, 0, widthGetset, 20);
        begin += widthGetset;

        this.setPosition(begin, 0, maxLengthInPixels + 20, 20);
        roleBoxParentCell.insert(this);
        begin += maxLengthInPixels + 10;

        setPositionMandatory(begin, 5, widthMandatory, 10);
        roleBoxParentCell.insert(mandatory);
        mandatory.setId("Mandatory" + getRole().getRoleNameOrNr());

        begin = 0;
    }

    public void addParentUniqueness(mxCell parent) {
        parent.insert(uniqueness);
    }

    public void addParentGetset(mxCell parent) {
        parent.insert(getset);
    }

    public void addParentMandatory(mxCell parent) {
        parent.insert(mandatory);
    }

    public void setValueGetset(String value) {
        getset.setValue(value);
    }

    public void setPositionMandatory(double x, double y, double width, double height) {
        mandatory.setGeometry(new mxGeometry(x, y, width, height));
    }

    public void setPositionUniqueness(double x, double y, double width, double height) {
        uniqueness.setGeometry(new mxGeometry(x, y, width, height));
    }

    public void setPositionGetset(double x, double y, double width, double height) {
        getset.setGeometry(new mxGeometry(x, y, width, height));
    }

    public Role getRole() {
        return _role;
    }

    public void setRole(Role role) {
        this._role = role;
    }

    public void setPosition(double x, double y, double width, double height) {
        this.setGeometry(new mxGeometry(x, y, width, height));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) {
            super.setValue(evt.getNewValue());
        } else if (evt.getPropertyName().toLowerCase().equals("nr")) {
            super.setId(evt.getNewValue().toString());
        } else if (evt.getPropertyName().toLowerCase().equals("mandatory")) {

            for (int i = 0; i < roleBoxParentCell.getChildCount(); i++) {
                Object obj = roleBoxParentCell.getChildAt(i);
                if (obj instanceof mxCell) {
                    if (((mxCell) obj).getId() != null && ((mxCell) obj).getId().equals("Mandatory" + _role.getRoleNameOrNr())) {
                    }
                }
            }

            if ((Boolean) evt.getNewValue() == true) {
                mandatory.setStyle("ellipse;fillColor=#000000;gradientColor=#000000;lineColor=#000000");
                mandatory.setVertex(true);
            } else {
                mandatory.setVertex(false);
            }
        } else if (evt.getPropertyName().toLowerCase().equals("composition")) {
        } else if (evt.getPropertyName().toLowerCase().equals("frozenId")) {
        } else if (evt.getPropertyName().toLowerCase().equals("settable")) {

            for (int i = 0; i < roleBoxParentCell.getChildCount(); i++) {
                Object obj = roleBoxParentCell.getChildAt(i);
                if (obj instanceof mxCell) {
                    if (((mxCell) obj).getId() != null && ((mxCell) obj).getId().equals("Getset" + _role.getRoleNameOrNr())) {
                    }
                }
            }

            String getsetS = "";

            if (getRole().isNavigable()) {
                getsetS = "<";
            }

            if (getRole().isSettable()) {
                getsetS += ">";
            }

            setValueGetset(getsetS);

        } else if (evt.getPropertyName().toLowerCase().equals("addable")) {
        } else if (evt.getPropertyName().toLowerCase().equals("removable")) {
        } else if (evt.getPropertyName().toLowerCase().equals("navigable")) {
            for (int i = 0; i < roleBoxParentCell.getChildCount(); i++) {
                Object obj = roleBoxParentCell.getChildAt(i);
                if (obj instanceof mxCell) {
                    if (((mxCell) obj).getId() != null && ((mxCell) obj).getId().equals("Getset" + _role.getRoleNameOrNr())) {
                    }
                }
            }

            String getsetS = "";

            if (getRole().isNavigable()) {
                getsetS = "<";
            }

            if (getRole().isSettable()) {
                getsetS += ">";
            }

            setValueGetset(getsetS);
        } else if (evt.getPropertyName().toLowerCase().equals("hidden")) {
        } else if (evt.getPropertyName().toLowerCase().equals("frozen")) {
        } else if (evt.getPropertyName().toLowerCase().equals("hiddenId")) {
        }

        super.setValue(_role.getRoleName() + ":" + _role.getSubstitutionType().getName());
        if (_controller.getGraphComponent() != null) {
            _controller.getGraphComponent().getGraph().refresh();
        }
    }

    @Override
    public String getStyle() {
        return "fillColor=#FFFFFF;gradientColor=#FFFFFF;overflow=fill;";
    }
}
