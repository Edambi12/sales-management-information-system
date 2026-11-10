package projet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GestionCommandes extends JFrame {

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
    private Color TEAL = new Color(0, 150, 136);

    private JTextField txtIdCommande, txtRemise, txtRecherche;
    private JSpinner spinnerDateCommande, spinnerDatePrevue;
    private JComboBox<String> cmbStatut;

    private JComboBox<String> cmbClient;
    private JLabel lblInfoClient;

    private JComboBox<String> cmbProduit;
    private JLabel lblInfoProduit;
    private JTextField txtQteCommande;

    private JTable tableCommande;
    private JTable tableLigneCommande;
    private DefaultTableModel modelCommande;
    private DefaultTableModel modelLigneCommande;

    private JLabel lblTitreLignes;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // JDBC settings are loaded from config/database.properties (see DatabaseConfig).
    public GestionCommandes() {
        setTitle("SOKTRAPA - Gestion des Commandes");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1600, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        chargerDriver();

        JPanel main = new JPanel(new BorderLayout());
        main.add(creerSidebar(), BorderLayout.WEST);
        main.add(creerContenu(), BorderLayout.CENTER);

        add(main);
        loadDataCommandes();
    }

    private void chargerDriver() {
        try {
            if (!DatabaseConfig.loadDriver()) throw new ClassNotFoundException(DatabaseConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Driver MySQL introuvable!", "Erreur Driver", JOptionPane.ERROR_MESSAGE);
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

        JLabel logo = new JLabel("📦", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        logo.setForeground(BLANC);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titre = new JLabel("SOKTRAPA");
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        titre.setForeground(BLANC);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel("Gestion Commandes");
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
            loadDataCommandes();
            modelLigneCommande.setRowCount(0);
            lblTitreLignes.setText("📝 Lignes de Commande");
            viderChamps();
            chargerClients();
            chargerProduits();
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
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(255, 255, 255, 40)); btn.repaint(); }
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

        JLabel titre = new JLabel("📋 Gestion des Commandes");
        titre.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        titre.setForeground(VERT_FONCE);

        JPanel panelRecherche = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelRecherche.setOpaque(false);

        JLabel lblRecherche = new JLabel("🔍 ID Commande :");
        lblRecherche.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));

        txtRecherche = new JTextField(12);
        txtRecherche.setFont(new Font("Arial", Font.PLAIN, 13));
        txtRecherche.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        txtRecherche.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) rechercher();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    txtRecherche.setText("");
                    loadDataCommandes();
                }
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
            public void mouseExited(MouseEvent e)  { btn.setBackground(couleur); }
        });

        return btn;
    }

    private JPanel creerCorps() {
        JPanel corps = new JPanel(new BorderLayout(15, 0));
        corps.setOpaque(false);
        corps.setBorder(new EmptyBorder(15, 15, 15, 15));
        corps.add(creerFormulaire(), BorderLayout.WEST);
        corps.add(creerPanelTables(), BorderLayout.CENTER);
        return corps;
    }

    
    private JPanel creerFormulaire() {
        JPanel panel = new JPanel();
        panel.setBackground(BLANC);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titreCommande = new JLabel("📦 Commande");
        titreCommande.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        titreCommande.setForeground(VERT_FONCE);
        titreCommande.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtIdCommande = creerChampTexte(310);
        txtRemise     = creerChampTexte(310);
        txtRemise.setText("0");

        spinnerDateCommande = creerSpinnerDate();
        spinnerDatePrevue   = creerSpinnerDate();

        
        String[] statuts = {"En attente", "Confirmée", "Annulée"};
        cmbStatut = new JComboBox<>(statuts);
        cmbStatut.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbStatut.setMaximumSize(new Dimension(310, 32));
        cmbStatut.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbStatut.setBackground(BLANC);
        cmbStatut.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String val = value.toString();
                    if (isSelected) { setBackground(VERT_CLAIR); setForeground(VERT_FONCE); }
                    else {
                        setBackground(BLANC);
                        switch (val) {
                            case "En attente": setForeground(ORANGE);     setText("⏳ " + val); break;
                            case "Confirmée":  setForeground(VERT_FONCE); setText("✅ " + val); break;
                            case "Annulée":    setForeground(ROUGE);      setText("❌ " + val); break;
                        }
                    }
                }
                return this;
            }
        });

        cmbClient = new JComboBox<>();
        cmbClient.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbClient.setMaximumSize(new Dimension(310, 32));
        cmbClient.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbClient.setBackground(BLANC);
        chargerClients();

        lblInfoClient = new JLabel("<html><i>Sélectionnez un client...</i></html>");
        lblInfoClient.setFont(new Font("Arial", Font.PLAIN, 9));
        lblInfoClient.setForeground(GRIS_TEXTE);
        lblInfoClient.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblInfoClient.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 255), 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
        lblInfoClient.setOpaque(true);
        lblInfoClient.setBackground(new Color(245, 245, 255));
        lblInfoClient.setMaximumSize(new Dimension(310, 80));
        cmbClient.addActionListener(e -> afficherInfoClient());

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(310, 2));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titreLigne = new JLabel("📝 Ligne Commande");
        titreLigne.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        titreLigne.setForeground(BLEU);
        titreLigne.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbProduit = new JComboBox<>();
        cmbProduit.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbProduit.setMaximumSize(new Dimension(310, 32));
        cmbProduit.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbProduit.setBackground(BLANC);
        chargerProduits();

        lblInfoProduit = new JLabel("<html><i>Sélectionnez un produit...</i></html>");
        lblInfoProduit.setFont(new Font("Arial", Font.PLAIN, 9));
        lblInfoProduit.setForeground(GRIS_TEXTE);
        lblInfoProduit.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblInfoProduit.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 255, 200), 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
        lblInfoProduit.setOpaque(true);
        lblInfoProduit.setBackground(new Color(245, 255, 245));
        lblInfoProduit.setMaximumSize(new Dimension(310, 65));
        cmbProduit.addActionListener(e -> afficherInfoProduit());

        txtQteCommande = creerChampTexte(310);
        txtQteCommande.setText("1");

        JPanel panelQte = new JPanel(new BorderLayout(4, 0));
        panelQte.setBackground(BLANC);
        panelQte.setMaximumSize(new Dimension(310, 32));
        panelQte.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblKg = new JLabel(" kg ");
        lblKg.setFont(new Font("Arial", Font.BOLD, 11));
        lblKg.setForeground(BLANC);
        lblKg.setBackground(VERT);
        lblKg.setOpaque(true);
        lblKg.setBorder(new EmptyBorder(4, 8, 4, 8));
        lblKg.setHorizontalAlignment(JLabel.CENTER);

        panelQte.add(txtQteCommande, BorderLayout.CENTER);
        panelQte.add(lblKg, BorderLayout.EAST);

        JButton btnAdd            = creerBoutonAction("➕ Ajouter Commande",     VERT);
        JButton btnUpdate         = creerBoutonAction("✏️ Modifier Commande",    ORANGE);
        JButton btnDelete         = creerBoutonAction("🗑️ Supprimer Commande",   ROUGE);
        JButton btnAddLigne       = creerBoutonAction("➕ Ajouter Ligne",        BLEU);
        JButton btnAfficherLignes = creerBoutonAction("📋 Afficher Toutes Lignes", TEAL);

        btnAdd.addActionListener(e -> ajouterCommande());
        btnUpdate.addActionListener(e -> modifierCommande());
        btnDelete.addActionListener(e -> supprimerCommande());
        btnAddLigne.addActionListener(e -> ajouterLigneCommande());
        btnAfficherLignes.addActionListener(e -> loadToutesLignesCommande());

        panel.add(titreCommande);
        panel.add(Box.createVerticalStrut(8));
        panel.add(creerLabel("ID Commande * :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(txtIdCommande);
        panel.add(Box.createVerticalStrut(6));
        panel.add(creerLabel("Date Commande :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(spinnerDateCommande);
        panel.add(Box.createVerticalStrut(6));
        panel.add(creerLabel("Statut :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(cmbStatut);
        panel.add(Box.createVerticalStrut(6));
        panel.add(creerLabel("Date Prévue :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(spinnerDatePrevue);
        panel.add(Box.createVerticalStrut(6));
        panel.add(creerLabel("Remise (%) :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(txtRemise);
        panel.add(Box.createVerticalStrut(6));
        panel.add(creerLabel("👤 Sélectionner Client * :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(cmbClient);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblInfoClient);
        panel.add(Box.createVerticalStrut(10));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titreLigne);
        panel.add(Box.createVerticalStrut(8));
        panel.add(creerLabel("🏷️ Sélectionner Produit * :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(cmbProduit);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblInfoProduit);
        panel.add(Box.createVerticalStrut(6));
        panel.add(creerLabel("Quantité (kg) * :"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(panelQte);
        panel.add(Box.createVerticalStrut(12));
        panel.add(btnAdd);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnUpdate);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnDelete);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnAddLigne);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnAfficherLignes);

        JScrollPane scrollForm = new JScrollPane(panel);
        scrollForm.setBorder(null);
        scrollForm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(340, 0));
        wrapper.add(scrollForm);

        return wrapper;
    }

    
    private void loadToutesLignesCommande() {
        modelLigneCommande.setRowCount(0);
        tableCommande.clearSelection();

        String sql = "SELECT lc.id_commande, lc.id_produit, p.nom_produit, lc.qte_commande " +
                     "FROM ligne_commande lc JOIN produit p ON lc.id_produit = p.id_produit " +
                     "ORDER BY lc.id_commande DESC";

        try (Connection c = getCon(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                modelLigneCommande.addRow(new Object[]{
                    rs.getInt("id_commande"), rs.getInt("id_produit"),
                    rs.getString("nom_produit"), rs.getInt("qte_commande")
                });
                count++;
            }
            lblTitreLignes.setText("📋 Toutes les Lignes (" + count + " lignes)");
            lblTitreLignes.setForeground(TEAL);
        } catch (SQLException e) { showError(e); }
    }

   
    private void chargerClients() {
        cmbClient.removeAllItems();
        cmbClient.addItem("-- Sélectionner un client --");
        String sql = "SELECT id_client, nom_client, prenom_client FROM client ORDER BY nom_client";

        try (Connection c = getCon(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id_client");
                String nom = rs.getString("nom_client");
                String prenom = rs.getString("prenom_client");
                cmbClient.addItem(id + " - " + nom + " " + (prenom != null ? prenom : ""));
            }
        } catch (SQLException e) { showError(e); }
    }

    private void afficherInfoClient() {
        String selection = (String) cmbClient.getSelectedItem();
        if (selection == null || selection.startsWith("--")) {
            lblInfoClient.setText("<html><i>Sélectionnez un client...</i></html>");
            lblInfoClient.setBackground(new Color(245, 245, 255));
            return;
        }
        String[] parts = selection.split(" - ");
        if (parts.length < 2) return;
        int idClient = Integer.parseInt(parts[0].trim());

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM client WHERE id_client = ?")) {
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String info = "<html><div style='font-size:9px;'>" +
                    "<b>👤 " + rs.getString("nom_client") + " " +
                    (rs.getString("prenom_client") != null ? rs.getString("prenom_client") : "") + "</b><br/>" +
                    "📞 " + (rs.getString("tel_client") != null ? rs.getString("tel_client") : "N/A") + "<br/>" +
                    "📧 " + (rs.getString("email_client") != null ? rs.getString("email_client") : "N/A") + "<br/>" +
                    "💰 Plafond: " + String.format("%.2f TND", rs.getDouble("plafond_credit")) +
                    "</div></html>";
                lblInfoClient.setText(info);
                lblInfoClient.setBackground(new Color(232, 245, 233));
            }
        } catch (SQLException e) { showError(e); }
    }

   
    private void chargerProduits() {
        cmbProduit.removeAllItems();
        cmbProduit.addItem("-- Sélectionner un produit --");
        String sql = "SELECT id_produit, nom_produit FROM produit ORDER BY nom_produit";

        try (Connection c = getCon(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cmbProduit.addItem(rs.getInt("id_produit") + " - " + rs.getString("nom_produit"));
            }
        } catch (SQLException e) { showError(e); }
    }

    private void afficherInfoProduit() {
        String selection = (String) cmbProduit.getSelectedItem();
        if (selection == null || selection.startsWith("--")) {
            lblInfoProduit.setText("<html><i>Sélectionnez un produit...</i></html>");
            lblInfoProduit.setBackground(new Color(245, 255, 245));
            return;
        }
        String[] parts = selection.split(" - ");
        if (parts.length < 2) return;
        int idProduit = Integer.parseInt(parts[0].trim());

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM produit WHERE id_produit = ?")) {
            ps.setInt(1, idProduit);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String info = "<html><div style='font-size:9px;'>" +
                    "<b>🏷️ " + rs.getString("nom_produit") + "</b><br/>" +
                    "💲 Prix: " + String.format("%.2f TND", rs.getDouble("prix_produit")) + "<br/>" +
                    "📦 Type: " + (rs.getString("type_produit") != null ? rs.getString("type_produit") : "N/A") +
                    "</div></html>";
                lblInfoProduit.setText(info);
                lblInfoProduit.setBackground(new Color(232, 255, 232));
            }
        } catch (SQLException e) { showError(e); }
    }

    private Integer getIdClientSelectionne() {
        String sel = (String) cmbClient.getSelectedItem();
        if (sel == null || sel.startsWith("--")) return null;
        try { return Integer.parseInt(sel.split(" - ")[0].trim()); }
        catch (Exception e) { return null; }
    }

    private Integer getIdProduitSelectionne() {
        String sel = (String) cmbProduit.getSelectedItem();
        if (sel == null || sel.startsWith("--")) return null;
        try { return Integer.parseInt(sel.split(" - ")[0].trim()); }
        catch (Exception e) { return null; }
    }

    private JSpinner creerSpinnerDate() {
        SpinnerDateModel dm = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(dm);
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        sp.setFont(new Font("Arial", Font.PLAIN, 12));
        sp.setMaximumSize(new Dimension(310, 32));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
    }

    private JLabel creerLabel(String texte) {
        JLabel lbl = new JLabel(texte);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        lbl.setForeground(GRIS_TEXTE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField creerChampTexte(int largeur) {
        JTextField champ = new JTextField();
        champ.setFont(new Font("Arial", Font.PLAIN, 12));
        champ.setMaximumSize(new Dimension(largeur, 32));
        champ.setAlignmentX(Component.LEFT_ALIGNMENT);
        champ.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return champ;
    }

    private JButton creerBoutonAction(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        btn.setForeground(BLANC);
        btn.setBackground(couleur);
        btn.setMaximumSize(new Dimension(310, 32));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hover = couleur.brighter();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(couleur); }
        });
        return btn;
    }

 
    private JPanel creerPanelTables() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 15));
        panel.setOpaque(false);
        panel.add(creerTableCommande());
        panel.add(creerTableLigneCommande());
        return panel;
    }

    private JPanel creerTableCommande() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BLANC);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titreTable = new JLabel("📦 Liste des Commandes");
        titreTable.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        titreTable.setForeground(VERT_FONCE);
        titreTable.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] cols = {"ID", "Date Commande", "Statut", "Date Prévue", "Remise (%)", "ID Client"};
        modelCommande = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tableCommande = new JTable(modelCommande);
        configurerTableCommande(tableCommande, VERT_HEADER);

        tableCommande.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tableCommande.getSelectedRow();
                if (row >= 0) {
                    remplirFormulaireCommande(row);
                    int idCmd = Integer.parseInt(modelCommande.getValueAt(row, 0).toString());
                    loadLignesCommande(idCmd);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableCommande);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scroll.getViewport().setBackground(GRIS_FOND);

        panel.add(titreTable, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel creerTableLigneCommande() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BLANC);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        lblTitreLignes = new JLabel("📝 Lignes de Commande");
        lblTitreLignes.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        lblTitreLignes.setForeground(BLEU);
        lblTitreLignes.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] cols = {"ID Commande", "ID Produit", "Nom Produit", "Quantité (kg)"};
        modelLigneCommande = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tableLigneCommande = new JTable(modelLigneCommande);
        configurerTable(tableLigneCommande, BLEU);

        tableLigneCommande.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tableLigneCommande.getSelectedRow();
                if (row >= 0) remplirFormulaireLigne(row);
            }
        });

        JScrollPane scroll = new JScrollPane(tableLigneCommande);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scroll.getViewport().setBackground(GRIS_FOND);

        JButton btnModifierLigne = new JButton("✏️ Modifier Ligne");
        btnModifierLigne.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        btnModifierLigne.setForeground(BLANC);
        btnModifierLigne.setBackground(ORANGE);
        btnModifierLigne.setFocusPainted(false);
        btnModifierLigne.setBorderPainted(false);
        btnModifierLigne.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnModifierLigne.addActionListener(e -> modifierLigneCommande());
        btnModifierLigne.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnModifierLigne.setBackground(ORANGE.brighter()); }
            public void mouseExited(MouseEvent e)  { btnModifierLigne.setBackground(ORANGE); }
        });

        JButton btnDeleteLigne = new JButton("🗑️ Supprimer Ligne");
        btnDeleteLigne.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        btnDeleteLigne.setForeground(BLANC);
        btnDeleteLigne.setBackground(ROUGE);
        btnDeleteLigne.setFocusPainted(false);
        btnDeleteLigne.setBorderPainted(false);
        btnDeleteLigne.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeleteLigne.addActionListener(e -> supprimerLigneCommande());
        btnDeleteLigne.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnDeleteLigne.setBackground(ROUGE.brighter()); }
            public void mouseExited(MouseEvent e)  { btnDeleteLigne.setBackground(ROUGE); }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnModifierLigne);
        bottomPanel.add(btnDeleteLigne);

        panel.add(lblTitreLignes, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

   
    private void configurerTableCommande(JTable table, Color headerColor) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setSelectionBackground(VERT_CLAIR);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setBackground(GRIS_FOND);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v != null ? v.toString() : "");
                lbl.setOpaque(true);
                lbl.setBackground(headerColor);
                lbl.setForeground(BLANC);
                lbl.setFont(new Font("Arial", Font.BOLD, 11));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                if (sel) {
                    comp.setBackground(VERT_CLAIR);
                    comp.setForeground(Color.BLACK);
                } else {
                    comp.setBackground(r % 2 == 0 ? BLANC : GRIS_LIGNE);
                    comp.setForeground(new Color(40, 40, 40));
                    if (c == 2 && v != null) {
                        switch (v.toString()) {
                            case "En attente":
                                comp.setForeground(ORANGE);
                                ((JLabel) comp).setFont(getFont().deriveFont(Font.BOLD)); break;
                            case "Confirmée": case "Confirmee":
                                comp.setForeground(VERT_FONCE);
                                ((JLabel) comp).setFont(getFont().deriveFont(Font.BOLD)); break;
                            case "Annulée": case "Annulee":
                                comp.setForeground(ROUGE);
                                ((JLabel) comp).setFont(getFont().deriveFont(Font.BOLD)); break;
                        }
                    }
                }
                return comp;
            }
        });
    }

    
    private void configurerTable(JTable table, Color headerColor) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setSelectionBackground(VERT_CLAIR);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setBackground(GRIS_FOND);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v != null ? v.toString() : "");
                lbl.setOpaque(true);
                lbl.setBackground(headerColor);
                lbl.setForeground(BLANC);
                lbl.setFont(new Font("Arial", Font.BOLD, 11));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                if (sel) comp.setBackground(VERT_CLAIR);
                else     comp.setBackground(r % 2 == 0 ? BLANC : GRIS_LIGNE);
                return comp;
            }
        });
    }


    private void remplirFormulaireCommande(int row) {
        try {
            txtIdCommande.setText(modelCommande.getValueAt(row, 0).toString());

            Object dateCmd = modelCommande.getValueAt(row, 1);
            if (dateCmd != null) spinnerDateCommande.setValue(dateFormat.parse(dateCmd.toString()));

            Object statut = modelCommande.getValueAt(row, 2);
            if (statut != null) {
                for (int i = 0; i < cmbStatut.getItemCount(); i++) {
                    if (cmbStatut.getItemAt(i).equalsIgnoreCase(statut.toString())) {
                        cmbStatut.setSelectedIndex(i); break;
                    }
                }
            }

            Object datePrev = modelCommande.getValueAt(row, 3);
            if (datePrev != null && !datePrev.toString().isEmpty())
                spinnerDatePrevue.setValue(dateFormat.parse(datePrev.toString()));

            Object remise = modelCommande.getValueAt(row, 4);
            txtRemise.setText(remise != null ? remise.toString() : "0");

            Object idClientObj = modelCommande.getValueAt(row, 5);
            if (idClientObj != null) {
                int idClient = Integer.parseInt(idClientObj.toString());
                for (int i = 0; i < cmbClient.getItemCount(); i++) {
                    if (cmbClient.getItemAt(i).startsWith(idClient + " - ")) {
                        cmbClient.setSelectedIndex(i); break;
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void remplirFormulaireLigne(int row) {
        Object idProduitObj = modelLigneCommande.getValueAt(row, 1);
        if (idProduitObj != null) {
            int idProduit = Integer.parseInt(idProduitObj.toString());
            for (int i = 0; i < cmbProduit.getItemCount(); i++) {
                if (cmbProduit.getItemAt(i).startsWith(idProduit + " - ")) {
                    cmbProduit.setSelectedIndex(i); break;
                }
            }
        }
        txtQteCommande.setText(modelLigneCommande.getValueAt(row, 3).toString());
    }

    
    private Connection getCon() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    private void loadDataCommandes() {
        modelCommande.setRowCount(0);
        try (Connection c = getCon(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM commande ORDER BY id_commande DESC")) {
            while (rs.next()) {
                modelCommande.addRow(new Object[]{
                    rs.getInt("id_commande"), rs.getDate("date_commande"),
                    rs.getString("statut"),   rs.getDate("date_prevu"),
                    rs.getDouble("remise"),   rs.getInt("id_client")
                });
            }
        } catch (SQLException e) { showError(e); }
    }

    private void loadLignesCommande(int idCommande) {
        modelLigneCommande.setRowCount(0);
        String sql = "SELECT lc.id_commande, lc.id_produit, p.nom_produit, lc.qte_commande " +
                     "FROM ligne_commande lc JOIN produit p ON lc.id_produit = p.id_produit " +
                     "WHERE lc.id_commande = ?";

        try (Connection c = getCon(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCommande);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                modelLigneCommande.addRow(new Object[]{
                    rs.getInt("id_commande"), rs.getInt("id_produit"),
                    rs.getString("nom_produit"), rs.getInt("qte_commande")
                });
                count++;
            }
            lblTitreLignes.setText("📝 Lignes Commande #" + idCommande + " (" + count + " lignes)");
            lblTitreLignes.setForeground(BLEU);
        } catch (SQLException e) { showError(e); }
    }

    private void rechercher() {
        String recherche = txtRecherche.getText().trim();
        if (recherche.isEmpty()) { loadDataCommandes(); return; }

        try {
            int idRecherche = Integer.parseInt(recherche);
            modelCommande.setRowCount(0);
            try (Connection c = getCon();
                 PreparedStatement ps = c.prepareStatement("SELECT * FROM commande WHERE id_commande = ?")) {
                ps.setInt(1, idRecherche);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    modelCommande.addRow(new Object[]{
                        rs.getInt("id_commande"), rs.getDate("date_commande"),
                        rs.getString("statut"),   rs.getDate("date_prevu"),
                        rs.getDouble("remise"),   rs.getInt("id_client")
                    });
                    tableCommande.setRowSelectionInterval(0, 0);
                    remplirFormulaireCommande(0);
                    loadLignesCommande(idRecherche);
                } else {
                    showMsg("⚠️ Commande non trouvée!", "Attention", JOptionPane.WARNING_MESSAGE);
                    loadDataCommandes();
                }
            }
        } catch (NumberFormatException e) { showMsg("⚠️ ID invalide!", "Attention", JOptionPane.WARNING_MESSAGE); }
        catch (SQLException e) { showError(e); }
    }

    private void ajouterCommande() {
        if (txtIdCommande.getText().trim().isEmpty()) {
            showMsg("⚠️ ID Commande obligatoire!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }
        Integer idClient = getIdClientSelectionne();
        if (idClient == null) {
            showMsg("⚠️ Sélectionnez un client!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO commande (id_commande, date_commande, statut, date_prevu, remise, id_client) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, Integer.parseInt(txtIdCommande.getText().trim()));
            ps.setDate(2, new java.sql.Date(((Date) spinnerDateCommande.getValue()).getTime()));
            ps.setString(3, cmbStatut.getSelectedItem().toString());
            ps.setDate(4, new java.sql.Date(((Date) spinnerDatePrevue.getValue()).getTime()));
            ps.setDouble(5, Double.parseDouble(txtRemise.getText().trim()));
            ps.setInt(6, idClient);
            ps.executeUpdate();
            showMsg("✅ Commande ajoutée!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            loadDataCommandes(); viderChamps();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) showMsg("❌ Commande existe déjà!", "Erreur", JOptionPane.ERROR_MESSAGE);
            else showError(e);
        } catch (NumberFormatException e) { showMsg("⚠️ Valeurs invalides!", "Erreur", JOptionPane.WARNING_MESSAGE); }
    }

    private void modifierCommande() {
        if (txtIdCommande.getText().trim().isEmpty()) {
            showMsg("⚠️ Sélectionnez une commande!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }
        Integer idClient = getIdClientSelectionne();
        if (idClient == null) {
            showMsg("⚠️ Sélectionnez un client!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE commande SET date_commande=?, statut=?, date_prevu=?, remise=?, id_client=? WHERE id_commande=?")) {
            ps.setDate(1, new java.sql.Date(((Date) spinnerDateCommande.getValue()).getTime()));
            ps.setString(2, cmbStatut.getSelectedItem().toString());
            ps.setDate(3, new java.sql.Date(((Date) spinnerDatePrevue.getValue()).getTime()));
            ps.setDouble(4, Double.parseDouble(txtRemise.getText().trim()));
            ps.setInt(5, idClient);
            ps.setInt(6, Integer.parseInt(txtIdCommande.getText().trim()));
            if (ps.executeUpdate() > 0) {
                showMsg("✅ Commande modifiée!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                loadDataCommandes();
            }
        } catch (SQLException e) { showError(e); }
        catch (NumberFormatException e) { showMsg("⚠️ Valeurs invalides!", "Erreur", JOptionPane.WARNING_MESSAGE); }
    }

    private void supprimerCommande() {
        String id = txtIdCommande.getText().trim();
        if (id.isEmpty()) {
            showMsg("⚠️ Sélectionnez une commande!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "🗑️ Supprimer commande #" + id + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection c = null;
        try {
            c = getCon(); c.setAutoCommit(false);
            int idCmd = Integer.parseInt(id);

            PreparedStatement psL = c.prepareStatement("DELETE FROM ligne_commande WHERE id_commande = ?");
            psL.setInt(1, idCmd); psL.executeUpdate();

            PreparedStatement psC = c.prepareStatement("DELETE FROM commande WHERE id_commande = ?");
            psC.setInt(1, idCmd);
            if (psC.executeUpdate() > 0) {
                c.commit();
                showMsg("✅ Commande supprimée!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                loadDataCommandes(); modelLigneCommande.setRowCount(0);
                lblTitreLignes.setText("📝 Lignes de Commande");
                lblTitreLignes.setForeground(BLEU);
                viderChamps();
            } else { c.rollback(); }
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ex) {}
            showError(e);
        } finally {
            try { if (c != null) { c.setAutoCommit(true); c.close(); } } catch (SQLException ex) {}
        }
    }

    
    private String controlerStock(Connection con, int idProduit, int qteCommande) throws SQLException {

        String sqlStock = "SELECT d.id_depot, d.qte_disponible, d.lieu_depot " +
                          "FROM stocker s " +
                          "JOIN `depot` d ON s.id_depot = d.id_depot " +
                          "WHERE s.id_produit = ? " +
                          "ORDER BY d.qte_disponible DESC " +
                          "LIMIT 1";

        PreparedStatement ps = con.prepareStatement(sqlStock);
        ps.setInt(1, idProduit);
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) return "NO_DEPOT";

        int    idDepot       = rs.getInt("id_depot");
        int    qteDispo      = rs.getInt("qte_disponible");
        String lieuDepot     = rs.getString("lieu_depot");
        int    stockRestant  = qteDispo - qteCommande;

        
        if (qteCommande > qteDispo) {
            JOptionPane.showMessageDialog(this,
                "❌ STOCK INSUFFISANT!\n\n" +
                "══════════════════════════════════════\n" +
                "   Produit ID : " + idProduit + "\n" +
                "   Dépôt      : " + lieuDepot + " (#" + idDepot + ")\n" +
                "══════════════════════════════════════\n" +
                "   Stock disponible : " + qteDispo + " kg\n" +
                "   Quantité demandée: " + qteCommande + " kg\n" +
                "   Manque           : " + (qteCommande - qteDispo) + " kg\n" +
                "══════════════════════════════════════\n\n" +
                "⛔ Impossible de passer cette commande!",
                "Stock Insuffisant", JOptionPane.ERROR_MESSAGE);
            return "INSUFFISANT";
        }

        
        if (stockRestant <= 1000) {
            JOptionPane.showMessageDialog(this,
                "⚠️ STOCK ALERTE — Il faut passer une commande!\n\n" +
                "══════════════════════════════════════\n" +
                "   Produit ID : " + idProduit + "\n" +
                "   Dépôt      : " + lieuDepot + " (#" + idDepot + ")\n" +
                "══════════════════════════════════════\n" +
                "   Stock disponible  : " + qteDispo + " kg\n" +
                "   Quantité demandée : " + qteCommande + " kg\n" +
                "   Stock restant     : " + stockRestant + " kg\n" +
                "══════════════════════════════════════\n\n" +
                "📦 Le stock est très bas!\n" +
                "Veuillez passer une commande d'approvisionnement.\n\n" +
                "✅ La ligne sera quand même enregistrée.",
                "Stock Alerte — Commander", JOptionPane.WARNING_MESSAGE);
            return "ALERT_COMMANDER";
        }

        
        if (qteCommande < qteDispo && stockRestant < (qteDispo * 0.5)) {
            JOptionPane.showMessageDialog(this,
                "ℹ️ STOCK ALERTE — Début d'exploitation!\n\n" +
                "══════════════════════════════════════\n" +
                "   Produit ID : " + idProduit + "\n" +
                "   Dépôt      : " + lieuDepot + " (#" + idDepot + ")\n" +
                "══════════════════════════════════════\n" +
                "   Stock disponible  : " + qteDispo + " kg\n" +
                "   Quantité demandée : " + qteCommande + " kg\n" +
                "   Stock restant     : " + stockRestant + " kg\n" +
                "══════════════════════════════════════\n\n" +
                "📊 Le stock a commencé à être exploité.\n\n" +
                "✅ La ligne sera enregistrée.",
                "Stock — Début Exploitation", JOptionPane.INFORMATION_MESSAGE);
            return "ALERT_EXPLOITER";
        }

        return "OK";
    }

    
    private void diminuerStock(Connection con, int idProduit, int qteCommande) throws SQLException {
        String sql = "UPDATE `depot` d " +
                     "JOIN stocker s ON d.id_depot = s.id_depot " +
                     "SET d.qte_disponible = d.qte_disponible - ? " +
                     "WHERE s.id_produit = ? " +
                     "AND d.qte_disponible >= ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, qteCommande);
        ps.setInt(2, idProduit);
        ps.setInt(3, qteCommande);
        ps.executeUpdate();
    }

    private void restaurerStock(Connection con, int idProduit, int qteCommande) throws SQLException {
        String sql = "UPDATE `depot` d " +
                     "JOIN stocker s ON d.id_depot = s.id_depot " +
                     "SET d.qte_disponible = d.qte_disponible + ? " +
                     "WHERE s.id_produit = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, qteCommande);
        ps.setInt(2, idProduit);
        ps.executeUpdate();
    }

   
    private void ajouterLigneCommande() {
        if (txtIdCommande.getText().trim().isEmpty()) {
            showMsg("⚠️ ID Commande obligatoire!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }
        Integer idProduit = getIdProduitSelectionne();
        if (idProduit == null) {
            showMsg("⚠️ Sélectionnez un produit!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }
        if (txtQteCommande.getText().trim().isEmpty()) {
            showMsg("⚠️ Quantité obligatoire!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }

        int qteCommande;
        try {
            qteCommande = Integer.parseInt(txtQteCommande.getText().trim());
            if (qteCommande <= 0) {
                showMsg("⚠️ La quantité doit être positive!", "Attention", JOptionPane.WARNING_MESSAGE); return;
            }
        } catch (NumberFormatException e) {
            showMsg("⚠️ Quantité invalide!", "Erreur", JOptionPane.WARNING_MESSAGE); return;
        }

        Connection c = null;
        try {
            c = getCon();
            c.setAutoCommit(false);

            
            String resultat = controlerStock(c, idProduit, qteCommande);

            if (resultat.equals("INSUFFISANT")) {
                c.rollback(); return;
            }

            if (resultat.equals("NO_DEPOT")) {
                showMsg("⚠️ Aucun dépôt trouvé pour ce produit!\nVérifiez la table Stocker.",
                        "Attention", JOptionPane.WARNING_MESSAGE);
                c.rollback(); return;
            }

            
            int idCmd = Integer.parseInt(txtIdCommande.getText().trim());
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO ligne_commande (id_commande, id_produit, qte_commande) VALUES (?, ?, ?)");
            ps.setInt(1, idCmd);
            ps.setInt(2, idProduit);
            ps.setInt(3, qteCommande);
            ps.executeUpdate();

            diminuerStock(c, idProduit, qteCommande);

            c.commit();

            showMsg("✅ Ligne ajoutée!\nStock mis à jour: -" + qteCommande + " kg",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            loadLignesCommande(idCmd);
            cmbProduit.setSelectedIndex(0);
            txtQteCommande.setText("1");

        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ex) {}
            if (e.getMessage().contains("Duplicate"))
                showMsg("❌ Ligne existe déjà!", "Erreur", JOptionPane.ERROR_MESSAGE);
            else if (e.getMessage().contains("foreign key"))
                showMsg("❌ Commande/Produit inexistant!", "Erreur", JOptionPane.ERROR_MESSAGE);
            else showError(e);
        } finally {
            try { if (c != null) { c.setAutoCommit(true); c.close(); } } catch (SQLException ex) {}
        }
    }

   
    private void modifierLigneCommande() {
        int row = tableLigneCommande.getSelectedRow();
        if (row < 0) {
            showMsg("⚠️ Sélectionnez une ligne!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }

        int idCmd     = Integer.parseInt(modelLigneCommande.getValueAt(row, 0).toString());
        int idProdOld = Integer.parseInt(modelLigneCommande.getValueAt(row, 1).toString());
        int oldQte    = Integer.parseInt(modelLigneCommande.getValueAt(row, 3).toString());

        Integer idProdNew = getIdProduitSelectionne();
        if (idProdNew == null) {
            showMsg("⚠️ Sélectionnez un produit!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }
        if (txtQteCommande.getText().trim().isEmpty()) {
            showMsg("⚠️ Quantité obligatoire!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }

        int newQte;
        try {
            newQte = Integer.parseInt(txtQteCommande.getText().trim());
            if (newQte <= 0) {
                showMsg("⚠️ Quantité positive!", "Attention", JOptionPane.WARNING_MESSAGE); return;
            }
        } catch (NumberFormatException e) {
            showMsg("⚠️ Quantité invalide!", "Erreur", JOptionPane.WARNING_MESSAGE); return;
        }

        Connection c = null;
        try {
            c = getCon();
            c.setAutoCommit(false);

            if (idProdOld != idProdNew) {
              
                restaurerStock(c, idProdOld, oldQte);

              
                String resultat = controlerStock(c, idProdNew, newQte);
                if (resultat.equals("INSUFFISANT")) { c.rollback(); return; }
                if (resultat.equals("NO_DEPOT")) {
                    showMsg("⚠️ Aucun dépôt pour ce nouveau produit!", "Attention", JOptionPane.WARNING_MESSAGE);
                    c.rollback(); return;
                }

                
                PreparedStatement psDel = c.prepareStatement(
                    "DELETE FROM ligne_commande WHERE id_commande = ? AND id_produit = ?");
                psDel.setInt(1, idCmd); psDel.setInt(2, idProdOld);
                psDel.executeUpdate();

                
                PreparedStatement psIns = c.prepareStatement(
                    "INSERT INTO ligne_commande (id_commande, id_produit, qte_commande) VALUES (?, ?, ?)");
                psIns.setInt(1, idCmd); psIns.setInt(2, idProdNew); psIns.setInt(3, newQte);
                psIns.executeUpdate();

                
                diminuerStock(c, idProdNew, newQte);

            } else {
                
                int diffQte = newQte - oldQte;

                if (diffQte > 0) {
                    
                    String resultat = controlerStock(c, idProdNew, diffQte);
                    if (resultat.equals("INSUFFISANT")) { c.rollback(); return; }
                    if (resultat.equals("NO_DEPOT")) {
                        showMsg("⚠️ Aucun dépôt pour ce produit!", "Attention", JOptionPane.WARNING_MESSAGE);
                        c.rollback(); return;
                    }
                    diminuerStock(c, idProdNew, diffQte);

                } else if (diffQte < 0) {
                    
                    restaurerStock(c, idProdNew, Math.abs(diffQte));
                }
                

                PreparedStatement psUpd = c.prepareStatement(
                    "UPDATE ligne_commande SET qte_commande = ? WHERE id_commande = ? AND id_produit = ?");
                psUpd.setInt(1, newQte); psUpd.setInt(2, idCmd); psUpd.setInt(3, idProdOld);
                psUpd.executeUpdate();
            }

            c.commit();
            showMsg("✅ Ligne modifiée!\nStock mis à jour automatiquement.",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            loadLignesCommande(idCmd);
            cmbProduit.setSelectedIndex(0);
            txtQteCommande.setText("1");

        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ex) {}
            if (e.getMessage().contains("Duplicate"))
                showMsg("❌ Cette ligne existe déjà!", "Erreur", JOptionPane.ERROR_MESSAGE);
            else showError(e);
        } finally {
            try { if (c != null) { c.setAutoCommit(true); c.close(); } } catch (SQLException ex) {}
        }
    }

    
    private void supprimerLigneCommande() {
        int row = tableLigneCommande.getSelectedRow();
        if (row < 0) {
            showMsg("⚠️ Sélectionnez une ligne!", "Attention", JOptionPane.WARNING_MESSAGE); return;
        }

        int idCmd  = Integer.parseInt(modelLigneCommande.getValueAt(row, 0).toString());
        int idProd = Integer.parseInt(modelLigneCommande.getValueAt(row, 1).toString());
        int qte    = Integer.parseInt(modelLigneCommande.getValueAt(row, 3).toString());

        if (JOptionPane.showConfirmDialog(this,
                "🗑️ Supprimer cette ligne?\n\n" +
                "Le stock sera restauré: +" + qte + " kg",
                "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        Connection c = null;
        try {
            c = getCon();
            c.setAutoCommit(false);

            PreparedStatement ps = c.prepareStatement(
                "DELETE FROM ligne_commande WHERE id_commande = ? AND id_produit = ?");
            ps.setInt(1, idCmd); ps.setInt(2, idProd);

            if (ps.executeUpdate() > 0) {
                
                restaurerStock(c, idProd, qte);
                c.commit();

                showMsg("✅ Ligne supprimée!\nStock restauré: +" + qte + " kg",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                loadLignesCommande(idCmd);
            } else {
                c.rollback();
            }

        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ex) {}
            showError(e);
        } finally {
            try { if (c != null) { c.setAutoCommit(true); c.close(); } } catch (SQLException ex) {}
        }
    }

    private void viderChamps() {
        txtIdCommande.setText("");
        spinnerDateCommande.setValue(new Date());
        spinnerDatePrevue.setValue(new Date());
        cmbStatut.setSelectedIndex(0);
        txtRemise.setText("0");
        cmbClient.setSelectedIndex(0);
        lblInfoClient.setText("<html><i>Sélectionnez un client...</i></html>");
        lblInfoClient.setBackground(new Color(245, 245, 255));
        cmbProduit.setSelectedIndex(0);
        lblInfoProduit.setText("<html><i>Sélectionnez un produit...</i></html>");
        lblInfoProduit.setBackground(new Color(245, 255, 245));
        txtQteCommande.setText("1");
        tableCommande.clearSelection();
        tableLigneCommande.clearSelection();
    }

    private void showMsg(String msg, String titre, int type) {
        JOptionPane.showMessageDialog(this, msg, titre, type);
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "❌ ERREUR: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GestionCommandes().setVisible(true));
    }
}