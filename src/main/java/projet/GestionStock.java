package projet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class GestionStock extends JFrame {

    
    // JDBC settings are loaded from config/database.properties (see DatabaseConfig).
    private JTable table;
    private DefaultTableModel model;

    
    private JTable tableStockage;
    private DefaultTableModel modelStockage;

    private JTextField txtIdDepot, txtQteReelle, txtSortie, txtIdVerif;
    private JLabel lblStatus;

    
    private JPanel panelFormulaire;
    private JPanel panelIdDepot, panelQteReelle, panelSortie, panelIdVerif;
    private JPanel panelBoutons;
    private JButton btnAction;
    private JLabel formTitle;

    
    private String modeActuel = "CONSULTER";

    
    private final Color VERT_FONCE = new Color(27, 94, 32);
    private final Color VERT_MOYEN = new Color(56, 142, 60);
    private final Color VERT_CLAIR = new Color(76, 175, 80);
    private final Color VERT_HOVER = new Color(46, 125, 50);
    private final Color VERT_HEADER = new Color(33, 97, 47);
    private final Color BLEU_HEADER = new Color(25, 118, 210);  // Couleur pour header table Stockage
    private final Color GRIS_FOND = new Color(245, 245, 245);
    private final Color GRIS_CLAIR = new Color(240, 240, 240);
    private final Color GRIS_TABLE = new Color(250, 250, 250);
    private final Color GRIS_LIGNE = new Color(235, 235, 235);

    public GestionStock() {

        setTitle("SOKTRAPA - Gestion Stock des Produits Finis");
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        
        chargerDriver();

        add(sidebar(), BorderLayout.WEST);
        add(mainPanel(), BorderLayout.CENTER);
        add(statusBar(), BorderLayout.SOUTH);

        
        afficherModeConsulter();
        consulterStock();
        chargerStockage();  
    }


    private void chargerDriver() {
        try {
            if (!DatabaseConfig.loadDriver()) throw new ClassNotFoundException(DatabaseConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                "[ERREUR] Driver MySQL introuvable!\n\n" +
                "Assurez-vous que le fichier mysql-connector-java.jar\n" +
                "est dans le classpath du projet.",
                "Erreur Driver",
                JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(230, 800));
        p.setBackground(VERT_FONCE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        
        JLabel logo = new JLabel("[ SOKTRAPA ]");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 22));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Gestion Stock PF");
        subtitle.setForeground(new Color(200, 230, 200));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 2));
        sep.setForeground(VERT_CLAIR);

        
        JButton btnConsulter = createMenuButton("[>>] Consulter Stock");
        JButton btnInventaire = createMenuButton("[#] Inventaire Stock");
        JButton btnSortie = createMenuButton("[-] Sortie Stock");
        JButton btnVerifier = createMenuButton("[?] Verifier Dispo");
        JButton btnActualiser = createMenuButton("[O] Actualiser");
        JButton btnMenu = createMenuButton("[<] Menu Principal");

        
        btnConsulter.addActionListener(e -> afficherModeConsulter());
        btnInventaire.addActionListener(e -> afficherModeInventaire());
        btnSortie.addActionListener(e -> afficherModeSortie());
        btnVerifier.addActionListener(e -> afficherModeVerifier());
        btnActualiser.addActionListener(e -> {
            consulterStock();
            chargerStockage();
        });
        btnMenu.addActionListener(e -> retourMenu());

        
        p.add(Box.createVerticalStrut(30));
        p.add(logo);
        p.add(Box.createVerticalStrut(5));
        p.add(subtitle);
        p.add(Box.createVerticalStrut(20));
        p.add(sep);
        p.add(Box.createVerticalStrut(30));
        p.add(btnConsulter);
        p.add(Box.createVerticalStrut(10));
        p.add(btnInventaire);
        p.add(Box.createVerticalStrut(10));
        p.add(btnSortie);
        p.add(Box.createVerticalStrut(10));
        p.add(btnVerifier);
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

        JLabel title = new JLabel("   [*] Gestion du Stock - Produits Finis");
        title.setFont(new Font("Arial", Font.BOLD, 24));
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

        
        formTitle = new JLabel("[>>] Consultation Stock");
        formTitle.setFont(new Font("Arial", Font.BOLD, 16));
        formTitle.setForeground(VERT_MOYEN);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

       
        txtIdDepot = new JTextField();
        txtQteReelle = new JTextField();
        txtSortie = new JTextField();
        txtIdVerif = new JTextField();

      
        panelIdDepot = createFieldPanel("[1] ID Depot", txtIdDepot);
        panelQteReelle = createFieldPanel("[#] Stock Reel (Inventaire)", txtQteReelle);
        panelSortie = createFieldPanel("[-] Quantite Sortie", txtSortie);
        panelIdVerif = createFieldPanel("[?] ID a Verifier", txtIdVerif);

        
        btnAction = createActionButton("[>>] Consulter Stock", new Color(33, 150, 243), new Color(25, 118, 210));
        btnAction.addActionListener(e -> executerAction());

       
        panelBoutons = new JPanel();
        panelBoutons.setLayout(new BoxLayout(panelBoutons, BoxLayout.Y_AXIS));
        panelBoutons.setBackground(GRIS_CLAIR);
        panelBoutons.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        construireFormulaire();

        return panelFormulaire;
    }

    
    private void construireFormulaire() {
        panelFormulaire.removeAll();
        
        panelFormulaire.add(formTitle);
        panelFormulaire.add(Box.createVerticalStrut(20));

        switch (modeActuel) {
            case "CONSULTER":
                JLabel lblInfo = new JLabel("<html><center>Cliquez sur le bouton ci-dessous<br>pour consulter le stock disponible.</center></html>");
                lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
                lblInfo.setForeground(new Color(100, 100, 100));
                lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelFormulaire.add(lblInfo);
                break;

            case "INVENTAIRE":
                panelFormulaire.add(panelIdDepot);
                panelFormulaire.add(Box.createVerticalStrut(10));
                panelFormulaire.add(panelQteReelle);
                break;

            case "SORTIE":
                panelFormulaire.add(panelIdDepot);
                panelFormulaire.add(Box.createVerticalStrut(10));
                panelFormulaire.add(panelSortie);
                break;

            case "VERIFIER":
                panelFormulaire.add(panelIdVerif);
                break;
        }

        panelFormulaire.add(Box.createVerticalStrut(30));
        panelFormulaire.add(btnAction);
        panelFormulaire.add(Box.createVerticalGlue());

        panelFormulaire.revalidate();
        panelFormulaire.repaint();
    }

    
    private void afficherModeConsulter() {
        modeActuel = "CONSULTER";
        formTitle.setText("[>>] Consultation Stock");
        btnAction.setText("[>>] Consulter Stock");
        btnAction.setBackground(new Color(33, 150, 243));
        clearFields();
        construireFormulaire();
        consulterStock();
        chargerStockage();
        updateStatus("[>>] Mode: Consultation du stock");
    }

    private void afficherModeInventaire() {
        modeActuel = "INVENTAIRE";
        formTitle.setText("[#] Inventaire Stock");
        btnAction.setText("[#] Valider Inventaire");
        btnAction.setBackground(new Color(255, 152, 0));
        clearFields();
        construireFormulaire();
        updateStatus("[#] Mode: Inventaire - Saisissez ID Depot et Stock Reel");
    }

    private void afficherModeSortie() {
        modeActuel = "SORTIE";
        formTitle.setText("[-] Sortie Stock");
        btnAction.setText("[-] Effectuer Sortie");
        btnAction.setBackground(new Color(244, 67, 54));
        clearFields();
        construireFormulaire();
        updateStatus("[-] Mode: Sortie Stock - Saisissez ID Depot et Quantite");
    }

    private void afficherModeVerifier() {
        modeActuel = "VERIFIER";
        formTitle.setText("[?] Verifier Disponibilite");
        btnAction.setText("[?] Verifier");
        btnAction.setBackground(new Color(156, 39, 176));
        clearFields();
        construireFormulaire();
        updateStatus("[?] Mode: Verification - Saisissez l'ID a verifier");
    }

    
    private void executerAction() {
        switch (modeActuel) {
            case "CONSULTER":
                consulterStock();
                chargerStockage();
                break;
            case "INVENTAIRE":
                inventaireStock();
                break;
            case "SORTIE":
                sortieStock();
                break;
            case "VERIFIER":
                verifierDisponibilite();
                break;
        }
    }

    
    private JPanel tablesPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 10));
        p.setBackground(GRIS_FOND);
        
        p.add(tableStockPanel());      
        p.add(tableStockagePanel());   
        
        return p;
    }

    
    private JPanel tableStockPanel() {

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GRIS_CLAIR);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        
        JLabel tableTitle = new JLabel("📦 Liste du Stock Disponible");
        tableTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        tableTitle.setForeground(VERT_MOYEN);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        
        String[] cols = {"ID Depot", "Lot", "Lieu", "Mouvement", "Qte Disponible"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(180, 220, 180));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(210, 210, 210));
        table.setBackground(GRIS_TABLE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);
        
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setBackground(VERT_HEADER);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, VERT_FONCE),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                
                return label;
            }
        });

       
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (isSelected) {
                    c.setBackground(new Color(180, 220, 180));
                    c.setForeground(Color.BLACK);
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(GRIS_TABLE);
                    } else {
                        c.setBackground(GRIS_LIGNE);
                    }
                    c.setForeground(new Color(50, 50, 50));
                }
                
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
                
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtIdDepot.setText(model.getValueAt(row, 0).toString());
                txtIdVerif.setText(model.getValueAt(row, 0).toString());
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scroll.getViewport().setBackground(GRIS_CLAIR);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createCornerPanel(VERT_HEADER));

        p.add(tableTitle, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    
    private JPanel tableStockagePanel() {

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GRIS_CLAIR);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

      
        JLabel tableTitle = new JLabel("🏭 Stockage (Produit - Dépôt)");
        tableTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        tableTitle.setForeground(BLEU_HEADER);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        
        String[] cols = {"ID Produit", "Nom Produit", "ID Dépôt", "Lieu Dépôt", "Lot"};
        modelStockage = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableStockage = new JTable(modelStockage);

        
        tableStockage.setRowHeight(28);
        tableStockage.setFont(new Font("Arial", Font.PLAIN, 12));
        tableStockage.setSelectionBackground(new Color(180, 200, 230));
        tableStockage.setSelectionForeground(Color.BLACK);
        tableStockage.setGridColor(new Color(210, 210, 210));
        tableStockage.setBackground(GRIS_TABLE);
        tableStockage.setShowGrid(true);
        tableStockage.setIntercellSpacing(new Dimension(1, 1));

        
        JTableHeader header = tableStockage.getTableHeader();
        header.setPreferredSize(new Dimension(0, 35));
        header.setReorderingAllowed(false);
        
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setBackground(BLEU_HEADER);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(20, 90, 160)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                
                return label;
            }
        });

        
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (isSelected) {
                    c.setBackground(new Color(180, 200, 230));
                    c.setForeground(Color.BLACK);
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(GRIS_TABLE);
                    } else {
                        c.setBackground(GRIS_LIGNE);
                    }
                    c.setForeground(new Color(50, 50, 50));
                }
                
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
                
                return c;
            }
        };

        for (int i = 0; i < tableStockage.getColumnCount(); i++) {
            tableStockage.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        
        tableStockage.getSelectionModel().addListSelectionListener(e -> {
            int row = tableStockage.getSelectedRow();
            if (row >= 0) {
                
                Object idDepotObj = modelStockage.getValueAt(row, 2);
                if (idDepotObj != null) {
                    txtIdDepot.setText(idDepotObj.toString());
                    txtIdVerif.setText(idDepotObj.toString());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableStockage);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scroll.getViewport().setBackground(GRIS_CLAIR);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createCornerPanel(BLEU_HEADER));

        p.add(tableTitle, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    
    private void chargerStockage() {
        modelStockage.setRowCount(0);

        
        String sql = "SELECT s.id_produit, p.nom_produit, s.id_depot, d.lieu_depot, d.lot_depot " +
                     "FROM stocker s " +
                     "LEFT JOIN produit p ON s.id_produit = p.id_produit " +
                     "LEFT JOIN depot d ON s.id_depot = d.id_depot " +
                     "ORDER BY s.id_produit, s.id_depot";

        try (Connection c = getCon();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                modelStockage.addRow(new Object[]{
                    rs.getInt("id_produit"),
                    rs.getString("nom_produit") != null ? rs.getString("nom_produit") : "N/A",
                    rs.getInt("id_depot"),
                    rs.getString("lieu_depot") != null ? rs.getString("lieu_depot") : "N/A",
                    rs.getString("lot_depot")
                });
            }

            if (count == 0) {
                
                chargerStockageSimple();
            }

        } catch (SQLException e) {
            
            chargerStockageSimple();
        }
    }

    
    private void chargerStockageSimple() {
        modelStockage.setRowCount(0);

        String sql = "SELECT * FROM stocker ORDER BY id_produit, id_depot";

        try (Connection c = getCon();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                modelStockage.addRow(new Object[]{
                    rs.getInt("id_produit"),
                    "-",  
                    rs.getInt("id_depot"),
                    "-",  
                    "-"   
                });
            }

        } catch (SQLException e) {
            updateStatus("[X] Erreur chargement table stockage");
            e.printStackTrace();
        }
    }

    
    private JPanel createCornerPanel(Color color) {
        JPanel corner = new JPanel();
        corner.setBackground(color);
        return corner;
    }

    
    private JPanel statusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GRIS_CLAIR);
        p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        lblStatus = new JLabel("[OK] Systeme pret - Connecte a la base de donnees soktrapa");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
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
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(VERT_CLAIR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(VERT_MOYEN);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(VERT_HOVER);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(VERT_CLAIR);
            }
        });

        return btn;
    }

    
    private JButton createActionButton(String text, Color normalColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setBackground(normalColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        final Color[] currentColor = {normalColor};

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(btn.getBackground().darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(currentColor[0]);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(btn.getBackground().darker().darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(btn.getBackground().brighter());
            }
        });

        return btn;
    }

    
    private JPanel createFieldPanel(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(GRIS_CLAIR);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(new Color(60, 60, 60));

        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(new Color(255, 255, 255));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);

        return p;
    }

    
    private Connection getCon() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    
    private void updateStatus(String message) {
        lblStatus.setText(message);
    }

    
    private void clearFields() {
        txtIdDepot.setText("");
        txtQteReelle.setText("");
        txtSortie.setText("");
        txtIdVerif.setText("");
        table.clearSelection();
        tableStockage.clearSelection();
    }

    
    private void consulterStock() {
        updateStatus("[...] Consultation du stock en cours...");
        model.setRowCount(0);

        try (Connection c = getCon();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM depot ORDER BY id_depot")) {

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String mouvement = rs.getString("mouvement_depot");
                
                if (mouvement != null && !mouvement.equalsIgnoreCase("Transfert")) {
                    model.addRow(new Object[]{
                        rs.getInt("id_depot"),
                        rs.getString("lot_depot"),
                        rs.getString("lieu_depot"),
                        mouvement,
                        rs.getInt("qte_disponible")
                    });
                }
            }

            if (model.getRowCount() > 0) {
                updateStatus("[OK] Stock consulte - " + model.getRowCount() + " enregistrement(s)");
            } else {
                updateStatus("[!] Stock indisponible - Aucun produit trouve");
                if (modeActuel.equals("CONSULTER")) {
                    JOptionPane.showMessageDialog(this,
                        "[!] Stock indisponible\n\n" +
                        "Aucun produit trouve dans le depot.",
                        "Stock Vide",
                        JOptionPane.WARNING_MESSAGE);
                }
            }

        } catch (SQLException e) {
            updateStatus("[X] Erreur de connexion a la base de donnees");
            JOptionPane.showMessageDialog(this,
                "[ERREUR] Erreur de consultation:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
    private void inventaireStock() {
        updateStatus("[#] Inventaire en cours...");

        if (txtIdDepot.getText().trim().isEmpty() || txtQteReelle.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "[!] Champs obligatoires:\n\n" +
                "   - ID Depot\n" +
                "   - Stock Reel (Inventaire)",
                "Champs requis",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection c = getCon()) {

            int id = Integer.parseInt(txtIdDepot.getText().trim());
            int stockReel = Integer.parseInt(txtQteReelle.getText().trim());

            PreparedStatement ps = c.prepareStatement(
                "SELECT qte_disponible, lot_depot, lieu_depot FROM depot WHERE id_depot = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int stockEnregistre = rs.getInt("qte_disponible");
                String lot = rs.getString("lot_depot");
                String lieu = rs.getString("lieu_depot");

                if (stockEnregistre != stockReel) {
                    
                    int ecart = stockReel - stockEnregistre;
                    String typeEcart = ecart > 0 ? "EXCEDENT" : "MANQUE";

                    PreparedStatement update = c.prepareStatement(
                        "UPDATE depot SET qte_disponible = ? WHERE id_depot = ?");
                    update.setInt(1, stockReel);
                    update.setInt(2, id);
                    update.executeUpdate();

                    updateStatus("[OK] Inventaire valide - Stock mis a jour");

                    JOptionPane.showMessageDialog(this,
                        "[#] ECART DETECTE!\n\n" +
                        "========================================\n" +
                        "   ID Depot: " + id + "\n" +
                        "   Lot: " + lot + "\n" +
                        "   Lieu: " + lieu + "\n" +
                        "========================================\n" +
                        "   Stock enregistre: " + stockEnregistre + "\n" +
                        "   Stock reel: " + stockReel + "\n" +
                        "   Ecart: " + Math.abs(ecart) + " (" + typeEcart + ")\n" +
                        "========================================\n\n" +
                        "[OK] INVENTAIRE VALIDE\n" +
                        "Stock mis a jour avec succes!",
                        "Inventaire - Ecart Detecte",
                        JOptionPane.WARNING_MESSAGE);

                } else {
                    updateStatus("[OK] Inventaire valide - Stock correct");

                    JOptionPane.showMessageDialog(this,
                        "[OK] STOCK CORRECT!\n\n" +
                        "========================================\n" +
                        "   ID Depot: " + id + "\n" +
                        "   Lot: " + lot + "\n" +
                        "   Lieu: " + lieu + "\n" +
                        "   Quantite: " + stockEnregistre + "\n" +
                        "========================================\n\n" +
                        "Le stock reel correspond au stock enregistre.\n" +
                        "Aucune modification necessaire.",
                        "Inventaire - Aucun Ecart",
                        JOptionPane.INFORMATION_MESSAGE);
                }

                consulterStock();
                chargerStockage();
                clearFields();

            } else {
                JOptionPane.showMessageDialog(this,
                    "[X] Depot non trouve!\n\n" +
                    "L'ID " + id + " n'existe pas dans la base.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "[X] Valeurs invalides!\n\n" +
                "Veuillez saisir des nombres valides.",
                "Erreur de saisie",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            updateStatus("[X] Erreur inventaire");
            JOptionPane.showMessageDialog(this,
                "[ERREUR] Erreur d'inventaire:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
    private void sortieStock() {
        updateStatus("[-] Sortie stock en cours...");

        if (txtIdDepot.getText().trim().isEmpty() || txtSortie.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "[!] Champs obligatoires:\n\n" +
                "   - ID Depot\n" +
                "   - Quantite Sortie",
                "Champs requis",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection c = getCon()) {

            int id = Integer.parseInt(txtIdDepot.getText().trim());
            int qteSortie = Integer.parseInt(txtSortie.getText().trim());

            if (qteSortie <= 0) {
                JOptionPane.showMessageDialog(this,
                    "[X] La quantite de sortie doit etre superieure a 0",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement ps = c.prepareStatement(
                "SELECT qte_disponible, lot_depot, lieu_depot FROM depot WHERE id_depot = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int stockDisponible = rs.getInt("qte_disponible");
                String lot = rs.getString("lot_depot");
                String lieu = rs.getString("lieu_depot");

                if (stockDisponible >= qteSortie) {

                    PreparedStatement update = c.prepareStatement(
                        "UPDATE depot SET qte_disponible = qte_disponible - ? WHERE id_depot = ?");
                    update.setInt(1, qteSortie);
                    update.setInt(2, id);
                    update.executeUpdate();

                    int nouveauStock = stockDisponible - qteSortie;

                    updateStatus("[OK] Sortie reussie - " + qteSortie + " unite(s)");

                    JOptionPane.showMessageDialog(this,
                        "[OK] SORTIE REUSSIE!\n\n" +
                        "========================================\n" +
                        "   ID Depot: " + id + "\n" +
                        "   Lot: " + lot + "\n" +
                        "   Lieu: " + lieu + "\n" +
                        "========================================\n" +
                        "   Quantite sortie: " + qteSortie + "\n" +
                        "   Stock avant: " + stockDisponible + "\n" +
                        "   Stock apres: " + nouveauStock + "\n" +
                        "========================================",
                        "Sortie Stock",
                        JOptionPane.INFORMATION_MESSAGE);

                    consulterStock();
                    chargerStockage();
                    clearFields();

                } else {
                    updateStatus("[X] Stock insuffisant");

                    JOptionPane.showMessageDialog(this,
                        "[X] STOCK INSUFFISANT!\n\n" +
                        "========================================\n" +
                        "   ID Depot: " + id + "\n" +
                        "   Lot: " + lot + "\n" +
                        "   Lieu: " + lieu + "\n" +
                        "========================================\n" +
                        "   Stock disponible: " + stockDisponible + "\n" +
                        "   Quantite demandee: " + qteSortie + "\n" +
                        "   Manque: " + (qteSortie - stockDisponible) + "\n" +
                        "========================================\n\n" +
                        "Impossible d'effectuer cette sortie.",
                        "Stock Insuffisant",
                        JOptionPane.ERROR_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(this,
                    "[X] Produit non trouve!\n\n" +
                    "L'ID " + id + " n'existe pas dans la base.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "[X] Valeurs invalides!\n\n" +
                "Veuillez saisir des nombres valides.",
                "Erreur de saisie",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            updateStatus("[X] Erreur sortie stock");
            JOptionPane.showMessageDialog(this,
                "[ERREUR] Erreur de sortie:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
    private void verifierDisponibilite() {
        updateStatus("[?] Verification de disponibilite...");

        if (txtIdVerif.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "[!] Champ obligatoire:\n\n" +
                "   - ID a Verifier",
                "Champ requis",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection c = getCon()) {

            int id = Integer.parseInt(txtIdVerif.getText().trim());

            PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM depot WHERE id_depot = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int qteDisponible = rs.getInt("qte_disponible");
                String lot = rs.getString("lot_depot");
                String lieu = rs.getString("lieu_depot");
                String mouvement = rs.getString("mouvement_depot");

                if (qteDisponible > 0) {
                    updateStatus("[OK] Produit disponible - Qte: " + qteDisponible);

                    JOptionPane.showMessageDialog(this,
                        "[OK] PRODUIT DISPONIBLE!\n\n" +
                        "========================================\n" +
                        "   ID Depot: " + id + "\n" +
                        "   Lot: " + lot + "\n" +
                        "   Lieu: " + lieu + "\n" +
                        "   Mouvement: " + mouvement + "\n" +
                        "========================================\n" +
                        "   Quantite disponible: " + qteDisponible + "\n" +
                        "========================================",
                        "Disponibilite",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    updateStatus("[!] Produit indisponible - Stock epuise");

                    JOptionPane.showMessageDialog(this,
                        "[!] PRODUIT INDISPONIBLE!\n\n" +
                        "========================================\n" +
                        "   ID Depot: " + id + "\n" +
                        "   Lot: " + lot + "\n" +
                        "   Lieu: " + lieu + "\n" +
                        "========================================\n" +
                        "   Quantite disponible: 0\n" +
                        "========================================\n\n" +
                        "Stock epuise pour ce produit.",
                        "Produit Indisponible",
                        JOptionPane.WARNING_MESSAGE);
                }

            } else {
                updateStatus("[X] Produit non trouve");

                JOptionPane.showMessageDialog(this,
                    "[X] PRODUIT INDISPONIBLE!\n\n" +
                    "L'ID " + id + " n'existe pas dans la base de donnees.",
                    "Produit Indisponible",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "[X] ID invalide!\n\n" +
                "Veuillez saisir un nombre valide.",
                "Erreur de saisie",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            updateStatus("[X] Erreur verification");
            JOptionPane.showMessageDialog(this,
                "[ERREUR] Erreur de verification:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
    private void retourMenu() {
        
            this.dispose();
            new MenuPrincipal().setVisible(true);
        }
    

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GestionStock().setVisible(true);
        });
    }
}