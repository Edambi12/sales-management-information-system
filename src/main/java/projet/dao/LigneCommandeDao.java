package projet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data access for order lines (table {@code ligne_commande}).
 * Methods never commit; the caller owns the transaction.
 */
public final class LigneCommandeDao {

    /**
     * Reads and locks the quantity of one order line.
     * @return the quantity stored in the database, or null if the line does not exist
     */
    public Integer verrouillerQuantite(Connection con, int idCommande, int idProduit) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT qte_commande FROM ligne_commande " +
                "WHERE id_commande = ? AND id_produit = ? FOR UPDATE")) {
            ps.setInt(1, idCommande);
            ps.setInt(2, idProduit);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Reads and locks every line of an order: product id -> quantity, ordered by product id. */
    public Map<Integer, Integer> verrouillerLignes(Connection con, int idCommande) throws SQLException {
        Map<Integer, Integer> lignes = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_produit, qte_commande FROM ligne_commande " +
                "WHERE id_commande = ? ORDER BY id_produit FOR UPDATE")) {
            ps.setInt(1, idCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lignes.put(rs.getInt(1), rs.getInt(2));
            }
        }
        return lignes;
    }

    public void inserer(Connection con, int idCommande, int idProduit, int qte) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ligne_commande (id_commande, id_produit, qte_commande) VALUES (?, ?, ?)")) {
            ps.setInt(1, idCommande);
            ps.setInt(2, idProduit);
            ps.setInt(3, qte);
            ps.executeUpdate();
        }
    }

    public void modifierQuantite(Connection con, int idCommande, int idProduit, int qte) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE ligne_commande SET qte_commande = ? WHERE id_commande = ? AND id_produit = ?")) {
            ps.setInt(1, qte);
            ps.setInt(2, idCommande);
            ps.setInt(3, idProduit);
            ps.executeUpdate();
        }
    }

    public int supprimer(Connection con, int idCommande, int idProduit) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM ligne_commande WHERE id_commande = ? AND id_produit = ?")) {
            ps.setInt(1, idCommande);
            ps.setInt(2, idProduit);
            return ps.executeUpdate();
        }
    }

    public int supprimerToutes(Connection con, int idCommande) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM ligne_commande WHERE id_commande = ?")) {
            ps.setInt(1, idCommande);
            return ps.executeUpdate();
        }
    }
}
