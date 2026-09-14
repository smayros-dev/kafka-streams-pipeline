import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatCard } from '../StatCard';

describe('StatCard', () => {
  it('renders title and value', () => {
    render(
      <StatCard
        title="Total Sinistres"
        value={42}
        icon="📊"
        color="#3B82F6"
      />
    );

    expect(screen.getByText('Total Sinistres')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
  });

  it('renders string value', () => {
    render(
      <StatCard
        title="Montant Total"
        value="25,000 EUR"
        icon="💰"
        color="#10B981"
      />
    );

    expect(screen.getByText('Montant Total')).toBeInTheDocument();
    expect(screen.getByText('25,000 EUR')).toBeInTheDocument();
  });

  it('renders icon', () => {
    render(
      <StatCard
        title="Critiques"
        value={5}
        icon="⚠️"
        color="#EF4444"
      />
    );

    expect(screen.getByText('⚠️')).toBeInTheDocument();
  });
});
