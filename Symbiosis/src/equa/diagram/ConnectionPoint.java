/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxPoint;

/**
 *
 * @author frankpeeters
 */
public class ConnectionPoint extends mxCell {

    private static final long serialVersionUID = 1L;
    private mxPoint center;

    public ConnectionPoint(mxPoint center) {
        this.center = center;
        setStyle("shape=ellipse;fillColor=blue");
    }

    public mxPoint getCenter() {
        return center;
    }
}
