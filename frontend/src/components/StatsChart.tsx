import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { StatsContrat } from '../types';

interface StatsChartProps {
  stats: StatsContrat[];
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82CA9D'];

export const StatsChart: React.FC<StatsChartProps> = ({ stats }) => {
  const chartData = stats.map((s) => ({
    name: s.contratId,
    totalMontant: s.totalMontant,
    nbSinistres: s.nbSinistres
  }));

  const pieData = stats.map((s) => ({
    name: s.contratId,
    value: s.totalMontant
  }));

  return (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '12px',
      padding: '24px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
    }}>
      <h2 style={{ margin: '0 0 20px 0', color: '#333' }}>Statistiques par Contrat</h2>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
        <div>
          <h3 style={{ color: '#666', fontSize: '14px', marginBottom: '12px' }}>Montant par contrat</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip formatter={(value) => new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(value as number)} />
              <Bar dataKey="totalMontant" fill="#8884d8" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div>
          <h3 style={{ color: '#666', fontSize: '14px', marginBottom: '12px' }}>Répartition</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {pieData.map((_entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(value) => new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(value as number)} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
