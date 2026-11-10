package projet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data access for depot quantities (tables {@code depot} and {@code stocker}).
 *
 * <p>Every method works on a {@link Connection} supplied by the caller and never commits:
 * the service layer owns the transaction. Rows are read with {@code FOR UPDATE} so that
 * a concurrent session cannot change the quantity between the control and the update.</p>
 *
 * <p>Depot selection rule (the schema does not record which depot an order line was
 * served from):</p>
 * <ul>
 *   <li><b>decrease</b> — the depot holding the largest quantity of the product
 *       (lowest id on ties); exactly one depot is updated;</li>
 *   <li><b>restore</b> — the product's first depot (lowest id); exactly one depot is updated.</li>
 * </ul>
 * With one depot per product (the usual case) both rules designate the same depot.
 * With several depots the product's total quantity is always conserved.
 */
public final class StockDao {

    /** Snapshot of a locked depot row. */
    public static final class Depot {
        public final int idDepot;
        public final String lieu;
        public final int qteDisponible;

        Depot(int idDepot, String lieu, int qteDisponible) {
            this.idDepot = idDepot;
            this.lieu = lieu;
            this.qteDisponible = qteDisponible;
        }
    }

    private static final String SQL_DEPOT_SORTIE =
        "SELECT d.id_depot, d.lieu_depot, d.qte_disponible " +
        "FROM stocker s JOIN depot d ON s.id_depot = d.id_depot " +
        "WHERE s.id_produit = ? " +
        "ORDER BY d.qte_disponible DESC, d.id_depot ASC LIMIT 1 FOR UPDATE";

    private static final String SQL_DEPOT_RETOUR =
        "SELECT d.id_depot, d.lieu_depot, d.qte_disponible " +
        "FROM stocker s JOIN depot d ON s.id_depot = d.id_depot " +
        "WHERE s.id_produit = ? " +
        "ORDER BY d.id_depot ASC LIMIT 1 FOR UPDATE";

    /** Locks and returns the depot a quantity would be taken from, or null if none. */
    public Depot verrouillerDepotSortie(Connection con, int idProduit) throws SQLException {
        return lireDepot(con, SQL_DEPOT_SORTIE, idProduit);
    }

    /** Locks and returns the depot a quantity would be returned to, or null if none. */
    public Depot verrouillerDepotRetour(Connection con, int idProduit) throws SQLException {
        return lireDepot(con, SQL_DEPOT_RETOUR, idProduit);
    }

    private Depot lireDepot(Connection con, String sql, int idProduit) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProduit);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Depot(rs.getInt("id_depot"), rs.getString("lieu_depot"), rs.getInt("qte_disponible"));
            }
        }
    }

    /**
     * Removes a quantity from one depot. The guard {@code qte_disponible >= ?} makes it
     * impossible to go negative; if no row is updated the transaction must be rolled back.
     */
    public void diminuer(Connection con, int idDepot, int qte) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE depot SET qte_disponible = qte_disponible - ? " +
                "WHERE id_depot = ? AND qte_disponible >= ?")) {
            ps.setInt(1, qte);
            ps.setInt(2, idDepot);
            ps.setInt(3, qte);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Stock insuffisant ou dépôt #" + idDepot
                        + " introuvable: aucune quantité n'a été déduite.");
            }
        }
    }

    /** Returns a quantity to the product's depot. Fails if the product has no depot. */
    public int restaurer(Connection con, int idProduit, int qte) throws SQLException {
        Depot depot = verrouillerDepotRetour(con, idProduit);
        if (depot == null) {
            throw new SQLException("Aucun dépôt associé au produit #" + idProduit
                    + ": impossible de restaurer le stock.");
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE depot SET qte_disponible = qte_disponible + ? WHERE id_depot = ?")) {
            ps.setInt(1, qte);
            ps.setInt(2, depot.idDepot);
            ps.executeUpdate();
        }
        return depot.idDepot;
    }
}
