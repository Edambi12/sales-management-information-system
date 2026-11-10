package projet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GestionPaiement extends JFrame {

    // JDBC settings are loaded from config/database.properties (see DatabaseConfig).
    private JTable tablePaiement;
    private DefaultTableModel modelPaiement;

    private JTable tableFacture;
    private DefaultTableModel modelFacture;

    private JTable tableDetailCommande;
    private DefaultTableModel modelDetailCommande;

    private JTextField txtIdPaie, txtMontant, txtRecherche;
    private JSpinner spinnerDate;
    private JComboBox<String> cmbStatut, cmbMode, cmbFacture;
    private JLabel lblStatus, lblInfoFacture;

    private JTextField txtIdFacture, txtQteFacture, txtMontantFacture;
    private JSpinner spinnerDateFacture;
    private JComboBox<String> cmbStatutFacture, cmbCommande;
    private JLabel lblInfoCommande, lblDetailFacture;
    private JTextArea txtDetailFacture;

    private JPanel panelFormulaire;
    private JPanel panelIdPaie, panelMontant, panelDate, panelStatut, panelMode, panelFacture;
    private JPanel panelIdFacture, panelDateFacture, panelStatutFacture, panelCommande,
                   panelQteFacture, panelMontantFacture, panelDetailFacture;

    private JPanel panelBoutons;
    private JButton btnAction, btnSecondaire;
    private JLabel formTitle;

    private String modeActuel = "CREER_FACTURE";

    private final Color VERT_FONCE  = new Color(27, 94, 32);
    private final Color VERT_MOYEN  = new Color(56, 142, 60);
    private final Color VERT_CLAIR  = new Color(76, 175, 80);
    private final Color VERT_HOVER  = new Color(46, 125, 50);
    private final Color VERT_HEADER = new Color(33, 97, 47);
    private final Color BLEU_HEADER = new Color(25, 118, 210);
    private final Color ORANGE      = new Color(255, 152, 0);
    private final Color ROUGE       = new Color(244, 67, 54);
    private final Color VIOLET      = new Color(156, 39, 176);
    private final Color TEAL        = new Color(0, 150, 136);
    private final Color GRIS_FOND   = new Color(245, 245, 245);
    private final Color GRIS_CLAIR  = new Color(240, 240, 240);
    private final Color GRIS_TABLE  = new Color(250, 250, 250);
    private final Color GRIS_LIGNE  = new Color(235, 235, 235);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public GestionPaiement() {
        setTitle("SOKTRAPA - Gestion Factures & Paiements");
        setSize(1500, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        chargerDriver();

        add(sidebar(), BorderLayout.WEST);
        add(mainPanel(), BorderLayout.CENTER);
        add(statusBar(), BorderLayout.SOUTH);

        afficherModeCreerFacture();
        chargerFactures();
    }

    private void chargerDriver() {
        try {
            if (!DatabaseConfig.loadDriver()) throw new ClassNotFoundException(DatabaseConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "[ERREUR] Driver MySQL introuvable!",
                "Erreur Driver", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(230, 900));
        p.setBackground(VERT_FONCE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("[ SOKTRAPA ]");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 22));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Factures & Paiements");
        subtitle.setForeground(new Color(200, 230, 200));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 2));
        sep.setForeground(VERT_CLAIR);

        JButton btnCreerFacture = createMenuButton("📝 Créer Facture");

        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(200, 1));
        sep2.setForeground(new Color(255, 255, 255, 60));

        JButton btnConsulter  = createMenuButton("📋 Consulter Paiements");
        JButton btnAjouter    = createMenuButton("➕ Nouvel Encaissement");
        JButton btnModifier   = createMenuButton("✏️ Modifier Paiement");
        JButton btnSupprimer  = createMenuButton("🗑️ Supprimer Paiement");
        JButton btnRechercher = createMenuButton("🔍 Rechercher");
        JButton btnActualiser = createMenuButton("🔄 Actualiser");
        JButton btnMenu       = createMenuButton("⬅️ Menu Principal");

        btnCreerFacture.addActionListener(e -> afficherModeCreerFacture());
        btnConsulter .addActionListener(e -> afficherModeConsulter());
        btnAjouter   .addActionListener(e -> afficherModeAjouter());
        btnModifier  .addActionListener(e -> afficherModeModifier());
        btnSupprimer .addActionListener(e -> afficherModeSupprimer());
        btnRechercher.addActionListener(e -> afficherModeRechercher());
        btnActualiser.addActionListener(e -> {
            if (modeActuel.equals("CREER_FACTURE")) {
                chargerFactures();
                chargerCommandesCombo();
            } else {
                consulterPaiements();
                chargerFactures();
            }
            updateStatus("[OK] Données actualisées");
        });
        btnMenu.addActionListener(e -> retourMenu());

        p.add(Box.createVerticalStrut(30));
        p.add(logo);
        p.add(Box.createVerticalStrut(5));
        p.add(subtitle);
        p.add(Box.createVerticalStrut(20));
        p.add(sep);
        p.add(Box.createVerticalStrut(30));
        p.add(btnCreerFacture);
        p.add(Box.createVerticalStrut(15));
        p.add(sep2);
        p.add(Box.createVerticalStrut(15));
        p.add(btnConsulter);
        p.add(Box.createVerticalStrut(10));
        p.add(btnAjouter);
        p.add(Box.createVerticalStrut(10));
        p.add(btnModifier);
        p.add(Box.createVerticalStrut(10));
        p.add(btnSupprimer);
        p.add(Box.createVerticalStrut(10));
        p.add(btnRechercher);
        p.add(Box.createVerticalStrut(30));
        p.add(btnActualiser);
        p.add(Box.createVerticalGlue());
        p.add(btnMenu);
        p.add(Box.createVerticalStrut(30));

        return p;
    }

    private JPanel mainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(GRIS_FOND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(VERT_MOYEN);
        header.setPreferredSize(new Dimension(0, 60));

        JLabel title = new JLabel("   💰 Gestion Factures & Paiements");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        header.add(title, BorderLayout.CENTER);
        main.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 15, 15));
        center.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        center.setBackground(GRIS_FOND);

        center.add(formPanel());
        center.add(tablesPanel());

        main.add(center, BorderLayout.CENTER);
        return main;
    }

    private JPanel formPanel() {
        panelFormulaire = new JPanel();
        panelFormulaire.setLayout(new BoxLayout(panelFormulaire, BoxLayout.Y_AXIS));
        panelFormulaire.setBackground(GRIS_CLAIR);
        panelFormulaire.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        formTitle = new JLabel("📝 Créer Facture");
        formTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        formTitle.setForeground(TEAL);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtIdPaie    = new JTextField();
        txtMontant   = new JTextField();
        txtRecherche = new JTextField();

        SpinnerDateModel dm = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spinnerDate = new JSpinner(dm);
        spinnerDate.setEditor(new JSpinner.DateEditor(spinnerDate, "yyyy-MM-dd"));
        spinnerDate.setFont(new Font("Arial", Font.PLAIN, 14));

        String[] statuts = {"En attente", "Payé", "Partiel", "Annulé", "Remboursé"};
        cmbStatut = new JComboBox<>(statuts);
        cmbStatut.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbStatut.setBackground(Color.WHITE);

        String[] modes = {"Espèces", "Chèque", "Virement", "Carte bancaire", "Effet", "Autre"};
        cmbMode = new JComboBox<>(modes);
        cmbMode.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbMode.setBackground(Color.WHITE);

        cmbFacture = new JComboBox<>();
        cmbFacture.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbFacture.setBackground(Color.WHITE);
        cmbFacture.addActionListener(e -> afficherInfoFacture());

        lblInfoFacture = new JLabel("<html><i>Sélectionnez une facture...</i></html>");
        lblInfoFacture.setFont(new Font("Arial", Font.PLAIN, 10));
        lblInfoFacture.setForeground(new Color(100, 100, 100));
        lblInfoFacture.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblInfoFacture.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 255), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        lblInfoFacture.setOpaque(true);
        lblInfoFacture.setBackground(new Color(245, 245, 255));
        lblInfoFacture.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        txtIdFacture = new JTextField();
        txtQteFacture = new JTextField();
        txtQteFacture.setEditable(false);
        txtQteFacture.setBackground(new Color(240, 248, 255));

        txtMontantFacture = new JTextField();
        txtMontantFacture.setEditable(false);
        txtMontantFacture.setBackground(new Color(240, 248, 255));

        SpinnerDateModel dmF = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spinnerDateFacture = new JSpinner(dmF);
        spinnerDateFacture.setEditor(new JSpinner.DateEditor(spinnerDateFacture, "yyyy-MM-dd"));
        spinnerDateFacture.setFont(new Font("Arial", Font.PLAIN, 14));

        String[] statutsFacture = {"En attente", "Payée", "Annulée"};
        cmbStatutFacture = new JComboBox<>(statutsFacture);
        cmbStatutFacture.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbStatutFacture.setBackground(Color.WHITE);

        cmbStatutFacture.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String val = value.toString();
                    if (isSelected) {
                        setBackground(new Color(200, 230, 201));
                        setForeground(VERT_FONCE);
                    } else {
                        setBackground(Color.WHITE);
                        switch (val) {
                            case "En attente": setForeground(ORANGE);     setText("⏳ " + val); break;
                            case "Payée":      setForeground(VERT_FONCE); setText("✅ " + val); break;
                            case "Annulée":    setForeground(ROUGE);      setText("❌ " + val); break;
                        }
                    }
                }
                return this;
            }
        });

        cmbCommande = new JComboBox<>();
        cmbCommande.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbCommande.setBackground(Color.WHITE);
        cmbCommande.addActionListener(e -> calculerFactureDetaillee());

        lblInfoCommande = new JLabel("<html><i>Sélectionnez une commande...</i></html>");
        lblInfoCommande.setFont(new Font("Arial", Font.PLAIN, 10));
        lblInfoCommande.setForeground(new Color(100, 100, 100));
        lblInfoCommande.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblInfoCommande.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 255, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        lblInfoCommande.setOpaque(true);
        lblInfoCommande.setBackground(new Color(245, 255, 245));
        lblInfoCommande.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        txtDetailFacture = new JTextArea(6, 30);
        txtDetailFacture.setEditable(false);
        txtDetailFacture.setFont(new Font("Consolas", Font.PLAIN, 10));
        txtDetailFacture.setBackground(new Color(255, 255, 240));
        txtDetailFacture.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 200, 100), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        txtDetailFacture.setText("📋 Détail de la facture apparaîtra ici...");

        panelIdPaie       = createFieldPanel("🔢 ID Paiement *", txtIdPaie);
        panelMontant      = createFieldPanel("💵 Montant (TND) *", txtMontant);
        panelDate         = createSpinnerPanel("📅 Date Paiement", spinnerDate);
        panelStatut       = createComboPanel("📊 Statut Paiement", cmbStatut);
        panelMode         = createComboPanel("💳 Mode de Paiement", cmbMode);
        panelFacture      = createComboPanel("📄 Facture Associée *", cmbFacture);

        panelIdFacture      = createFieldPanel("🔢 ID Facture *", txtIdFacture);
        panelDateFacture    = createSpinnerPanel("📅 Date Facture", spinnerDateFacture);
        panelStatutFacture  = createComboPanel("📊 Statut Facture", cmbStatutFacture);
        panelCommande       = createComboPanel("🛒 Commande Confirmée *", cmbCommande);
        panelQteFacture     = createFieldPanel("📦 Quantité Totale (auto)", txtQteFacture);
        panelMontantFacture = createFieldPanel("💰 NET TTC (auto)", txtMontantFacture);

        panelDetailFacture = new JPanel(new BorderLayout(5, 3));
        panelDetailFacture.setBackground(GRIS_CLAIR);
        panelDetailFacture.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblDetail = new JLabel("📋 Détail Facture");
        lblDetail.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        lblDetail.setForeground(ORANGE);
        JScrollPane scrollDetail = new JScrollPane(txtDetailFacture);
        scrollDetail.setPreferredSize(new Dimension(300, 160));
        panelDetailFacture.add(lblDetail, BorderLayout.NORTH);
        panelDetailFacture.add(scrollDetail, BorderLayout.CENTER);

        btnAction = createActionButton("📝 Créer Facture", TEAL);
        btnAction.addActionListener(e -> executerAction());

        btnSecondaire = createActionButton("🧹 Vider", new Color(158, 158, 158));
        btnSecondaire.addActionListener(e -> clearFields());

        construireFormulaire();
        return panelFormulaire;
    }

    private void construireFormulaire() {
        panelFormulaire.removeAll();
        panelFormulaire.add(formTitle);
        panelFormulaire.add(Box.createVerticalStrut(15));

        switch (modeActuel) {
            case "CREER_FACTURE":
                JLabel lblInfoFact = new JLabel("<html>" +
                    "📝 <b>Création de Facture Détaillée</b><br>" +
                    "<i>Commandes confirmées sans facture uniquement</i></html>");
                lblInfoFact.setFont(new Font("Arial", Font.PLAIN, 11));
                lblInfoFact.setForeground(new Color(80, 80, 80));
                lblInfoFact.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelFormulaire.add(lblInfoFact);
                panelFormulaire.add(Box.createVerticalStrut(10));
                panelFormulaire.add(panelIdFacture);
                panelFormulaire.add(Box.createVerticalStrut(6));
                panelFormulaire.add(panelDateFacture);
                panelFormulaire.add(Box.createVerticalStrut(6));
                panelFormulaire.add(panelStatutFacture);
                panelFormulaire.add(Box.createVerticalStrut(6));
                panelFormulaire.add(panelCommande);
                panelFormulaire.add(Box.createVerticalStrut(4));
                panelFormulaire.add(lblInfoCommande);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelDetailFacture);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelQteFacture);
                panelFormulaire.add(Box.createVerticalStrut(6));
                panelFormulaire.add(panelMontantFacture);
                break;

            case "CONSULTER":
                JLabel lblInfo = new JLabel("<html><center>" +
                    "📋 <b>Mode Consultation</b><br><br>" +
                    "Consultez tous les paiements<br>dans le tableau.</center></html>");
                lblInfo.setFont(new Font("Arial", Font.PLAIN, 13));
                lblInfo.setForeground(new Color(80, 80, 80));
                lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelFormulaire.add(lblInfo);
                break;

            case "AJOUTER":
                JLabel lblAide = new JLabel("<html>" +
                    "➕ <b>Nouvel Encaissement</b><br>" +
                    "<i style='color:gray;'>Seules les factures non entièrement payées sont affichées.<br>" +
                    "Le montant restant est indiqué dans la liste.</i></html>");
                lblAide.setFont(new Font("Arial", Font.PLAIN, 10));
                lblAide.setForeground(new Color(60, 60, 60));
                lblAide.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelFormulaire.add(lblAide);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelIdPaie);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelMontant);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelDate);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelStatut);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelMode);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelFacture);
                panelFormulaire.add(Box.createVerticalStrut(5));
                panelFormulaire.add(lblInfoFacture);
                break;

            case "MODIFIER":
                JLabel lblModif = new JLabel("<html>✏️ <i>Sélectionnez un paiement à modifier.</i></html>");
                lblModif.setFont(new Font("Arial", Font.PLAIN, 11));
                lblModif.setForeground(ORANGE);
                lblModif.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelFormulaire.add(lblModif);
                panelFormulaire.add(Box.createVerticalStrut(12));
                panelFormulaire.add(panelIdPaie);
                txtIdPaie.setEditable(false);
                txtIdPaie.setBackground(new Color(240, 240, 240));
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelMontant);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelDate);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelStatut);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelMode);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelFacture);
                panelFormulaire.add(Box.createVerticalStrut(5));
                panelFormulaire.add(lblInfoFacture);
                break;

            case "SUPPRIMER":
                JLabel lblSupp = new JLabel("<html>🗑️ <i>Sélectionnez un paiement à supprimer.</i></html>");
                lblSupp.setFont(new Font("Arial", Font.PLAIN, 11));
                lblSupp.setForeground(ROUGE);
                lblSupp.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelFormulaire.add(lblSupp);
                panelFormulaire.add(Box.createVerticalStrut(15));
                panelFormulaire.add(panelIdPaie);
                txtIdPaie.setEditable(false);
                txtIdPaie.setBackground(new Color(240, 240, 240));
                break;

            case "RECHERCHER":
                panelFormulaire.add(createFieldPanel("🔍 ID Paiement à rechercher", txtRecherche));
                break;
        }

        panelFormulaire.add(Box.createVerticalStrut(15));
        panelFormulaire.add(btnAction);
        panelFormulaire.add(Box.createVerticalStrut(8));
        panelFormulaire.add(btnSecondaire);
        panelFormulaire.add(Box.createVerticalGlue());
        panelFormulaire.revalidate();
        panelFormulaire.repaint();
    }

    private void afficherModeCreerFacture() {
        modeActuel = "CREER_FACTURE";
        formTitle.setText("📝 Créer Facture Détaillée");
        formTitle.setForeground(TEAL);
        btnAction.setText("📝 Créer Facture");
        btnAction.setBackground(TEAL);
        btnSecondaire.setText("🧹 Vider Formulaire");
        clearFields();
        txtIdFacture.setEditable(true);
        txtIdFacture.setBackground(Color.WHITE);
        chargerCommandesCombo();
        construireFormulaire();
        updateStatus("[📝] Mode: Création de facture");
    }

    private void afficherModeConsulter() {
        modeActuel = "CONSULTER";
        formTitle.setText("📋 Consultation Paiements");
        formTitle.setForeground(BLEU_HEADER);
        btnAction.setText("📋 Consulter Tous");
        btnAction.setBackground(BLEU_HEADER);
        btnSecondaire.setText("🧹 Vider Sélection");
        clearFields();
        txtIdPaie.setEditable(true);
        txtIdPaie.setBackground(Color.WHITE);
        construireFormulaire();
        consulterPaiements();
        updateStatus("[>>] Mode: Consultation des paiements");
    }

    private void afficherModeAjouter() {
        modeActuel = "AJOUTER";
        formTitle.setText("➕ Nouvel Encaissement");
        formTitle.setForeground(VERT_MOYEN);
        btnAction.setText("➕ Enregistrer Paiement");
        btnAction.setBackground(VERT_MOYEN);
        btnSecondaire.setText("🧹 Vider Formulaire");
        clearFields();
        txtIdPaie.setEditable(true);
        txtIdPaie.setBackground(Color.WHITE);
        chargerFacturesCombo();
        construireFormulaire();
        updateStatus("[+] Mode: Nouvel encaissement");
    }

    private void afficherModeModifier() {
        modeActuel = "MODIFIER";
        formTitle.setText("✏️ Modifier Paiement");
        formTitle.setForeground(ORANGE);
        btnAction.setText("✏️ Valider Modification");
        btnAction.setBackground(ORANGE);
        btnSecondaire.setText("🧹 Annuler");
        chargerFacturesCombo();
        construireFormulaire();
        updateStatus("[#] Mode: Modification paiement");
    }

    private void afficherModeSupprimer() {
        modeActuel = "SUPPRIMER";
        formTitle.setText("🗑️ Supprimer Paiement");
        formTitle.setForeground(ROUGE);
        btnAction.setText("🗑️ Confirmer Suppression");
        btnAction.setBackground(ROUGE);
        btnSecondaire.setText("🧹 Annuler");
        construireFormulaire();
        updateStatus("[-] Mode: Suppression paiement");
    }

    private void afficherModeRechercher() {
        modeActuel = "RECHERCHER";
        formTitle.setText("🔍 Rechercher Paiement");
        formTitle.setForeground(VIOLET);
        btnAction.setText("🔍 Rechercher");
        btnAction.setBackground(VIOLET);
        btnSecondaire.setText("📋 Afficher Tous");
        clearFields();
        construireFormulaire();
        updateStatus("[?] Mode: Recherche paiement");
    }

    private void executerAction() {
        switch (modeActuel) {
            case "CREER_FACTURE": creerFacture();       break;
            case "CONSULTER":     consulterPaiements();  break;
            case "AJOUTER":       ajouterPaiement();    break;
            case "MODIFIER":      modifierPaiement();   break;
            case "SUPPRIMER":     supprimerPaiement();  break;
            case "RECHERCHER":    rechercherPaiement(); break;
        }
    }

 
    private void chargerCommandesCombo() {
        cmbCommande.removeAllItems();
        cmbCommande.addItem("-- Sélectionner une commande confirmée --");

        String sql = "SELECT c.id_commande, c.date_commande, c.statut, c.remise, " +
                     "cl.nom_client, cl.prenom_client " +
                     "FROM commande c " +
                     "LEFT JOIN client cl ON c.id_client = cl.id_client " +
                     "WHERE (c.statut = 'Confirmée' OR c.statut = 'Confirmee') " +
                     "AND c.id_commande NOT IN " +
                     "  (SELECT id_commande FROM facture WHERE id_commande IS NOT NULL) " +
                     "ORDER BY c.id_commande DESC";

        try (Connection con = getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                int    id     = rs.getInt("id_commande");
                String nom    = rs.getString("nom_client");
                String prenom = rs.getString("prenom_client");
                String client = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
                cmbCommande.addItem(id + " - " + client.trim() + " (Confirmée)");
            }

            updateStatus(count == 0 ?
                "[!] Toutes les commandes confirmées ont déjà une facture" :
                "[📝] " + count + " commande(s) disponible(s) pour facturation");

        } catch (SQLException e) {
            updateStatus("[!] Impossible de charger les commandes confirmées");
            e.printStackTrace();
        }
    }

    
    private void calculerFactureDetaillee() {
        String selection = (String) cmbCommande.getSelectedItem();

        if (selection == null || selection.startsWith("--")) {
            lblInfoCommande.setText("<html><i>Sélectionnez une commande...</i></html>");
            lblInfoCommande.setBackground(new Color(245, 255, 245));
            txtQteFacture.setText("");
            txtMontantFacture.setText("");
            txtDetailFacture.setText("📋 Détail de la facture apparaîtra ici...");
            return;
        }

        Integer idCommande = getIdCommandeSelectionnee();
        if (idCommande == null) return;

        String sqlDetail = "SELECT p.id_produit, p.nom_produit, p.prix_produit, " +
                     "lc.qte_commande, (p.prix_produit * lc.qte_commande) as sous_total " +
                     "FROM ligne_commande lc " +
                     "JOIN produit p ON lc.id_produit = p.id_produit " +
                     "WHERE lc.id_commande = ? ORDER BY p.nom_produit";

        String sqlCommande = "SELECT c.*, cl.nom_client, cl.prenom_client " +
                            "FROM commande c LEFT JOIN client cl ON c.id_client = cl.id_client " +
                            "WHERE c.id_commande = ?";

        try (Connection con = getCon()) {

            PreparedStatement psCmd = con.prepareStatement(sqlCommande);
            psCmd.setInt(1, idCommande);
            ResultSet rsCmd = psCmd.executeQuery();

            String infoClient = "";
            double remise = 0;
            if (rsCmd.next()) {
                String nom    = rsCmd.getString("nom_client");
                String prenom = rsCmd.getString("prenom_client");
                infoClient = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
                remise = rsCmd.getDouble("remise");
            }

            PreparedStatement psDetail = con.prepareStatement(sqlDetail);
            psDetail.setInt(1, idCommande);
            ResultSet rs = psDetail.executeQuery();

            StringBuilder detail = new StringBuilder();
            detail.append("══════════════════════════════════════════════\n");
            detail.append("       FACTURE DÉTAILLÉE - Commande #").append(idCommande).append("\n");
            detail.append("══════════════════════════════════════════════\n");
            detail.append(" Client: ").append(infoClient.trim()).append("\n");
            detail.append("══════════════════════════════════════════════\n");
            detail.append(" PRODUIT          QTÉ    PRIX U.    TOTAL\n");
            detail.append("──────────────────────────────────────────────\n");

            int    qteTotale   = 0;
            double montantBrut = 0;
            int    nbLignes    = 0;

            while (rs.next()) {
                nbLignes++;
                String nomProduit  = rs.getString("nom_produit");
                double prixProduit = rs.getDouble("prix_produit");
                int    qte         = rs.getInt("qte_commande");
                double sousTotal   = rs.getDouble("sous_total");

                qteTotale   += qte;
                montantBrut += sousTotal;

                String nomFormate = nomProduit.length() > 15 ?
                    nomProduit.substring(0, 12) + "..." : nomProduit;

                detail.append(String.format(" %-15s %4d x %8.2f = %9.2f\n",
                    nomFormate, qte, prixProduit, sousTotal));
            }

            if (nbLignes == 0) detail.append("   Aucune ligne de commande trouvée\n");

            detail.append("──────────────────────────────────────────────\n");
            detail.append(String.format(" MONTANT BRUT:                %11.2f TND\n", montantBrut));

            double montantRemise = 0;
            if (remise > 0) {
                montantRemise = montantBrut * (remise / 100);
                detail.append(String.format(" REMISE (%.0f%%):               -%10.2f TND\n", remise, montantRemise));
            }

            double netCommercial = montantBrut - montantRemise;
            detail.append("──────────────────────────────────────────────\n");
            detail.append(String.format(" NET COMMERCIAL:              %11.2f TND\n", netCommercial));

            double tauxTVA    = 7.0;
            double montantTVA = netCommercial * (tauxTVA / 100);
            detail.append(String.format(" TVA (%.0f%%):                   %11.2f TND\n", tauxTVA, montantTVA));

            double netTTC = netCommercial + montantTVA;
            detail.append("══════════════════════════════════════════════\n");
            detail.append(String.format(" 💰 NET TTC:                  %11.2f TND\n", netTTC));
            detail.append("══════════════════════════════════════════════\n");

            txtQteFacture.setText(String.valueOf(qteTotale));
            txtMontantFacture.setText(String.format("%.2f", netTTC));
            txtDetailFacture.setText(detail.toString());

            lblInfoCommande.setText("<html><div style='font-size:9px;'>" +
                "<b>🛒 Commande #" + idCommande + "</b> | Client: " + infoClient.trim() +
                " | " + nbLignes + " produit(s)</div></html>");
            lblInfoCommande.setBackground(new Color(232, 255, 232));

            updateStatus("[OK] NET TTC=" + String.format("%.2f TND", netTTC));

        } catch (SQLException e) {
            lblInfoCommande.setText("<html><i>Erreur calcul...</i></html>");
            txtDetailFacture.setText("Erreur:\n" + e.getMessage());
            showError(e);
        }
    }

    
    private void creerFacture() {
        if (txtIdFacture.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ L'ID Facture est obligatoire!",
                "Champ requis", JOptionPane.WARNING_MESSAGE);
            txtIdFacture.requestFocus();
            return;
        }

        Integer idCommande = getIdCommandeSelectionnee();
        if (idCommande == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Veuillez sélectionner une commande!",
                "Commande requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtQteFacture.getText().trim().isEmpty() || txtMontantFacture.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez une commande valide!",
                "Calcul requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "📝 CONFIRMER LA CRÉATION?\n\n" +
            "   ID Facture : " + txtIdFacture.getText() + "\n" +
            "   Commande   : #" + idCommande + "\n" +
            "   NET TTC    : " + txtMontantFacture.getText() + " TND",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "INSERT INTO facture (id_facture, date_facture, montant, qte, statut, id_commande) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            int    idFacture = Integer.parseInt(txtIdFacture.getText().trim());
            int    qte       = Integer.parseInt(txtQteFacture.getText().trim());
            double montant   = Double.parseDouble(txtMontantFacture.getText().trim().replace(",", "."));

            ps.setInt(1, idFacture);
            ps.setDate(2, new java.sql.Date(((Date) spinnerDateFacture.getValue()).getTime()));
            ps.setDouble(3, montant);
            ps.setInt(4, qte);
            ps.setString(5, cmbStatutFacture.getSelectedItem().toString());
            ps.setInt(6, idCommande);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                "✅ Facture #" + idFacture + " créée!\nNET TTC: " + String.format("%.2f TND", montant),
                "Succès", JOptionPane.INFORMATION_MESSAGE);

            chargerFactures();
            clearFields();
            chargerCommandesCombo();
            updateStatus("[OK] Facture #" + idFacture + " créée");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Valeurs invalides!", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                JOptionPane.showMessageDialog(this, "❌ Cette facture existe déjà!", "Doublon", JOptionPane.ERROR_MESSAGE);
            } else if (e.getMessage().contains("foreign key")) {
                JOptionPane.showMessageDialog(this, "❌ La commande n'existe pas!", "Référence", JOptionPane.ERROR_MESSAGE);
            } else { showError(e); }
        }
    }

   
    private JPanel tablesPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 10));
        p.setBackground(GRIS_FOND);
        p.add(tablePaiementPanel());
        p.add(tableFacturePanel());
        return p;
    }

    private JPanel tablePaiementPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GRIS_CLAIR);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel tableTitle = new JLabel("💰 Liste des Paiements");
        tableTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        tableTitle.setForeground(VERT_MOYEN);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"ID Paie", "Montant (TND)", "Date", "Statut", "Mode", "ID Facture", "Reste (TND)"};
        modelPaiement = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablePaiement = new JTable(modelPaiement);
        styliserTablePaiement(tablePaiement, VERT_HEADER);

        tablePaiement.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablePaiement.getSelectedRow();
                if (row >= 0) remplirFormulaire(row);
            }
        });

        JScrollPane scroll = new JScrollPane(tablePaiement);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scroll.getViewport().setBackground(GRIS_CLAIR);

        p.add(tableTitle, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel tableFacturePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GRIS_CLAIR);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel tableTitle = new JLabel("📄 Factures Disponibles");
        tableTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        tableTitle.setForeground(BLEU_HEADER);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"ID Facture", "Date", "NET TTC (TND)", "Qté", "Statut", "ID Cmd", "Client"};
        modelFacture = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableFacture = new JTable(modelFacture);
        styliserTable(tableFacture, BLEU_HEADER);

        tableFacture.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableFacture.getSelectedRow();
                if (row >= 0 && (modeActuel.equals("AJOUTER") || modeActuel.equals("MODIFIER"))) {
                    Object idFacture = modelFacture.getValueAt(row, 0);
                    Object montant   = modelFacture.getValueAt(row, 2);

                    if (idFacture != null) {
                        for (int i = 0; i < cmbFacture.getItemCount(); i++) {
                            if (cmbFacture.getItemAt(i).startsWith(idFacture.toString() + " -")) {
                                cmbFacture.setSelectedIndex(i); break;
                            }
                        }
                        if (txtMontant.getText().trim().isEmpty() && montant != null) {
                            String montantStr = montant.toString().replace(" TND", "").replace(",", ".");
                            txtMontant.setText(montantStr);
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableFacture);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scroll.getViewport().setBackground(GRIS_CLAIR);

        p.add(tableTitle, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }


    private void styliserTablePaiement(JTable table, Color headerColor) {
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setSelectionBackground(new Color(180, 220, 180));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(210, 210, 210));
        table.setBackground(GRIS_TABLE);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setOpaque(true);
                label.setBackground(headerColor);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 11));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, headerColor.darker()));
                return label;
            }
        });

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSel, hasFocus, row, col);
                if (isSel) {
                    c.setBackground(new Color(180, 220, 180));
                } else {
                    c.setBackground(row % 2 == 0 ? GRIS_TABLE : GRIS_LIGNE);
                }
                c.setForeground(new Color(50, 50, 50));

                String colName = t.getColumnName(col);

                if (colName.equals("Statut") && value != null) {
                    String statut = value.toString().toLowerCase();
                    if (statut.contains("payé") || statut.contains("payée")) c.setForeground(new Color(46, 125, 50));
                    else if (statut.contains("annulé")) c.setForeground(new Color(211, 47, 47));
                    else if (statut.contains("partiel") || statut.contains("attente")) c.setForeground(ORANGE);
                }

                if (colName.equals("Reste (TND)") && value != null && !isSel) {
                    try {
                        double reste = Double.parseDouble(value.toString().replace(" TND", "").replace(",", "."));
                        if (reste <= 0) {
                            c.setForeground(new Color(27, 94, 32));
                            ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(new Color(183, 28, 28));
                            ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                        }
                    } catch (NumberFormatException ignored) {}
                }

                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }


    private void styliserTable(JTable table, Color headerColor) {
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setSelectionBackground(new Color(180, 220, 180));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(210, 210, 210));
        table.setBackground(GRIS_TABLE);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setOpaque(true);
                label.setBackground(headerColor);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 11));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, headerColor.darker()));
                return label;
            }
        });

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSel, hasFocus, row, col);
                if (isSel) {
                    c.setBackground(new Color(180, 220, 180));
                } else {
                    c.setBackground(row % 2 == 0 ? GRIS_TABLE : GRIS_LIGNE);
                }
                c.setForeground(new Color(50, 50, 50));

                String colName = t.getColumnName(col);
                if (colName.equals("Statut") && value != null) {
                    String statut = value.toString().toLowerCase();
                    if (statut.contains("payé") || statut.contains("payée")) c.setForeground(new Color(46, 125, 50));
                    else if (statut.contains("annulé") || statut.contains("annulée")) c.setForeground(new Color(211, 47, 47));
                    else if (statut.contains("partiel") || statut.contains("attente")) c.setForeground(ORANGE);
                }

                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }


    private void afficherInfoFacture() {
        String selection = (String) cmbFacture.getSelectedItem();
        if (selection == null || selection.startsWith("--")) {
            lblInfoFacture.setText("<html><i>Sélectionnez une facture...</i></html>");
            lblInfoFacture.setBackground(new Color(245, 245, 255));
            return;
        }

        Integer idFacture = getIdFactureSelectionne();
        if (idFacture == null) return;

        String sql = "SELECT f.id_facture, f.date_facture, f.montant, f.qte, f.statut, " +
                     "cl.nom_client, cl.prenom_client, " +
                     "COALESCE(SUM(p.montant_paie), 0) AS total_paye " +
                     "FROM facture f " +
                     "LEFT JOIN commande c ON f.id_commande = c.id_commande " +
                     "LEFT JOIN client cl ON c.id_client = cl.id_client " +
                     "LEFT JOIN paiement p ON f.id_facture = p.id_facture " +
                     "WHERE f.id_facture = ? " +
                     "GROUP BY f.id_facture, f.date_facture, f.montant, f.qte, " +
                     "         f.statut, cl.nom_client, cl.prenom_client";

        try (Connection c = getCon(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double montant   = rs.getDouble("montant");
                double totalPaye = rs.getDouble("total_paye");
                double reste     = montant - totalPaye;
                String nom       = rs.getString("nom_client");
                String prenom    = rs.getString("prenom_client");
                String client    = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");

                lblInfoFacture.setText(
                    "<html><div style='font-size:9px;'>" +
                    "<b>📄 Facture #" + rs.getInt("id_facture") + "</b> | " +
                    "📅 " + rs.getDate("date_facture") + "<br/>" +
                    "👤 " + client.trim() + "<br/>" +
                    "💰 NET TTC: " + String.format("%.2f TND", montant) + " | " +
                    "✅ Payé: " + String.format("%.2f TND", totalPaye) + "<br/>" +
                    "⏳ <b>Reste: " + String.format("%.2f TND", reste) + "</b>" +
                    "</div></html>"
                );
                lblInfoFacture.setBackground(reste <= 0 ?
                    new Color(232, 245, 233) : new Color(255, 243, 224));
            }
        } catch (SQLException e) {
            lblInfoFacture.setText("<html><i>Erreur...</i></html>");
        }
    }

    private void remplirFormulaire(int row) {
        try {
            txtIdPaie.setText(modelPaiement.getValueAt(row, 0).toString());

            Object montantObj = modelPaiement.getValueAt(row, 1);
            if (montantObj != null) txtMontant.setText(montantObj.toString().replace(",", "."));

            Object dateObj = modelPaiement.getValueAt(row, 2);
            if (dateObj != null) spinnerDate.setValue(dateFormat.parse(dateObj.toString()));

            Object statut = modelPaiement.getValueAt(row, 3);
            if (statut != null) {
                for (int i = 0; i < cmbStatut.getItemCount(); i++) {
                    if (cmbStatut.getItemAt(i).equalsIgnoreCase(statut.toString())) {
                        cmbStatut.setSelectedIndex(i); break;
                    }
                }
            }

            Object mode = modelPaiement.getValueAt(row, 4);
            if (mode != null) {
                for (int i = 0; i < cmbMode.getItemCount(); i++) {
                    if (cmbMode.getItemAt(i).equalsIgnoreCase(mode.toString())) {
                        cmbMode.setSelectedIndex(i); break;
                    }
                }
            }

            Object idFacture = modelPaiement.getValueAt(row, 5);
            if (idFacture != null) {
                chargerFacturesCombo();
                for (int i = 0; i < cmbFacture.getItemCount(); i++) {
                    if (cmbFacture.getItemAt(i).startsWith(idFacture.toString() + " -")) {
                        cmbFacture.setSelectedIndex(i); break;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    
    private void consulterPaiements() {
        modelPaiement.setRowCount(0);

        String sql = "SELECT p.id_paie, p.montant_paie, p.date_paie, p.statut_paie, " +
                     "p.mode_paie, p.id_facture, f.montant AS net_ttc " +
                     "FROM paiement p " +
                     "LEFT JOIN facture f ON p.id_facture = f.id_facture " +
                     "ORDER BY p.id_paie DESC";

        try (Connection c = getCon(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
                double montantPaie = rs.getDouble("montant_paie");
                double netTTC      = rs.getDouble("net_ttc");
                double reste       = netTTC - montantPaie;

                modelPaiement.addRow(new Object[]{
                    rs.getInt("id_paie"),
                    String.format("%.2f", montantPaie),
                    rs.getDate("date_paie"),
                    rs.getString("statut_paie"),
                    rs.getString("mode_paie"),
                    rs.getInt("id_facture"),
                    String.format("%.2f TND", reste)
                });
            }
            updateStatus("[OK] " + count + " paiement(s) trouvé(s)");
        } catch (SQLException e) {
            updateStatus("[X] Erreur: " + e.getMessage());
            showError(e);
        }
    }

    private void chargerFactures() {
        modelFacture.setRowCount(0);

        String sql = "SELECT f.id_facture, f.date_facture, f.montant, f.qte, f.statut, " +
                     "f.id_commande, cl.nom_client, cl.prenom_client " +
                     "FROM facture f " +
                     "LEFT JOIN commande c ON f.id_commande = c.id_commande " +
                     "LEFT JOIN client cl ON c.id_client = cl.id_client " +
                     "ORDER BY f.id_facture DESC";

        try (Connection c = getCon(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String nom    = rs.getString("nom_client");
                String prenom = rs.getString("prenom_client");
                String client = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");

                modelFacture.addRow(new Object[]{
                    rs.getInt("id_facture"),
                    rs.getDate("date_facture"),
                    String.format("%.2f TND", rs.getDouble("montant")),
                    rs.getInt("qte"),
                    rs.getString("statut"),
                    rs.getInt("id_commande"),
                    client.trim()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }


    private void chargerFacturesCombo() {
        cmbFacture.removeAllItems();
        cmbFacture.addItem("-- Sélectionner une facture --");

        String sql = "SELECT f.id_facture, f.date_facture, f.montant, f.statut, " +
                     "cl.nom_client, cl.prenom_client, " +
                     "COALESCE(SUM(p.montant_paie), 0) AS total_paye " +
                     "FROM facture f " +
                     "LEFT JOIN commande c ON f.id_commande = c.id_commande " +
                     "LEFT JOIN client cl ON c.id_client = cl.id_client " +
                     "LEFT JOIN paiement p ON f.id_facture = p.id_facture " +
                     "GROUP BY f.id_facture, f.date_facture, f.montant, f.statut, " +
                     "         cl.nom_client, cl.prenom_client " +
                     "HAVING COALESCE(SUM(p.montant_paie), 0) < f.montant " +
                     "ORDER BY f.date_facture ASC";

        try (Connection c = getCon(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int    id        = rs.getInt("id_facture");
                String dateF     = rs.getDate("date_facture") != null ?
                                   rs.getDate("date_facture").toString() : "N/A";
                double montant   = rs.getDouble("montant");
                double totalPaye = rs.getDouble("total_paye");
                double reste     = montant - totalPaye;
                String nom       = rs.getString("nom_client");
                String prenom    = rs.getString("prenom_client");
                String client    = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");

                
                String display = id + " - " + client.trim() +
                                 " (" + dateF + ")" +
                                 " | Reste: " + String.format("%.2f TND", reste);
                cmbFacture.addItem(display);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void ajouterPaiement() {
        if (txtIdPaie.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ ID Paiement obligatoire!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtMontant.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Montant obligatoire!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer idFacture = getIdFactureSelectionne();
        if (idFacture == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez une facture!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO paiement (id_paie, montant_paie, date_paie, statut_paie, mode_paie, id_facture) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection c = getCon(); PreparedStatement ps = c.prepareStatement(sql)) {
            int    idPaie  = Integer.parseInt(txtIdPaie.getText().trim());
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));

            ps.setInt(1, idPaie);
            ps.setDouble(2, montant);
            ps.setDate(3, new java.sql.Date(((Date) spinnerDate.getValue()).getTime()));
            ps.setString(4, cmbStatut.getSelectedItem().toString());
            ps.setString(5, cmbMode.getSelectedItem().toString());
            ps.setInt(6, idFacture);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                "✅ Paiement #" + idPaie + " enregistré!\nMontant: " + String.format("%.2f TND", montant),
                "Succès", JOptionPane.INFORMATION_MESSAGE);

            consulterPaiements();
            chargerFactures();
            clearFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Valeurs invalides!", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                JOptionPane.showMessageDialog(this, "❌ Ce paiement existe déjà!", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else { showError(e); }
        }
    }

    private void modifierPaiement() {
        if (txtIdPaie.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez un paiement!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtMontant.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Montant obligatoire!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer idFacture = getIdFactureSelectionne();
        if (idFacture == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez une facture!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE paiement SET montant_paie=?, date_paie=?, statut_paie=?, mode_paie=?, id_facture=? WHERE id_paie=?";

        try (Connection c = getCon(); PreparedStatement ps = c.prepareStatement(sql)) {
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            int    idPaie  = Integer.parseInt(txtIdPaie.getText().trim());

            ps.setDouble(1, montant);
            ps.setDate(2, new java.sql.Date(((Date) spinnerDate.getValue()).getTime()));
            ps.setString(3, cmbStatut.getSelectedItem().toString());
            ps.setString(4, cmbMode.getSelectedItem().toString());
            ps.setInt(5, idFacture);
            ps.setInt(6, idPaie);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "✅ Paiement modifié!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                consulterPaiements();
                clearFields();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Valeurs invalides!", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) { showError(e); }
    }

    private void supprimerPaiement() {
        if (txtIdPaie.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez un paiement!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPaie = Integer.parseInt(txtIdPaie.getText().trim());

        int confirm = JOptionPane.showConfirmDialog(this,
            "🗑️ Supprimer le paiement #" + idPaie + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection c = getCon();
             PreparedStatement ps = c.prepareStatement("DELETE FROM paiement WHERE id_paie = ?")) {
            ps.setInt(1, idPaie);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "✅ Paiement supprimé!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                consulterPaiements();
                clearFields();
            }
        } catch (SQLException e) { showError(e); }
    }


    private void rechercherPaiement() {
        String recherche = txtRecherche.getText().trim();
        if (recherche.isEmpty()) { consulterPaiements(); return; }

        try {
            int idRecherche = Integer.parseInt(recherche);
            modelPaiement.setRowCount(0);

            String sql = "SELECT p.id_paie, p.montant_paie, p.date_paie, p.statut_paie, " +
                         "p.mode_paie, p.id_facture, f.montant AS net_ttc " +
                         "FROM paiement p LEFT JOIN facture f ON p.id_facture = f.id_facture " +
                         "WHERE p.id_paie = ?";

            try (Connection c = getCon(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idRecherche);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    double montantPaie = rs.getDouble("montant_paie");
                    double netTTC      = rs.getDouble("net_ttc");
                    double reste       = netTTC - montantPaie;

                    modelPaiement.addRow(new Object[]{
                        rs.getInt("id_paie"),
                        String.format("%.2f", montantPaie),
                        rs.getDate("date_paie"),
                        rs.getString("statut_paie"),
                        rs.getString("mode_paie"),
                        rs.getInt("id_facture"),
                        String.format("%.2f TND", reste)
                    });
                    tablePaiement.setRowSelectionInterval(0, 0);
                    remplirFormulaire(0);
                } else {
                    JOptionPane.showMessageDialog(this, "🔍 Aucun paiement trouvé: " + idRecherche,
                        "Résultat", JOptionPane.INFORMATION_MESSAGE);
                    consulterPaiements();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "⚠️ ID invalide!", "Erreur", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) { showError(e); }
    }

    
    private Integer getIdFactureSelectionne() {
        String sel = (String) cmbFacture.getSelectedItem();
        if (sel == null || sel.startsWith("--")) return null;
        try { return Integer.parseInt(sel.split(" - ")[0].trim()); }
        catch (Exception e) { return null; }
    }

    private Integer getIdCommandeSelectionnee() {
        String sel = (String) cmbCommande.getSelectedItem();
        if (sel == null || sel.startsWith("--")) return null;
        try { return Integer.parseInt(sel.split(" - ")[0].trim()); }
        catch (Exception e) { return null; }
    }

    private JPanel statusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GRIS_CLAIR);
        p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        lblStatus = new JLabel("💰 Système prêt - Gestion Factures & Paiements");
        lblStatus.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(100, 100, 100));

        p.add(lblStatus, BorderLayout.WEST);
        return p;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(VERT_MOYEN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(VERT_CLAIR); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(VERT_MOYEN); }
        });
        return btn;
    }

    private JButton createActionButton(String text, Color normalColor) {
        JButton btn = new JButton(text);
        btn.setBackground(normalColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        final Color[] cur = {normalColor};
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(cur[0].darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(cur[0]); }
        });
        return btn;
    }

    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setBackground(GRIS_CLAIR);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        l.setForeground(new Color(60, 60, 60));

        field.setPreferredSize(new Dimension(200, 28));
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSpinnerPanel(String label, JSpinner spinner) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setBackground(GRIS_CLAIR);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        l.setForeground(new Color(60, 60, 60));

        spinner.setPreferredSize(new Dimension(200, 28));
        p.add(l, BorderLayout.NORTH);
        p.add(spinner, BorderLayout.CENTER);
        return p;
    }

    private JPanel createComboPanel(String label, JComboBox<String> combo) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setBackground(GRIS_CLAIR);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        l.setForeground(new Color(60, 60, 60));

        combo.setPreferredSize(new Dimension(200, 28));
        p.add(l, BorderLayout.NORTH);
        p.add(combo, BorderLayout.CENTER);
        return p;
    }

    private Connection getCon() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    private void updateStatus(String message) { lblStatus.setText(message); }

    private void clearFields() {
        txtIdPaie.setText("");
        txtMontant.setText("");
        txtRecherche.setText("");
        txtIdFacture.setText("");
        txtQteFacture.setText("");
        txtMontantFacture.setText("");
        spinnerDate.setValue(new Date());
        spinnerDateFacture.setValue(new Date());
        cmbStatut.setSelectedIndex(0);
        cmbMode.setSelectedIndex(0);
        cmbStatutFacture.setSelectedIndex(0);
        if (cmbFacture.getItemCount() > 0)  cmbFacture.setSelectedIndex(0);
        if (cmbCommande.getItemCount() > 0) cmbCommande.setSelectedIndex(0);
        lblInfoFacture.setText("<html><i>Sélectionnez une facture...</i></html>");
        lblInfoFacture.setBackground(new Color(245, 245, 255));
        lblInfoCommande.setText("<html><i>Sélectionnez une commande...</i></html>");
        lblInfoCommande.setBackground(new Color(245, 255, 245));
        txtDetailFacture.setText("📋 Détail de la facture apparaîtra ici...");
        tablePaiement.clearSelection();
        tableFacture.clearSelection();
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "❌ ERREUR: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    private void retourMenu() {
        this.dispose();
        new MenuPrincipal().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GestionPaiement().setVisible(true));
    }
}