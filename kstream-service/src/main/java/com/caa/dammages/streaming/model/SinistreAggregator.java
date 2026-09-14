package com.caa.dammages.streaming.model;

/**
 * Aggregator for sinistre statistics per contract.
 */
public final class SinistreAggregator {

    /** Total claim amount. */
    private double totalMontant;

    /** Number of claims. */
    private int nbSinistres;

    /** Default constructor. */
    public SinistreAggregator() {
        this.totalMontant = 0.0;
        this.nbSinistres = 0;
    }

    /** @return total amount */
    public double getTotalMontant() {
        return totalMontant;
    }

    /** @param totalMontant total amount */
    public void setTotalMontant(final double totalMontant) {
        this.totalMontant = totalMontant;
    }

    /** @return number of sinistres */
    public int getNbSinistres() {
        return nbSinistres;
    }

    /** @param nbSinistres number of sinistres */
    public void setNbSinistres(final int nbSinistres) {
        this.nbSinistres = nbSinistres;
    }

    /**
     * Add a claim amount to the aggregate.
     *
     * @param montant claim amount
     * @return this aggregator
     */
    public SinistreAggregator addMontant(final double montant) {
        this.totalMontant += montant;
        this.nbSinistres++;
        return this;
    }

    @Override
    public String toString() {
        return "SinistreAggregator{"
                + "totalMontant=" + totalMontant
                + ", nbSinistres=" + nbSinistres
                + '}';
    }
}
