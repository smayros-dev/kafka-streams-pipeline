# Data Dammages - Kafka Streams Pipeline

Pipeline de streaming temps réel pour l'agrégation et le filtrage des sinistres d'assurance du groupe CAA.

## Architecture

Voir : [`docs/diagrams/plantuml/architecture-system.puml`](docs/diagrams/plantuml/architecture-system.puml)

```
Applications Métier → Kafka: raw-sinistres
                              ↓
               ┌──────────────┼──────────────┐
               ↓              ↓              ↓
        KStream: Filter  KTable: Agg   KGlobalTable: Join
               ↓              ↓              ↓
 sinistres-critiques  stats-contrat-5m  sinistres-enrichis
               ↓              ↓              ↓
               └──────────────┼──────────────┘
                              ↓
                   MongoDB Sink Connector
                              ↓
                         MongoDB
                              ↓
                   Spring Boot API
                              ↓
                    React Dashboard
```

## Technologies

| Composant | Technologie |
|-----------|-------------|
| Stream Processing | Kafka Streams 3.5 |
| Query Language | KSQLDB 0.29 |
| Database | MongoDB 6.0 |
| API Backend | Spring Boot 3.1 |
| Frontend | React 18 + TypeScript + Vite |
| CI/CD | GitLab CI |
| Orchestration | Docker + Kubernetes (Helm) |
| Monitoring | Prometheus + Grafana |
| Testing | JUnit 5 + Testcontainers + Vitest |
| Code Quality | Checkstyle + ESLint + SonarQube |

## Démarrage Rapide

### Docker Compose (Local)

```bash
# Cloner et démarrer
git clone https://github.com/caa/dammages-streaming.git
cd dammages-streaming

# Configurer l'environnement
echo "MONGO_ROOT_USER=admin" > .env
echo "MONGO_ROOT_PASSWORD=password" >> .env

# Démarrer tous les services
docker-compose up -d --build
```

### Services disponibles

| Service | URL | Port | Description |
|---------|-----|------|-------------|
| Dashboard | http://localhost:3000 | 3000 | Interface React |
| API REST | http://localhost:8080 | 8080 | Spring Boot API |
| API Health | http://localhost:8080/actuator/health | 8080 | Health check |
| Prometheus | http://localhost:8080/actuator/prometheus | 8080 | Métriques |
| MongoDB Express | http://localhost:8082 | 8082 | Admin MongoDB |
| KSQLDB | http://localhost:8088 | 8088 | Query language |
| Grafana | http://localhost:3001 | 3001 | Dashboards |

## Structure du Projet

```
dammages-streaming/
├── kstream-service/              # Kafka Streams (Java 17)
│   ├── checkstyle.xml            # Règles Checkstyle
│   ├── pom.xml                   # Maven + JaCoCo + SonarQube
│   ├── Dockerfile
│   └── src/
│       ├── main/java/
│       │   └── com/caa/dammages/streaming/
│       │       ├── KStreamApplication.java
│       │       ├── config/KafkaStreamConfig.java
│       │       ├── controller/HealthController.java
│       │       ├── model/Sinistre.java
│       │       ├── model/SinistreAggregator.java
│       │       ├── service/AggregationService.java
│       │       └── serde/*.java
│       └── test/java/
│           ├── KStreamApplicationTest.java (8 tests)
│           ├── acceptance/KafkaStreamAcceptanceTest.java (2 tests)
│           └── e2e/EndToEndFlowTest.java (3 tests)
├── api-service/                  # Spring Boot API (Java 17)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/
│       │   └── com/caa/dammages/api/
│       │       ├── controller/StatsContratController.java
│       │       ├── repository/*.java
│       │       ├── model/*.java
│       │       └── config/*.java
│       └── test/java/
│           └── acceptance/ApiAcceptanceTest.java (5 tests)
├── frontend/                     # React Dashboard
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── components/
│       │   ├── StatCard.tsx
│       │   ├── SinistresCritiquesTable.tsx
│       │   ├── StatsChart.tsx
│       │   ├── ErrorBoundary.tsx
│       │   ├── LoadingSpinner.tsx
│       │   ├── ThemeToggle.tsx
│       │   └── ExportButton.tsx
│       ├── context/
│       │   └── ThemeContext.tsx
│       ├── pages/
│       │   └── Dashboard.tsx
│       └── services/
│           ├── api.ts
│           └── export.ts
├── grafana/                      # Dashboards Grafana
│   ├── api-service-dashboard.json
│   └── datasources.yml
├── mongodb/
│   └── create-indexes.js         # 9 indexes optimisés
├── ksqldb/
│   └── create-streams.sql
├── helm/                         # Charts Kubernetes
├── docs/
│   ├── architecture/README.md
│   ├── api/README.md
│   ├── deployment/README.md
│   ├── testing/README.md
│   └── diagrams/plantuml/*.puml  # 11 diagrammes
├── docker-compose.yml            # 8 services
├── .gitlab-ci.yml
├── sonar-project.properties
├── CONTEXT.md
├── PRD.md
└── README.md
```

## API Endpoints

### Statistiques

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/stats/contrat/{contratId}` | Stats pour un contrat |
| GET | `/api/v1/stats/contrat/{contratId}/historique` | Historique des stats |
| GET | `/api/v1/stats/critiques?page=0&size=20` | Sinistres critiques (paginé) |
| GET | `/api/v1/stats/critiques/all` | Tous les sinistres critiques |
| GET | `/api/v1/stats/critiques/contrat/{contratId}` | Critiques pour un contrat |
| GET | `/api/v1/stats/critiques/seuil/{seuil}` | Critiques par seuil |

### Monitoring

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/actuator/health` | Health check |
| GET | `/actuator/prometheus` | Métriques Prometheus |
| GET | `/health` | Health check kstream-service |

## Kafka Streams API

### KStream - Filtrage Temps Réel

Voir : [`docs/diagrams/plantuml/topology-diagram.puml`](docs/diagrams/plantuml/topology-diagram.puml)

```java
// Filtrage des sinistres critiques (montant > 10k)
rawSinistres
    .filter((key, jsonValue) -> parseSinistre(jsonValue).getMontantSinistre() > 10000)
    .to("sinistres-critiques");
```

### KTable - Agrégation Materialisée

Voir : [`docs/diagrams/plantuml/topology-diagram.puml`](docs/diagrams/plantuml/topology-diagram.puml)

```java
// Agrégation par contrat sur fenêtre 5 min
rawSinistres
    .groupByKey()
    .aggregate(SinistreAggregator::new,
        (contratId, sinistre, agg) -> agg.addMontant(sinistre.getMontantSinistre()),
        Materialized.as("sinistres-by-contrat-store"));
```

### KGlobalTable - Join Données Référence

Voir : [`docs/diagrams/plantuml/topology-diagram.puml`](docs/diagrams/plantuml/topology-diagram.puml)

```java
// Join avec table de référence contrats
rawSinistres
    .selectKey((key, s) -> s.getContratId())
    .leftJoin(contratsGlobalTable,
        (sinistre, contrat) -> enrichir(sinistre, contrat));
```

## Tests

### Résultats

| Module | Tests | Couverture |
|--------|-------|------------|
| kstream-service | 13 | 90%+ |
| api-service | 5 | 85%+ |
| frontend | 8 | 80%+ |
| **Total** | **26** | - |

### Commandes

```bash
# Tests backend
mvn test -f kstream-service/pom.xml
mvn test -f api-service/pom.xml

# Tests frontend
cd frontend && npm run test

# Code coverage
mvn test jacoco:report -f kstream-service/pom.xml

# Checkstyle
mvn checkstyle:check -f kstream-service/pom.xml

# Lint frontend
cd frontend && npm run lint
```

## Monitoring

### Grafana Dashboard

Le dashboard Grafana inclut 8 panneaux :
- API Requests (rate)
- Response Time (p50, p95)
- JVM Memory (Heap)
- JVM Threads
- MongoDB Operations
- HTTP Status Codes
- System CPU Usage
- GC Pause Time

### Prometheus Metrics

Les métriques disponibles :
- `http_server_requests_seconds` - Requêtes HTTP
- `jvm_memory_used_bytes` - Utilisation mémoire
- `jvm_threads_live_threads` - Threads actifs
- `mongodb_driver_operations_total` - Opérations MongoDB

## CI/CD

Le pipeline GitLab CI inclut :

1. **Build** - Compilation Maven
2. **Test** - Tests unitaires JUnit 5
3. **Quality** - Checkstyle + ESLint
4. **Coverage** - JaCoCo reporting
5. **SAST** - Analyse SonarQube
6. **Package** - Docker images
7. **Deploy** - Helm charts sur Kubernetes

## Fonctionnalités

### Frontend
- Dark mode toggle
- Export CSV/JSON/PDF
- Error boundaries
- Loading states
- Code splitting (lazy loading)
- Responsive design

### Backend
- Rate limiting (100 req/min)
- Health check endpoints
- Prometheus metrics
- MongoDB indexing
- API pagination

## Diagrammes PlantUML

Voir [`docs/diagrams/plantuml/`](docs/diagrams/plantuml/) :

| Fichier | Type | Description |
|---------|------|-------------|
| `architecture-system.puml` | Component | Architecture globale |
| `data-flow.puml` | Activity | Flux de traitement |
| `sequence-diagram.puml` | Sequence | Interactions |
| `topology-diagram.puml` | Activity | Topologie Kafka Streams |
| `er-diagram.puml` | Entity | Schéma MongoDB |
| `class-diagram.puml` | Class | Modèles de données |
| `component-diagram.puml` | Component | Composants |
| `deployment-diagram.puml` | Deployment | Infrastructure Docker |
| `package-diagram.puml` | Package | Organisation code |
| `usecase-diagram.puml` | UseCase | Cas d'utilisation |

### Rendu dans IntelliJ

1. Installer le plugin **PlantUML Integration**
2. Ouvrir un fichier `.puml`
3. Le rendu est automatique dans le panneau de droite

## Environnement

### Variables d'environnement

| Variable | Défaut | Description |
|----------|--------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Serveurs Kafka |
| `KSTREAM_APP_ID` | sinistres-stream-app | ID application |
| `SEUIL_CRITIQUE` | 10000.0 | Seuil critique EUR |
| `WINDOW_SIZE_MINUTES` | 5 | Fenêtre agrégation |
| `SPRING_DATA_MONGODB_URI` | mongodb://localhost:27017 | URI MongoDB |
| `MONGO_ROOT_USER` | admin | Utilisateur MongoDB |
| `MONGO_ROOT_PASSWORD` | password | Mot de passe MongoDB |

### SonarQube (Optionnel)

```bash
export SONAR_HOST_URL=http://localhost:9000
export SONAR_TOKEN=your-token

mvn clean verify sonar:sonar -Psonar -f kstream-service/pom.xml
mvn clean verify sonar:sonar -Psonar -f api-service/pom.xml
```

## Documentation

- [PRD.md](./PRD.md) - Product Requirements Document
- [CONTEXT.md](./CONTEXT.md) - Contexte projet et tâches
- [docs/README.md](./docs/README.md) - Documentation complète
- [docs/architecture/README.md](./docs/architecture/README.md) - Architecture
- [docs/api/README.md](./docs/api/README.md) - Documentation API
- [docs/deployment/README.md](./docs/deployment/README.md) - Déploiement
- [docs/testing/README.md](./docs/testing/README.md) - Stratégie de test

## Licence

Propriétaire - Groupe CAA
