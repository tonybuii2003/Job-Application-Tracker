import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class JobTablePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JobDataManager dataManager;
    private JTextField searchField;
    private JFrame frame;

    public JobTablePanel(JobDataManager dataManager, JFrame frame) {
        this.dataManager = dataManager;
        this.frame = frame;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(dataManager.getColumns(), 0);
        loadDataIntoTable();

        table = new JTable(model);
        table.setRowHeight(30);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);

        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getFirstRow() != -1) {
                dataManager.saveData(model, frame); // Save data after update
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Company:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);
        attachSearchFunctionality();

        // Status Dropdown
        TableColumn statusColumn = table.getColumnModel().getColumn(8);
        statusColumn.setCellRenderer(new StatusCellRenderer());
        JComboBox<String> comboBox = new JComboBox<>(dataManager.getStatusOptions());
        statusColumn.setCellEditor(new DefaultCellEditor(comboBox));

        // Clickable "App Link"
        table.getColumnModel().getColumn(2).setCellRenderer(new LinkCellRenderer());
        table.addMouseListener(new LinkClickListener());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Job Application");
        JButton deleteButton = new JButton("Delete Selected Row");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(this::addJobApplication);
        deleteButton.addActionListener(this::deleteSelectedRow);
    }

    private void loadDataIntoTable() {
        for (String[] row : dataManager.getJobData()) {
            model.addRow(row);
        }
    }

    private void addJobApplication(ActionEvent e) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField[] fields = new JTextField[dataManager.getColumns().length - 3]; // Exclude Date and Status
        for (int i = 0; i < dataManager.getColumns().length - 3; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            panel.add(new JLabel(dataManager.getColumns()[i]), gbc);

            gbc.gridx = 1;
            fields[i] = new JTextField(20);
            panel.add(fields[i], gbc);
        }

        // Add calendar picker for Date
        gbc.gridx = 0;
        gbc.gridy = dataManager.getColumns().length - 3;
        panel.add(new JLabel("Date"), gbc);

        gbc.gridx = 1;
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date()); // Default to today's date
        panel.add(dateSpinner, gbc);

        // Add status dropdown
        gbc.gridx = 0;
        gbc.gridy = dataManager.getColumns().length - 2;
        panel.add(new JLabel("Status"), gbc);

        gbc.gridx = 1;
        JComboBox<String> statusComboBox = new JComboBox<>(dataManager.getStatusOptions());
        panel.add(statusComboBox, gbc);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Job Application", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Vector<String> row = new Vector<>();
            for (JTextField field : fields) {
                String value = field.getText();
                if (field == fields[2] && !value.startsWith("http")) { // App Link field
                    value = "https://" + value;
                }
                row.add(value);
            }
            row.add(new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue())); // Add selected date
            row.add((String) statusComboBox.getSelectedItem()); // Add selected status
            row.add(""); // Add empty comment
            model.addRow(row);
            dataManager.sortTableByDate(model);
            dataManager.saveData(model, frame);
        }
    }

    private void deleteSelectedRow(ActionEvent e) {
        int selectedRow = table.getSelectedRow(); // Get the selected row in the view
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a row to delete.");
        } else {
            // Convert the view index to the model index
            int modelRow = table.convertRowIndexToModel(selectedRow);
    
            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete this row?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                model.removeRow(modelRow); // Remove the row from the model
                dataManager.saveData(model, frame);// Auto-save after deleting
            }
        }
    } 

    private void attachSearchFunctionality() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText().trim();
                sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 0));
            }
        });
    }
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";

            // Assign colors based on the status value
            switch (status) {
                case "Applied":
                    cell.setBackground(Color.LIGHT_GRAY);
                    break;
                case "Phone Screen":
                    cell.setBackground(Color.YELLOW);
                    cell.setForeground(Color.BLACK);
                    break;
                case "Take Home":
                    cell.setBackground(new Color(173, 255, 47)); // Light green
                    break;
                case "On-Site":
                    cell.setBackground(new Color(34, 139, 34)); // Dark green
                    break;
                case "Offer":
                    cell.setBackground(new Color(135, 206, 250)); // Light blue
                    break;
                case "Rejected - Application":
                case "Rejected after Phone Screen":
                case "Rejected after Take Home":
                case "Rejected after On-Site":
                    cell.setBackground(new Color(255, 182, 193)); // Light pink
                    break;
                case "No Longer Interested":
                    cell.setBackground(Color.DARK_GRAY);
                    cell.setForeground(Color.WHITE); // White text for dark background
                    break;
                default:
                    cell.setBackground(Color.WHITE); // Default background for unknown statuses
                    cell.setForeground(Color.BLACK);
                    break;
            }

            // Ensure selection colors are handled correctly
            if (isSelected) {
                cell.setBackground(table.getSelectionBackground());
                cell.setForeground(table.getSelectionForeground());
            }

            return cell;
        }
    }

    private static class LinkCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? "<html><a href=''>" + value + "</a></html>" : "");
            return this;
        }
    }

    private class LinkClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (col == 2) {
                String url = (String) table.getValueAt(row, col);
                if (url != null && !url.isEmpty()) {
                    try { Desktop.getDesktop().browse(new URI(url)); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(JobTablePanel.this, "Invalid URL"); }
                }
            }
        }
    }
}
