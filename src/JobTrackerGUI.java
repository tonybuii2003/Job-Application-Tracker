import javax.swing.*;

public class JobTrackerGUI {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JobTablePanel jobTablePanel;
    private JobStatisticsPanel jobStatisticsPanel;
    private JobDataManager dataManager;

    public JobTrackerGUI(JobDataManager dataManager) {
        this.dataManager = dataManager;
        frame = new JFrame("Job Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        tabbedPane = new JTabbedPane();
        
        jobTablePanel = new JobTablePanel(dataManager, frame);
        jobStatisticsPanel = new JobStatisticsPanel(dataManager);
        
        tabbedPane.addTab("Job Applications", jobTablePanel);
        tabbedPane.addTab("Statistics", jobStatisticsPanel);
        
        frame.add(tabbedPane);
        frame.setVisible(true);
    }
}
