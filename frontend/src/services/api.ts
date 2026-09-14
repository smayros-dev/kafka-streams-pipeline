import { SinistreCritique, StatsContrat } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

export const api = {
  async getSinistresCritiques(): Promise<SinistreCritique[]> {
    const data = await fetchJson<{ content: SinistreCritique[] }>(`${API_BASE_URL}/stats/critiques`);
    return data.content || [];
  },

  async getSinistresCritiquesByContrat(contratId: string): Promise<SinistreCritique[]> {
    const data = await fetchJson<{ content: SinistreCritique[] }>(`${API_BASE_URL}/stats/critiques/contrat/${contratId}`);
    return data.content || [];
  },

  async getSinistresAboveSeuil(seuil: number): Promise<SinistreCritique[]> {
    const data = await fetchJson<{ content: SinistreCritique[] }>(`${API_BASE_URL}/stats/critiques/seuil/${seuil}`);
    return data.content || [];
  },

  async getStatsByContrat(contratId: string): Promise<StatsContrat> {
    return fetchJson<StatsContrat>(`${API_BASE_URL}/stats/contrat/${contratId}`);
  },

  async getHistoriqueByContrat(contratId: string): Promise<StatsContrat[]> {
    return fetchJson<StatsContrat[]>(`${API_BASE_URL}/stats/contrat/${contratId}/historique`);
  },
};
