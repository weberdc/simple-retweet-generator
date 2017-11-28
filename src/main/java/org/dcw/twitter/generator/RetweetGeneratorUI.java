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
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Tool to facilitate mocking up retweets of tweets.
 *
 * The SortedComboBox for the retweeter name is borrowed from
 * <a href="https://stackoverflow.com/questions/7387299/dynamically-adding-items-to-a-jcombobox">Dynamically adding items to a JComboBox</a>
 *
 * Table button rendering and editor stuff adapted from
 * <a href="http://www.java2s.com/Code/Java/Swing-Components/ButtonTableExample.htm">ButtonTableExample</a>.
 */
public class RetweetGeneratorUI extends JPanel {

    private static final ImageIcon DELETE_ICON = new ImageIcon(
        RetweetGeneratorUI.class.getResource("/icons/Remove-16.png")
    );
    private final String[] NAME_PARTS = {
        "salted", "tables", "benign", "sawfly", "sweaty", "noggin",
        "willow", "powder", "untorn", "rewire", "placid", "joists"
    };

    private DefaultTableModel tableModel;
    private JTable tweetTable;

    private enum COMMANDS { RT, DELETE }

    private final String[] columnNames = {"RT", "User", "Tweet Text", "Delete"};

    private final TweetCorpusModel model;
    private final SortedComboBoxModel nameCBModel = new SortedComboBoxModel(new String[]{""});

    public RetweetGeneratorUI(TweetCorpusModel model) {
        this.model = model;

        buildUI();
    }

    private void buildUI() {

        // STRUCTURE
        setLayout(new GridBagLayout());

        // retweeter name
        int row = 0;
        final JButton nameButton = new JButton("Retweeter");
        nameButton.setToolTipText("Click to generate a new random name");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(0, 0, 5, 5);
        add(nameButton, gbc);

        final JComboBox namePicker = new JComboBox<>(nameCBModel);
        namePicker.setEditable(true);
        namePicker.setRenderer(new ButtonComboRenderer(DELETE_ICON, namePicker));
        namePicker.addItem("");
        namePicker.setSelectedItem("");

        gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        add(namePicker, gbc);


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
        nameButton.addActionListener(e -> {
            final String newName = generateName(nameCBModel.getElements());
            namePicker.addItem(newName);
            namePicker.setSelectedItem(newName); // will trigger the ActionListener above
        });
        namePicker.addActionListener(e -> {
            final String newName = (String) namePicker.getSelectedItem();
            namePicker.addItem(newName);
        });
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

    private String generateName(final List<String> elements) {
        String newName;
        do {
            final int index1 = (int) Math.floor(Math.random() * NAME_PARTS.length);
            final int index2 = (int) Math.floor(Math.random() * NAME_PARTS.length);
            newName = NAME_PARTS[index1] + "." + NAME_PARTS[index2];
        } while (elements.contains(newName));
        return newName;
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

    // COMBO STUFF
    /**
     * Borrowed from https://stackoverflow.com/questions/7387299/dynamically-adding-items-to-a-jcombobox
     */
    private class SortedComboBoxModel extends DefaultComboBoxModel<String> {

        private static final long serialVersionUID = 1L;

        public SortedComboBoxModel(final String[] items) {
            Stream.of(items).sorted().filter(Objects::nonNull).forEach(this::addElement);
            setSelectedItem(items[0]);
        }

        @Override
        public void addElement(final String element) {
            if (element == null) return;
            for (int i = 0; i < getSize(); i++) {
                Object elementAtI = getElementAt(i);
                if (elementAtI.equals(element)) {
                    return; // already present
                }
            }

            insertElementAt(element, 0);
        }

        @Override
        public void insertElementAt(final String element, int index) {
            if (element == null) return;
            int size = getSize();
            //  Determine where to insert element to keep model in sorted order
            for (index = 0; index < size; index++) {
                Comparable c = getElementAt(index);
                if (c.compareTo(element) > 0) {
                    break;
                }
            }
            super.insertElementAt(element, index);
        }

        public List<String> getElements() {
            return IntStream.range(0, getSize()).mapToObj(this::getElementAt).collect(Collectors.toList());
        }
    }

    /**
     * Grabbed from https://stackoverflow.com/questions/11065282/display-buttons-in-jcombobox-items
     */
    class ButtonComboRenderer implements ListCellRenderer {
        final Icon icon;
        final JPanel panel;
        final JLabel label;
        final JButton button;

        public ButtonComboRenderer(final Icon removeIcon, final JComboBox<String> combo) {
            icon = removeIcon;
            label = new JLabel();
            button = new JButton(icon);
            button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            panel = new JPanel(new BorderLayout());
            panel.add(label);
            panel.add(button, BorderLayout.EAST);
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (button.getX() < e.getX()) {
//                        System.out.println("button contains the click remove the item");
                        combo.removeItem(label.getText());
                    }
                }
            });
        }
        //so we will install the mouse listener once
        boolean isFirst = true;

        @Override
        public Component getListCellRendererComponent(
            final JList list,
            final Object value,
            final int index,
            final boolean isSelected,
            final boolean cellHasFocus
        ) {
            if (isFirst) {
                isFirst = false;
                list.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        panel.dispatchEvent(e);
                        e.consume();
                    }
                });
            }
            String text = (String) value;
            label.setText(text);
            if (text == null)
                button.setIcon(null);
            else if (button.getIcon() == null)
                button.setIcon(icon);
            panel.setBackground(isSelected ? Color.YELLOW : Color.WHITE);
            panel.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            return panel;
        }
    }

    // TABLE STUFF

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
