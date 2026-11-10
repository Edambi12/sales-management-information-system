package projet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import projet.DatabaseConfig;
import projet.dao.LigneCommandeDao;
import projet.dao.StockDao;
import projet.model.ControleStock;
import projet.model.ControleStock.Statut;
import projet.model.SuppressionCommande;

/**
 * Business rules that keep order lines and depot stock consistent.
 *
 * <p><b>Invariant:</b> a quantity is reserved (removed from the depot) for as long as the
 * order line exists. Every operation below changes the order line(s) and the stock in
 * <b>one transaction</b>: either both are applied, or neither is.</p>
 *
 * <p>The class has no Swing dependency. It returns value objects ({@link ControleStock},
 * {@link SuppressionCommande}); the calling window displays them <i>after</i> the
 * transaction has ended, so no dialog is ever shown while database rows are locked.</p>
 *
 * <p>Quantities to restore are always read from the database (with a row lock),
 * never taken from what a table on screen happened to display.</p>
 */
public final class CommandeService {

    /** Remaining quantity (kg) at or below which a reorder alert is raised. */
    public static final int SEUIL_ALERTE_COMMANDE = 1000;
    /** Share of the depot stock under which the "stock is being used" notice is raised. */
    public static final double SEUIL_EXPLOITATION = 0.5;

    /** MySQL/MariaDB error: a parent row cannot be deleted because child rows reference it. */
    public static final int ERREUR_LIGNE_REFERENCEE = 1451;

    private final StockDao stockDao = new StockDao();
    private final LigneCommandeDao ligneDao = new LigneCommandeDao();

    /** Adds a line to an order and reserves its quantity. */
    public ControleStock ajouterLigne(int idCommande, int idProduit, int qte) throws SQLException {
        exigerQuantitePositive(qte);
        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try {
                ControleStock controle = reserver(con, idProduit, qte);
                if (!controle.estAccepte()) {
                    con.rollback();
                    return controle;
                }
                ligneDao.inserer(con, idCommande, idProduit, qte);
                con.commit();
                return controle;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Changes the quantity and/or the product of an existing line.
     * <ul>
     *   <li>same product: only the difference is reserved or released;</li>
     *   <li>other product: the old quantity is released, the new one reserved.</li>
     * </ul>
     * If the new reservation is refused, nothing is changed (the release is rolled back too).
     */
    public ControleStock modifierLigne(int idCommande, int idProduitActuel,
                                       int idProduitNouveau, int nouvelleQte) throws SQLException {
        exigerQuantitePositive(nouvelleQte);
        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try {
                Integer qteActuelle = ligneDao.verrouillerQuantite(con, idCommande, idProduitActuel);
                if (qteActuelle == null) {
                    con.rollback();
                    return ControleStock.sansDepot(Statut.LIGNE_INTROUVABLE, idProduitActuel, nouvelleQte);
                }

                ControleStock controle;
                if (idProduitActuel != idProduitNouveau) {
                    stockDao.restaurer(con, idProduitActuel, qteActuelle);
                    controle = reserver(con, idProduitNouveau, nouvelleQte);
                    if (!controle.estAccepte()) {
                        con.rollback();
                        return controle;
                    }
                    ligneDao.supprimer(con, idCommande, idProduitActuel);
                    ligneDao.inserer(con, idCommande, idProduitNouveau, nouvelleQte);
                } else {
                    int difference = nouvelleQte - qteActuelle;
                    if (difference > 0) {
                        controle = reserver(con, idProduitNouveau, difference);
                        if (!controle.estAccepte()) {
                            con.rollback();
                            return controle;
                        }
                    } else {
                        if (difference < 0) {
                            stockDao.restaurer(con, idProduitNouveau, -difference);
                        }
                        controle = ControleStock.sansDepot(Statut.OK, idProduitNouveau, 0);
                    }
                    ligneDao.modifierQuantite(con, idCommande, idProduitNouveau, nouvelleQte);
                }
                con.commit();
                return controle;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Deletes a line and releases its quantity.
     * @return the quantity returned to stock, or -1 if the line no longer exists
     */
    public int supprimerLigne(int idCommande, int idProduit) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try {
                Integer qte = ligneDao.verrouillerQuantite(con, idCommande, idProduit);
                if (qte == null) {
                    con.rollback();
                    return -1;
                }
                ligneDao.supprimer(con, idCommande, idProduit);
                stockDao.restaurer(con, idProduit, qte);
                con.commit();
                return qte;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Deletes an order with all its lines and releases every reserved quantity.
     *
     * <p>If the order is referenced by an invoice the database refuses the deletion
     * (foreign key {@code ON DELETE RESTRICT}); the {@link SQLException} with error code
     * {@link #ERREUR_LIGNE_REFERENCEE} is propagated after a full rollback, so neither the
     * lines nor the stock are touched.</p>
     */
    public SuppressionCommande supprimerCommande(int idCommande) throws SQLException {
        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try {
                Map<Integer, Integer> lignes = ligneDao.verrouillerLignes(con, idCommande);
                int total = 0;
                for (Map.Entry<Integer, Integer> ligne : lignes.entrySet()) {
                    stockDao.restaurer(con, ligne.getKey(), ligne.getValue());
                    total += ligne.getValue();
                }
                ligneDao.supprimerToutes(con, idCommande);

                int supprimees;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM commande WHERE id_commande = ?")) {
                    ps.setInt(1, idCommande);
                    supprimees = ps.executeUpdate();
                }
                if (supprimees == 0) {
                    con.rollback();
                    return new SuppressionCommande(false, 0, 0);
                }
                con.commit();
                return new SuppressionCommande(true, lignes.size(), total);
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** True if the exception means "this row is still referenced by other records". */
    public static boolean estReferencee(SQLException e) {
        return e.getErrorCode() == ERREUR_LIGNE_REFERENCEE;
    }

    /**
     * Controls the stock and, if accepted, removes the quantity from the selected depot.
     * Must be called inside a transaction; the depot row stays locked until it ends.
     */
    private ControleStock reserver(Connection con, int idProduit, int qte) throws SQLException {
        StockDao.Depot depot = stockDao.verrouillerDepotSortie(con, idProduit);
        if (depot == null) {
            return ControleStock.sansDepot(Statut.AUCUN_DEPOT, idProduit, qte);
        }
        Statut statut = evaluer(depot.qteDisponible, qte);
        ControleStock controle = new ControleStock(statut, idProduit, depot.idDepot, depot.lieu,
                                                   depot.qteDisponible, qte);
        if (controle.estAccepte()) {
            stockDao.diminuer(con, depot.idDepot, qte);
        }
        return controle;
    }

    /** Pure decision rule, identical to the thresholds of the original application. */
    static Statut evaluer(int qteDisponible, int qteDemandee) {
        int restant = qteDisponible - qteDemandee;
        if (qteDemandee > qteDisponible) return Statut.INSUFFISANT;
        if (restant <= SEUIL_ALERTE_COMMANDE) return Statut.ALERTE_COMMANDER;
        if (qteDemandee < qteDisponible && restant < qteDisponible * SEUIL_EXPLOITATION) {
            return Statut.ALERTE_EXPLOITER;
        }
        return Statut.OK;
    }

    private static void exigerQuantitePositive(int qte) {
        if (qte <= 0) throw new IllegalArgumentException("La quantité doit être positive.");
    }
}
