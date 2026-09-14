// MongoDB Indexes for Data Dammages
// Run this script to create indexes for query optimization

// Database: dommages_db
db = db.getSiblingDB('dommages_db');

// =====================================================
// sinistres_critiques collection
// =====================================================

// Index for querying by contratId
db.sinistres_critiques.createIndex(
  { "contratId": 1 },
  { name: "idx_sinistres_critiques_contratId" }
);

// Index for querying by dateDeclaration (descending for recent first)
db.sinistres_critiques.createIndex(
  { "dateDeclaration": -1 },
  { name: "idx_sinistres_critiques_dateDeclaration" }
);

// Compound index for contratId + dateDeclaration
db.sinistres_critiques.createIndex(
  { "contratId": 1, "dateDeclaration": -1 },
  { name: "idx_sinistres_critiques_contratId_date" }
);

// Index for querying by montantSinistre (for threshold queries)
db.sinistres_critiques.createIndex(
  { "montantSinistre": -1 },
  { name: "idx_sinistres_critiques_montant" }
);

// Index for typeSinistre
db.sinistres_critiques.createIndex(
  { "typeSinistre": 1 },
  { name: "idx_sinistres_critiques_type" }
);

// =====================================================
// stats_contrat collection
// =====================================================

// Index for querying by contratId
db.stats_contrat.createIndex(
  { "contratId": 1 },
  { name: "idx_stats_contrat_contratId" }
);

// Compound index for contratId + windowEnd (for historical queries)
db.stats_contrat.createIndex(
  { "contratId": 1, "windowEnd": -1 },
  { name: "idx_stats_contrat_contratId_windowEnd" }
);

// Index for windowStart + windowEnd (for time-range queries)
db.stats_contrat.createIndex(
  { "windowStart": 1, "windowEnd": 1 },
  { name: "idx_stats_contrat_window" }
);

// =====================================================
// Print created indexes
// =====================================================

print("=== sinistres_critiques indexes ===");
db.sinistres_critiques.getIndexes().forEach(function(idx) {
  print("  " + idx.name + ": " + JSON.stringify(idx.key));
});

print("\n=== stats_contrat indexes ===");
db.stats_contrat.getIndexes().forEach(function(idx) {
  print("  " + idx.name + ": " + JSON.stringify(idx.key));
});

print("\nIndexes created successfully!");
