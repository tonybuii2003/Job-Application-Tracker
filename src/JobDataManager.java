import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class JobDataManager {
    private final String fileName;
    private final List<String[]> jobData = new ArrayList<>();
    private final String[] columns = {
        "Company", "Location", "App Link", "Recruiter", "Connection #1", 
        "Connection #2", "Documents", "Date", "Status", "Comments/Note"
    };

    private final String[] statusOptions = {
        "Applied", "Phone Screen", "Take Home", "On-Site", "Offer",
        "Rejected - Application", "Rejected after Phone Screen",
        "Rejected after Take Home", "Rejected after On-Site", "No Longer Interested"
    };

    public JobDataManager(String fileName) {
        this.fileName = fileName;
        loadData();
    }

    public void loadData() {
        jobData.clear();
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i].replaceAll("^\"|\"$", ""); // Remove quotes
                    }
                    if (data.length < columns.length) continue; // Prevent incorrect rows

                    // Ensure Date is not empty
                    if (data[7].isEmpty()) data[7] = getCurrentDate();
                    
                    jobData.add(data);
                }
                sortByDate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveData(DefaultTableModel model, JFrame frame) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    String value = (String) model.getValueAt(row, col);
                    if (value != null && value.contains(",")) {
                        value = "\"" + value + "\""; // Enclose in quotes if value contains commas
                    }
                    writer.print(value != null ? value : "");
                    if (col < model.getColumnCount() - 1) writer.print(",");
                }
                writer.println();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving data: " + e.getMessage());
        }
    }

    public List<String[]> getJobData() {
        return jobData;
    }

    private void sortByDate() {
        jobData.sort((row1, row2) -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date d1 = dateFormat.parse(row1[7]);
                Date d2 = dateFormat.parse(row2[7]);
                return d2.compareTo(d1);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public String[] getColumns() {
        return columns;
    }
    public void sortTableByDate(DefaultTableModel model) {
        Vector<Vector<Object>> rows = new Vector<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Vector<Object> row = new Vector<>();
            for (int j = 0; j < model.getColumnCount(); j++) {
                row.add(model.getValueAt(i, j));
            }
            rows.add(row);
        }
    
        // Sort rows by the Date column (index 7)
        rows.sort((row1, row2) -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = dateFormat.parse((String) row1.get(7));
                Date date2 = dateFormat.parse((String) row2.get(7));
                return date2.compareTo(date1); // Newest date first
            } catch (Exception e) {
                return 0; // If parsing fails, keep the current order
            }
        });
    
        // Clear the table and re-add sorted rows
        model.setRowCount(0); // Remove all rows
        for (Vector<Object> row : rows) {
            model.addRow(row);
        }
    }

    public String[] getStatusOptions() {
        return statusOptions;
    }
}
