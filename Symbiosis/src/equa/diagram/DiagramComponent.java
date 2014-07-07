/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram;

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.Serializable;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxUtils;

/**
 *
 * @author frankpeeters
 */
public abstract class DiagramComponent extends mxCell implements Serializable {

    private static final long serialVersionUID = 1L;
    protected DiagramContainer container;

    public DiagramComponent(DiagramContainer container) {
        this.container = container;
    }

    public abstract Font getFont();

    public double preferredWidth(Font font) {
        FontMetrics fm = mxUtils.getFontMetrics(font);
        return fm.stringWidth(getValue().toString());
    }

    public double preferredHeight(Font font) {
        FontMetrics fm = mxUtils.getFontMetrics(font);
        return fm.getHeight();
    }

    @Override
    public void setGeometry(mxGeometry geometry) {
        if (geometry != null) {
            double width = geometry.getWidth();
            if (width == 0) {
                width = preferredWidth(getFont());
            }
            double height = geometry.getHeight();
            if (height == 0) {
                height = preferredHeight(getFont());
            }
            super.setGeometry(new mxGeometry(geometry.getX(), geometry.getY(), width, height));
        } else {
            super.setGeometry(null);
        }
    }

    protected void setGeometrySuper(mxGeometry geometry) {
        super.setGeometry(geometry);
    }

    public void remove() {
        container = null;
        removeFromParent();
    }

    public DiagramComponent getContainer() {
        return container;
    }
}
