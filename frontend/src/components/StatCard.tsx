import React from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: string;
  color: string;
}

export const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color }) => {
  return (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '12px',
      padding: '24px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
      display: 'flex',
      alignItems: 'center',
      gap: '16px',
      borderLeft: `4px solid ${color}`
    }}>
      <div style={{
        fontSize: '32px',
        width: '60px',
        height: '60px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: `${color}20`,
        borderRadius: '12px'
      }}>
        {icon}
      </div>
      <div>
        <p style={{ margin: 0, color: '#666', fontSize: '14px' }}>{title}</p>
        <p style={{ margin: 0, color: '#333', fontSize: '28px', fontWeight: 'bold' }}>{value}</p>
      </div>
    </div>
  );
};
