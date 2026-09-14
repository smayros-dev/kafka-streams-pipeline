import React, { useState, useEffect } from 'react';
import { StatCard } from '../components/StatCard';
import { SinistresCritiquesTable } from '../components/SinistresCritiquesTable';
import { StatsChart } from '../components/StatsChart';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorBoundary } from '../components/ErrorBoundary';
import { ExportButton } from '../components/ExportButton';
import { api } from '../services/api';
import { SinistreCritique, StatsContrat } from '../types';

export const Dashboard: React.FC = () => {
  const [sinistresCritiques, setSinistresCritiques] = useState<SinistreCritique[]>([]);
  const [stats, setStats] = useState<StatsContrat[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 30000);
    return () => clearInterval(interval);
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      const critiques = await api.getSinistresCritiques();
      setSinistresCritiques(critiques);

      const contratIds = [...new Set(critiques.map(c => c.contratId))];
      const statsPromises = contratIds.map(id => api.getHistoriqueByContrat(id));
      const statsResults = await Promise.all(statsPromises);
      setStats(statsResults.flat());

      setError(null);
    } catch (err) {
      setError('Erreur de connexion à l\'API');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  const totalMontant = sinistresCritiques.reduce((sum, s) => sum + s.montantSinistre, 0);
  const avgMontant = sinistresCritiques.length > 0 ? totalMontant / sinistresCritiques.length : 0;

  if (loading && sinistresCritiques.length === 0) {
    return (
      <div style={{ padding: '24px', maxWidth: '1400px', margin: '0 auto' }}>
        <h1 style={{ color: '#333', marginBottom: '24px' }}>
          Tableau de Bord - Data Dammages
        </h1>
        <LoadingSpinner message="Chargement des données..." />
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <div style={{ padding: '24px', maxWidth: '1400px', margin: '0 auto' }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '24px'
        }}>
          <h1 style={{ color: '#333', margin: 0 }}>
            Tableau de Bord - Data Dammages
          </h1>
          <ExportButton data={sinistresCritiques} />
        </div>

        {error && (
          <div style={{
            backgroundColor: '#fee2e2',
            color: '#dc2626',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '24px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <span>⚠️</span>
            <span>{error}</span>
            <button
              onClick={loadData}
              style={{
                marginLeft: 'auto',
                padding: '4px 12px',
                backgroundColor: '#dc2626',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Réessayer
            </button>
          </div>
        )}

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: '24px',
          marginBottom: '24px'
        }}>
          <StatCard
            title="Sinistres Critiques"
            value={sinistresCritiques.length}
            icon="⚠️"
            color="#e74c3c"
          />
          <StatCard
            title="Montant Total"
            value={new Intl.NumberFormat('fr-FR', {
              style: 'currency',
              currency: 'EUR'
            }).format(totalMontant)}
            icon="💰"
            color="#f39c12"
          />
          <StatCard
            title="Montant Moyen"
            value={new Intl.NumberFormat('fr-FR', {
              style: 'currency',
              currency: 'EUR'
            }).format(avgMontant)}
            icon="📊"
            color="#3498db"
          />
          <StatCard
            title="Contrats Affectés"
            value={[...new Set(sinistresCritiques.map(s => s.contratId))].length}
            icon="📋"
            color="#2ecc71"
          />
        </div>

        <div style={{ display: 'grid', gap: '24px' }}>
          <ErrorBoundary fallback={<div>Erreur lors du chargement du graphique</div>}>
            <StatsChart stats={stats} />
          </ErrorBoundary>
          <ErrorBoundary fallback={<div>Erreur lors du chargement de la table</div>}>
            <SinistresCritiquesTable sinistres={sinistresCritiques} />
          </ErrorBoundary>
        </div>
      </div>
    </ErrorBoundary>
  );
};
