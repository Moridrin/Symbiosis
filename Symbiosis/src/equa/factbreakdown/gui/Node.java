/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author frankpeeters
 */
public interface Node {

    Color VALUE_COLOR = Color.YELLOW;
    Color TEXT_COLOR = Color.WHITE;
    Color READY_COLOR = Color.GREEN;
    Color SUPERTYPE_COLOR = Color.BLUE;
    Color COLLECTION_COLOR = Color.BLACK;
    int VALUE_FONTSTYLE = Font.PLAIN;
    int TEXT_FONTSTYLE = Font.PLAIN;
    int READY_FONTSTYLE = Font.PLAIN;
    int SUPERTYPE_FONTSTYLE = Font.PLAIN;
    int COLLECTION_FONTSTYLE = Font.PLAIN;
    int ROW_HEIGHT = 28;
}
