/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

import equa.meta.objectmodel.BaseValueRole;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.Role;
import fontys.observer.PropertyListener;

/**
 *
 * @author Maurice-Asus
 */
public class FTorOTBox extends mxCell implements PropertyListener {

    private static final long serialVersionUID = 1L;
    private FactType _factType;
    private ArrayList<RoleBox> _roleBoxes;
    private mxCell _typeExpressionCell;
    private ObjectRoleDiagram _controller;
    protected double maxLengthInPixels;

    public double getMaxLengthInPixels() {
        return maxLengthInPixels;
    }
    private double maxLength;
    protected int fontHeight;

    public FactType getFactType() {
        return _factType;
    }

    public ArrayList<RoleBox> getRoleBoxes() {
        return _roleBoxes;
    }

    public FTorOTBox(FactType factType, ObjectRoleDiagram controller) {
        _factType = factType;
        _controller = controller;
        _roleBoxes = new ArrayList<RoleBox>();
        initialize();
        generateMaxlength();
        setTypeName();
        _factType.addListener(this, "name");
        _factType.addListener(this, "objectType");
        _factType.addListener(this, "rolesIterator");
        _factType.addListener(this, "typeExpression");
    }

    private void setTypeName() {
        double widthConstraintBoxes = getRoleBoxes().get(0).getWidthConstraintBoxes();
        double widthFullRoleBox = widthConstraintBoxes + maxLengthInPixels;

        super.setValue(_factType.getName());
        super.setGeometry(new mxGeometry(0, 0, widthFullRoleBox + 20, fontHeight)); //binnenblok
        super.setStyle(getStyle(true));

        super.setVertex(true);
    }

    private int generateMaxlength() {
        maxLengthInPixels = 0;

        for (RoleBox roleBox : getRoleBoxes()) {
            //Get the width of the typeExpression
            String s = roleBox.getRole().getRoleName() + ":" + roleBox.getRole().getSubstitutionType().getName();
            maxLength = s.length();
            int widthInPixels = getWidthInPixels(s);

            if (widthInPixels > maxLengthInPixels) {
                maxLengthInPixels = widthInPixels;
            }
        }

        return (int) maxLengthInPixels;
    }

    protected int getWidthInPixels(String s) {
        Font font = new Font("Verdana", Font.PLAIN, 13);
        FontMetrics metrics = new FontMetrics(font) {

            private static final long serialVersionUID = 1L;
        };
        Rectangle2D bounds = metrics.getStringBounds(s, null);
        int widthInPixels = (int) bounds.getWidth();
        fontHeight = (int) bounds.getHeight() + 1;
        return widthInPixels;
    }

    protected int getTextLengthFromPixels(String s, double maxWidth) {
        int count = 0;
        String s2 = "";
        int characterWidth = 0;

        while (characterWidth < maxWidth && count < s.length()) {
            s2 += s.charAt(count);
            characterWidth = getWidthInPixels(s2);
            count++;
        }

        return s2.length();
    }

    public double getMaxlength() {
        return maxLength;
    }

    public void initialize() {
        _roleBoxes.clear();
        for (Iterator<Role> it = _factType.roles(); it.hasNext();) {
            Role role = it.next();

            if (role instanceof BaseValueRole) {
                _roleBoxes.add(new BaseValueRoleBox(role, _controller));
            } else {
                _roleBoxes.add(new ObjectRoleBox(role, _controller));
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) {
            super.setValue(evt.getNewValue());
        } else if (evt.getPropertyName().equals("typeExpression")) {
            _typeExpressionCell.setValue(evt.getNewValue());
        } else if (evt.getPropertyName().toLowerCase().equals("rolesiterator")) {
            initialize();
        }

        if (_controller.getGraphComponent() != null) {
            _controller.getGraphComponent().getGraph().refresh();
        }
    }

    public String getStyle(boolean bold) {
        if (bold) {
            return "objecttypeName;";
        } else {
            return "objecttype;fillColor=#DDDDDD;gradientColor=#DDDDDD";
        }

    }
}
