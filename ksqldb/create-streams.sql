-- =====================================================
-- KSQLDB Scripts for Data Dammages Pipeline
-- CAA Group - Sinistres Aggregation
-- =====================================================

-- =====================================================
-- 1. KSTREAM : Flux d'événements sinistres (event-by-event)
-- =====================================================
-- Un KStream traite chaque événement individuellement
-- Utilisé pour le filtrage temps réel des sinistres critiques

CREATE STREAM stream_sinistres (
  sinistreId VARCHAR KEY,
  contratId VARCHAR,
  montantSinistre DOUBLE,
  typeSinistre VARCHAR,
  dateDeclaration BIGINT
) WITH (
  KAFKA_TOPIC = 'raw-sinistres',
  VALUE_FORMAT = 'JSON'
);

-- KStream : Filtrage sinistres critiques (montant > 10 000 EUR)
CREATE STREAM sinistres_critiques_stream AS
  SELECT
    sinistreId,
    contratId,
    montantSinistre,
    typeSinistre,
    dateDeclaration
  FROM stream_sinistres
  WHERE montantSinistre > 10000
  EMIT CHANGES;

-- =====================================================
-- 2. KTABLE : Table materialisée avec état (changelog)
-- =====================================================
-- Un KTable représente l'état actuel groupé par clé
-- Utilisé pour l'agrégation par contrat avec materialisation

CREATE TABLE contrats_ref (
  contratId VARCHAR KEY,
  nomClient VARCHAR,
  typeContrat VARCHAR,
  dateDebut BIGINT,
  dateFin BIGINT,
  statut VARCHAR
) WITH (
  KAFKA_TOPIC = 'contrats-ref',
  VALUE_FORMAT = 'JSON'
);

-- KTable : Agrégation glissante par contrat (fenêtre 5 min)
-- Chaque mise à jour produit un nouvel état dans la table
CREATE TABLE stats_contrat_table AS
  SELECT
    contratId,
    SUM(montantSinistre) AS totalMontant,
    COUNT(*) AS nbSinistres,
    WINDOWSTART AS debutFenetre,
    WINDOWEND AS finFenetre
  FROM stream_sinistres
  WINDOW TUMBLING (SIZE 5 MINUTES)
  GROUP BY contratId
  EMIT CHANGES;

-- =====================================================
-- 3. KGLOBALTABLE : Table de référence globale (répliquée)
-- =====================================================
-- Un KGlobalTable est répliqué sur chaque partition
-- Utilisé pour les joins avec des données de référence

-- KGlobalTable : Join sinistres avec contrats de référence
CREATE STREAM sinistres_enrichis_stream AS
  SELECT
    s.sinistreId,
    s.contratId,
    s.montantSinistre,
    s.typeSinistre,
    s.dateDeclaration,
    c.nomClient,
    c.typeContrat,
    c.statut
  FROM stream_sinistres s
  LEFT JOIN contrats_ref c ON s.contratId = c.contratId
  EMIT CHANGES;

-- =====================================================
-- 4. VÉRIFICATION
-- =====================================================
SHOW STREAMS;
SHOW TABLES;

-- =====================================================
-- 5. TESTS DE VALIDATION
-- =====================================================
-- Insérer un contrat de référence
-- INSERT INTO contrats_ref (contratId, nomClient, typeContrat, dateDebut, dateFin, statut)
-- VALUES ('CTR-001', 'Dupont Assurance', 'AUTO', 1704067200000, 1735603200000, 'ACTIF');

-- Insérer un test sinistre
-- INSERT INTO stream_sinistres (sinistreId, contratId, montantSinistre, typeSinistre, dateDeclaration)
-- VALUES ('sin-test-001', 'CTR-001', 15000.0, 'COLLISION', UNIX_TIMESTAMP());

-- Vérifier les résultats
-- SELECT * FROM sinistres_critiques_stream EMIT CHANGES LIMIT 5;
-- SELECT * FROM stats_contrat_table EMIT CHANGES LIMIT 5;
-- SELECT * FROM sinistres_enrichis_stream EMIT CHANGES LIMIT 5;
