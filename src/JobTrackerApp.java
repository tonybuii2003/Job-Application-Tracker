import javax.swing.*;

public class JobTrackerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JobDataManager dataManager = new JobDataManager("job_tracking.csv");
            new JobTrackerGUI(dataManager);
        });
    }
}

