/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author frankpeeters
 */
public class SwingUtils {

    public static void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        int[] desiredWidthTable = new int[table.getColumnCount()];
        int totalCorrectedWidth = 0;
        int totalNiceWidth = 0;
        for (int column = 0; column < table.getColumnCount(); column++) {
            int minWidth = columnModel.getColumn(column).getMinWidth();
            int desiredWidth = minWidth;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                int comp_width = comp.getPreferredSize().width;
                desiredWidth = Math.max(comp_width, desiredWidth);
            }
            desiredWidthTable[column] = desiredWidth;
            if (desiredWidth > minWidth) {
                totalCorrectedWidth += desiredWidth;
            } else {
                totalNiceWidth += minWidth;
            }
        }
        double factor = (1.0 * table.getParent().getWidth() - totalNiceWidth) / totalCorrectedWidth;
        if (factor > 1.0) {
            factor = 1.0;
        }
        for (int column = 0; column < table.getColumnCount(); column++) {
            if (desiredWidthTable[column] > columnModel.getColumn(column).getMinWidth()) {
                columnModel.getColumn(column).setPreferredWidth((int) (desiredWidthTable[column] * factor));
            } else {
                columnModel.getColumn(column).setPreferredWidth((desiredWidthTable[column]));
            }
        }
    }
}
