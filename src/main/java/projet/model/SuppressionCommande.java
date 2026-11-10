package projet.model;

/** Outcome of an order deletion: what was removed and how much stock was released. */
public final class SuppressionCommande {

    private final boolean supprimee;
    private final int nbLignes;
    private final int qteRestauree;

    public SuppressionCommande(boolean supprimee, int nbLignes, int qteRestauree) {
        this.supprimee = supprimee;
        this.nbLignes = nbLignes;
        this.qteRestauree = qteRestauree;
    }

    /** False when no order with this id exists (nothing was changed). */
    public boolean estSupprimee() { return supprimee; }
    public int getNbLignes()      { return nbLignes; }
    /** Total quantity (kg) returned to the depots. */
    public int getQteRestauree()  { return qteRestauree; }
}
