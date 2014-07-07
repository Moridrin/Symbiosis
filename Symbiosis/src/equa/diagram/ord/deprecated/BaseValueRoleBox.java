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
public class BaseValueRoleBox extends RoleBox {

    private static final long serialVersionUID = 1L;

    public BaseValueRoleBox(Role role, ObjectRoleDiagram controller) {
        super(role, controller);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
    }

    @Override
    public String getStyle() {
        return "fillColor=#FFFFFF;gradientColor=#FFFFFF";
    }
}
