export interface SinistreCritique {
  id: string;
  sinistreId: string;
  contratId: string;
  montantSinistre: number;
  typeSinistre: string;
  dateDeclaration: number;
  seuilDepasse: number;
}

export interface StatsContrat {
  id: string;
  contratId: string;
  totalMontant: number;
  nbSinistres: number;
  windowStart: number;
  windowEnd: number;
}

export interface DashboardStats {
  totalSinistres: number;
  totalMontant: number;
  sinistresCritiques: number;
  avgMontant: number;
}
