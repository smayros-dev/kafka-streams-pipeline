package com.caa.dammages.streaming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model representing an insurance claim (sinistre).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Sinistre {

    /** Unique sinistre identifier. */
    private String sinistreId;

    /** Contract identifier. */
    private String contratId;

    /** Claim amount in EUR. */
    private double montantSinistre;

    /** Claim type (COLLISION, VOL, INCENDIE, etc.). */
    private String typeSinistre;

    /** Declaration timestamp in epoch millis. */
    private long dateDeclaration;

    /** Default constructor for Jackson deserialization. */
    public Sinistre() { }

    /**
     * Full constructor.
     *
     * @param sinistreId unique identifier
     * @param contratId contract identifier
     * @param montantSinistre claim amount
     * @param typeSinistre claim type
     * @param dateDeclaration declaration timestamp
     */
    public Sinistre(
            final String sinistreId,
            final String contratId,
            final double montantSinistre,
            final String typeSinistre,
            final long dateDeclaration) {
        this.sinistreId = sinistreId;
        this.contratId = contratId;
        this.montantSinistre = montantSinistre;
        this.typeSinistre = typeSinistre;
        this.dateDeclaration = dateDeclaration;
    }

    /** @return sinistre identifier */
    public String getSinistreId() {
        return sinistreId;
    }

    /** @param sinistreId unique identifier */
    public void setSinistreId(final String sinistreId) {
        this.sinistreId = sinistreId;
    }

    /** @return contract identifier */
    public String getContratId() {
        return contratId;
    }

    /** @param contratId contract identifier */
    public void setContratId(final String contratId) {
        this.contratId = contratId;
    }

    /** @return claim amount */
    public double getMontantSinistre() {
        return montantSinistre;
    }

    /** @param montantSinistre claim amount */
    public void setMontantSinistre(final double montantSinistre) {
        this.montantSinistre = montantSinistre;
    }

    /** @return claim type */
    public String getTypeSinistre() {
        return typeSinistre;
    }

    /** @param typeSinistre claim type */
    public void setTypeSinistre(final String typeSinistre) {
        this.typeSinistre = typeSinistre;
    }

    /** @return declaration timestamp */
    public long getDateDeclaration() {
        return dateDeclaration;
    }

    /** @param dateDeclaration declaration timestamp */
    public void setDateDeclaration(final long dateDeclaration) {
        this.dateDeclaration = dateDeclaration;
    }

    @Override
    public String toString() {
        return "Sinistre{"
                + "sinistreId='" + sinistreId + '\''
                + ", contratId='" + contratId + '\''
                + ", montantSinistre=" + montantSinistre
                + ", typeSinistre='" + typeSinistre + '\''
                + ", dateDeclaration=" + dateDeclaration
                + '}';
    }
}
