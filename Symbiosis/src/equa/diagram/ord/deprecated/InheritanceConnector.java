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
public class InheritanceConnector extends mxCell {

    private static final long serialVersionUID = 1L;
    private OTBox _source;
    private OTBox _destination;

    public InheritanceConnector(OTBox source, OTBox destination) {
        _source = source;
        _destination = destination;
    }

    public OTBox getDestination() {
        return _destination;
    }

    @Override
    public OTBox getSource() {
        return _source;
    }

    @Override
    public String getStyle() {
        return "startArrow=none;endArrow=arrow;strokeWidth=1;endSize=15";
    }
}
