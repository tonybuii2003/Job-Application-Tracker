import javax.swing.*;
import java.awt.*;
import java.util.List;

public class JobStatisticsPanel extends JPanel {
    private JTextArea statsArea;
    private JobDataManager dataManager;
    private String[] statusOptions = {
        "Applied", "Phone Screen", "Take Home", "On-Site", "Offer",
        "Rejected - Application", "Rejected after Phone Screen",
        "Rejected after Take Home", "Rejected after On-Site", "No Longer Interested"
    };

    public JobStatisticsPanel(JobDataManager dataManager) {
        this.dataManager = dataManager;
        setLayout(new BorderLayout());

        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        updateStatistics(); // Generate initial statistics

        add(new JScrollPane(statsArea), BorderLayout.CENTER);

        // Button to refresh statistics
        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.addActionListener(e -> updateStatistics());
        add(refreshButton, BorderLayout.SOUTH);
    }

    private void updateStatistics() {
        List<String[]> jobData = dataManager.getJobData();
        int totalApplications = jobData.size();
        int[] statusCounts = new int[statusOptions.length];

        // Count occurrences of each status
        for (String[] row : jobData) {
            String status = row[8]; // Status column index
            for (int i = 0; i < statusOptions.length; i++) {
                if (statusOptions[i].equals(status)) {
                    statusCounts[i]++;
                    break;
                }
            }
        }

        // Build the statistics text
        StringBuilder stats = new StringBuilder();
        stats.append("Total Applications: ").append(totalApplications).append("\n\n");
        for (int i = 0; i < statusOptions.length; i++) {
            stats.append(statusOptions[i]).append(": ").append(statusCounts[i]).append("\n");
        }

        // Update the stats area
        statsArea.setText(stats.toString());
    }
}
