package com.caa.dammages.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stats_contrat")
public class StatsContrat {

    @Id
    private String id;
    private String contratId;
    private double totalMontant;
    private int nbSinistres;
    private long windowStart;
    private long windowEnd;

    public StatsContrat() {}

    public StatsContrat(String contratId, double totalMontant, int nbSinistres, long windowStart, long windowEnd) {
        this.contratId = contratId;
        this.totalMontant = totalMontant;
        this.nbSinistres = nbSinistres;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContratId() { return contratId; }
    public void setContratId(String contratId) { this.contratId = contratId; }

    public double getTotalMontant() { return totalMontant; }
    public void setTotalMontant(double totalMontant) { this.totalMontant = totalMontant; }

    public int getNbSinistres() { return nbSinistres; }
    public void setNbSinistres(int nbSinistres) { this.nbSinistres = nbSinistres; }

    public long getWindowStart() { return windowStart; }
    public void setWindowStart(long windowStart) { this.windowStart = windowStart; }

    public long getWindowEnd() { return windowEnd; }
    public void setWindowEnd(long windowEnd) { this.windowEnd = windowEnd; }
}
