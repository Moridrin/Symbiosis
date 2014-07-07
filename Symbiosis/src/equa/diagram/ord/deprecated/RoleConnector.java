/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import com.mxgraph.model.mxCell;

/**
 *
 * @author Maurice-Asus
 */
public class RoleConnector extends mxCell {

    private static final long serialVersionUID = 1L;
    private boolean _isVisible;
    private RoleBox _roleBox;
    private OTBox _otBox;

    public OTBox getOtBox() {
        return _otBox;
    }

    public RoleBox getRoleBox() {
        return _roleBox;
    }

    public RoleConnector(RoleBox box) {
        _roleBox = box;
        _isVisible = true;
    }

    /**
     * @return the _isVisible
     */
    public boolean IsVisible() {
        return _isVisible;
    }

    /**
     * @param isVisible the _isVisible to set
     */
    public void IsVisible(boolean isVisible) {
        this._isVisible = isVisible;
    }

    public void SetOTBox(OTBox box) {
        _otBox = box;
    }

    @Override
    public String getStyle() {
        return "startArrow=none;endArrow=none;strokeWidth=1";
    }
}
