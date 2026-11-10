/*
 * SOKTRAPA - end-to-end integration test of the Swing modules against a real MySQL/MariaDB.
 *
 * It instantiates the real JFrame classes (LoginSoktrapa, MenuPrincipal, GestionClients,
 * GestionCommandes, GestionStock, GestionPaiement, Reclamation), fills their form fields
 * through reflection, invokes their private CRUD handlers and checks the resulting rows
 * with SQL. Confirmation / message dialogs are answered automatically ("Yes"/"OK").
 *
 * The "Regression tests" section (R1-R12) covers order/stock consistency (order deletion,
 * line modification, rollbacks, stale screens, several depots) and delete safety
 * (ON DELETE RESTRICT on customers, orders and invoices).
 *
 * Requirements: database created with database/schema.sql + database/sample_data.sql
 * (the test expects the demo dataset and restores it at the end, so it can be re-run),
 * and a display (xvfb-run is used automatically on headless Linux).
 *
 * Run: tests/run-integration-tests.sh
 */
import java.awt.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import projet.*;
import projet.model.ControleStock;
import projet.service.CommandeService;

public class SwingIntegrationTest {
    static final List<String> dialogs = Collections.synchronizedList(new ArrayList<>());
    static int pass = 0, fail = 0;

    public static void main(String[] a) throws Exception {
      try { run(); } catch (Throwable e) { e.printStackTrace(); fail++; }
      System.out.println("\nRESULT: " + pass + " passed, " + fail + " failed");
      System.exit(fail == 0 ? 0 : 1);
    }
    static void run() throws Exception {
        Thread t = new Thread(SwingIntegrationTest::autoAnswer); t.setDaemon(true); t.start();

        // ---- Login
        LoginSoktrapa login = onEdt(LoginSoktrapa::new);
        check("login: demo credentials accepted", (Boolean) call(login, "verifyCredentials", "admin", "Soktrapa@Demo2026"));
        check("login: wrong password rejected", !(Boolean) call(login, "verifyCredentials", "admin", "wrong"));
        check("login: unknown user rejected", !(Boolean) call(login, "verifyCredentials", "nobody", "x"));
        check("login: SQL-injection-style input rejected", !(Boolean) call(login, "verifyCredentials", "admin' OR '1'='1", "' OR '1'='1"));
        onEdtRun(login::dispose);

        onEdtRun(() -> new MenuPrincipal().dispose());
        check("menu: main menu builds", true);

        // ---- Customers
        GestionClients gc = onEdt(GestionClients::new);
        check("clients: 4 rows loaded", model(gc, "model").getRowCount() == 4);
        text(gc, "txtId", "90"); text(gc, "txtNom", "Test"); text(gc, "txtPrenom", "Client");
        text(gc, "txtAdresse", "Sfax"); text(gc, "txtTelephone", "+216 70 000 090"); text(gc, "txtEmail", "t@example.com");
        combo(gc, "cmbTypeClient", "Grossiste"); combo(gc, "cmbConditionPaiement", "30 jours"); text(gc, "txtPlafondCredit", "1500");
        call(gc, "ajouterClient");
        check("clients: create", q("SELECT COUNT(*) FROM client WHERE id_client=90 AND type_client='Grossiste'") == 1);
        text(gc, "txtId", "90"); text(gc, "txtNom", "Test2"); combo(gc, "cmbTypeClient", "Entreprise");
        call(gc, "modifierClient");
        check("clients: update", q("SELECT COUNT(*) FROM client WHERE id_client=90 AND nom_client='Test2'") == 1);
        text(gc, "txtRecherche", "Test2"); call(gc, "rechercher");
        check("clients: search", model(gc, "model").getRowCount() == 1);
        text(gc, "txtId", "90"); call(gc, "supprimerClient");
        check("clients: delete", q("SELECT COUNT(*) FROM client WHERE id_client=90") == 0);
        text(gc, "txtId", "abc"); text(gc, "txtNom", "X"); combo(gc, "cmbTypeClient", "Grossiste");
        dialogs.clear(); call(gc, "ajouterClient");
        check("clients: non-numeric id rejected with message", last().contains("nombre"));
        onEdtRun(gc::dispose);

        // ---- Orders + stock control
        GestionCommandes go = onEdt(GestionCommandes::new);
        call(go, "loadDataCommandes");
        check("orders: 9 orders loaded", model(go, "modelCommande").getRowCount() == 9);
        text(go, "txtIdCommande", "100"); comboPrefix(go, "cmbClient", "3 - "); text(go, "txtRemise", "5");
        combo(go, "cmbStatut", "En attente");
        call(go, "ajouterCommande");
        check("orders: create", q("SELECT COUNT(*) FROM commande WHERE id_commande=100 AND id_client=3") == 1);
        int before = q("SELECT qte_disponible FROM depot WHERE id_depot=4");
        text(go, "txtIdCommande", "100"); comboPrefix(go, "cmbProduit", "4 - "); text(go, "txtQteCommande", "200");
        call(go, "ajouterLigneCommande");
        check("orders: add line", q("SELECT qte_commande FROM ligne_commande WHERE id_commande=100 AND id_produit=4") == 200);
        check("orders: stock decreased by 200", q("SELECT qte_disponible FROM depot WHERE id_depot=4") == before - 200);
        text(go, "txtIdCommande", "100"); comboPrefix(go, "cmbProduit", "1 - "); text(go, "txtQteCommande", "999999");
        dialogs.clear(); call(go, "ajouterLigneCommande");
        check("orders: insufficient stock blocked", q("SELECT COUNT(*) FROM ligne_commande WHERE id_commande=100 AND id_produit=1") == 0
              && last().contains("STOCK INSUFFISANT"));
        call(go, "loadLignesCommande", 100);
        selectRow(go, "tableLigneCommande", 0);
        call(go, "supprimerLigneCommande");
        check("orders: delete line restores stock", q("SELECT qte_disponible FROM depot WHERE id_depot=4") == before
              && q("SELECT COUNT(*) FROM ligne_commande WHERE id_commande=100") == 0);
        text(go, "txtIdCommande", "100"); combo(go, "cmbStatut", "Confirmée"); comboPrefix(go, "cmbClient", "3 - ");
        call(go, "modifierCommande");
        check("orders: update status", q("SELECT COUNT(*) FROM commande WHERE id_commande=100 AND statut='Confirmée'") == 1);
        text(go, "txtIdCommande", "100"); call(go, "supprimerCommande");
        check("orders: delete", q("SELECT COUNT(*) FROM commande WHERE id_commande=100") == 0);
        onEdtRun(go::dispose);

        // ---- Stock
        GestionStock gs = onEdt(GestionStock::new);
        call(gs, "consulterStock");
        check("stock: 8 depots listed", model(gs, "model").getRowCount() == 8);
        check("stock: product/depot mapping listed", model(gs, "modelStockage").getRowCount() == 8);
        text(gs, "txtIdDepot", "2"); text(gs, "txtSortie", "100"); call(gs, "sortieStock");
        check("stock: exit -100", q("SELECT qte_disponible FROM depot WHERE id_depot=2") == 1900);
        text(gs, "txtIdDepot", "2"); text(gs, "txtSortie", "999999"); call(gs, "sortieStock");
        check("stock: exit above stock refused", q("SELECT qte_disponible FROM depot WHERE id_depot=2") == 1900);
        text(gs, "txtIdDepot", "2"); text(gs, "txtQteReelle", "2000"); call(gs, "inventaireStock");
        check("stock: inventory adjustment", q("SELECT qte_disponible FROM depot WHERE id_depot=2") == 2000);
        text(gs, "txtIdVerif", "2"); dialogs.clear(); call(gs, "verifierDisponibilite");
        check("stock: availability check answers", !dialogs.isEmpty());
        onEdtRun(gs::dispose);

        // ---- Invoices & payments
        GestionPaiement gp = onEdt(GestionPaiement::new);
        call(gp, "consulterPaiements");
        check("payments: 5 rows loaded", model(gp, "modelPaiement").getRowCount() == 5);
        comboPrefix(gp, "cmbCommande", "70 - "); call(gp, "calculerFactureDetaillee");
        String ttc = ((JTextField) get(gp, "txtMontantFacture")).getText().replace(",", ".");
        check("invoice: NET TTC computed (4000*2.9*0.9*1.07=11170.80) got " + ttc, ttc.equals("11170.80"));
        text(gp, "txtIdFacture", "200"); call(gp, "creerFacture");
        check("invoice: create", q("SELECT COUNT(*) FROM facture WHERE id_facture=200 AND id_commande=70") == 1);
        call(gp, "chargerFacturesCombo");
        text(gp, "txtIdPaie", "300"); text(gp, "txtMontant", "1000"); comboPrefix(gp, "cmbFacture", "200 - ");
        combo(gp, "cmbStatut", "Partiel"); combo(gp, "cmbMode", "Chèque");
        call(gp, "ajouterPaiement");
        check("payments: create", q("SELECT COUNT(*) FROM paiement WHERE id_paie=300 AND id_facture=200") == 1);
        text(gp, "txtIdPaie", "300"); text(gp, "txtMontant", "11170.80"); comboPrefix(gp, "cmbFacture", "200 - "); combo(gp, "cmbStatut", "Payé");
        call(gp, "modifierPaiement");
        check("payments: update status", q("SELECT COUNT(*) FROM paiement WHERE id_paie=300 AND statut_paie='Payé'") == 1);
        text(gp, "txtRecherche", "300"); call(gp, "rechercherPaiement");
        check("payments: search by id", model(gp, "modelPaiement").getRowCount() == 1);
        text(gp, "txtIdPaie", "300"); call(gp, "supprimerPaiement");
        check("payments: delete", q("SELECT COUNT(*) FROM paiement WHERE id_paie=300") == 0);
        onEdtRun(gp::dispose);

        // ---- Returns
        Reclamation rc = onEdt(Reclamation::new);
        call(rc, "consulterRetours");
        check("returns: 2 rows loaded", model(rc, "modelRetour").getRowCount() == 2);
        call(rc, "chargerFacturesCombo");
        text(rc, "txtIdRetour", "50"); combo(rc, "cmbMotif", "Produit défectueux"); combo(rc, "cmbEtatProduit", "Endommagé");
        comboPrefix(rc, "cmbFacture", "200 - ");
        call(rc, "soumettreDemande");
        check("returns: create linked to invoice", q("SELECT COUNT(*) FROM retour WHERE id_retour=50 AND id_facture=200") == 1);
        text(rc, "txtIdRetour", "50"); combo(rc, "cmbEtatProduit", "Inutilisable"); comboPrefix(rc, "cmbFacture", "200 - "); combo(rc, "cmbMotif", "Produit défectueux");
        call(rc, "modifierRetour");
        check("returns: update", q("SELECT COUNT(*) FROM retour WHERE id_retour=50 AND etat_produit='Inutilisable'") == 1);
        text(rc, "txtRecherche", "50"); call(rc, "rechercherRetour");
        check("returns: search", model(rc, "modelRetour").getRowCount() == 1);
        text(rc, "txtIdRetour", "50"); call(rc, "supprimerRetour");
        check("returns: delete", q("SELECT COUNT(*) FROM retour WHERE id_retour=50") == 0);
        onEdtRun(rc::dispose);


        // =====================================================================
        // Regression tests: order / stock consistency and delete safety
        // =====================================================================
        int totalInitial = q("SELECT SUM(qte_disponible) FROM depot");
        int d1 = stock(1), d2 = stock(2), d4 = stock(4), d8 = stock(8);
        CommandeService service = new CommandeService();
        GestionCommandes rg = onEdt(GestionCommandes::new);

        // R1 - deleting an order restores the stock of ALL its lines
        creerCommande(rg, 101);
        ajouterLigne(rg, 101, 4, 300);
        ajouterLigne(rg, 101, 8, 200);
        check("R1 setup: two lines reserved", stock(4) == d4 - 300 && stock(8) == d8 - 200);
        text(rg, "txtIdCommande", "101"); dialogs.clear(); call(rg, "supprimerCommande");
        check("R1 order deletion removes order and lines",
              q("SELECT COUNT(*) FROM commande WHERE id_commande=101") == 0
              && q("SELECT COUNT(*) FROM ligne_commande WHERE id_commande=101") == 0);
        check("R1 order deletion restores stock of every line (was: stock lost)",
              stock(4) == d4 && stock(8) == d8);
        check("R1 user is told how much stock was restored", last().contains("+500 kg"));

        // R2 - quantity change on a line adjusts stock by the difference, atomically
        creerCommande(rg, 101);
        ajouterLigne(rg, 101, 4, 300);
        modifierLigne(rg, 101, 4, 4, 500);
        check("R2 quantity increase reserves the difference", qteLigne(101, 4) == 500 && stock(4) == d4 - 500);
        modifierLigne(rg, 101, 4, 4, 100);
        check("R2 quantity decrease releases the difference", qteLigne(101, 4) == 100 && stock(4) == d4 - 100);
        dialogs.clear(); modifierLigne(rg, 101, 4, 4, 999999);
        check("R2 increase beyond stock is refused, line and stock unchanged",
              qteLigne(101, 4) == 100 && stock(4) == d4 - 100 && last().contains("STOCK INSUFFISANT"));

        // R3 - product change: old product released, new one reserved, or nothing at all
        modifierLigne(rg, 101, 4, 8, 150);
        check("R3 product change moves the reservation",
              qteLigne(101, 4) == -1 && qteLigne(101, 8) == 150 && stock(4) == d4 && stock(8) == d8 - 150);
        dialogs.clear(); modifierLigne(rg, 101, 8, 1, 5000);
        check("R3 refused product change rolls back the release of the old product",
              qteLigne(101, 8) == 150 && qteLigne(101, 1) == -1 && stock(8) == d8 - 150 && stock(1) == d1
              && last().contains("STOCK INSUFFISANT"));

        // R4 - a failure in the middle of the transaction (duplicate line) changes nothing
        ajouterLigne(rg, 101, 2, 50);
        dialogs.clear(); modifierLigne(rg, 101, 8, 2, 10);
        check("R4 duplicate-line error rolls back lines and stock",
              qteLigne(101, 8) == 150 && qteLigne(101, 2) == 50 && stock(8) == d8 - 150 && stock(2) == d2 - 50
              && last().contains("existe"));

        // R5 / R6 - the screen is stale (line changed by another session): the quantity
        // restored / adjusted must come from the database, not from the JTable
        call(rg, "loadLignesCommande", 101);                    // table shows 8 x 150, 2 x 50
        service.modifierLigne(101, 8, 8, 400);                  // "other session"
        service.modifierLigne(101, 2, 2, 80);
        selectLigne(rg, 8); call(rg, "supprimerLigneCommande"); // rebuilds the table
        check("R5 deleting a stale line restores the real quantity (was: displayed quantity)",
              qteLigne(101, 8) == -1 && stock(8) == d8);
        service.modifierLigne(101, 2, 2, 90);                   // table now shows 2 x 80
        selectLigne(rg, 2); comboPrefix(rg, "cmbProduit", "2 - "); text(rg, "txtQteCommande", "60");
        call(rg, "modifierLigneCommande");
        check("R6 modifying a stale line uses the real quantity", qteLigne(101, 2) == 60 && stock(2) == d2 - 60);

        // R7 - product stored in two depots: exactly one depot is updated, total is conserved
        exec("INSERT INTO stocker (id_produit, id_depot) VALUES (4, 8)");
        try {
            int dep4 = q("SELECT qte_disponible FROM depot WHERE id_depot=4");
            int dep8 = q("SELECT qte_disponible FROM depot WHERE id_depot=8");
            ajouterLigne(rg, 101, 4, 100);
            check("R7 multi-depot: only the selected depot is decreased (was: every depot)",
                  q("SELECT qte_disponible FROM depot WHERE id_depot=4") == dep4 - 100
                  && q("SELECT qte_disponible FROM depot WHERE id_depot=8") == dep8);
            call(rg, "loadLignesCommande", 101); selectLigne(rg, 4); call(rg, "supprimerLigneCommande");
            check("R7 multi-depot: restoration updates one depot, total conserved (was: every depot)",
                  q("SELECT qte_disponible FROM depot WHERE id_depot=4") == dep4
                  && q("SELECT qte_disponible FROM depot WHERE id_depot=8") == dep8);
        } finally {
            exec("DELETE FROM stocker WHERE id_produit=4 AND id_depot=8");
        }

        // R8 - an invoiced order cannot be deleted; nothing is changed
        int lignesCmd1 = q("SELECT COUNT(*) FROM ligne_commande WHERE id_commande=1");
        int totalAvant = q("SELECT SUM(qte_disponible) FROM depot");
        text(rg, "txtIdCommande", "1"); dialogs.clear(); call(rg, "supprimerCommande");
        check("R8 invoiced order is protected (was: invoices, payments and returns cascade-deleted)",
              q("SELECT COUNT(*) FROM commande WHERE id_commande=1") == 1
              && q("SELECT COUNT(*) FROM facture WHERE id_commande=1") == 1
              && q("SELECT COUNT(*) FROM ligne_commande WHERE id_commande=1") == lignesCmd1
              && q("SELECT SUM(qte_disponible) FROM depot") == totalAvant
              && last().contains("facture"));

        // R9 - a customer with orders cannot be deleted
        GestionClients rc2 = onEdt(GestionClients::new);
        text(rc2, "txtId", "2"); dialogs.clear(); call(rc2, "supprimerClient");
        check("R9 customer with orders is protected (was: whole history cascade-deleted)",
              q("SELECT COUNT(*) FROM client WHERE id_client=2") == 1
              && q("SELECT COUNT(*) FROM commande WHERE id_client=2") == 3
              && last().contains("Impossible"));
        onEdtRun(rc2::dispose);

        // R10 - a customer without orders can still be deleted (feature preserved)
        exec("INSERT INTO client (id_client, nom_client, adress_client) VALUES (91, 'Temp', 'Sfax')");
        GestionClients rc3 = onEdt(GestionClients::new);
        text(rc3, "txtId", "91"); call(rc3, "supprimerClient");
        check("R10 customer without orders can be deleted", q("SELECT COUNT(*) FROM client WHERE id_client=91") == 0);
        onEdtRun(rc3::dispose);

        // R11 - service-level results
        check("R11 service: deleting an unknown order reports 'not found'", !service.supprimerCommande(987654).estSupprimee());
        check("R11 service: deleting an unknown line reports -1", service.supprimerLigne(101, 7) == -1);
        check("R11 service: product without depot is refused",
              stockerSansDepot(service));

        // R12 - final order deletion returns every depot to its initial quantity
        text(rg, "txtIdCommande", "101"); call(rg, "supprimerCommande");
        check("R12 all depots back to their initial quantities",
              stock(1) == d1 && stock(2) == d2 && stock(4) == d4 && stock(8) == d8
              && q("SELECT SUM(qte_disponible) FROM depot") == totalInitial
              && q("SELECT COUNT(*) FROM ligne_commande") == 18);
        onEdtRun(rg::dispose);

        // ---- Cleanup: remove the invoice created by this test so the run is repeatable
        exec("DELETE FROM facture WHERE id_facture=200");
        check("cleanup: demo dataset restored", q("SELECT COUNT(*) FROM facture") == 5
              && q("SELECT qte_disponible FROM depot WHERE id_depot=2") == 2000
              && q("SELECT qte_disponible FROM depot WHERE id_depot=4") == 18425);

        System.out.println("\nRESULT: " + pass + " passed, " + fail + " failed");
        System.exit(fail == 0 ? 0 : 1);
    }

    // ---------- helpers
    /**
     * Answers every JOptionPane dialog with "Yes"/"OK" and records its message.
     * A dialog is answered only once it has been visible for a short while: Swing resets
     * the option pane value in its componentShown handler, so answering earlier could be
     * overwritten and read back as "dialog closed".
     */
    static void autoAnswer() {
        Map<JDialog, Long> vus = new HashMap<>();
        Set<JDialog> repondus = new HashSet<>();
        while (true) {
            try { Thread.sleep(50); } catch (InterruptedException e) { return; }
            long maintenant = System.currentTimeMillis();
            for (Window w : Window.getWindows()) {
                if (!(w instanceof JDialog d) || !d.isVisible() || repondus.contains(d)) continue;
                long premierPassage = vus.computeIfAbsent(d, k -> maintenant);
                if (maintenant - premierPassage < 250) continue;
                JOptionPane p = find(d.getContentPane());
                if (p == null) continue;
                repondus.add(d);
                dialogs.add(String.valueOf(p.getMessage()));
                SwingUtilities.invokeLater(() -> p.setValue(JOptionPane.YES_OPTION));
            }
            vus.keySet().removeIf(d -> !d.isDisplayable());
            repondus.removeIf(d -> !d.isDisplayable());
        }
    }
    static JOptionPane find(Container c) {
        for (Component k : c.getComponents()) {
            if (k instanceof JOptionPane p) return p;
            if (k instanceof Container cc) { JOptionPane p = find(cc); if (p != null) return p; }
        }
        return null;
    }
    static String last() { synchronized (dialogs) { return dialogs.isEmpty() ? "" : String.join(" | ", dialogs); } }
    static void check(String name, boolean ok) {
        if (ok) pass++; else fail++;
        System.out.println((ok ? "PASS " : "FAIL ") + name + (ok ? "" : "   dialogs=" + last().replace('\n', ' ')));
    }
    static int q(String sql) throws SQLException {
        try (Connection c = DatabaseConfig.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : -1;
        }
    }
    // ---------- regression helpers
    static int stock(int idProduit) throws SQLException {
        return q("SELECT COALESCE(SUM(d.qte_disponible),0) FROM stocker s JOIN depot d ON d.id_depot=s.id_depot WHERE s.id_produit=" + idProduit);
    }
    static int qteLigne(int idCommande, int idProduit) throws SQLException {
        return q("SELECT qte_commande FROM ligne_commande WHERE id_commande=" + idCommande + " AND id_produit=" + idProduit);
    }
    static void creerCommande(Object fenetre, int id) throws Exception {
        text(fenetre, "txtIdCommande", String.valueOf(id)); comboPrefix(fenetre, "cmbClient", "3 - ");
        text(fenetre, "txtRemise", "0"); combo(fenetre, "cmbStatut", "En attente");
        call(fenetre, "ajouterCommande");
    }
    static void ajouterLigne(Object fenetre, int idCommande, int idProduit, int qte) throws Exception {
        text(fenetre, "txtIdCommande", String.valueOf(idCommande));
        comboPrefix(fenetre, "cmbProduit", idProduit + " - "); text(fenetre, "txtQteCommande", String.valueOf(qte));
        call(fenetre, "ajouterLigneCommande");
    }
    /** Reloads the lines of the order, selects the line of idProduitActuel and applies the change through the UI. */
    static void modifierLigne(Object fenetre, int idCommande, int idProduitActuel, int idProduitNouveau, int qte) throws Exception {
        call(fenetre, "loadLignesCommande", idCommande);
        selectLigne(fenetre, idProduitActuel);
        comboPrefix(fenetre, "cmbProduit", idProduitNouveau + " - "); text(fenetre, "txtQteCommande", String.valueOf(qte));
        call(fenetre, "modifierLigneCommande");
    }
    static void selectLigne(Object fenetre, int idProduit) throws Exception {
        JTable t = (JTable) get(fenetre, "tableLigneCommande");
        for (int r = 0; r < t.getModel().getRowCount(); r++) {
            if (Integer.parseInt(t.getModel().getValueAt(r, 1).toString()) == idProduit) {
                int v = t.convertRowIndexToView(r); t.setRowSelectionInterval(v, v); return;
            }
        }
        throw new IllegalStateException("no displayed line for product " + idProduit);
    }
    /** Product 6 is temporarily detached from its depot: a reservation must be refused, nothing inserted. */
    static boolean stockerSansDepot(CommandeService service) throws Exception {
        exec("DELETE FROM stocker WHERE id_produit=6 AND id_depot=6");
        try {
            ControleStock c = service.ajouterLigne(101, 6, 10);
            return c.getStatut() == ControleStock.Statut.AUCUN_DEPOT && !c.estAccepte() && qteLigne(101, 6) == -1;
        } finally {
            exec("INSERT INTO stocker (id_produit, id_depot) VALUES (6, 6)");
        }
    }
    static void exec(String sql) throws SQLException {
        try (Connection c = DatabaseConfig.getConnection(); Statement s = c.createStatement()) { s.executeUpdate(sql); }
    }
    static <T> T onEdt(java.util.concurrent.Callable<T> f) throws Exception {
        Object[] out = new Object[1];
        SwingUtilities.invokeAndWait(() -> { try { out[0] = f.call(); } catch (Exception e) { throw new RuntimeException(e); } });
        @SuppressWarnings("unchecked") T t = (T) out[0]; return t;
    }
    static void onEdtRun(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
    static Field field(Object o, String n) throws Exception {
        Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f;
    }
    static Object get(Object o, String n) throws Exception { return field(o, n).get(o); }
    static DefaultTableModel model(Object o, String n) throws Exception { return (DefaultTableModel) get(o, n); }
    static void text(Object o, String n, String v) throws Exception { ((JTextField) get(o, n)).setText(v); }
    static void combo(Object o, String n, String v) throws Exception {
        JComboBox<?> c = (JComboBox<?>) get(o, n);
        for (int i = 0; i < c.getItemCount(); i++) if (String.valueOf(c.getItemAt(i)).equals(v)) { c.setSelectedIndex(i); return; }
        throw new IllegalStateException(n + " has no item " + v);
    }
    static void comboPrefix(Object o, String n, String prefix) throws Exception {
        JComboBox<?> c = (JComboBox<?>) get(o, n);
        for (int i = 0; i < c.getItemCount(); i++) if (String.valueOf(c.getItemAt(i)).startsWith(prefix)) { c.setSelectedIndex(i); return; }
        List<String> items = new ArrayList<>(); for (int i = 0; i < c.getItemCount(); i++) items.add(String.valueOf(c.getItemAt(i)));
        throw new IllegalStateException(n + " has no item starting with " + prefix + " in " + items);
    }
    static void selectRow(Object o, String n, int row) throws Exception {
        JTable t = (JTable) get(o, n); t.setRowSelectionInterval(row, row);
    }
    static Object call(Object o, String name, Object... args) throws Exception {
        for (Method m : o.getClass().getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                m.setAccessible(true);
                try { return m.invoke(o, args); }
                catch (InvocationTargetException e) { throw new RuntimeException(name + " threw", e.getCause()); }
            }
        }
        throw new NoSuchMethodException(name);
    }
}
