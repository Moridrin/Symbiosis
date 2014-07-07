/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.configurator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;

/**
 *
 * @author frankpeeters
 */
public class InheritanceLabel extends Panel {

    private static final long serialVersionUID = 1L;

    public InheritanceLabel() {
    }

    public InheritanceLabel(int x, int y, int width, int height) {

        this.setSize(width, height);
        this.setLocation(x, y);

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLUE);
        int[] xPoints = {0, getWidth() / 2, getWidth()};
        int[] yPoints = {getHeight() / 3, 0, getHeight() / 3};

        g.fillPolygon(xPoints, yPoints, 3);

        int w = 4;

        g.fillRect(getWidth() / 2 - w / 2, getHeight() / 3, w, 2 * getHeight() / 3);
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
        g.drawRect(getWidth() / 2 - w / 2, getHeight() / 3, w, 2 * getHeight() / 3);
    }
}
