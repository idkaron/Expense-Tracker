import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class CompanyExpenseTracker extends JFrame {
    private JTextField descriptionField, amountField, newCategoryField;
    private JComboBox<String> categoryField;
    private JButton addButton, viewButton, pieChartButton, clearButton, addCategoryButton, removeCategoryButton, editBudgetButton; // Added removeCategoryButton
    private JTextArea outputArea;
    private double totalBudget; // Total budget variable
    private final String DB_URL = "jdbc:mysql://localhost:3306/corporate_expense_db";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "root";
    private JButton showAnalyticsButton;


    public CompanyExpenseTracker() {
        setTitle("Company Expense Tracker");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(descriptionLabel, gbc);

        descriptionField = new JTextField();
        descriptionField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(descriptionField, gbc);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(amountLabel, gbc);

        amountField = new JTextField();
        amountField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(amountField, gbc);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(categoryLabel, gbc);

        categoryField = new JComboBox<>();
        loadCategories();
        categoryField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 2;
        inputPanel.add(categoryField, gbc);

        JLabel newCategoryLabel = new JLabel("New Category:");
        newCategoryLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(newCategoryLabel, gbc);

        newCategoryField = new JTextField();
        newCategoryField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 3;
        inputPanel.add(newCategoryField, gbc);

        addCategoryButton = new JButton("Add Category");
        addCategoryButton.setFont(labelFont);
        addCategoryButton.setBackground(new Color(70, 130, 180));
        addCategoryButton.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(addCategoryButton, gbc);

        removeCategoryButton = new JButton("Remove Category"); // New button to remove category
        removeCategoryButton.setFont(labelFont);
        removeCategoryButton.setBackground(new Color(220, 20, 60));
        removeCategoryButton.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5; // Position it below the Add Category button
        gbc.gridwidth = 2;
        inputPanel.add(removeCategoryButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(inputPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10)); // Updated GridLayout to accommodate the new button
        buttonPanel.setBackground(new Color(245, 245, 245));

        addButton = new JButton("Add Expense");
        styleButton(addButton, labelFont);
        buttonPanel.add(addButton);

        showAnalyticsButton = new JButton("Show Analytics");
        styleButton(showAnalyticsButton, labelFont);
        buttonPanel.add(showAnalyticsButton);

        showAnalyticsButton.addActionListener(e -> openShowAnalyticsPage());


        viewButton = new JButton("View Expenses");
        styleButton(viewButton, labelFont);
        buttonPanel.add(viewButton);

        pieChartButton = new JButton("Show Pie Chart");
        styleButton(pieChartButton, labelFont);
        buttonPanel.add(pieChartButton);

        clearButton = new JButton("Clear All Expenses");
        styleButton(clearButton, labelFont);
        buttonPanel.add(clearButton);

        editBudgetButton = new JButton("Edit Budget"); // New button to edit budget
        styleButton(editBudgetButton, labelFont);
        buttonPanel.add(editBudgetButton); // Add the button to the panel

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(scrollPane, gbc);

        addButton.addActionListener(e -> addExpense());
        viewButton.addActionListener(e -> viewExpenses());
        pieChartButton.addActionListener(e -> showPieChart());
        clearButton.addActionListener(e -> clearExpenses());
        addCategoryButton.addActionListener(e -> addCategory());
        removeCategoryButton.addActionListener(e -> removeCategory()); // Add action listener for the new button
        editBudgetButton.addActionListener(e -> openEditBudgetPage()); // Add action listener for the new button

        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 12));
        UIManager.put("TextArea.font", new Font("Arial", Font.PLAIN, 12));
    }

    private void loadCategories() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT category_name FROM categories";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                categoryField.addItem(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCategory() {
        String newCategory = newCategoryField.getText();
        if (newCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO categories (category_name) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newCategory);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Category added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            categoryField.addItem(newCategory);
            newCategoryField.setText(""); // Clear input field after adding
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeCategory() {
        String selectedCategory = (String) categoryField.getSelectedItem();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "No category selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this category?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "DELETE FROM categories WHERE category_name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, selectedCategory);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Category removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                categoryField.removeItem(selectedCategory); // Remove from combo box
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addExpense() {
        String description = descriptionField.getText();
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String category = categoryField.getSelectedItem().toString();

        double budgetLimit = getBudgetForCategory(category); // Get budget for the selected category

        double totalExpenses = calculateTotalExpenses();

        if (totalExpenses + amount > budgetLimit) {
            JOptionPane.showMessageDialog(this, "Budget exceeded! Total expenses will be: " + (totalExpenses + amount), "Warning", JOptionPane.WARNING_MESSAGE);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO expenses (description, amount, category) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, description);
            stmt.setDouble(2, amount);
            stmt.setString(3, category);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Expense added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            descriptionField.setText(""); // Clear input field after adding
            amountField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double getBudgetForCategory(String category) {
        double budget = 0.0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT budget FROM category_budgets WHERE category_name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                budget = rs.getDouble("budget");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return budget;
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

    private void viewExpenses() {
        outputArea.setText("");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM expenses";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                outputArea.append("Description: " + rs.getString("description") + ", Amount: " + rs.getDouble("amount") + ", Category: " + rs.getString("category") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT category, SUM(amount) AS total FROM expenses GROUP BY category";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                dataset.setValue(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart("Expenses by Category", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame chartFrame = new JFrame("Pie Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.add(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setVisible(true);
    }

    private void clearExpenses() {
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all expenses?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "DELETE FROM expenses";
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query);
                outputArea.setText("");
                JOptionPane.showMessageDialog(this, "All expenses cleared successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void openEditBudgetPage() {
        JFrame editBudgetFrame = new JFrame("Edit Budget");
        editBudgetFrame.setSize(400, 300);
        editBudgetFrame.setLayout(new GridBagLayout());
        editBudgetFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editBudgetFrame.setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel categoryLabel = new JLabel("Select Category:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        editBudgetFrame.add(categoryLabel, gbc);

        JComboBox<String> categoryComboBox = new JComboBox<>();
        loadCategoriesForBudget(categoryComboBox);
        gbc.gridx = 1;
        gbc.gridy = 0;
        editBudgetFrame.add(categoryComboBox, gbc);

        JLabel budgetLabel = new JLabel("Set Budget:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        editBudgetFrame.add(budgetLabel, gbc);

        JTextField budgetField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        editBudgetFrame.add(budgetField, gbc);

        JButton saveButton = new JButton("Save Budget");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        editBudgetFrame.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            double newBudget;

            try {
                newBudget = Double.parseDouble(budgetField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editBudgetFrame, "Invalid budget amount!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "INSERT INTO category_budgets (category_name, budget) VALUES (?, ?) ON DUPLICATE KEY UPDATE budget = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, selectedCategory);
                stmt.setDouble(2, newBudget);
                stmt.setDouble(3, newBudget);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(editBudgetFrame, "Budget updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        editBudgetFrame.setVisible(true);
    }

    private void loadCategoriesForBudget(JComboBox<String> categoryComboBox) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT category_name FROM categories";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                categoryComboBox.addItem(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void styleButton(JButton button, Font font) {
        button.setFont(font);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
    }

    private void openShowAnalyticsPage() {
        ShowAnalytics analyticsPage = new ShowAnalytics();
        analyticsPage.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginPage(); // Open the login page
        });
    }

}
