# Data Dammages - Project Context

## DONE TASKS

### Infrastructure
- [x] Docker Compose with 12 services (Zookeeper, Kafka, KSQLDB, MongoDB, Mongo Express, Kafka Connect, kstream-service, api-service, frontend, Grafana, Microcks, Microcks Async Minion)
- [x] Helm charts for kstream-service and api-service
- [x] GitLab CI/CD pipeline
- [x] MongoDB authentication enabled
- [x] Docker health checks for all services
- [x] Grafana dashboard for API monitoring
- [x] Microcks for async API mocking (Kafka producer simulation)
- [x] Microcks Async Minion for dynamic Kafka message production
- [x] MongoDB Sink Connectors (sinistres-critiques, stats-contrat) via Kafka Connect

### Backend - kstream-service
- [x] KStreamApplication main entry point with CountDownLatch shutdown
- [x] AggregationService with 3 branches: KStream filter, KTable aggregation, KGlobalTable join
- [x] Sinistre model with Jackson annotations
- [x] SinistreAggregator model for stats aggregation
- [x] Custom Serde implementations (SinistreSerde, SinistreAggregatorSerde)
- [x] KafkaStreamConfig with environment variables
- [x] Checkstyle: 0 violations
- [x] Java 17 migration
- [x] JaCoCo code coverage reporting

### Backend - api-service
- [x] Spring Boot 3.1 REST API
- [x] StatsContratController with 6 endpoints (including paginated /critiques)
- [x] SinistreCritiqueRepository
- [x] StatsContratRepository
- [x] Dockerfile updated to Java 17
- [x] Prometheus metrics endpoint (/actuator/prometheus)
- [x] JaCoCo code coverage reporting

### Frontend
- [x] React 18 + TypeScript + Vite setup
- [x] StatCard, SinistresCritiquesTable, StatsChart components
- [x] Dashboard page with routing
- [x] ESLint configured and passing
- [x] Dockerfile with nginx
- [x] Build passing
- [x] ErrorBoundary component for error handling
- [x] LoadingSpinner component for loading states
- [x] ThemeContext with dark mode toggle
- [x] ExportButton for CSV/JSON export
- [x] Vitest unit tests (8 tests passing)

### Testing
- [x] 8 unit tests (KStreamApplicationTest) - TopologyTestDriver
- [x] 2 acceptance tests (KafkaStreamAcceptanceTest) - Testcontainers Kafka
- [x] 5 acceptance tests (ApiAcceptanceTest) - Testcontainers MongoDB
- [x] 3 E2E tests (EndToEndFlowTest) - Full pipeline scenarios
- [x] 8 frontend tests (StatCard, SinistresCritiquesTable)
- [x] **Total: 26 tests, all passing**

### Documentation
- [x] PRD.md - Product requirements
- [x] README.md - Project overview
- [x] docs/architecture/ - Architecture with PlantUML diagrams
- [x] docs/api/ - API reference (6 endpoints)
- [x] docs/deployment/ - Docker + Kubernetes deployment guide
- [x] docs/diagrams/plantuml/ - 11 PlantUML diagram files
- [x] docs/testing/ - Testing strategy documentation
- [x] CONTEXT.md - Project context and TODO list

### Code Quality
- [x] Checkstyle configured with custom checkstyle.xml
- [x] ESLint for frontend
- [x] All imports sorted
- [x] Javadoc on public classes/methods

### Monitoring
- [x] Grafana dashboard for API service
- [x] Prometheus datasource configuration
- [x] MongoDB indexes for query optimization

---

## TODO TASKS

### High Priority
- [ ] Configure SonarQube integration

### Medium Priority
- [ ] Add API rate limiting

### Low Priority
- [ ] Add export to PDF for sinistres

---

## FILE STRUCTURE

```
kafka-stream/
├── kstream-service/           # Kafka Streams (Java 17)
│   ├── checkstyle.xml         # Custom checkstyle rules
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/
│       │   └── com/caa/dammages/streaming/
│       │       ├── KStreamApplication.java
│       │       ├── config/KafkaStreamConfig.java
│       │       ├── model/Sinistre.java
│       │       ├── model/SinistreAggregator.java
│       │       ├── service/AggregationService.java
│       │       └── serde/*.java
│       └── test/java/
│           └── com/caa/dammages/streaming/
│               ├── KStreamApplicationTest.java
│               ├── acceptance/KafkaStreamAcceptanceTest.java
│               └── e2e/EndToEndFlowTest.java
├── api-service/               # Spring Boot API (Java 17)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── frontend/                  # React Dashboard
│   ├── package.json
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
├── grafana/                   # Grafana dashboards
│   ├── api-service-dashboard.json
│   └── datasources.yml
├── kafka-connect/             # MongoDB Sink Connector
│   ├── Dockerfile
│   └── mongo-kafka-connect-*.jar
├── microcks/                  # Microcks AsyncAPI Mocking
│   ├── sinistres-asyncapi.yml
│   ├── docker-compose.yml
│   └── config/
│       └── application.properties
├── kafka-producer/            # Kafka init (topics + test data)
│   ├── Dockerfile
│   └── start.sh
├── docs/                      # Documentation
│   ├── testing/README.md
│   ├── architecture/README.md
│   ├── api/README.md
│   ├── deployment/README.md
│   └── diagrams/plantuml/*.puml
├── helm/                      # Kubernetes charts
├── ksqldb/                    # KSQL scripts
├── docker-compose.yml
├── .gitlab-ci.yml
├── README.md
├── PRD.md
└── CONTEXT.md
```

---

## TEST COMMANDS

```bash
# Run all tests
mvn test -f kstream-service/pom.xml

# Run specific test
mvn test -Dtest=KStreamApplicationTest -f kstream-service/pom.xml
mvn test -Dtest=KafkaStreamAcceptanceTest -f kstream-service/pom.xml
mvn test -Dtest=EndToEndFlowTest -f kstream-service/pom.xml
mvn test -Dtest=ApiAcceptanceTest -f api-service/pom.xml

# Checkstyle
mvn checkstyle:check -f kstream-service/pom.xml

# Frontend
cd frontend && npm run lint
cd frontend && npm run build
cd frontend && npm run test

# Code Coverage
mvn test jacoco:report -f kstream-service/pom.xml
mvn test jacoco:report -f api-service/pom.xml
```

---

## ENVIRONMENT VARIABLES

### kstream-service
| Variable | Default | Description |
|----------|---------|-------------|
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka brokers |
| KSTREAM_APP_ID | sinistres-stream-app | App identifier |
| SEUIL_CRITIQUE | 10000.0 | Critical threshold EUR |
| WINDOW_SIZE_MINUTES | 5 | Aggregation window |

### api-service
| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_DATA_MONGODB_URI | mongodb://localhost:27017 | MongoDB URI |
| SPRING_DATA_MONGODB_DATABASE | dommages_db | Database name |

### Docker Compose
| Variable | Default | Description |
|----------|---------|-------------|
| MONGO_ROOT_USER | admin | MongoDB admin user |
| MONGO_ROOT_PASSWORD | password | MongoDB admin password |

---

## API ENDPOINTS

### GET /api/v1/stats/contrat/{contratId}
Get stats for a specific contract.

### GET /api/v1/stats/contrat/{contratId}/historique
Get historical stats for a contract.

### GET /api/v1/stats/critiques?page=0&size=20
Get paginated critical sinistres.

### GET /api/v1/stats/critiques/all
Get all critical sinistres (no pagination).

### GET /api/v1/stats/critiques/contrat/{contratId}
Get critical sinistres for a contract.

### GET /api/v1/stats/critiques/seuil/{seuil}
Get sinistres above a threshold.

### GET /actuator/prometheus
Prometheus metrics endpoint.
