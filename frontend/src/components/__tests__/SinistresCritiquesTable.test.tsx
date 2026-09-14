import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SinistresCritiquesTable } from '../SinistresCritiquesTable';
import { SinistreCritique } from '../../types';

describe('SinistresCritiquesTable', () => {
  const mockSinistres: SinistreCritique[] = [
    {
      id: '1',
      sinistreId: 'sin-001',
      contratId: 'CTR-001',
      montantSinistre: 15000,
      typeSinistre: 'COLLISION',
      dateDeclaration: 1773220000000,
      seuilDepasse: 10000
    },
    {
      id: '2',
      sinistreId: 'sin-002',
      contratId: 'CTR-002',
      montantSinistre: 25000,
      typeSinistre: 'VOL',
      dateDeclaration: 1773220100000,
      seuilDepasse: 10000
    }
  ];

  it('renders table headers', () => {
    render(<SinistresCritiquesTable sinistres={[]} />);

    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Contrat')).toBeInTheDocument();
    expect(screen.getByText('Montant')).toBeInTheDocument();
    expect(screen.getByText('Type')).toBeInTheDocument();
    expect(screen.getByText('Date')).toBeInTheDocument();
  });

  it('renders sinistres data', () => {
    render(<SinistresCritiquesTable sinistres={mockSinistres} />);

    expect(screen.getByText('sin-001')).toBeInTheDocument();
    expect(screen.getByText('CTR-001')).toBeInTheDocument();
    expect(screen.getByText('COLLISION')).toBeInTheDocument();
    expect(screen.getByText('sin-002')).toBeInTheDocument();
    expect(screen.getByText('CTR-002')).toBeInTheDocument();
    expect(screen.getByText('VOL')).toBeInTheDocument();
  });

  it('formats montant as currency', () => {
    render(<SinistresCritiquesTable sinistres={mockSinistres} />);

    expect(screen.getByText('15 000,00 €')).toBeInTheDocument();
    expect(screen.getByText('25 000,00 €')).toBeInTheDocument();
  });

  it('shows empty message when no sinistres', () => {
    render(<SinistresCritiquesTable sinistres={[]} />);

    expect(screen.getByText('Aucun sinistre critique')).toBeInTheDocument();
  });

  it('renders title', () => {
    render(<SinistresCritiquesTable sinistres={[]} />);

    expect(screen.getByText('Sinistres Critiques')).toBeInTheDocument();
  });
});
