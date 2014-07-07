/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram;

import java.awt.Font;
import java.awt.FontMetrics;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxUtils;

/**
 *
 * @author frankpeeters
 */
public abstract class DiagramContainer extends DiagramComponent {

    private static final long serialVersionUID = 1L;
    private int east_marge;
    private int west_marge;
    private int v_marge;

    public DiagramContainer(DiagramContainer container) {
        super(container);
        east_marge = 0;
        west_marge = 0;
        v_marge = 0;
    }

    /**
     *
     * @return the distance between this container and his component at the west
     * side
     */
    public int getWestMarge() {
        return west_marge;
    }

    public void setWestMarge(int west_marge) {
        this.west_marge = west_marge;
    }

    /**
     *
     * @return the distance between this container and his component at the east
     * side
     */
    public int getEastMarge() {
        return east_marge;
    }

    /**
     *
     * @return the distance between this container and his component at the
     * north and south side
     */
    public int getVerticalMarge() {
        return v_marge;
    }

    public void setEastMarge(int marge) {
        if (marge >= 0) {
            east_marge = marge;
        }
    }

    public void setVerticalMarge(int marge) {
        if (marge >= 0) {
            v_marge = marge;
        }
    }

    @Override
    public double preferredWidth(Font font) {
        // width of header value as initializing value:
        int marge = getEastMarge() + getWestMarge();
        double width = super.preferredWidth(font) + marge;

        if (!isCollapsed()) {
            // but if one of the components is larger then adjustment is needed:
            for (int i = 0; i < size(); i++) {
                width = Math.max(width, component(i).preferredWidth(getComponentFont()) + marge);
            }
        }
        return width;
    }

    @Override
    public double preferredHeight(Font font) {
        double height = super.preferredHeight(font);
        if (!isCollapsed()) {
            for (int i = 0; i < size(); i++) {
                DiagramComponent component = component(i);
                height += component.preferredHeight(getComponentFont());
            }
        }
        return height + 2 * getVerticalMarge();
    }

    @Override
    public void setGeometry(mxGeometry geometry) {
        if (geometry != null) {
            double width = Math.max(geometry.getWidth(), preferredWidth(getFont()));
            setGeometrySuper(new mxGeometry(geometry.getX(), geometry.getY(),
                    width, preferredHeight(getFont())));

            // resetting of all components:
            double height = getVerticalMarge() + super.preferredHeight(getFont());
            int total_horizontal_marge = getEastMarge() + getWestMarge();
            for (int i = 0; i < size(); i++) {
                DiagramComponent component = component(i);
                if (isCollapsed()) {
                    component.setGeometry(null);
                } else {
                    component.setGeometry(new mxGeometry(getWestMarge(), height, width - total_horizontal_marge, 0));
                    height += component.getGeometry().getHeight();
                }
            }
        } else {
            setGeometrySuper(null);
        }
    }

    /**
     *
     * @param i 0<=i<size() @
     * return the ie component of this container
     */
    protected abstract DiagramComponent component(int i);

    /**
     *
     * @return the number of components of this container
     */
    protected abstract int size();

    public double getHeaderHeight() {
        FontMetrics fm = mxUtils.getFontMetrics(getFont());
        return fm.getHeight();
    }

    public abstract Font getComponentFont();

    @Override
    public void setCollapsed(boolean collapsed) {
        super.setCollapsed(collapsed);
        if (geometry != null) {
            if (container != null) {
                //searching for the top level container
                DiagramContainer dc = container;
                while (dc.container != null) {
                    dc = dc.container;
                }
                //the top level container must do the resizing of him and all his children
                dc.setGeometrySuper(new mxGeometry(dc.geometry.getX(), dc.geometry.getY(),
                        dc.preferredWidth(getFont()), dc.geometry.getHeight()));
            } else {
                setGeometrySuper(new mxGeometry(geometry.getX(), geometry.getY(), preferredWidth(getFont()), geometry.getHeight()));
            }
        }
    }
}
