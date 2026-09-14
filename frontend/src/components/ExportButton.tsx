import React, { useState } from 'react';
import { SinistreCritique } from '../types';
import { exportToCSV, exportToJSON, downloadPDF } from '../services/export';

interface ExportButtonProps {
  data: SinistreCritique[];
}

export const ExportButton: React.FC<ExportButtonProps> = ({ data }) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleExport = (format: 'csv' | 'json' | 'pdf') => {
    if (format === 'csv') {
      exportToCSV(data);
    } else if (format === 'json') {
      exportToJSON(data);
    } else {
      downloadPDF(data);
    }
    setIsOpen(false);
  };

  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        disabled={data.length === 0}
        style={{
          padding: '8px 16px',
          backgroundColor: data.length === 0 ? '#9ca3af' : '#10b981',
          color: 'white',
          border: 'none',
          borderRadius: '6px',
          cursor: data.length === 0 ? 'not-allowed' : 'pointer',
          fontSize: '14px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}
      >
        <span>📥</span>
        Exporter
      </button>

      {isOpen && (
        <div style={{
          position: 'absolute',
          top: '100%',
          right: 0,
          marginTop: '4px',
          backgroundColor: 'white',
          borderRadius: '8px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          zIndex: 1000,
          minWidth: '180px'
        }}>
          <button
            onClick={() => handleExport('csv')}
            style={{
              width: '100%',
              padding: '12px 16px',
              backgroundColor: 'transparent',
              border: 'none',
              textAlign: 'left',
              cursor: 'pointer',
              borderBottom: '1px solid #eee'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            📄 Exporter en CSV
          </button>
          <button
            onClick={() => handleExport('json')}
            style={{
              width: '100%',
              padding: '12px 16px',
              backgroundColor: 'transparent',
              border: 'none',
              textAlign: 'left',
              cursor: 'pointer',
              borderBottom: '1px solid #eee'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            📋 Exporter en JSON
          </button>
          <button
            onClick={() => handleExport('pdf')}
            style={{
              width: '100%',
              padding: '12px 16px',
              backgroundColor: 'transparent',
              border: 'none',
              textAlign: 'left',
              cursor: 'pointer'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            📑 Exporter en PDF
          </button>
        </div>
      )}
    </div>
  );
};
