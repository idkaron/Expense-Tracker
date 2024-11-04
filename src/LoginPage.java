import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginPage() {
        setTitle("Login Page");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        // GridBag Constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        ImageIcon logo = new ImageIcon("lib/explogo.jpg"); // Update the path to your logo
        JLabel logoLabel = new JLabel(logo);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across two columns
        add(logoLabel, gbc);

        // Panel for Input Fields with Rounded Corners
        RoundedPanel panel = new RoundedPanel();
        panel.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white background
        panel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2)); // Light blue border
        panel.setLayout(new GridBagLayout());

        // Add panel to the frame
        gbc.gridy = 1; // Move down for the panel
        gbc.gridwidth = 2; // Reset grid width
        add(panel, gbc);

        // Label and Text Field for Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        GridBagConstraints usernameGbc = new GridBagConstraints();
        usernameGbc.insets = new Insets(5, 5, 5, 5);
        usernameGbc.gridx = 0;
        usernameGbc.gridy = 0;
        panel.add(usernameLabel, usernameGbc);

        usernameField = new JTextField(20); // Set preferred width to 20 columns
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameGbc.gridx = 1;
        panel.add(usernameField, usernameGbc);

        // Label and Password Field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameGbc.gridx = 0;
        usernameGbc.gridy = 1;
        panel.add(passwordLabel, usernameGbc);

        passwordField = new JPasswordField(20); // Set preferred width to 20 columns
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameGbc.gridx = 1;
        panel.add(passwordField, usernameGbc);

        // Login Button with Rounded Corners
        loginButton = new RoundedButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false); // Remove focus border
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Add padding
        usernameGbc.gridx = 0;
        usernameGbc.gridy = 2;
        usernameGbc.gridwidth = 2; // Span across two columns
        panel.add(loginButton, usernameGbc);

        // Action Listener for Login Button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword()); // Get the password

        // You can modify this condition to match your actual login logic
        if (username.equals("admin") && password.equals("root")) {
            JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close the login window
            new CompanyExpenseTracker().setVisible(true); // Open the main application
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }

    // Custom Panel with Rounded Corners
    class RoundedPanel extends JPanel {
        private int radius = 15; // Radius for rounded corners

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    // Custom Button with Rounded Corners
    class RoundedButton extends JButton {
        private int radius = 15; // Radius for rounded corners

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isArmed()) {
                g.setColor(getBackground().darker());
            } else {
                g.setColor(getBackground());
            }
            g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(size.width + 20, size.height + 10); // Adjust size for rounded effect
        }
    }
}
