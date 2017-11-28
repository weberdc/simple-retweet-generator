package org.dcw.twitter.generator;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.FontMetrics;
import java.util.stream.IntStream;

/**
 * Automatically sizes columns in a {@link JTable} according to their content.
 *
 * Props to the author.
 *
 * @see <a href="https://bosmeeuw.wordpress.com/2011/08/07/java-swing-automatically-resize-table-columns-to-their-contents/">Java Swing: Automatically Resize Table Columns To Their Contents</a>
 */
public class ColumnsAutoSizer {
 
    public static void sizeColumnsToFit(final JTable table) {
        sizeColumnsToFit(table, 5);
    }
 
    public static void sizeColumnsToFit(final JTable table, final int columnMargin) {
        final JTableHeader tableHeader = table.getTableHeader();
 
        if (tableHeader == null) {
            // can't auto size a table without a header
            return;
        }

        final FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

        final int[] minWidths = new int[table.getColumnCount()];
        final int[] maxWidths = new int[table.getColumnCount()];
 
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            final int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));
 
            minWidths[columnIndex] = headerWidth + columnMargin;

            final int maxWidth = getMaximalRequiredColumnWidth(table, columnIndex, headerWidth);
 
            maxWidths[columnIndex] = Math.max(maxWidth, minWidths[columnIndex]) + columnMargin;
        }
 
        adjustMaximumWidths(table, minWidths, maxWidths);
 
        for (int i = 0; i < minWidths.length; i++) {
            if (minWidths[i] > 0) {
                table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
            }
 
            if (maxWidths[i] > 0) {
                table.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);
 
                table.getColumnModel().getColumn(i).setWidth(maxWidths[i]);
            }
        }
    }
 
    private static void adjustMaximumWidths(
        final JTable table,
        final int[] minWidths,
        final int[] maxWidths
    ) {
        if (table.getWidth() > 0) {
            // to prevent infinite loops in exceptional situations
            int breaker = 0;
 
            // keep stealing one pixel of the maximum width of the highest column until we can fit in the width of the table
            while (sum(maxWidths) > table.getWidth() && breaker < 10000) {
                int highestWidthIndex = findLargestIndex(maxWidths);
 
                maxWidths[highestWidthIndex] -= 1;
 
                maxWidths[highestWidthIndex] = Math.max(maxWidths[highestWidthIndex], minWidths[highestWidthIndex]);
 
                breaker++;
            }
        }
    }
 
    private static int getMaximalRequiredColumnWidth(
        final JTable table,
        final int columnIndex,
        final int headerWidth
    ) {
        int maxWidth = headerWidth;
 
        final TableColumn column = table.getColumnModel().getColumn(columnIndex);
 
        TableCellRenderer cellRenderer = column.getCellRenderer();
 
        if (cellRenderer == null) {
            cellRenderer = new DefaultTableCellRenderer();
        }
 
        for (int row = 0; row < table.getModel().getRowCount(); row++) {
            final Component rendererComponent = cellRenderer.getTableCellRendererComponent(
                table,
                table.getModel().getValueAt(row, columnIndex),
                false,
                false,
                row,
                columnIndex
            );
 
            final double valueWidth = rendererComponent.getPreferredSize().getWidth();
 
            maxWidth = (int) Math.max(maxWidth, valueWidth);
        }
 
        return maxWidth;
    }
 
    private static int findLargestIndex(int[] widths) {
        int largestIndex = 0;
        int largestValue = 0;
 
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] > largestValue) {
                largestIndex = i;
                largestValue = widths[i];
            }
        }
 
        return largestIndex;
    }
 
    private static int sum(int[] widths) {
        return IntStream.of(widths).sum();
    }
 
}