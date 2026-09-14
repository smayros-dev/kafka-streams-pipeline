package com.caa.dammages.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sinistres_critiques")
public class SinistreCritique {

    @Id
    private String id;
    private String sinistreId;
    private String contratId;
    private double montantSinistre;
    private String typeSinistre;
    private long dateDeclaration;
    private double seuilDepasse;

    public SinistreCritique() {}

    public SinistreCritique(String sinistreId, String contratId, double montantSinistre,
                           String typeSinistre, long dateDeclaration, double seuilDepasse) {
        this.sinistreId = sinistreId;
        this.contratId = contratId;
        this.montantSinistre = montantSinistre;
        this.typeSinistre = typeSinistre;
        this.dateDeclaration = dateDeclaration;
        this.seuilDepasse = seuilDepasse;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSinistreId() { return sinistreId; }
    public void setSinistreId(String sinistreId) { this.sinistreId = sinistreId; }

    public String getContratId() { return contratId; }
    public void setContratId(String contratId) { this.contratId = contratId; }

    public double getMontantSinistre() { return montantSinistre; }
    public void setMontantSinistre(double montantSinistre) { this.montantSinistre = montantSinistre; }

    public String getTypeSinistre() { return typeSinistre; }
    public void setTypeSinistre(String typeSinistre) { this.typeSinistre = typeSinistre; }

    public long getDateDeclaration() { return dateDeclaration; }
    public void setDateDeclaration(long dateDeclaration) { this.dateDeclaration = dateDeclaration; }

    public double getSeuilDepasse() { return seuilDepasse; }
    public void setSeuilDepasse(double seuilDepasse) { this.seuilDepasse = seuilDepasse; }
}
