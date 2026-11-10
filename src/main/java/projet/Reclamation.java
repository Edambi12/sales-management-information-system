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

public class Reclamation extends JFrame {

    // JDBC settings are loaded from config/database.properties (see DatabaseConfig).
    private JTable tableRetour;
    private DefaultTableModel modelRetour;

    private JTable tableFacture;
    private DefaultTableModel modelFacture;

    private JTextField txtIdRetour, txtRecherche;
    private JSpinner spinnerDate;
    private JComboBox<String> cmbMotif, cmbEtatProduit, cmbFacture;
    private JLabel lblStatus, lblInfoFacture;

    private JPanel panelFormulaire;
    private JPanel panelIdRetour, panelDate, panelMotif, panelEtat, panelFacture;
    private JButton btnAction, btnSecondaire;
    private JLabel formTitle;

    private String modeActuel = "CONSULTER";

    
    private final Color VERT_FONCE      = new Color(27, 94, 32);
    private final Color VERT_MOYEN      = new Color(46, 125, 50);
    private final Color VERT_CLAIR      = new Color(76, 175, 80);
    private final Color VERT_HOVER      = new Color(56, 142, 60);
    private final Color VERT_HEADER     = new Color(33, 97, 47);
    private final Color VERT_SIDEBAR    = new Color(20, 80, 25);
    private final Color VERT_TRES_CLAIR = new Color(200, 230, 201);
    private final Color BLEU_HEADER     = new Color(25, 118, 210);
    private final Color ORANGE          = new Color(255, 152, 0);
    private final Color ROUGE           = new Color(211, 47, 47);
    private final Color VIOLET          = new Color(156, 39, 176);
    private final Color GRIS_FOND       = new Color(245, 247, 250);
    private final Color GRIS_CLAIR      = new Color(240, 242, 245);
    private final Color GRIS_TABLE      = new Color(250, 252, 250);
    private final Color GRIS_LIGNE      = new Color(235, 240, 235);
    private final Color BLANC           = Color.WHITE;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    
    public Reclamation() {
        setTitle("SOKTRAPA - Gestion des Réclamations & Retours");
        setSize(1350, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chargerDriver();

        add(sidebar(), BorderLayout.WEST);
        add(mainPanel(), BorderLayout.CENTER);
        add(statusBar(), BorderLayout.SOUTH);

        afficherModeConsulter();
        consulterRetours();
        chargerFacturesTable();
    }

    
    private void chargerDriver() {
        try {
            if (!DatabaseConfig.loadDriver()) throw new ClassNotFoundException(DatabaseConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                "[ERREUR] Driver MySQL introuvable!",
                "Erreur Driver", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private JPanel sidebar() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, VERT_FONCE, 0, getHeight(), VERT_MOYEN);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setPreferredSize(new Dimension(235, 850));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JLabel logo = new JLabel("📦 SOKTRAPA");
        logo.setForeground(BLANC);
        logo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Réclamations & Retours");
        subtitle.setForeground(new Color(200, 230, 200));
        subtitle.setFont(new Font("Arial", Font.ITALIC, 11));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel ligne = new JPanel();
        ligne.setMaximumSize(new Dimension(200, 2));
        ligne.setBackground(new Color(255, 255, 255, 60));
        ligne.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnConsulter  = createMenuButton("📋 Consulter Retours");
        JButton btnDemande    = createMenuButton("📝 Demande de Retour");
        JButton btnModifier   = createMenuButton("✏️ Modifier Retour");
        JButton btnSupprimer  = createMenuButton("🗑️ Supprimer Retour");
        JButton btnRechercher = createMenuButton("🔍 Rechercher");
        JButton btnActualiser = createMenuButton("🔄 Actualiser");
        JButton btnMenu       = createMenuButton("⬅️ Menu Principal");

        btnConsulter .addActionListener(e -> afficherModeConsulter());
        btnDemande   .addActionListener(e -> afficherModeDemande());
        btnModifier  .addActionListener(e -> afficherModeModifier());
        btnSupprimer .addActionListener(e -> afficherModeSupprimer());
        btnRechercher.addActionListener(e -> afficherModeRechercher());
        btnActualiser.addActionListener(e -> {
            consulterRetours();
            chargerFacturesTable();
            updateStatus("[OK] Données actualisées");
        });
        btnMenu.addActionListener(e -> retourMenu());

        p.add(Box.createVerticalStrut(30));
        p.add(logo);
        p.add(Box.createVerticalStrut(5));
        p.add(subtitle);
        p.add(Box.createVerticalStrut(20));
        p.add(ligne);
        p.add(Box.createVerticalStrut(25));
        p.add(btnConsulter);
        p.add(Box.createVerticalStrut(10));
        p.add(btnDemande);
        p.add(Box.createVerticalStrut(10));
        p.add(btnModifier);
        p.add(Box.createVerticalStrut(10));
        p.add(btnSupprimer);
        p.add(Box.createVerticalStrut(10));
        p.add(btnRechercher);
        p.add(Box.createVerticalStrut(25));
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
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, VERT_FONCE));

        JLabel title = new JLabel("   🔄 Gestion des Réclamations & Retours Produits");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        title.setForeground(BLANC);

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
        panelFormulaire.setBackground(BLANC);
        panelFormulaire.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        formTitle = new JLabel("📋 Consultation des Retours");
        formTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        formTitle.setForeground(VERT_FONCE);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtIdRetour  = new JTextField();
        txtRecherche = new JTextField();

        SpinnerDateModel dm = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spinnerDate = new JSpinner(dm);
        spinnerDate.setEditor(new JSpinner.DateEditor(spinnerDate, "yyyy-MM-dd"));
        spinnerDate.setFont(new Font("Arial", Font.PLAIN, 13));

        
        String[] motifs = {
            "-- Sélectionner un motif --",
            "Produit défectueux",
            "Erreur de livraison",
            "Non conforme à la commande"
        };
        cmbMotif = new JComboBox<>(motifs);
        cmbMotif.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        cmbMotif.setBackground(BLANC);
        cmbMotif.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(VERT_TRES_CLAIR);
                    setForeground(VERT_FONCE);
                } else {
                    setBackground(BLANC);
                }
                if (value != null) {
                    String val = value.toString();
                    if (val.contains("défectueux")) {
                        if (!isSelected) setForeground(new Color(211, 47, 47));
                        setText("🔧 " + val);
                    } else if (val.contains("livraison")) {
                        if (!isSelected) setForeground(new Color(230, 100, 0));
                        setText("🚚 " + val);
                    } else if (val.contains("conforme")) {
                        if (!isSelected) setForeground(VERT_FONCE);
                        setText("📋 " + val);
                    } else {
                        setText(val);
                    }
                }
                return this;
            }
        });

        
        String[] etats = {
            "-- Sélectionner état --",
            "Neuf",
            "Bon état",
            "Endommagé",
            "Très endommagé",
            "Inutilisable"
        };
        cmbEtatProduit = new JComboBox<>(etats);
        cmbEtatProduit.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbEtatProduit.setBackground(BLANC);

        
        cmbFacture = new JComboBox<>();
        cmbFacture.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbFacture.setBackground(BLANC);
        cmbFacture.addActionListener(e -> afficherInfoFacture());

        
        lblInfoFacture = new JLabel("<html><i>Sélectionnez une facture...</i></html>");
        lblInfoFacture.setFont(new Font("Arial", Font.PLAIN, 10));
        lblInfoFacture.setForeground(new Color(100, 100, 100));
        lblInfoFacture.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblInfoFacture.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 220, 180), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        lblInfoFacture.setOpaque(true);
        lblInfoFacture.setBackground(new Color(240, 255, 240));
        lblInfoFacture.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        panelIdRetour = createFieldPanel("🔢 ID Retour *", txtIdRetour);
        panelDate     = createSpinnerPanel("📅 Date Retour", spinnerDate);
        panelMotif    = createComboPanel("❓ Motif du Retour *", cmbMotif);
        panelEtat     = createComboPanel("📦 État du Produit *", cmbEtatProduit);
        panelFacture  = createComboPanel("📄 Facture Concernée *", cmbFacture);

        btnAction = createActionButton("📋 Consulter", VERT_MOYEN);
        btnAction.addActionListener(e -> executerAction());

        btnSecondaire = createActionButton("🧹 Vider", new Color(120, 120, 120));
        btnSecondaire.addActionListener(e -> clearFields());

        construireFormulaire();
        return panelFormulaire;
    }

    
    private void construireFormulaire() {
        panelFormulaire.removeAll();

        panelFormulaire.add(formTitle);
        panelFormulaire.add(Box.createVerticalStrut(15));

        switch (modeActuel) {
            case "CONSULTER":
                panelFormulaire.add(creerCarteInfo(
                    "📋 Mode Consultation",
                    "Consultez toutes les demandes de retour.\n\n" +
                    "Cliquez sur une ligne dans le tableau\npour voir les détails.",
                    VERT_TRES_CLAIR, VERT_FONCE));
                break;

            case "DEMANDE":
                panelFormulaire.add(creerCarteAide());
                panelFormulaire.add(Box.createVerticalStrut(10));
                panelFormulaire.add(panelIdRetour);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelDate);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelMotif);
                panelFormulaire.add(Box.createVerticalStrut(4));
                panelFormulaire.add(creerPanelDescriptionMotif());
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelEtat);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelFacture);
                panelFormulaire.add(Box.createVerticalStrut(5));
                panelFormulaire.add(lblInfoFacture);
                break;

            case "MODIFIER":
                panelFormulaire.add(creerCarteInfo(
                    "✏️ Mode Modification",
                    "Sélectionnez un retour dans le tableau\npuis modifiez les champs.",
                    new Color(255, 243, 224), ORANGE));
                panelFormulaire.add(Box.createVerticalStrut(12));
                panelFormulaire.add(panelIdRetour);
                txtIdRetour.setEditable(false);
                txtIdRetour.setBackground(new Color(245, 245, 245));
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelDate);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelMotif);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelEtat);
                panelFormulaire.add(Box.createVerticalStrut(8));
                panelFormulaire.add(panelFacture);
                panelFormulaire.add(Box.createVerticalStrut(5));
                panelFormulaire.add(lblInfoFacture);
                break;

            case "SUPPRIMER":
                panelFormulaire.add(creerCarteInfo(
                    "🗑️ Mode Suppression",
                    "Sélectionnez un retour dans le tableau\npuis confirmez la suppression.",
                    new Color(255, 235, 238), ROUGE));
                panelFormulaire.add(Box.createVerticalStrut(15));
                panelFormulaire.add(panelIdRetour);
                txtIdRetour.setEditable(false);
                txtIdRetour.setBackground(new Color(245, 245, 245));
                break;

            case "RECHERCHER":
                panelFormulaire.add(creerCarteInfo(
                    "🔍 Mode Recherche",
                    "Saisissez un ID de retour\npour le rechercher.",
                    new Color(237, 231, 246), VIOLET));
                panelFormulaire.add(Box.createVerticalStrut(12));
                panelFormulaire.add(createFieldPanel("🔍 ID Retour à rechercher", txtRecherche));
                break;
        }

        panelFormulaire.add(Box.createVerticalStrut(20));
        panelFormulaire.add(btnAction);
        panelFormulaire.add(Box.createVerticalStrut(8));
        panelFormulaire.add(btnSecondaire);
        panelFormulaire.add(Box.createVerticalGlue());

        panelFormulaire.revalidate();
        panelFormulaire.repaint();
    }

    
    private JPanel creerCarteInfo(String titre, String message, Color bgColor, Color fgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fgColor.brighter(), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lblTitre.setForeground(fgColor);

        JLabel lblMsg = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        lblMsg.setFont(new Font("Arial", Font.PLAIN, 11));
        lblMsg.setForeground(new Color(60, 60, 60));

        card.add(lblTitre, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        return card;
    }

    private JPanel creerCarteAide() {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(VERT_TRES_CLAIR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VERT_MOYEN, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        JLabel lblTitre = new JLabel("📝 Comment faire une demande de retour ?");
        lblTitre.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblTitre.setForeground(VERT_FONCE);

        JLabel lblMsg = new JLabel("<html>" +
            "1. Saisissez l'<b>ID Retour</b><br>" +
            "2. Sélectionnez le <b>motif</b> du retour<br>" +
            "3. Indiquez l'<b>état</b> du produit<br>" +
            "4. Choisissez la <b>facture</b> concernée<br>" +
            "5. Cliquez sur '<b>Soumettre Demande</b>'" +
            "</html>");
        lblMsg.setFont(new Font("Arial", Font.PLAIN, 11));
        lblMsg.setForeground(VERT_FONCE);

        card.add(lblTitre, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        return card;
    }

    private JPanel creerPanelDescriptionMotif() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(232, 245, 233));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VERT_CLAIR, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lbl = new JLabel("<html>" +
            "<b style='color:#1B5E20;'>📋 Descriptions :</b><br>" +
            "🔧 <b>Défectueux</b> : Le produit ne fonctionne pas<br>" +
            "🚚 <b>Erreur livraison</b> : Mauvais produit livré<br>" +
            "📋 <b>Non conforme</b> : Ne correspond pas à la commande" +
            "</html>");
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));

        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    
    private void afficherModeConsulter() {
        modeActuel = "CONSULTER";
        formTitle.setText("📋 Consultation des Retours");
        formTitle.setForeground(VERT_FONCE);
        btnAction.setText("📋 Consulter Tous");
        btnAction.setBackground(VERT_MOYEN);
        btnSecondaire.setText("🧹 Vider Sélection");
        clearFields();
        txtIdRetour.setEditable(true);
        txtIdRetour.setBackground(BLANC);
        construireFormulaire();
        consulterRetours();
        updateStatus("[>>] Mode: Consultation des retours produits");
    }

    private void afficherModeDemande() {
        modeActuel = "DEMANDE";
        formTitle.setText("📝 Demande de Retour Produit");
        formTitle.setForeground(VERT_FONCE);
        btnAction.setText("📝 Soumettre Demande");
        btnAction.setBackground(VERT_FONCE);
        btnSecondaire.setText("🧹 Vider Formulaire");
        clearFields();
        txtIdRetour.setEditable(true);
        txtIdRetour.setBackground(BLANC);
        chargerFacturesCombo();
        construireFormulaire();
        updateStatus("[📝] Mode: Demande de retour - Remplissez tous les champs (*)");
    }

    private void afficherModeModifier() {
        modeActuel = "MODIFIER";
        formTitle.setText("✏️ Modifier Demande de Retour");
        formTitle.setForeground(ORANGE);
        btnAction.setText("✏️ Valider Modification");
        btnAction.setBackground(ORANGE);
        btnSecondaire.setText("🧹 Annuler");
        chargerFacturesCombo();
        construireFormulaire();
        updateStatus("[#] Mode: Modification - Sélectionnez un retour dans le tableau");
    }

    private void afficherModeSupprimer() {
        modeActuel = "SUPPRIMER";
        formTitle.setText("🗑️ Supprimer Demande de Retour");
        formTitle.setForeground(ROUGE);
        btnAction.setText("🗑️ Confirmer Suppression");
        btnAction.setBackground(ROUGE);
        btnSecondaire.setText("🧹 Annuler");
        construireFormulaire();
        updateStatus("[-] Mode: Suppression - Sélectionnez un retour dans le tableau");
    }

    private void afficherModeRechercher() {
        modeActuel = "RECHERCHER";
        formTitle.setText("🔍 Rechercher Retour");
        formTitle.setForeground(VIOLET);
        btnAction.setText("🔍 Rechercher");
        btnAction.setBackground(VIOLET);
        btnSecondaire.setText("📋 Afficher Tous");
        clearFields();
        construireFormulaire();
        updateStatus("[?] Mode: Recherche par ID retour");
    }

    
    private void executerAction() {
        switch (modeActuel) {
            case "CONSULTER":  consulterRetours();  break;
            case "DEMANDE":    soumettreDemande();  break;
            case "MODIFIER":   modifierRetour();    break;
            case "SUPPRIMER":  supprimerRetour();   break;
            case "RECHERCHER": rechercherRetour();  break;
        }
    }

    
    private JPanel tablesPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 10));
        p.setBackground(GRIS_FOND);
        p.add(tableRetourPanel());
        p.add(tableFacturePanel());
        return p;
    }

    
    private JPanel tableRetourPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BLANC);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel tableTitle = new JLabel("🔄 Liste des Demandes de Retour");
        tableTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        tableTitle.setForeground(VERT_FONCE);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"ID Retour", "Motif", "État Produit", "Date", "ID Facture", "Client"};
        modelRetour = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableRetour = new JTable(modelRetour);

        styliserTableRetour(tableRetour, VERT_HEADER);

        tableRetour.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableRetour.getSelectedRow();
                if (row >= 0) remplirFormulaireDepuisTable(row);
            }
        });

        JScrollPane scroll = new JScrollPane(tableRetour);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));
        scroll.getViewport().setBackground(GRIS_CLAIR);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, creerCoin(VERT_HEADER));

        p.add(tableTitle, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    
    private JPanel tableFacturePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BLANC);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel tableTitle = new JLabel("📄 Factures Disponibles (Référence)");
        tableTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        tableTitle.setForeground(BLEU_HEADER);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        
        String[] cols = {"ID Facture", "Date", "Montant (TND)", "Qté", "Statut", "ID Commande"};
        modelFacture = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableFacture = new JTable(modelFacture);

        styliserTable(tableFacture, BLEU_HEADER);

        tableFacture.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableFacture.getSelectedRow();
                if (row >= 0 && (modeActuel.equals("DEMANDE") || modeActuel.equals("MODIFIER"))) {
                    Object idFacture = modelFacture.getValueAt(row, 0);
                    if (idFacture != null) {
                        for (int i = 0; i < cmbFacture.getItemCount(); i++) {
                            if (cmbFacture.getItemAt(i).startsWith(idFacture + " -")) {
                                cmbFacture.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableFacture);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));
        scroll.getViewport().setBackground(GRIS_CLAIR);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, creerCoin(BLEU_HEADER));

        p.add(tableTitle, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    
    private JPanel creerCoin(Color color) {
        JPanel coin = new JPanel();
        coin.setBackground(color);
        return coin;
    }

    
    private void styliserTableRetour(JTable table, Color headerColor) {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setSelectionBackground(VERT_TRES_CLAIR);
        table.setSelectionForeground(VERT_FONCE);
        table.setGridColor(new Color(210, 225, 210));
        table.setBackground(GRIS_TABLE);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "");
                lbl.setOpaque(true);
                lbl.setBackground(headerColor);
                lbl.setForeground(BLANC);
                lbl.setFont(new Font("Arial", Font.BOLD, 12));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, headerColor.darker()));
                return lbl;
            }
        });

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSel, hasFocus, row, col);

                if (isSel) {
                    c.setBackground(VERT_TRES_CLAIR);
                    c.setForeground(VERT_FONCE);
                } else {
                    c.setBackground(row % 2 == 0 ? GRIS_TABLE : GRIS_LIGNE);
                    c.setForeground(new Color(40, 40, 40));

                    if (col == 1 && value != null) {
                        String m = value.toString();
                        if (m.contains("défectueux")) {
                            c.setForeground(new Color(183, 28, 28));
                            ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                        } else if (m.contains("livraison")) {
                            c.setForeground(new Color(230, 100, 0));
                            ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                        } else if (m.contains("conforme")) {
                            c.setForeground(VERT_FONCE);
                            ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                        }
                    }

                    if (col == 2 && value != null) {
                        String e = value.toString();
                        if (e.contains("Inutilisable") || e.contains("Très endommagé")) {
                            c.setForeground(new Color(183, 28, 28));
                        } else if (e.contains("Endommagé")) {
                            c.setForeground(ORANGE);
                        } else if (e.contains("Bon") || e.contains("Neuf")) {
                            c.setForeground(VERT_MOYEN);
                        }
                    }
                }

                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cr);
        }
    }

    
    private void styliserTable(JTable table, Color headerColor) {
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setSelectionBackground(VERT_TRES_CLAIR);
        table.setSelectionForeground(VERT_FONCE);
        table.setGridColor(new Color(210, 225, 210));
        table.setBackground(GRIS_TABLE);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "");
                lbl.setOpaque(true);
                lbl.setBackground(headerColor);
                lbl.setForeground(BLANC);
                lbl.setFont(new Font("Arial", Font.BOLD, 11));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, headerColor.darker()));
                return lbl;
            }
        });

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSel, hasFocus, row, col);
                if (isSel) {
                    c.setBackground(VERT_TRES_CLAIR);
                    c.setForeground(VERT_FONCE);
                } else {
                    c.setBackground(row % 2 == 0 ? GRIS_TABLE : GRIS_LIGNE);
                    c.setForeground(new Color(40, 40, 40));
                }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cr);
        }
    }

    
    private void afficherInfoFacture() {
        String sel = (String) cmbFacture.getSelectedItem();
        if (sel == null || sel.startsWith("--")) {
            lblInfoFacture.setText("<html><i>Sélectionnez une facture...</i></html>");
            lblInfoFacture.setBackground(new Color(240, 255, 240));
            return;
        }

        Integer idFacture = getIdFactureSelectionne();
        if (idFacture == null) return;

        String sql = "SELECT f.*, c.id_commande, c.statut AS statut_cmd " +
                     "FROM facture f LEFT JOIN commande c ON f.id_commande = c.id_commande " +
                     "WHERE f.id_facture = ?";

        try (Connection con = getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                
                lblInfoFacture.setText(
                    "<html><div style='font-size:9px;'>" +
                    "<b>📄 Facture #" + rs.getInt("id_facture") + "</b><br/>" +
                    "📅 Date: " + rs.getDate("date_facture") +
                    " | 📦 Qté: " + rs.getInt("qte") + "<br/>" +
                    "💰 Montant: " + String.format("%.2f TND", rs.getDouble("montant")) +
                    " | 📊 Statut: " + rs.getString("statut") + "<br/>" +
                    "🛒 Commande: #" + rs.getInt("id_commande") +
                    "</div></html>"
                );
                lblInfoFacture.setBackground(new Color(232, 255, 232));
            }
        } catch (SQLException e) {
            lblInfoFacture.setText("<html><i>Erreur chargement...</i></html>");
        }
    }

    
    private void remplirFormulaireDepuisTable(int row) {
        try {
            txtIdRetour.setText(modelRetour.getValueAt(row, 0).toString());

            Object motif = modelRetour.getValueAt(row, 1);
            if (motif != null) {
                for (int i = 0; i < cmbMotif.getItemCount(); i++) {
                    if (cmbMotif.getItemAt(i).equalsIgnoreCase(motif.toString())) {
                        cmbMotif.setSelectedIndex(i); break;
                    }
                }
            }

            Object etat = modelRetour.getValueAt(row, 2);
            if (etat != null) {
                for (int i = 0; i < cmbEtatProduit.getItemCount(); i++) {
                    if (cmbEtatProduit.getItemAt(i).equalsIgnoreCase(etat.toString())) {
                        cmbEtatProduit.setSelectedIndex(i); break;
                    }
                }
            }

            Object dateObj = modelRetour.getValueAt(row, 3);
            if (dateObj != null) spinnerDate.setValue(dateFormat.parse(dateObj.toString()));

            Object idFact = modelRetour.getValueAt(row, 4);
            if (idFact != null && cmbFacture.getItemCount() > 0) {
                chargerFacturesCombo();
                for (int i = 0; i < cmbFacture.getItemCount(); i++) {
                    if (cmbFacture.getItemAt(i).startsWith(idFact + " -")) {
                        cmbFacture.setSelectedIndex(i); break;
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    
    private void consulterRetours() {
        modelRetour.setRowCount(0);

        
        String sql = "SELECT r.id_retour, r.motif, r.etat_produit, r.date, r.id_facture, " +
                     "cl.nom_client, cl.prenom_client " +
                     "FROM retour r " +
                     "LEFT JOIN facture f ON r.id_facture = f.id_facture " +
                     "LEFT JOIN commande c ON f.id_commande = c.id_commande " +
                     "LEFT JOIN client cl ON c.id_client = cl.id_client " +
                     "ORDER BY r.id_retour DESC";

        try (Connection con = getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                String nom    = rs.getString("nom_client");
                String prenom = rs.getString("prenom_client");
                String client = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");

                modelRetour.addRow(new Object[]{
                    rs.getInt("id_retour"),
                    rs.getString("motif"),
                    rs.getString("etat_produit"),
                    rs.getDate("date"),
                    rs.getInt("id_facture"),
                    client.trim().isEmpty() ? "N/A" : client.trim()  // Colonne Client
                });
            }
            updateStatus("[OK] " + count + " demande(s) de retour trouvée(s)");

        } catch (SQLException e) {
            updateStatus("[X] Erreur: " + e.getMessage());
            showError(e);
        }
    }

    
    private void chargerFacturesTable() {
        modelFacture.setRowCount(0);
        String sql = "SELECT id_facture, date_facture, montant, qte, statut, id_commande " +
                     "FROM facture ORDER BY id_facture DESC";

        try (Connection con = getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                modelFacture.addRow(new Object[]{
                    rs.getInt("id_facture"),
                    rs.getDate("date_facture"),
                    
                    String.format("%.2f TND", rs.getDouble("montant")),
                    rs.getInt("qte"),
                    rs.getString("statut"),
                    rs.getInt("id_commande")
                });
            }
        } catch (SQLException e) {
            updateStatus("[!] Erreur chargement factures");
            e.printStackTrace();
        }
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
                     
                     "ORDER BY f.date_facture DESC";

        try (Connection c = getCon();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int    id     = rs.getInt("id_facture");
                String dateF  = rs.getDate("date_facture") != null ?
                                rs.getDate("date_facture").toString() : "N/A";
                String nom    = rs.getString("nom_client");
                String prenom = rs.getString("prenom_client");
                
                String client  = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
                String display = id + " - " + client.trim() + " (" + dateF + ")";
                cmbFacture.addItem(display);
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    
    private void soumettreDemande() {
        if (txtIdRetour.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ L'ID Retour est obligatoire!",
                "Champ requis", JOptionPane.WARNING_MESSAGE);
            txtIdRetour.requestFocus();
            return;
        }

        String motif = (String) cmbMotif.getSelectedItem();
        if (motif == null || motif.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez un motif de retour!",
                "Motif requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String etat = (String) cmbEtatProduit.getSelectedItem();
        if (etat == null || etat.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "⚠️ Indiquez l'état du produit!",
                "État requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idFacture = getIdFactureSelectionne();
        if (idFacture == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez la facture concernée!",
                "Facture requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "📝 CONFIRMER LA DEMANDE DE RETOUR ?\n\n" +
            "══════════════════════════════════\n" +
            "   ID Retour : " + txtIdRetour.getText() + "\n" +
            "   Motif     : " + motif + "\n" +
            "   État      : " + etat + "\n" +
            "   Date      : " + dateFormat.format(spinnerDate.getValue()) + "\n" +
            "   Facture   : #" + idFacture + "\n" +
            "══════════════════════════════════",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "INSERT INTO retour (id_retour, motif, etat_produit, date, id_facture) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            int idRetour = Integer.parseInt(txtIdRetour.getText().trim());
            ps.setInt(1, idRetour);
            ps.setString(2, motif);
            ps.setString(3, etat);
            ps.setDate(4, new java.sql.Date(((Date) spinnerDate.getValue()).getTime()));
            ps.setInt(5, idFacture);
            ps.executeUpdate();

            String icone = motif.contains("défectueux") ? "🔧" :
                           motif.contains("livraison")  ? "🚚" : "📋";

            JOptionPane.showMessageDialog(this,
                "✅ DEMANDE DE RETOUR SOUMISE !\n\n" +
                "══════════════════════════════════\n" +
                "   ID Retour : " + idRetour + "\n" +
                "   " + icone + " Motif  : " + motif + "\n" +
                "   📦 État   : " + etat + "\n" +
                "   📅 Date   : " + dateFormat.format(spinnerDate.getValue()) + "\n" +
                "   📄 Facture: #" + idFacture + "\n" +
                "══════════════════════════════════\n\n" +
                "Votre demande a été enregistrée avec succès.",
                "Demande Soumise", JOptionPane.INFORMATION_MESSAGE);

            consulterRetours();
            clearFields();
            updateStatus("[OK] Demande de retour #" + idRetour + " soumise");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ ID Retour invalide!", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                JOptionPane.showMessageDialog(this,
                    "❌ Ce retour existe déjà!\nL'ID " + txtIdRetour.getText() + " est utilisé.",
                    "Doublon", JOptionPane.ERROR_MESSAGE);
            } else if (e.getMessage().contains("foreign key")) {
                JOptionPane.showMessageDialog(this,
                    "❌ La facture sélectionnée n'existe pas!",
                    "Référence invalide", JOptionPane.ERROR_MESSAGE);
            } else { showError(e); }
        }
    }

    
    private void modifierRetour() {
        if (txtIdRetour.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez un retour dans le tableau!",
                "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motif = (String) cmbMotif.getSelectedItem();
        if (motif == null || motif.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez un motif!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String etat = (String) cmbEtatProduit.getSelectedItem();
        if (etat == null || etat.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez un état!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idFacture = getIdFactureSelectionne();
        if (idFacture == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez une facture!", "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idRetour = Integer.parseInt(txtIdRetour.getText().trim());

        int confirm = JOptionPane.showConfirmDialog(this,
            "✏️ Confirmer la modification du retour #" + idRetour + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE retour SET motif=?, etat_produit=?, date=?, id_facture=? WHERE id_retour=?";

        try (Connection con = getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, motif);
            ps.setString(2, etat);
            ps.setDate(3, new java.sql.Date(((Date) spinnerDate.getValue()).getTime()));
            ps.setInt(4, idFacture);
            ps.setInt(5, idRetour);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this,
                    "✅ Retour #" + idRetour + " modifié avec succès!",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                consulterRetours();
                clearFields();
                updateStatus("[OK] Retour #" + idRetour + " modifié");
            }
        } catch (SQLException e) { showError(e); }
    }

    
    private void supprimerRetour() {
        if (txtIdRetour.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Sélectionnez un retour dans le tableau!",
                "Requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idRetour = Integer.parseInt(txtIdRetour.getText().trim());

        int confirm = JOptionPane.showConfirmDialog(this,
            "🗑️ ATTENTION !\n\nSupprimer la demande de retour #" + idRetour +
            " ?\n\n⚠️ Action IRRÉVERSIBLE!",
            "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = getCon();
             PreparedStatement ps = con.prepareStatement("DELETE FROM retour WHERE id_retour = ?")) {
            ps.setInt(1, idRetour);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this,
                    "✅ Demande de retour #" + idRetour + " supprimée!",
                    "Supprimé", JOptionPane.INFORMATION_MESSAGE);
                consulterRetours();
                clearFields();
                updateStatus("[OK] Retour #" + idRetour + " supprimé");
            }
        } catch (SQLException e) { showError(e); }
    }

    
    private void rechercherRetour() {
        String recherche = txtRecherche.getText().trim();
        if (recherche.isEmpty()) { consulterRetours(); return; }

        try {
            int idRecherche = Integer.parseInt(recherche);
            modelRetour.setRowCount(0);

            String sql = "SELECT r.id_retour, r.motif, r.etat_produit, r.date, r.id_facture, " +
                         "cl.nom_client, cl.prenom_client " +
                         "FROM retour r " +
                         "LEFT JOIN facture f ON r.id_facture = f.id_facture " +
                         "LEFT JOIN commande c ON f.id_commande = c.id_commande " +
                         "LEFT JOIN client cl ON c.id_client = cl.id_client " +
                         "WHERE r.id_retour = ?";

            try (Connection con = getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idRecherche);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String nom    = rs.getString("nom_client");
                    String prenom = rs.getString("prenom_client");
                    String client = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");

                    modelRetour.addRow(new Object[]{
                        rs.getInt("id_retour"),
                        rs.getString("motif"),
                        rs.getString("etat_produit"),
                        rs.getDate("date"),
                        rs.getInt("id_facture"),
                        client.trim().isEmpty() ? "N/A" : client.trim()
                    });
                    tableRetour.setRowSelectionInterval(0, 0);
                    remplirFormulaireDepuisTable(0);
                    updateStatus("[OK] Retour #" + idRecherche + " trouvé");
                } else {
                    JOptionPane.showMessageDialog(this,
                        "🔍 Aucun retour trouvé avec l'ID: " + idRecherche,
                        "Résultat", JOptionPane.INFORMATION_MESSAGE);
                    consulterRetours();
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

    private JPanel statusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(232, 245, 233));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 220, 200)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        lblStatus = new JLabel("🔄 Système prêt - Gestion des Réclamations & Retours | Base: soktrapa");
        lblStatus.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblStatus.setForeground(VERT_FONCE);

        p.add(lblStatus, BorderLayout.WEST);
        return p;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(BLANC);
        btn.setBackground(new Color(255, 255, 255, 30));
        btn.setMaximumSize(new Dimension(210, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(VERT_HOVER);
                btn.setOpaque(true);
                btn.repaint();
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 30));
                btn.setOpaque(false);
                btn.repaint();
            }
        });

        return btn;
    }

    private JButton createActionButton(String text, Color normalColor) {
        JButton btn = new JButton(text);
        btn.setBackground(normalColor);
        btn.setForeground(BLANC);
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
        JPanel p = new JPanel(new BorderLayout(4, 3));
        p.setBackground(BLANC);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        l.setForeground(VERT_FONCE);

        field.setPreferredSize(new Dimension(200, 30));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBackground(BLANC);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSpinnerPanel(String label, JSpinner spinner) {
        JPanel p = new JPanel(new BorderLayout(4, 3));
        p.setBackground(BLANC);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        l.setForeground(VERT_FONCE);

        spinner.setPreferredSize(new Dimension(200, 30));
        p.add(l, BorderLayout.NORTH);
        p.add(spinner, BorderLayout.CENTER);
        return p;
    }

    private JPanel createComboPanel(String label, JComboBox<String> combo) {
        JPanel p = new JPanel(new BorderLayout(4, 3));
        p.setBackground(BLANC);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        l.setForeground(VERT_FONCE);

        combo.setPreferredSize(new Dimension(200, 30));
        p.add(l, BorderLayout.NORTH);
        p.add(combo, BorderLayout.CENTER);
        return p;
    }

    private Connection getCon() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    private void updateStatus(String message) { lblStatus.setText(message); }

    private void clearFields() {
        txtIdRetour.setText("");
        txtRecherche.setText("");
        spinnerDate.setValue(new Date());
        cmbMotif.setSelectedIndex(0);
        cmbEtatProduit.setSelectedIndex(0);
        if (cmbFacture.getItemCount() > 0) cmbFacture.setSelectedIndex(0);
        lblInfoFacture.setText("<html><i>Sélectionnez une facture...</i></html>");
        lblInfoFacture.setBackground(new Color(240, 255, 240));
        tableRetour.clearSelection();
        tableFacture.clearSelection();
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this,
            "❌ ERREUR: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    private void retourMenu() {
        this.dispose();
        new MenuPrincipal().setVisible(true);
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Reclamation().setVisible(true));
    }
}