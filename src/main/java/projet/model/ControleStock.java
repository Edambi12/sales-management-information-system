package projet.model;

/**
 * Result of a stock control performed before reserving a quantity for an order line.
 * Immutable value object: it carries no Swing or JDBC dependency, so the UI decides
 * how to present it and tests can assert on it directly.
 */
public final class ControleStock {

    public enum Statut {
        /** Enough stock, no alert. */
        OK,
        /** Reserved, but the remaining stock is at or below the reorder threshold. */
        ALERTE_COMMANDER,
        /** Reserved, and more than half of the depot stock is now used. */
        ALERTE_EXPLOITER,
        /** Refused: the requested quantity exceeds the available stock. */
        INSUFFISANT,
        /** Refused: the product is not linked to any depot (table stocker). */
        AUCUN_DEPOT,
        /** Refused: the order line to modify no longer exists in the database. */
        LIGNE_INTROUVABLE
    }

    private final Statut statut;
    private final int idProduit;
    private final int idDepot;
    private final String lieuDepot;
    private final int qteDisponible;
    private final int qteDemandee;

    public ControleStock(Statut statut, int idProduit, int idDepot, String lieuDepot,
                         int qteDisponible, int qteDemandee) {
        this.statut = statut;
        this.idProduit = idProduit;
        this.idDepot = idDepot;
        this.lieuDepot = lieuDepot;
        this.qteDisponible = qteDisponible;
        this.qteDemandee = qteDemandee;
    }

    public static ControleStock sansDepot(Statut statut, int idProduit, int qteDemandee) {
        return new ControleStock(statut, idProduit, 0, null, 0, qteDemandee);
    }

    public Statut getStatut()      { return statut; }
    public int getIdProduit()      { return idProduit; }
    public int getIdDepot()        { return idDepot; }
    public String getLieuDepot()   { return lieuDepot; }
    /** Quantity available in the depot before the operation. */
    public int getQteDisponible()  { return qteDisponible; }
    public int getQteDemandee()    { return qteDemandee; }
    public int getStockRestant()   { return qteDisponible - qteDemandee; }
    public int getManque()         { return qteDemandee - qteDisponible; }

    /** True when the operation was applied (possibly with an alert). */
    public boolean estAccepte() {
        return statut == Statut.OK || statut == Statut.ALERTE_COMMANDER || statut == Statut.ALERTE_EXPLOITER;
    }
}
