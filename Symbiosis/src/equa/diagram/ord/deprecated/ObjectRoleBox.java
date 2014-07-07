/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.beans.PropertyChangeEvent;

import equa.meta.objectmodel.Role;

/**
 *
 * @author Maurice-Asus
 */
public class ObjectRoleBox extends RoleBox {

    private static final long serialVersionUID = 1L;
    private RoleConnector _roleConnector;

    public RoleConnector getRoleConnector() {
        return _roleConnector;
    }

    public ObjectRoleBox(Role role, ObjectRoleDiagram controller) {
        super(role, controller);

        _roleConnector = new RoleConnector(this);
    }

    public void setRoleConnectorOTBox(OTBox box) {
        _roleConnector.SetOTBox(box);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt != null) {
            super.propertyChange(evt);
        }
    }

    @Override
    public String getStyle() {
        return "fillColor=#FFFFFF;gradientColor=#FFFFFF";
    }
}
