package projet;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

public class LoginSoktrapa extends JFrame {

    
    private final Color OLIVE_DARK = new Color(85, 107, 47);
    private final Color OLIVE_MEDIUM = new Color(107, 142, 35);
    private final Color OLIVE_LIGHT = new Color(154, 205, 50);
    private final Color CREAM = new Color(253, 245, 230);
    private final Color CREAM_DARK = new Color(245, 235, 220);
    private final Color GOLD = new Color(218, 165, 32);
    private final Color RED_DARK = new Color(192, 57, 43);
    private final Color RED_LIGHT = new Color(231, 76, 60);
    private final Color TEXT_DARK = new Color(45, 52, 54);
    private final Color TEXT_GRAY = new Color(99, 110, 114);
    private final Color SHADOW = new Color(0, 0, 0, 30);
    
    private JTextField txtUser;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnQuit;

    public LoginSoktrapa() {
        setupFrame();
        add(createMainPanel());
    }
    
    private void setupFrame() {
        setTitle("SOKTRAPA - Système de Gestion");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(false);
        
        try {
            setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        } catch (Exception ignored) {}
    }

    
    private JPanel createMainPanel() {
        JPanel main = new JPanel(new GridLayout(1, 2, 0, 0));
        main.add(createBrandingPanel());
        main.add(createLoginPanel());
        return main;
    }
    
    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                    RenderingHints.VALUE_RENDER_QUALITY);
                
                
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(46, 125, 50),
                    getWidth(), getHeight(), new Color(27, 94, 32)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
              
                drawDecorativeCircles(g2);
                
               
                drawOlivePattern(g2);
            }
        };
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
       
        JLabel logoIcon = createOliveIcon();
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
       
        JLabel companyName = new JLabel("SOKTRAPA");
        companyName.setFont(new Font("Segoe UI", Font.BOLD, 48));
        companyName.setForeground(Color.WHITE);
        companyName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        JPanel goldLine = createGoldLine();
        
        
        JLabel tagline = new JLabel("<html><center>Société de Conditionnement<br>et Transformation des<br>Produits Agricoles</center></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tagline.setForeground(new Color(255, 255, 255, 220));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        JPanel tunisiaBadge = createTunisiaBadge();
        
        
        JPanel olivesInfo = createOlivesInfo();
        
        
        content.add(logoIcon);
        content.add(Box.createVerticalStrut(15));
        content.add(companyName);
        content.add(Box.createVerticalStrut(20));
        content.add(goldLine);
        content.add(Box.createVerticalStrut(25));
        content.add(tagline);
        content.add(Box.createVerticalStrut(35));
        content.add(tunisiaBadge);
        content.add(Box.createVerticalStrut(30));
        content.add(olivesInfo);
        
        panel.add(content, gbc);
        return panel;
    }
    
    
    private JLabel createOliveIcon() {
        JLabel icon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                
               
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(10, 10, 80, 80);
                
               
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(20, 20, 60, 60);
                
                super.paintComponent(g);
            }
        };
        icon.setText("🫒");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(100, 100));
        return icon;
    }
    
    
    private JPanel createGoldLine() {
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 215, 0, 100),
                    getWidth()/2, 0, GOLD,
                    true
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
            }
        };
        line.setPreferredSize(new Dimension(180, 4));
        line.setMaximumSize(new Dimension(180, 4));
        line.setOpaque(false);
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        return line;
    }
    
    
    private JPanel createTunisiaBadge() {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        badge.setPreferredSize(new Dimension(180, 45));
        badge.setMaximumSize(new Dimension(180, 45));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel flag = new JLabel("🇹🇳");
        flag.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        
        JLabel text = new JLabel("Tunisie");
        text.setFont(new Font("Segoe UI", Font.BOLD, 16));
        text.setForeground(Color.WHITE);
        
        badge.add(flag);
        badge.add(text);
        return badge;
    }
    
   
    private JPanel createOlivesInfo() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel green = new JLabel("🟢 Vertes");
        green.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        green.setForeground(new Color(255, 255, 255, 180));
        
        JLabel separator = new JLabel("•");
        separator.setFont(new Font("Segoe UI", Font.BOLD, 14));
        separator.setForeground(GOLD);
        
        JLabel black = new JLabel("⚫ Noires");
        black.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        black.setForeground(new Color(255, 255, 255, 180));
        
        panel.add(green);
        panel.add(separator);
        panel.add(black);
        return panel;
    }
    
   
    private void drawDecorativeCircles(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 10));
        g2.fillOval(-100, -100, 300, 300);
        g2.fillOval(300, 400, 250, 250);
        
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillOval(350, -50, 200, 200);
        g2.fillOval(-50, 450, 180, 180);
    }
    
   
    private void drawOlivePattern(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 15));
        
        
        int[][] olives = {
            {50, 80, 25, 16}, {400, 30, 30, 18}, {380, 520, 28, 17},
            {30, 500, 22, 14}, {420, 300, 20, 12}, {20, 250, 18, 11}
        };
        
        for (int[] o : olives) {
            g2.fillOval(o[0], o[1], o[2], o[3]);
        }
    }

    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                
               
                GradientPaint gp = new GradientPaint(
                    0, 0, CREAM,
                    0, getHeight(), CREAM_DARK
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
              
                g2.setColor(new Color(0, 0, 0, 5));
                for (int i = 0; i < getHeight(); i += 30) {
                    g2.drawLine(0, i, getWidth(), i);
                }
            }
        };
        panel.setLayout(new GridBagLayout());
        
        
        JPanel formCard = createFormCard();
        
        panel.add(formCard);
        return panel;
    }
    
    
    private JPanel createFormCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                
             
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(8, 8, getWidth() - 8, getHeight() - 8, 30, 30);
                
               
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 30, 30);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(380, 480));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 45, 40, 55));
        
        
        JLabel title = new JLabel("Connexion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        
        JLabel subtitle = new JLabel("Bienvenue! Connectez-vous à votre compte");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        
        JPanel userField = createModernField("👤", "Nom d'utilisateur", false);
        JPanel passField = createModernField("🔒", "Mot de passe", true);
        
       
        btnLogin = createModernButton("Se connecter", OLIVE_DARK, OLIVE_MEDIUM, true);
        btnQuit = createModernButton("Quitter", RED_DARK, RED_LIGHT, false);
        
        btnLogin.addActionListener(e -> handleLogin());
        btnQuit.addActionListener(e -> System.exit(0));
        
       
        JLabel footer = new JLabel("© 2024 SOKTRAPA - Tous droits réservés");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(180, 180, 180));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(35));
        card.add(userField);
        card.add(Box.createVerticalStrut(20));
        card.add(passField);
        card.add(Box.createVerticalStrut(35));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(15));
        card.add(btnQuit);
        card.add(Box.createVerticalGlue());
        card.add(footer);
        
        return card;
    }
    
    
    private JPanel createModernField(String icon, String label, boolean isPassword) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setMaximumSize(new Dimension(300, 85));
        
      
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        
        JPanel fieldPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        fieldPanel.setOpaque(false);
        fieldPanel.setLayout(new BorderLayout());
        fieldPanel.setPreferredSize(new Dimension(280, 50));
        fieldPanel.setMaximumSize(new Dimension(280, 50));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
     
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLabel.setBorder(new EmptyBorder(0, 15, 0, 10));
        
     
        JTextField field;
        if (isPassword) {
            field = new JPasswordField();
            txtPassword = (JPasswordField) field;
        } else {
            field = new JTextField();
            txtUser = field;
        }
        
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(null);
        field.setOpaque(false);
        field.setForeground(TEXT_DARK);
        
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                fieldPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(OLIVE_MEDIUM, 2, true),
                    new EmptyBorder(-2, -2, -2, -2)
                ));
                fieldPanel.repaint();
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                fieldPanel.setBorder(null);
                fieldPanel.repaint();
            }
        });
        
        fieldPanel.add(iconLabel, BorderLayout.WEST);
        fieldPanel.add(field, BorderLayout.CENTER);
        
        container.add(lbl);
        container.add(Box.createVerticalStrut(10));
        container.add(fieldPanel);
        
        return container;
    }
    
   
    private JButton createModernButton(String text, Color normalColor, Color hoverColor, boolean isPrimary) {
        JButton btn = new JButton(text) {
            private boolean isHovered = false;
            private boolean isPressed = false;
            
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                    
                    @Override
                    public void mousePressed(MouseEvent e) {
                        isPressed = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        isPressed = false;
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor;
                if (isPressed) {
                    bgColor = normalColor.darker();
                } else if (isHovered) {
                    bgColor = hoverColor;
                } else {
                    bgColor = normalColor;
                }
                
                
                if (!isPressed) {
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 5, 15, 15);
                }
                
              
                g2.setColor(bgColor);
                g2.fillRoundRect(0, isPressed ? 3 : 0, getWidth() - 3, getHeight() - 5, 15, 15);
                
              
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() - (isPressed ? 0 : 2);
                g2.drawString(getText(), x, y);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(280, 52));
        btn.setMaximumSize(new Dimension(280, 52));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    
    private void handleLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPassword.getPassword());
        
        if (user.isEmpty() || pass.isEmpty()) {
            showMessage("⚠️ Veuillez remplir tous les champs!", "Champs requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        
        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion...");
        
       
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return verifyCredentials(user, pass);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        
                        dispose(); 

                        new MenuPrincipal().setVisible(true); 
                    }
                        
                     else {
                        showMessage(
                            "❌ Nom d'utilisateur ou mot de passe incorrect!",
                            "Erreur de connexion", JOptionPane.ERROR_MESSAGE
                        );
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception e) {
                    showMessage(
                        "❌ Erreur de connexion à la base de données!\n" + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE
                    );
                }
                btnLogin.setEnabled(true);
                btnLogin.setText("Se connecter");
            }
        };
        worker.execute();
    }
    
    /**
     * Checks the administrator credentials.
     *
     * <p>Passwords stored in the PBKDF2 format (see {@link PasswordHasher}) are verified
     * with a constant-time comparison. Legacy plain-text values are still accepted so that
     * existing local databases keep working, but a warning is logged: they should be
     * replaced with a hash generated by {@code PasswordHasher}.</p>
     *
     * <p>Database errors are propagated (instead of being reported as a wrong password)
     * so the UI can show a connection error message.</p>
     */
    private boolean verifyCredentials(String username, String password) throws SQLException {
        String sql = "SELECT password FROM administration WHERE nom_utilisateur = ? LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username);

            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String stored = rs.getString("password");
                if (PasswordHasher.isHashed(stored)) {
                    return PasswordHasher.verify(password.toCharArray(), stored);
                }
                System.err.println("[SOKTRAPA] Warning: plain-text password stored for user '"
                        + username + "'. Replace it with a PBKDF2 hash (projet.PasswordHasher).");
                return stored != null && java.security.MessageDigest.isEqual(
                        stored.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        }
    }
    
    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    
    public static void main(String[] args) {
        
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            
            new LoginSoktrapa().setVisible(true);
        });
    }
}