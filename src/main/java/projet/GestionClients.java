package projet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class GestionClients extends JFrame {

    private Color VERT_FONCE = new Color(27, 94, 32);
    private Color VERT = new Color(46, 125, 50);
    private Color VERT_CLAIR = new Color(200, 230, 201);
    private Color VERT_HOVER = new Color(76, 175, 80);
    private Color VERT_HEADER = new Color(33, 97, 47);
    private Color BLANC = Color.WHITE;
    private Color GRIS_FOND = new Color(245, 247, 250);
    private Color GRIS_TEXTE = new Color(100, 100, 100);
    private Color GRIS_LIGNE = new Color(248, 250, 252);
    private Color ROUGE = new Color(211, 47, 47);
    private Color BLEU = new Color(33, 150, 243);
    private Color ORANGE = new Color(255, 152, 0);

    private JTextField txtId, txtNom, txtPrenom, txtAdresse, txtTelephone, txtEmail;
    private JTextField txtPlafondCredit, txtRecherche;
    private JComboBox<String> cmbTypeClient, cmbConditionPaiement;
    private JTable table;
    private DefaultTableModel model;

    // JDBC settings are loaded from config/database.properties (see DatabaseConfig).
    public GestionClients() {
        setTitle("SOKTRAPA - Gestion des Clients");

       
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        chargerDriver();

        JPanel main = new JPanel(new BorderLayout());
        main.add(creerSidebar(), BorderLayout.WEST);
        main.add(creerContenu(), BorderLayout.CENTER);

        add(main);
        loadData();
    }

    private void chargerDriver() {
        try {
            if (!DatabaseConfig.loadDriver()) throw new ClassNotFoundException(DatabaseConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                "[ERREUR] Driver MySQL introuvable!",
                "Erreur Driver",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private JPanel creerSidebar() {
        JPanel sidebar = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, VERT_FONCE, 0, getHeight(), VERT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 15, 20, 15));

        JLabel logo = new JLabel("👤", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        logo.setForeground(BLANC);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titre = new JLabel("SOKTRAPA");
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        titre.setForeground(BLANC);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel("Gestion Clients");
        sousTitre.setFont(new Font("Arial", Font.ITALIC, 11));
        sousTitre.setForeground(new Color(255, 255, 255, 180));
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel ligne = new JPanel();
        ligne.setMaximumSize(new Dimension(160, 2));
        ligne.setBackground(new Color(255, 255, 255, 80));
        ligne.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnRefresh = creerBoutonSidebar("🔄 Actualiser");
        btnRefresh.addActionListener(e -> {
            txtRecherche.setText("");
            loadData();
            viderChamps();
        });

        JButton btnVider = creerBoutonSidebar("🗑️ Vider Formulaire");
        btnVider.addActionListener(e -> viderChamps());

        JButton btnRetour = creerBoutonSidebar("⬅️ Menu Principal");
        btnRetour.addActionListener(e -> {
            dispose();
            new MenuPrincipal().setVisible(true);
        });

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(titre);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(sousTitre);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(ligne);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(btnRefresh);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnVider);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnRetour);

        return sidebar;
    }

    private JButton creerBoutonSidebar(String texte) {
        JButton btn = new JButton(texte) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(BLANC);
        btn.setBackground(new Color(255, 255, 255, 40));
        btn.setMaximumSize(new Dimension(170, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(VERT_HOVER); btn.repaint(); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(255, 255, 255, 40)); btn.repaint(); }
        });

        return btn;
    }

   
    private JPanel creerContenu() {
        JPanel contenu = new JPanel(new BorderLayout());
        contenu.setBackground(GRIS_FOND);
        contenu.add(creerHeader(), BorderLayout.NORTH);
        contenu.add(creerCorps(), BorderLayout.CENTER);
        return contenu;
    }

    private JPanel creerHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLANC);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            new EmptyBorder(15, 25, 15, 25)
        ));

        JLabel titre = new JLabel("📋 Gestion des Clients");
        titre.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        titre.setForeground(VERT_FONCE);

        JPanel panelRecherche = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelRecherche.setOpaque(false);

        JLabel lblRecherche = new JLabel("🔍 Rechercher :");
        lblRecherche.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));

        txtRecherche = new JTextField(18);
        txtRecherche.setFont(new Font("Arial", Font.PLAIN, 13));
        txtRecherche.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        txtRecherche.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                rechercher();
            }
        });

        JButton btnRechercher = creerBoutonHeader("Rechercher", BLEU);
        btnRechercher.addActionListener(e -> rechercher());

        panelRecherche.add(lblRecherche);
        panelRecherche.add(txtRecherche);
        panelRecherche.add(btnRechercher);

        header.add(titre, BorderLayout.WEST);
        header.add(panelRecherche, BorderLayout.EAST);

        return header;
    }

    private JButton creerBoutonHeader(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(BLANC);
        btn.setBackground(couleur);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 35));

        Color hover = couleur.brighter();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(couleur); }
        });

        return btn;
    }

    private JPanel creerCorps() {
        JPanel corps = new JPanel(new BorderLayout(20, 0));
        corps.setOpaque(false);
        corps.setBorder(new EmptyBorder(20, 20, 20, 20));
        corps.add(creerFormulaire(), BorderLayout.WEST);
        corps.add(creerTable(), BorderLayout.CENTER);
        return corps;
    }

    
    private JPanel creerFormulaire() {
        JPanel panel = new JPanel();
        panel.setBackground(BLANC);

      
        panel.setPreferredSize(new Dimension(370, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel titreForm = new JLabel("📝 Formulaire Client");
        titreForm.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        titreForm.setForeground(VERT_FONCE);
        titreForm.setAlignmentX(Component.LEFT_ALIGNMENT);

       
        txtId            = creerChampTexte(330);
        txtNom           = creerChampTexte(330);
        txtPrenom        = creerChampTexte(330);
        txtAdresse       = creerChampTexte(330);
        txtTelephone     = creerChampTexte(330);
        txtEmail         = creerChampTexte(330);
        txtPlafondCredit = creerChampTexte(330);

        String[] typesClient = {
            "-- Sélectionner --",
            "Entreprise",
            "Grossiste",
            "Détaillant"
        };
        cmbTypeClient = new JComboBox<>(typesClient);
        cmbTypeClient.setFont(new Font("Arial", Font.PLAIN, 13));
        
        cmbTypeClient.setMaximumSize(new Dimension(330, 38));
        cmbTypeClient.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbTypeClient.setBackground(BLANC);

        cmbTypeClient.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String val = value.toString();
                    if (isSelected) {
                        setBackground(VERT_CLAIR);
                        setForeground(VERT_FONCE);
                    } else {
                        setBackground(BLANC);
                        switch (val) {
                            case "Entreprise": setForeground(new Color(25, 118, 210)); break;
                            case "Grossiste":  setForeground(new Color(230, 100, 0));  break;
                            case "Détaillant": setForeground(new Color(27, 94, 32));   break;
                            default:           setForeground(GRIS_TEXTE);              break;
                        }
                    }
                }
                return this;
            }
        });

        String[] conditionsPaiement = {
            "-- Sélectionner --",
            "Comptant",
            "30 jours",
            "60 jours",
            "90 jours",
            "Fin de mois"
        };
        cmbConditionPaiement = new JComboBox<>(conditionsPaiement);
        cmbConditionPaiement.setFont(new Font("Arial", Font.PLAIN, 13));
        
        cmbConditionPaiement.setMaximumSize(new Dimension(330, 38));
        cmbConditionPaiement.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbConditionPaiement.setBackground(BLANC);

        JButton btnAdd    = creerBoutonAction("➕ Ajouter Client", VERT);
        JButton btnUpdate = creerBoutonAction("✏️ Modifier Client", BLEU);
        JButton btnDelete = creerBoutonAction("🗑️ Supprimer Client", ROUGE);

        btnAdd.addActionListener(e -> ajouterClient());
        btnUpdate.addActionListener(e -> modifierClient());
        btnDelete.addActionListener(e -> supprimerClient());

        panel.add(titreForm);
        panel.add(Box.createVerticalStrut(12));

        panel.add(creerLabel("ID Client * :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(txtId);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Nom * :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(txtNom);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Prénom :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(txtPrenom);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Adresse :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(txtAdresse);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Téléphone :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(txtTelephone);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Email :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Type Client :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(cmbTypeClient);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Condition Paiement :"));
        panel.add(Box.createVerticalStrut(3));
        panel.add(cmbConditionPaiement);
        panel.add(Box.createVerticalStrut(8));

        panel.add(creerLabel("Plafond Crédit (TND) :"));
        panel.add(Box.createVerticalStrut(3));

        JPanel panelPlafond = new JPanel(new BorderLayout(5, 0));
        panelPlafond.setBackground(BLANC);
        
        panelPlafond.setMaximumSize(new Dimension(330, 38));
        panelPlafond.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTND = new JLabel(" TND ");
        lblTND.setFont(new Font("Arial", Font.BOLD, 11));
        lblTND.setForeground(BLANC);
        lblTND.setBackground(VERT);
        lblTND.setOpaque(true);
        lblTND.setBorder(new EmptyBorder(5, 6, 5, 6));
        lblTND.setHorizontalAlignment(JLabel.CENTER);

        panelPlafond.add(txtPlafondCredit, BorderLayout.CENTER);
        panelPlafond.add(lblTND, BorderLayout.EAST);

        panel.add(panelPlafond);
        panel.add(Box.createVerticalStrut(15));

        panel.add(btnAdd);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnUpdate);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnDelete);

        JScrollPane scrollForm = new JScrollPane(panel);
        scrollForm.setBorder(null);
        scrollForm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        
        wrapper.setPreferredSize(new Dimension(370, 0));
        wrapper.add(scrollForm);

        return wrapper;
    }

    private JLabel creerLabel(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(GRIS_TEXTE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField creerChampTexte(int largeur) {
        JTextField champ = new JTextField();
        champ.setFont(new Font("Arial", Font.PLAIN, 13));
        champ.setMaximumSize(new Dimension(largeur, 35));
        champ.setAlignmentX(Component.LEFT_ALIGNMENT);
        champ.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return champ;
    }

    private JButton creerBoutonAction(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(BLANC);
        btn.setBackground(couleur);
        
        btn.setMaximumSize(new Dimension(330, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hover = couleur.brighter();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(couleur); }
        });
        return btn;
    }

    
    private JPanel creerTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BLANC);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titreTable = new JLabel("📊 Liste des Clients");
        titreTable.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        titreTable.setForeground(VERT_FONCE);
        titreTable.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] cols = {"ID", "Nom", "Prénom", "Adresse", "Téléphone", "Email", "Type", "Condition", "Plafond (TND)"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.setSelectionBackground(VERT_CLAIR);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setBackground(GRIS_FOND);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setBackground(VERT_HEADER);
                label.setForeground(BLANC);
                label.setFont(new Font("Arial", Font.BOLD, 11));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, VERT_FONCE));
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                if (isSelected) {
                    c.setBackground(VERT_CLAIR);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? BLANC : GRIS_LIGNE);
                    c.setForeground(new Color(40, 40, 40));

                    if (column == 6 && value != null) {
                        switch (value.toString()) {
                            case "Entreprise":
                                c.setForeground(new Color(25, 118, 210));
                                ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                                break;
                            case "Grossiste":
                                c.setForeground(new Color(230, 100, 0));
                                ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                                break;
                            case "Détaillant":
                                c.setForeground(new Color(27, 94, 32));
                                ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                                break;
                        }
                    }

                    if (column == 8 && value != null) {
                        try {
                            double plafond = Double.parseDouble(value.toString());
                            if (plafond >= 10000)     { c.setForeground(new Color(27, 94, 32)); }
                            else if (plafond >= 5000) { c.setForeground(new Color(230, 100, 0)); }
                            else if (plafond > 0)     { c.setForeground(new Color(211, 47, 47)); }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) remplirFormulaire(row);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scroll.getViewport().setBackground(GRIS_FOND);

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelInfo.setOpaque(false);
        JLabel lblLegende = new JLabel("  🔵 Entreprise   🟠 Grossiste   🟢 Détaillant  ");
        lblLegende.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        lblLegende.setForeground(GRIS_TEXTE);
        lblLegende.setOpaque(true);
        lblLegende.setBackground(new Color(245, 245, 245));
        lblLegende.setBorder(new EmptyBorder(3, 8, 3, 8));
        panelInfo.add(lblLegende);

        panel.add(titreTable, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelInfo, BorderLayout.SOUTH);

        return panel;
    }

    
    private void remplirFormulaire(int row) {
        txtId.setText(getValueAt(row, 0));
        txtNom.setText(getValueAt(row, 1));
        txtPrenom.setText(getValueAt(row, 2));
        txtAdresse.setText(getValueAt(row, 3));
        txtTelephone.setText(getValueAt(row, 4));
        txtEmail.setText(getValueAt(row, 5));

        String type = getValueAt(row, 6);
        boolean typeFound = false;
        for (int i = 0; i < cmbTypeClient.getItemCount(); i++) {
            if (cmbTypeClient.getItemAt(i).equalsIgnoreCase(type)) {
                cmbTypeClient.setSelectedIndex(i);
                typeFound = true;
                break;
            }
        }
        if (!typeFound) cmbTypeClient.setSelectedIndex(0);

        String condition = getValueAt(row, 7);
        boolean condFound = false;
        for (int i = 0; i < cmbConditionPaiement.getItemCount(); i++) {
            if (cmbConditionPaiement.getItemAt(i).equalsIgnoreCase(condition)) {
                cmbConditionPaiement.setSelectedIndex(i);
                condFound = true;
                break;
            }
        }
        if (!condFound) cmbConditionPaiement.setSelectedIndex(0);

        txtPlafondCredit.setText(getValueAt(row, 8));
    }

    private String getValueAt(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value != null ? value.toString() : "";
    }

    private Connection getCon() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT id_client, nom_client, prenom_client, adress_client, " +
                     "tel_client, email_client, type_client, condition_paiement, plafond_credit " +
                     "FROM client ORDER BY id_client";

        try (Connection c = getCon();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_client"),
                    rs.getString("nom_client"),
                    rs.getString("prenom_client"),
                    rs.getString("adress_client"),
                    rs.getString("tel_client"),
                    rs.getString("email_client"),
                    rs.getString("type_client"),
                    rs.getString("condition_paiement"),
                    String.format("%.2f", rs.getDouble("plafond_credit"))
                });
            }
        } catch (SQLException e) { showError(e); }
    }

    private void rechercher() {
        String recherche = txtRecherche.getText().trim();
        if (recherche.isEmpty()) { loadData(); return; }

        model.setRowCount(0);
        String sql = "SELECT * FROM client WHERE " +
                     "nom_client LIKE ? OR prenom_client LIKE ? OR " +
                     "id_client LIKE ? OR tel_client LIKE ? OR type_client LIKE ?";

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String pattern = "%" + recherche + "%";
            ps.setString(1, pattern); ps.setString(2, pattern);
            ps.setString(3, pattern); ps.setString(4, pattern);
            ps.setString(5, pattern);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_client"),
                    rs.getString("nom_client"),
                    rs.getString("prenom_client"),
                    rs.getString("adress_client"),
                    rs.getString("tel_client"),
                    rs.getString("email_client"),
                    rs.getString("type_client"),
                    rs.getString("condition_paiement"),
                    String.format("%.2f", rs.getDouble("plafond_credit"))
                });
            }
        } catch (SQLException e) { showError(e); }
    }

    private void ajouterClient() {
        if (txtId.getText().trim().isEmpty() || txtNom.getText().trim().isEmpty()) {
            showMsg("⚠️ Champs obligatoires: ID Client, Nom Client", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String typeSelectionne = (String) cmbTypeClient.getSelectedItem();
        if (typeSelectionne == null || typeSelectionne.startsWith("--")) {
            showMsg("⚠️ Veuillez sélectionner un type de client!", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO client (id_client, nom_client, prenom_client, adress_client, " +
                     "tel_client, email_client, type_client, condition_paiement, plafond_credit) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
            ps.setString(2, txtNom.getText().trim());
            ps.setString(3, txtPrenom.getText().trim());
            ps.setString(4, txtAdresse.getText().trim());
            ps.setString(5, txtTelephone.getText().trim());
            ps.setString(6, txtEmail.getText().trim());
            ps.setString(7, typeSelectionne);

            String conditionSelectionnee = (String) cmbConditionPaiement.getSelectedItem();
            ps.setString(8, conditionSelectionnee != null && !conditionSelectionnee.startsWith("--") ?
                           conditionSelectionnee : null);

            double plafond = 0.0;
            if (!txtPlafondCredit.getText().trim().isEmpty()) {
                try {
                    plafond = Double.parseDouble(txtPlafondCredit.getText().trim());
                } catch (NumberFormatException ex) {
                    showMsg("⚠️ Plafond crédit doit être un nombre valide!", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            ps.setDouble(9, plafond);
            ps.executeUpdate();

            showMsg("✅ Client ajouté avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            viderChamps();
            loadData();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                showMsg("❌ Ce client existe déjà!", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else { showError(e); }
        } catch (NumberFormatException e) {
            showMsg("⚠️ ID Client doit être un nombre!", "Erreur", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modifierClient() {
        if (txtId.getText().trim().isEmpty()) {
            showMsg("⚠️ Sélectionnez un client!", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String typeSelectionne = (String) cmbTypeClient.getSelectedItem();
        if (typeSelectionne == null || typeSelectionne.startsWith("--")) {
            showMsg("⚠️ Veuillez sélectionner un type de client!", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE client SET nom_client=?, prenom_client=?, adress_client=?, " +
                     "tel_client=?, email_client=?, type_client=?, condition_paiement=?, " +
                     "plafond_credit=? WHERE id_client=?";

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, txtNom.getText().trim());
            ps.setString(2, txtPrenom.getText().trim());
            ps.setString(3, txtAdresse.getText().trim());
            ps.setString(4, txtTelephone.getText().trim());
            ps.setString(5, txtEmail.getText().trim());
            ps.setString(6, typeSelectionne);

            String conditionSelectionnee = (String) cmbConditionPaiement.getSelectedItem();
            ps.setString(7, conditionSelectionnee != null && !conditionSelectionnee.startsWith("--") ?
                           conditionSelectionnee : null);

            double plafond = 0.0;
            if (!txtPlafondCredit.getText().trim().isEmpty()) {
                plafond = Double.parseDouble(txtPlafondCredit.getText().trim());
            }
            ps.setDouble(8, plafond);
            ps.setInt(9, Integer.parseInt(txtId.getText().trim()));

            if (ps.executeUpdate() > 0) {
                showMsg("✅ Client modifié avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                viderChamps();
                loadData();
            } else {
                showMsg("⚠️ Aucun client trouvé avec cet ID!", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) { showError(e); }
        catch (NumberFormatException e) {
            showMsg("⚠️ Vérifiez les valeurs numériques!", "Erreur", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void supprimerClient() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            showMsg("⚠️ Sélectionnez un client!", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "🗑️ Supprimer le client #" + id + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection c = getCon();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM client WHERE id_client = ?")) {
                ps.setInt(1, Integer.parseInt(id));
                if (ps.executeUpdate() > 0) {
                    showMsg("✅ Client supprimé!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    viderChamps();
                    loadData();
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key")) {
                    showMsg("❌ Impossible: ce client est lié à d'autres données!",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                } else { showError(e); }
            }
        }
    }

    private void viderChamps() {
        txtId.setText("");
        txtNom.setText("");
        txtPrenom.setText("");
        txtAdresse.setText("");
        txtTelephone.setText("");
        txtEmail.setText("");
        cmbTypeClient.setSelectedIndex(0);
        cmbConditionPaiement.setSelectedIndex(0);
        txtPlafondCredit.setText("");
        table.clearSelection();
    }

    private void showMsg(String msg, String titre, int type) {
        JOptionPane.showMessageDialog(this, msg, titre, type);
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "❌ ERREUR: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GestionClients().setVisible(true));
    }
}