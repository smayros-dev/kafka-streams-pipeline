# Testing Strategy

## Vue d'ensemble

Le projet Data Dammages utilise une stratégie de test à 4 niveaux pour garantir la qualité et la fiabilité du code.

```
┌─────────────────────────────────────────────────────────────┐
│                     E2E Tests                               │
│              (3 scénarios complets)                          │
├─────────────────────────────────────────────────────────────┤
│                 Acceptance Tests                             │
│          (Testcontainers - Docker, 15 tests)                │
├─────────────────────────────────────────────────────────────┤
│                   Unit Tests                                │
│            (TopologyTestDriver, 8 tests)                    │
├─────────────────────────────────────────────────────────────┤
│                     Lint                                    │
│              (Checkstyle / ESLint)                           │
└─────────────────────────────────────────────────────────────┘
```

## Résumé des Tests

| Module | Unit | Acceptance | E2E | Frontend | Total |
|--------|------|------------|-----|----------|-------|
| kstream-service | 8 | 2 | 3 | - | **13** |
| api-service | - | 5 | - | - | **5** |
| frontend | - | - | - | 8 | **8** |
| **Total** | **8** | **7** | **3** | **8** | **26** |

## 1. Lint (Analyse Statique)

### Backend - Checkstyle

```bash
mvn checkstyle:check -f kstream-service/pom.xml
```

Vérifie :
- Convention de nommage (camelCase, pas de underscores)
- Formatage du code (indentation 4 espaces)
- Longueur des lignes (< 120 caractères)
- Javadoc sur les classes et méthodes publiques

### Frontend - ESLint

```bash
cd frontend && npm run lint
```

Vérifie :
- Variables non utilisées
- Types TypeScript
- Hooks React
- Import inutiles

## 2. Unit Tests (kstream-service)

```bash
mvn test -f kstream-service/pom.xml
```

**8 tests** utilisant `TopologyTestDriver` (pas de Docker requis) :

| Test | Description |
|------|-------------|
| `shouldFilterSinistresCritiques` | Filtrage montant > 10k |
| `shouldProduceToSinistresCritiquesTopic` | Publication topic critique |
| `shouldAggregateStatsByContrat` | Agrégation fenêtrée par contrat (vérifie windowStart/windowEnd) |
| `shouldJoinWithContratReference` | Join KGlobalTable |
| `shouldHandleMissingContratGracefully` | Gestion contrat manquant |
| `shouldProduceToStatsTopic` | Publication stats |
| `shouldProduceToEnrichisTopic` | Publication enrichis |
| `shouldRunWithoutErrors` | Validation topologie |

**Avantages :**
- Rapide (< 5 secondes)
- Pas de Docker requis
- Déterministe

## 3. Acceptance Tests (Testcontainers)

### Concept

Les acceptance tests valident l'intégration avec les dépendances réelles via Docker containers temporaires.

```java
@Container
static KafkaContainer kafka = new KafkaContainer(
    DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
);
```

### kstream-service

**Tests d'acceptance :** `KafkaStreamAcceptanceTest.java`

| Test | Scénario |
|------|----------|
| `shouldProduceAndConsumeSinistre` | Production + consommation Kafka |
| `shouldProduceMultipleSinistres` | Production de 5 sinistres |

### api-service

**Tests d'acceptance :** `ApiAcceptanceTest.java`

| Test | Scénario |
|------|----------|
| `shouldSaveAndRetrieveStatsContrat` | CRUD StatsContrat |
| `shouldSaveAndRetrieveSinistresCritiques` | CRUD SinistreCritique |
| `shouldReturnEmptyWhenContratNotFound` | Gestion 404 |
| `shouldSaveMultipleStatsForSameContrat` | Multi-versions (fenêtres) |
| `shouldDeleteAllData` | Nettoyage données |

### Exécution

```bash
# Tous les tests backend
mvn test -f kstream-service/pom.xml
mvn test -f api-service/pom.xml

# Tests spécifiques
mvn test -Dtest=KafkaStreamAcceptanceTest -f kstream-service/pom.xml
mvn test -Dtest=ApiAcceptanceTest -f api-service/pom.xml
```

## 4. E2E Tests (End-to-End)

Les tests E2E valident les scénarios métier complets à travers tout le pipeline Kafka Streams.

### Fichier de Test

`EndToEndFlowTest.java` - 3 scénarios E2E :

#### Scénario 1 : Filtrage Critique

```
Sinistre (15000 EUR) → Kafka → KStream Filter → sinistres-critiques
Sinistre (5000 EUR)  → Kafka → KStream Filter → ignoré
```

#### Scénario 2 : Agrégation Fenêtrée

```
Sinistre 1 (CTR-001, 12000 EUR) ─┐
                                  ├→ KTable (5min window) → stats-contrat-5m
Sinistre 2 (CTR-001, 8000 EUR)  ─┘

Output: { contratId: "CTR-001", totalMontant: 20000, nbSinistres: 2, windowStart: ..., windowEnd: ... }
```

#### Scénario 3 : Join Référence

```
Sinistre (CTR-001)      ─┐
                         ├→ KGlobalTable → sinistres-enrichis
Contrat ref (CTR-001)   ─┘
```

## 5. Frontend Tests

```bash
cd frontend && npm run test
```

**8 tests** avec Vitest + React Testing Library + jsdom :

| Composant | Tests | Description |
|-----------|-------|-------------|
| StatCard | 3 | Rendu montant, label, icône |
| SinistresCritiquesTable | 5 | Rendu tableau, données vides, formatage |

## Architecture des Tests

### TopologyTestDriver

```
┌─────────────────────────────────────────────────────────────┐
│                    Test JVM                                  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               TopologyTestDriver                        ││
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ ││
│  │  │ Input Topic │───▶│  Topology   │───▶│ Output Topic│ ││
│  │  └─────────────┘    └─────────────┘    └─────────────┘ ││
│  └─────────────────────────────────────────────────────────┘│
│                           │                                 │
│                           ▼                                 │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               Assertions (JUnit 5)                      ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### Testcontainers

```
┌─────────────────────────────────────────────────────────────┐
│                    Test JVM                                  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               Application Code                          ││
│  │         (Kafka Streams / Spring Boot)                   ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                 Docker Containers (éphémères)                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Kafka     │  │  MongoDB    │  │  Zookeeper  │        │
│  │  (port 9092)│  │ (port 27017)│  │ (port 2181) │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

## Couverture de Code

### Cibles

| Métrique | Cible |
|----------|-------|
| Line Coverage | > 80% |
| Branch Coverage | > 70% |
| Method Coverage | > 90% |

### Mesurer la Couverture

```bash
# Avec JaCoCo
mvn test jacoco:report -f kstream-service/pom.xml
mvn test jacoco:report -f api-service/pom.xml

# Rapports dans :
# kstream-service/target/site/jacoco/
# api-service/target/site/jacoco/
```

## Cas de Test par Composant

### KStream (Filtrage)

| # | Cas | Entrée | Sortie Attendue |
|---|-----|--------|-----------------|
| 1 | Montant > 10k | sinistre 15000 | sinistres-critiques |
| 2 | Montant < 10k | sinistre 5000 | ignoré |
| 3 | Montant = 10k | sinistre 10000 | ignoré |
| 4 | Montant = 0 | sinistre 0 | ignoré |
| 5 | Montant négatif | sinistre -1000 | ignoré |

### KTable (Agrégation Fenêtrée)

| # | Cas | Entrée | Sortie Attendue |
|---|-----|--------|-----------------|
| 1 | Premier sinistre | 1 sinistre | nbSinistres=1, windowStart/windowEnd |
| 2 | Deux sinistres | 2 sinistres | nbSinistres=2, totalMontant somme |
| 3 | Contrats différents | 2 contrats | 2 agrégations séparées |
| 4 | Fenêtre expirée | hors fenêtre | nouvel agrégat |

### KGlobalTable (Join)

| # | Cas | Entrée | Sortie Attendue |
|---|-----|--------|-----------------|
| 1 | Contrat existe | sinistre + contrat | enrichi |
| 2 | Contrat manquant | sinistre seul | sinistre brut |
| 3 | Contrat inactif | sinistre + contrat INACTIF | enrichi |

### API REST

| # | Cas | Endpoint | Code Attendu |
|---|-----|----------|--------------|
| 1 | Contrat existe | GET /stats/contrat/CTR-100 | 200 |
| 2 | Contrat manquant | GET /stats/contrat/UNKNOWN | 404 |
| 3 | Stats vides | GET /stats/critiques | 200 [] |
| 4 | Critiques existent | GET /stats/critiques | 200 [...] |
| 5 | Historique | GET /stats/contrat/CTR-100/historique | 200 [...] |
| 6 | Filtrage seuil | GET /stats/critiques/seuil/50000 | 200 [...] |

## CI/CD Pipeline

### Pipeline de Test

```yaml
stages:
  - lint
  - test
  - acceptance
  - e2e

lint:
  script:
    - mvn checkstyle:check -f kstream-service/pom.xml
    - cd frontend && npm run lint

test:
  script:
    - mvn test -f kstream-service/pom.xml
    - mvn test -f api-service/pom.xml

acceptance:
  services:
    - docker:dind
  script:
    - mvn test -Dtest=KafkaStreamAcceptanceTest -f kstream-service/pom.xml
    - mvn test -Dtest=ApiAcceptanceTest -f api-service/pom.xml

e2e:
  services:
    - docker:dind
  script:
    - mvn test -Dtest=EndToEndFlowTest -f kstream-service/pom.xml
```

## Exécution Rapide

```bash
# Tout exécuter
mvn test -f kstream-service/pom.xml && mvn test -f api-service/pom.xml

# Avec couverture
mvn test jacoco:report -f kstream-service/pom.xml

# Tests spécifiques
mvn test -Dtest=KStreamApplicationTest -f kstream-service/pom.xml
mvn test -Dtest=KafkaStreamAcceptanceTest -f kstream-service/pom.xml
mvn test -Dtest=EndToEndFlowTest -f kstream-service/pom.xml
mvn test -Dtest=ApiAcceptanceTest -f api-service/pom.xml

# Frontend
cd frontend && npm run test
cd frontend && npm run lint
cd frontend && npm run build
```
