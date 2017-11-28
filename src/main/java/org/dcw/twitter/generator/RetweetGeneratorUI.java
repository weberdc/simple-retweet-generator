/*
 * Copyright 2017 Derek Weber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dcw.twitter.generator;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Tool to facilitate mocking up retweets of tweets.
 *
 * Table button rendering and editor stuff adapted from
 * <a href="http://www.java2s.com/Code/Java/Swing-Components/ButtonTableExample.htm">ButtonTableExample</a>.
 */
public class RetweetGeneratorUI extends JPanel {

    private static final ImageIcon DELETE_ICON = new ImageIcon(
        RetweetGeneratorUI.class.getResource("/icons/Remove-16.png")
    );
    private DefaultTableModel tableModel;
    private JTable tweetTable;

    private enum COMMANDS { RT, DELETE }

    private final String[] columnNames = {"RT", "User", "Tweet Text", "Delete"};

    private final TweetCorpusModel model;

    public RetweetGeneratorUI(TweetCorpusModel model) {
        this.model = model;

        buildUI();
    }

    private void buildUI() {

        // STRUCTURE
        setLayout(new GridBagLayout());

        // retweet name
        int row = 0;
        final JLabel retweeterNameLabel = new JLabel("Retweeter");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(0, 0, 5, 0);
        add(retweeterNameLabel, gbc);

        final JLabel retweeterChooser = new JLabel("Retweeter Chooser (placeholder)");

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        add(retweeterChooser, gbc);


        // tweet table
        row++;
        final JTable tweetTable = buildTweetTable();

        final JScrollPane tableScrollPane = new JScrollPane(
            tweetTable,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        gbc = new GridBagConstraints();
        gbc.gridwidth = 2;
        gbc.gridy = row;
        gbc.weightx = gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 5, 0);
        add(tableScrollPane, gbc);


        // add JSON button
        row++;
        final JButton addButton = new JButton("Add a tweet's JSON from clipboard");

        gbc = new GridBagConstraints();
        gbc.gridwidth = 2;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(addButton, gbc);


        // BEHAVIOUR
        addButton.addActionListener(e -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String maybeJson = "unset";
            try {
                maybeJson = (String) clipboard.getData(DataFlavor.stringFlavor);
                model.addTweet(maybeJson);
                model.saveModel();
                SwingUtilities.invokeLater(this::updateTweetTable);
            } catch (UnsupportedFlavorException | IOException ex) {
                JOptionPane.showMessageDialog(
                    RetweetGeneratorUI.this,
                    "Error with the JSON on the clipboard:\n" + maybeJson,
                    "JSON paste error",
                    JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });
    }

    private JTable buildTweetTable() {
        tableModel = new DefaultTableModel();

        tweetTable = new JTable(tableModel);

        updateTweetTable();

        return tweetTable;
    }

    private void resetColumnLayout() {
        ColumnsAutoSizer.sizeColumnsToFit(tweetTable);
    }

    private void resetTableButtonHelpers(
        final ActionListener tableButtonAction
    ) {
        tweetTable.getColumn(tableModel.getColumnName(0)).setCellRenderer(new ButtonRenderer());
        tweetTable.getColumn(tableModel.getColumnName(0))
            .setCellEditor(new ButtonEditor(tableButtonAction));

        tweetTable.getColumn(tableModel.getColumnName(3)).setCellRenderer(new ButtonRenderer());
        tweetTable.getColumn(tableModel.getColumnName(3))
            .setCellEditor(new ButtonEditor(tableButtonAction));
    }

    class TableButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            final int row = e.getModifiers();
            switch (COMMANDS.valueOf(e.getActionCommand())) {
                case RT:

                    break;
                case DELETE:
                    System.out.println("Deleting tweet in row " + row);
                    model.removeTweet(row);
                    model.saveModel();
                    SwingUtilities.invokeLater(RetweetGeneratorUI.this::updateTweetTable);
                    break;
            }
        }
    }

    private void updateTweetTable() {
        resetTableData();
        resetTableButtonHelpers(new TableButtonListener());
        resetColumnLayout();
    }

    private void resetTableData() {
        tableModel.setDataVector(createDataVector(model), columnNames);
    }

    private Object[][] createDataVector(TweetCorpusModel model) {
        Object[][] data = new Object[model.size()][];
        for (int i = 0; i < model.size(); i++) {
            data[i] = new Object[]{"RT", model.getScreenName(i), model.getText(i), DELETE_ICON};
        }
        return data;
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column
        ) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            if (value == null || value instanceof String) {
                setText((value == null) ? "" : value.toString());
            } else {
                setIcon((Icon) value);
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final ActionListener action;
        protected JButton button;

        private boolean isPushed;
        private int pushedRow, pushedCol;

        public ButtonEditor(final ActionListener action) {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
            this.action = action;
        }

        public Component getTableCellEditorComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column
        ) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            if (value == null || value instanceof String) {
                button.setText((value == null) ? "" : value.toString());
            } else {
                button.setIcon((Icon) value);
            }
            pushedRow = row;
            pushedCol = column;
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                ActionEvent e = new ActionEvent(
                    button,
                    ActionEvent.ACTION_PERFORMED,
                    pushedCol == 0 /* ie pushedCol != 3 */
                        ? COMMANDS.RT.toString()
                        : COMMANDS.DELETE.toString(),
                    pushedRow // use the modifiers field
                );
                action.actionPerformed(e);
            }
            isPushed = false;
            return pushedCol == 0 ? columnNames[0] : DELETE_ICON;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
