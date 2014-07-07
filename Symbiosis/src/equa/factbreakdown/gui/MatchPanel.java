/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import javax.swing.JPanel;

/**
 *
 * @author FrankP
 */
public abstract class MatchPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    abstract void setNext(MatchPanel panel);

    abstract MatchPanel getNext();

    abstract void appendFront(String toMove);

    abstract String getText();

    void refreshLayout() {
        getParent().getParent().setVisible(false);
        getParent().getParent().setVisible(true);
    }
}
