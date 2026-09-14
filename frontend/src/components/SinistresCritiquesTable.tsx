import React from 'react';
import { SinistreCritique } from '../types';

interface SinistresCritiquesTableProps {
  sinistres: SinistreCritique[];
}

export const SinistresCritiquesTable: React.FC<SinistresCritiquesTableProps> = ({ sinistres }) => {
  const formatDate = (timestamp: number) => {
    return new Date(timestamp).toLocaleString('fr-FR');
  };

  const formatMontant = (montant: number) => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(montant);
  };

  return (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '12px',
      padding: '24px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
    }}>
      <h2 style={{ margin: '0 0 20px 0', color: '#333' }}>Sinistres Critiques</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #eee' }}>
            <th style={thStyle}>ID</th>
            <th style={thStyle}>Contrat</th>
            <th style={thStyle}>Montant</th>
            <th style={thStyle}>Type</th>
            <th style={thStyle}>Date</th>
          </tr>
        </thead>
        <tbody>
          {sinistres.map((sinistre) => (
            <tr key={sinistre.id || sinistre.sinistreId} style={{ borderBottom: '1px solid #eee' }}>
              <td style={tdStyle}>{sinistre.sinistreId}</td>
              <td style={tdStyle}>{sinistre.contratId}</td>
              <td style={{ ...tdStyle, color: '#e74c3c', fontWeight: 'bold' }}>
                {formatMontant(sinistre.montantSinistre)}
              </td>
              <td style={tdStyle}>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  backgroundColor: '#fee2e2',
                  color: '#dc2626',
                  fontSize: '12px'
                }}>
                  {sinistre.typeSinistre}
                </span>
              </td>
              <td style={tdStyle}>{formatDate(sinistre.dateDeclaration)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {sinistres.length === 0 && (
        <p style={{ textAlign: 'center', color: '#999', padding: '40px' }}>
          Aucun sinistre critique
        </p>
      )}
    </div>
  );
};

const thStyle: React.CSSProperties = {
  padding: '12px',
  textAlign: 'left',
  color: '#666',
  fontSize: '12px',
  textTransform: 'uppercase'
};

const tdStyle: React.CSSProperties = {
  padding: '12px',
  color: '#333'
};
