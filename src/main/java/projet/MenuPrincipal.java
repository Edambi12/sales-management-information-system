package projet;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MenuPrincipal extends JFrame {

  
    private Color VERT_FONCE = new Color(27, 94, 32);
    private Color VERT = new Color(46, 125, 50);
    private Color BLANC = Color.WHITE;
    private Color GRIS_FOND = new Color(245, 247, 250);
    private Color GRIS_TEXTE = new Color(100, 100, 100);

    
    private Font FONT_EMOJI = new Font("Segoe UI Emoji", Font.PLAIN, 50);
    private Font FONT_EMOJI_SMALL = new Font("Segoe UI Emoji", Font.PLAIN, 14);

    public MenuPrincipal() {

        setTitle("SOKTRAPA - Menu Principal");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.add(createSidebar(), BorderLayout.WEST);
        main.add(createContent(), BorderLayout.CENTER);

        add(main);
    }

   
    private JPanel createSidebar() {

        JPanel sidebar = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, VERT_FONCE, 0, getHeight(), VERT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 15, 20, 15));

        
        JLabel logo = new JLabel("\uD83E\uDED2", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 55));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nom = new JLabel("SOKTRAPA");
        nom.setFont(new Font("Arial", Font.BOLD, 22));
        nom.setForeground(BLANC);
        nom.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel slogan = new JLabel("Olives de Table - Tunisie");
        slogan.setFont(new Font("Arial", Font.ITALIC, 11));
        slogan.setForeground(new Color(255, 255, 255, 180));
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel ligne = new JPanel();
        ligne.setMaximumSize(new Dimension(180, 1));
        ligne.setBackground(new Color(255, 255, 255, 80));

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(nom);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(slogan);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(ligne);

        return sidebar;
    }

    
    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(GRIS_FOND);

        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createButtons(), BorderLayout.CENTER);

        return content;
    }

    
    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLANC);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("Menu Principal");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        JLabel lblDate = new JLabel("\uD83D\uDCC5 " + date);
        lblDate.setFont(FONT_EMOJI_SMALL);

        JLabel lblUser = new JLabel("\uD83D\uDC64 Admin");
        lblUser.setFont(FONT_EMOJI_SMALL);
        lblUser.setForeground(VERT);

        right.add(lblDate);
        right.add(lblUser);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createButtons() {

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(2, 3, 25, 25));
        grid.setBorder(new EmptyBorder(30, 30, 30, 30));
        grid.setOpaque(false);

        grid.add(createCard("\uD83D\uDC65", "Gestion Clients",   "Gérer les clients"));           // 👥
        grid.add(createCard("\uD83C\uDFEA", "Gestion Stock",     "Contrôler le stock"));           // 🏪
        grid.add(createCard("\uD83D\uDCE6", "Gestion Commandes", "Gérer les commandes"));          // 📦
        grid.add(createCard("\uD83D\uDCB3", "Gestion Paiements", "Gérer les paiements"));          // 💳
        grid.add(createCard("\uD83D\uDCDD", "Reclamations",      "Traiter les réclamations"));     // 📝

        container.add(grid);
        return container;
    }

   
    private JPanel createCard(String icon, String title, String desc) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BLANC);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(35, 30, 35, 30)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

       
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(FONT_EMOJI);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 16));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel d = new JLabel(desc);
        d.setFont(new Font("Arial", Font.PLAIN, 12));
        d.setForeground(GRIS_TEXTE);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);

        text.add(t);
        text.add(Box.createVerticalStrut(5));
        text.add(d);

        
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(224, 242, 241));
                card.setBorder(new CompoundBorder(
                        new LineBorder(VERT, 2),
                        new EmptyBorder(34, 29, 34, 29)
                ));
            }

            public void mouseExited(MouseEvent e) {
                card.setBackground(BLANC);
                card.setBorder(new CompoundBorder(
                        new LineBorder(new Color(220, 220, 220), 1),
                        new EmptyBorder(35, 30, 35, 30)
                ));
            }

            public void mouseClicked(MouseEvent e) {
                openModule(title);
            }
        });

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private void openModule(String module) {

        switch (module) {
            case "Gestion Clients":
                dispose();
                new GestionClients().setVisible(true);
                break;

            case "Gestion Stock":
                dispose();
                new GestionStock().setVisible(true);
                break;

            case "Gestion Commandes":
                dispose();
                new GestionCommandes().setVisible(true);
                break;

            case "Gestion Paiements":
                dispose();
                new GestionPaiement().setVisible(true);
                break;

            case "Reclamations":
                dispose();
                new Reclamation().setVisible(true);
                break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuPrincipal().setVisible(true);
        });
    }
}