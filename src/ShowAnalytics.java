import javax.swing.*;
import java.awt.*;
import java.sql.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ShowAnalytics extends JFrame {
    private final String DB_URL = "jdbc:mysql://localhost:3306/corporate_expense_db";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "root";

    private JTextArea analyticsArea;

    public ShowAnalytics() {
        setTitle("Expense Analytics");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10)); // Add spacing between components

        analyticsArea = new JTextArea();
        analyticsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        analyticsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(analyticsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Analytics Report")); // Add a border for better visibility
        add(scrollPane, BorderLayout.WEST);

        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new GridLayout(2, 1, 10, 10)); // 2 rows, 1 column with spacing
        add(chartPanel, BorderLayout.CENTER);

        createPieChartForHighestSpendingCategory(chartPanel);
        createBudgetComparisonBarChart(chartPanel);

        displayAnalytics();
    }

    private void displayAnalytics() {
        StringBuilder analyticsReport = new StringBuilder();

        double totalExpenses = calculateTotalExpenses();
        analyticsReport.append("Total Expenses: ").append(totalExpenses).append("\n\n");

        String highestSpendingCategory = getHighestSpendingCategory();
        analyticsReport.append("Highest Spending Category: ").append(highestSpendingCategory).append("\n\n");

        int exceededBudgets = getExceededBudgets();
        analyticsReport.append("Number of Categories Exceeding Budget: ").append(exceededBudgets).append("\n");

        analyticsArea.setText(analyticsReport.toString());
    }

    private double calculateTotalExpenses() {
        double total = 0.0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT SUM(amount) AS total FROM expenses";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    private String getHighestSpendingCategory() {
        String category = "";
        double highestAmount = 0.0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT category, SUM(amount) AS total FROM expenses GROUP BY category ORDER BY total DESC LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                category = rs.getString("category");
                highestAmount = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category + " (" + highestAmount + ")";
    }

    private int getExceededBudgets() {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT c.category_name, cb.budget FROM categories c " +
                    "JOIN category_budgets cb ON c.category_name = cb.category_name " +
                    "WHERE (SELECT SUM(amount) FROM expenses WHERE category = c.category_name) > cb.budget";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private void createPieChartForHighestSpendingCategory(JPanel chartPanel) {
        String highestSpendingCategory = getHighestSpendingCategory().split(" \\(")[0]; // Extract category name
        DefaultPieDataset pieDataset = new DefaultPieDataset();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT description, amount FROM expenses WHERE category = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, highestSpendingCategory);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                pieDataset.setValue(description, amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Expenses for " + highestSpendingCategory,
                pieDataset,
                true, // Include legend
                true, // Include tooltips
                false // No URLs
        );

        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 300)); // Set size for pie chart
        chartPanel.add(pieChartPanel);
    }

    private void createBudgetComparisonBarChart(JPanel chartPanel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT c.category_name, cb.budget, SUM(e.amount) AS total_expenses FROM categories c " +
                    "LEFT JOIN category_budgets cb ON c.category_name = cb.category_name " +
                    "LEFT JOIN expenses e ON c.category_name = e.category " +
                    "GROUP BY c.category_name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String categoryName = rs.getString("category_name");
                double budget = rs.getDouble("budget");
                double totalExpenses = rs.getDouble("total_expenses");

                dataset.addValue(totalExpenses, "Spent", categoryName);
                dataset.addValue(budget, "Budget", categoryName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Budget vs. Amount Spent",
                "Category",
                "Amount",
                dataset
        );

        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 300)); // Set size for bar chart
        chartPanel.add(barChartPanel);
    }
}
