# Data Dammages - Kafka Streams Pipeline

Pipeline de streaming temps réel pour l'agrégation et le filtrage des sinistres d'assurance du groupe CAA.

## Architecture

Voir : [`docs/diagrams/plantuml/architecture-system.puml`](docs/diagrams/plantuml/architecture-system.puml)

```
kafka-producer (mock data) → Kafka: raw-sinistres
                                    ↓
                  ┌─────────────────┼─────────────────┐
                  ↓                 ↓                 ↓
           KStream: Filter    KTable: Agg    KGlobalTable: Join
                  ↓                 ↓                 ↓
     sinistres-critiques    stats-contrat-5m   sinistres-enrichis
                  ↓                 ↓                 ↓
                  └─────────────────┼─────────────────┘
                                    ↓
                       MongoDB Sink Connectors
                                    ↓
                               MongoDB
                                    ↓
                       Spring Boot API (6 endpoints)
                                    ↓
                        React Dashboard (Dark Mode)
```

## Technologies

| Composant | Technologie | Version |
|-----------|-------------|---------|
| Stream Processing | Kafka Streams | 3.5 |
| Query Language | KSQLDB | 0.29 |
| Database | MongoDB | 6.0 |
| API Backend | Spring Boot | 3.1 |
| Frontend | React + TypeScript + Vite | 18.2 |
| CI/CD | GitLab CI | - |
| Orchestration | Docker + Kubernetes (Helm) | - |
| Monitoring | Prometheus + Grafana | - |
| Testing | JUnit 5 + Testcontainers + Vitest | - |
| Code Quality | Checkstyle + ESLint | - |

## Documentation

- **[Guide Utilisateur](docs/user-guide/README.md)** - Comment utiliser l'application (dashboard, API, MongoDB, Grafana)
- **[Guide Développeur](docs/developer-guide/README.md)** - Comment contribuer et développer
- [PRD.md](./PRD.md) - Product Requirements Document
- [CONTEXT.md](./CONTEXT.md) - Contexte projet et tâches
- [docs/architecture/README.md](./docs/architecture/README.md) - Architecture
- [docs/api/README.md](./docs/api/README.md) - Documentation API
- [docs/deployment/README.md](./docs/deployment/README.md) - Déploiement
- [docs/testing/README.md](./docs/testing/README.md) - Stratégie de test

## Démarrage Rapide

### Prérequis

- Docker 24.0+ et Docker Compose v2
- Java 17+ (pour build local)
- Node.js 20+ (pour frontend)

### 1. Cloner et démarrer

```bash
git clone https://gitlab.com/caa/kafka-streams-pipeline.git
cd kafka-streams-pipeline

# Configurer l'environnement
echo "MONGO_ROOT_USER=admin" > .env
echo "MONGO_ROOT_PASSWORD=password" >> .env

# Démarrer tous les services (12 containers)
docker compose up -d --build
```

### 2. Vérifier le pipeline

```bash
# Attendre que tous les services soient sains (~60s)
docker compose ps

# Vérifier les données qui circulent
curl -s http://localhost:8080/api/v1/stats/critiques/seuil/10000 | python3 -m json.tool | head -20
```

### 3. Accéder aux interfaces

| Service | URL | Identifiants |
|---------|-----|--------------|
| Dashboard React | http://localhost:3000 | - |
| API REST | http://localhost:8080 | - |
| API Health | http://localhost:8080/actuator/health | - |
| Prometheus Métriques | http://localhost:8080/actuator/prometheus | - |
| MongoDB Express | http://localhost:8082 | admin / password |
| KSQLDB | http://localhost:8088 | - |
| Kafka Connect | http://localhost:8083 | - |
| Grafana | http://localhost:3001 | admin / admin |
| Microcks | http://localhost:8585 | admin / admin |

### 4. Arrêter le stack

```bash
docker compose down -v  # -v supprime les volumes (données)
```

## Structure du Projet

```
kafka-stream/
├── kstream-service/              # Kafka Streams (Java 17)
│   ├── checkstyle.xml            # Règles Checkstyle
│   ├── pom.xml                   # Maven + JaCoCo
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
│       └── test/java/ (8 unit + 2 acceptance + 3 E2E = 13 tests)
├── api-service/                  # Spring Boot API (Java 17)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/
│       │   └── com/caa/dammages/api/
│       │       ├── controller/StatsContratController.java
│       │       ├── config/CorsConfig.java
│       │       ├── config/RateLimitInterceptor.java
│       │       ├── model/*.java
│       │       └── repository/*.java
│       └── test/java/ (5 acceptance tests)
├── frontend/                     # React Dashboard
│   ├── package.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── components/ (StatCard, SinistresCritiquesTable, StatsChart,
│       │                 ErrorBoundary, LoadingSpinner, ThemeToggle, ExportButton)
│       ├── context/ThemeContext.tsx
│       ├── pages/Dashboard.tsx (lazy loaded)
│       └── services/ (api.ts, export.ts)
├── grafana/                      # Dashboards Grafana
│   ├── api-service-dashboard.json
│   └── datasources.yml
├── kafka-connect/                # MongoDB Sink Connector
│   ├── Dockerfile                # Downloads JARs from Maven Central
│   └── (mongo-kafka-connect JARs auto-downloaded)
├── kafka-init/                   # Topics + Connectors init
│   ├── Dockerfile
│   └── start.sh
├── kafka-producer/               # Mock sinistres producer
│   ├── Dockerfile
│   └── produce.sh                # 15 realistic scenarios
├── microcks/                     # AsyncAPI spec
│   └── sinistres-asyncapi.yml
├── ksqldb/                       # KSQL scripts
├── helm/                         # Kubernetes charts
├── docs/                         # Documentation
│   ├── architecture/README.md
│   ├── api/README.md
│   ├── deployment/README.md
│   ├── testing/README.md
│   └── diagrams/plantuml/*.puml
├── docker-compose.yml            # 12 services
├── .gitlab-ci.yml
├── .gitignore
├── sonar-project.properties
├── README.md
├── PRD.md
└── CONTEXT.md
```

## API Endpoints

### Statistiques

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/stats/contrat/{contratId}` | Dernière agrégation pour un contrat |
| GET | `/api/v1/stats/contrat/{contratId}/historique` | Historique des fenêtres glissantes |
| GET | `/api/v1/stats/critiques?page=0&size=20` | Sinistres critiques (paginé) |
| GET | `/api/v1/stats/critiques/all` | Tous les sinistres critiques |
| GET | `/api/v1/stats/critiques/contrat/{contratId}` | Critiques pour un contrat |
| GET | `/api/v1/stats/critiques/seuil/{seuil}` | Critiques par seuil |

### Monitoring

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/actuator/health` | Health check Spring Boot |
| GET | `/actuator/prometheus` | Métriques Prometheus |
| GET | `/health` | Health check kstream-service |

## Kafka Streams API

### KStream - Filtrage Temps Réel

Filtrage des sinistres critiques (montant > 10 000 EUR). Stateless, faible latence.

### KTable - Agrégation Fenêtrée (5 min)

Agrégation glissante par contrat sur fenêtre de 5 minutes. Output inclut `windowStart` et `windowEnd`.

### KGlobalTable - Join Données Référence

Enrichissement des sinistres avec les informations contrat depuis la table de référence `contrats-ref`.

## Données Mock (kafka-producer)

Le service `kafka-producer` génère 15 scénarios réalistes de sinistres toutes les 3 secondes :

| Type | Montant | Contrat | Fréquence |
|------|---------|---------|-----------|
| AUTO | 12 000 - 95 000 EUR | CTR-100, CTR-300 | ~40% |
| HABITATION | 18 000 - 32 000 EUR | CTR-200, CTR-300 | ~30% |
| SANTE | 15 000 EUR | CTR-300 | ~15% |
| INCENDIE | 28 000 EUR | CTR-200 | ~15% |

## Tests

### Résultats

| Module | Unit | Acceptance | E2E | Total |
|--------|------|------------|-----|-------|
| kstream-service | 8 | 2 | 3 | **13** |
| api-service | - | 5 | - | **5** |
| frontend | - | 8 | - | **8** |
| **Total** | **8** | **15** | **3** | **26** |

### Commandes

```bash
# Tests backend
mvn test -f kstream-service/pom.xml
mvn test -f api-service/pom.xml

# Tests frontend
cd frontend && npm run test

# Checkstyle
mvn checkstyle:check -f kstream-service/pom.xml

# Lint frontend
cd frontend && npm run lint

# Code coverage
mvn test jacoco:report -f kstream-service/pom.xml
mvn test jacoco:report -f api-service/pom.xml
```

## Monitoring

### Grafana Dashboard (8 panneaux)

- API Requests (rate)
- Response Time (p50, p95)
- JVM Memory (Heap)
- JVM Threads
- MongoDB Operations
- HTTP Status Codes
- System CPU Usage
- GC Pause Time

### Prometheus Metrics

- `http_server_requests_seconds` - Requêtes HTTP
- `jvm_memory_used_bytes` - Utilisation mémoire
- `jvm_threads_live_threads` - Threads actifs
- `mongodb_driver_operations_total` - Opérations MongoDB

## CI/CD

Le pipeline GitLab CI inclut :

1. **Build** - Compilation Maven (Java 17)
2. **Test** - Tests unitaires JUnit 5
3. **Quality** - Checkstyle + ESLint
4. **Coverage** - JaCoCo reporting
5. **Package** - Docker images
6. **Deploy** - Helm charts sur Kubernetes

## Fonctionnalités

### Frontend
- Dark mode toggle (localStorage persistant)
- Export CSV/JSON/PDF
- Error boundaries (ErrorBoundary)
- Loading states (LoadingSpinner)
- Code splitting (React.lazy + Suspense)
- Responsive design

### Backend
- Rate limiting (Bucket4j, 100 req/min per IP)
- Health check endpoints (`/health`, `/actuator/health`)
- Prometheus metrics
- MongoDB indexing
- API pagination (Spring Data Pageable)
- CORS configuré (relative URL via nginx)

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

## Variables d'Environnement

### kstream-service

| Variable | Défaut | Description |
|----------|--------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Serveurs Kafka |
| `KSTREAM_APP_ID` | sinistres-stream-app | ID application |
| `SEUIL_CRITIQUE` | 10000.0 | Seuil critique EUR |
| `WINDOW_SIZE_MINUTES` | 5 | Fenêtre agrégation |

### api-service

| Variable | Défaut | Description |
|----------|--------|-------------|
| `SPRING_DATA_MONGODB_URI` | mongodb://localhost:27017 | URI MongoDB |
| `SPRING_DATA_MONGODB_DATABASE` | dommages_db | Base de données |

### Docker Compose

| Variable | Défaut | Description |
|----------|--------|-------------|
| `MONGO_ROOT_USER` | admin | Utilisateur MongoDB |
| `MONGO_ROOT_PASSWORD` | password | Mot de passe MongoDB |

## Licence

Propriétaire - Groupe CAA
