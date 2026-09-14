# Testing Strategy

## Vue d'ensemble

Le projet Data Dammages utilise une stratégie de test à 4 niveaux pour garantir la qualité et la fiabilité du code.

## Niveaux de Test

```
┌─────────────────────────────────────────────────────────────┐
│                     E2E Tests                               │
│                  (Scénarios complets)                        │
├─────────────────────────────────────────────────────────────┤
│                 Acceptance Tests                             │
│              (Testcontainers - Docker)                       │
├─────────────────────────────────────────────────────────────┤
│                   Unit Tests                                │
│                (KafkaTopologyTestDriver)                     │
├─────────────────────────────────────────────────────────────┤
│                     Lint                                    │
│              (Checkstyle / ESLint)                           │
└─────────────────────────────────────────────────────────────┘
```

## 1. Lint (Analyse Statique)

### Backend - Checkstyle

```bash
mvn checkstyle:check -f kstream-service/pom.xml
```

Vérifie :
- Convention de nommage
- Formatage du code
- Longueur des lignes
- Javadoc

### Frontend - ESLint

```bash
cd frontend && npm run lint
```

Vérifie :
- Variables non utilisées
- Types TypeScript
- Hooks React
- Import inutiles

## 2. Unit Tests

### Kafka Streams (kstream-service)

```bash
mvn test -f kstream-service/pom.xml
```

**8 tests** utilisant `TopologyTestDriver` :

| Test | Description |
|------|-------------|
| `shouldFilterSinistresCritiques` | Filtrage montant > 10k |
| `shouldProduceToSinistresCritiquesTopic` | Publication topic critique |
| `shouldAggregateStatsByContrat` | Agrégation par contrat |
| `shouldJoinWithContratReference` | Join KGlobalTable |
| `shouldHandleMissingContratGracefully` | Gestion contrat manquant |
| `shouldProduceToStatsTopic` | Publication stats |
| `shouldProduceToEnrichisTopic` | Publication enrichis |
| `shouldRunWithoutErrors` | Validation topologie |

**Avantages :**
- Rapide (< 5 secondes)
- Pas de Docker requis
- Déterministe

### Spring Boot (api-service)

```bash
mvn test -f api-service/pom.xml
```

Tests unitaires avec `@WebMvcTest` et mocks.

## 3. Acceptance Tests (Testcontainers)

### Concept

Les acceptance tests valident l'intégration avec les dépendances réelles (Kafka, MongoDB) via Docker.

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
| `shouldSaveMultipleStatsForSameContrat` | Multi-versions |
| `shouldDeleteAllData` | Nettoyage données |

### Exécution

```bash
# Tous les tests
mvn test -f kstream-service/pom.xml
mvn test -f api-service/pom.xml

# Tests spécifiques
mvn test -Dtest=KafkaStreamAcceptanceTest -f kstream-service/pom.xml
mvn test -Dtest=ApiAcceptanceTest -f api-service/pom.xml
```

## 4. E2E Tests (End-to-End)

### Concept

Les tests E2E valident les scénarios métier complets à travers tout le pipeline.

### Scénarios Testés

#### Scénario 1 : Filtrage Critique

```
Sinistre (15000 EUR) → Kafka → KStream Filter → sinistres-critiques
Sinistre (5000 EUR)  → Kafka → KStream Filter → ignoré
```

#### Scénario 2 : Agrégation

```
Sinistre 1 (CTR-001, 12000 EUR) ─┐
                                  ├→ KTable → stats-contrat-5m
Sinistre 2 (CTR-001, 8000 EUR)  ─┘
```

#### Scénario 3 : Join Référence

```
Sinistre (CTR-001)      ─┐
                         ├→ KGlobalTable → sinistres-enrichis
Contrat ref (CTR-001)   ─┘
```

### Fichier de Test

`EndToEndFlowTest.java` - 3 scénarios E2E :

```java
@Test
void shouldFilterSinistresCritiques() {
    // Given: 2 sinistres (1 critique, 1 non)
    // When: Production dans Kafka
    // Then: Seul le critique est filtré
}

@Test
void shouldAggregateStatsByContrat() {
    // Given: 2 sinistres pour le même contrat
    // When: Agrégation KTable
    // Then: Stats contiennent le total
}

@Test
void shouldJoinWithContratReference() {
    // Given: Sinistre + Contrat reference
    // When: Join KGlobalTable
    // Then: Sinistre enrichi avec nom client
}
```

## Architecture des Tests

### Testcontainers

```
┌─────────────────────────────────────────────────────────────┐
│                    Test JVM                                  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               Application Code                          ││
│  │         (Kafka Streams / Spring Boot)                   ││
│  └─────────────────────────────────────────────────────────┘│
│                           │                                 │
│                           ▼                                 │
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                 Docker Containers                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Kafka     │  │  MongoDB    │  │  Zookeeper  │        │
│  │  (port 9092)│  │ (port 27017)│  │ (port 2181) │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

### TopologyTestDriver

```
┌─────────────────────────────────────────────────────────────┐
│                    Test JVM                                  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               TopologyTestDriver                        ││
│  │  ┌─────────────┐    ┌─────────────┐                     ││
│  │  │ Input Topic │───▶│  Topology   │───▶│ Output Topic │  ││
│  │  └─────────────┘    └─────────────┘    └─────────────┘  ││
│  └─────────────────────────────────────────────────────────┘│
│                           │                                 │
│                           ▼                                 │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               Assertions                                ││
│  │         (Vérification résultats)                        ││
│  └─────────────────────────────────────────────────────────┘│
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

# Rapport dans target/site/jacoco/
```

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
    - mvn checkstyle:check
    - cd frontend && npm run lint

test:
  script:
    - mvn test -f kstream-service/pom.xml
    - mvn test -f api-service/pom.xml

acceptance:
  services:
    - docker:dind
  script:
    - mvn test -Dtest=*AcceptanceTest

e2e:
  services:
    - docker:dind
  script:
    - mvn test -Dtest=*EndToEndTest
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

### KTable (Agrégation)

| # | Cas | Entrée | Sortie Attendue |
|---|-----|--------|-----------------|
| 1 | Premier sinistre | 1 sinistre | nbSinistres=1 |
| 2 | Deux sinistres | 2 sinistres | nbSinistres=2 |
| 3 | Contrats différents | 2 contrats | 2agrégations |
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
| 1 | Contrat existe | GET /stats/contrat/CTR-001 | 200 |
| 2 | Contrat manquant | GET /stats/contrat/UNKNOWN | 404 |
| 3 | Stats vides | GET /stats/critiques | 200 [] |
| 4 | Critiques existent | GET /stats/critiques | 200 [...] |

## Exécution Rapide

```bash
# Tout exécuter
mvn test -f kstream-service/pom.xml && mvn test -f api-service/pom.xml

# Avec couverture
mvn test jacoco:report -f kstream-service/pom.xml

# Tests spécifiques
mvn test -Dtest=KStreamApplicationTest -f kstream-service/pom.xml
mvn test -Dtest=ApiAcceptanceTest -f api-service/pom.xml
mvn test -Dtest=EndToEndFlowTest -f kstream-service/pom.xml
```
