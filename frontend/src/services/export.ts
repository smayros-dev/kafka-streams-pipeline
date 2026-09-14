import { SinistreCritique } from '../types';

export function exportToCSV(
  data: SinistreCritique[],
  filename: string = 'sinistres-critiques.csv'
) {
  const headers = [
    'ID Sinistre',
    'ID Contrat',
    'Montant (EUR)',
    'Type',
    'Date Déclaration',
    'Seuil Dépassé'
  ];

  const rows = data.map(s => [
    s.sinistreId,
    s.contratId,
    s.montantSinistre.toString(),
    s.typeSinistre,
    new Date(s.dateDeclaration).toLocaleString('fr-FR'),
    s.seuilDepasse.toString()
  ]);

  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);

  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  link.style.visibility = 'hidden';

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

export function exportToJSON(
  data: SinistreCritique[],
  filename: string = 'sinistres-critiques.json'
) {
  const jsonContent = JSON.stringify(data, null, 2);
  const blob = new Blob([jsonContent], { type: 'application/json' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);

  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  link.style.visibility = 'hidden';

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

export function generatePDFContent(data: SinistreCritique[]): string {
  const totalMontant = data.reduce((sum, s) => sum + s.montantSinistre, 0);
  const averageMontant = data.length > 0 ? totalMontant / data.length : 0;

  const htmlContent = `
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Rapport Sinistres Critiques - Data Dammages</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 40px; }
    h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
    h2 { color: #34495e; margin-top: 30px; }
    .stats { display: flex; gap: 20px; margin: 20px 0; }
    .stat-box { background: #f8f9fa; padding: 15px; border-radius: 8px; flex: 1; }
    .stat-label { color: #666; font-size: 14px; }
    .stat-value { font-size: 24px; font-weight: bold; color: #2c3e50; }
    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
    th { background-color: #3498db; color: white; }
    tr:nth-child(even) { background-color: #f2f2f2; }
    .footer { margin-top: 40px; color: #666; font-size: 12px; border-top: 1px solid #ddd; padding-top: 10px; }
  </style>
</head>
<body>
  <h1>Rapport Sinistres Critiques</h1>
  <p>Data Dammages - ${new Date().toLocaleDateString('fr-FR')}</p>

  <h2>Statistiques</h2>
  <div class="stats">
    <div class="stat-box">
      <div class="stat-label">Nombre de sinistres</div>
      <div class="stat-value">${data.length}</div>
    </div>
    <div class="stat-box">
      <div class="stat-label">Montant total</div>
      <div class="stat-value">${new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(totalMontant)}</div>
    </div>
    <div class="stat-box">
      <div class="stat-label">Montant moyen</div>
      <div class="stat-value">${new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(averageMontant)}</div>
    </div>
  </div>

  <h2>Détail des Sinistres</h2>
  <table>
    <thead>
      <tr>
        <th>ID Sinistre</th>
        <th>ID Contrat</th>
        <th>Montant (EUR)</th>
        <th>Type</th>
        <th>Date Déclaration</th>
      </tr>
    </thead>
    <tbody>
      ${data.map(s => `
        <tr>
          <td>${s.sinistreId}</td>
          <td>${s.contratId}</td>
          <td>${new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(s.montantSinistre)}</td>
          <td>${s.typeSinistre}</td>
          <td>${new Date(s.dateDeclaration).toLocaleString('fr-FR')}</td>
        </tr>
      `).join('')}
    </tbody>
  </table>

  <div class="footer">
    Document généré automatiquement par Data Dammages - CAA
  </div>
</body>
</html>`;

  return htmlContent;
}

export function downloadPDF(
  data: SinistreCritique[],
  filename: string = 'sinistres-critiques.pdf'
) {
  const htmlContent = generatePDFContent(data);
  const blob = new Blob([htmlContent], { type: 'text/html' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);

  link.setAttribute('href', url);
  link.setAttribute('download', filename.replace('.pdf', '.html'));
  link.style.visibility = 'hidden';

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
