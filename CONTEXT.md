# Data Dammages - Project Context

## DONE TASKS

### Infrastructure
- [x] Docker Compose with 12 services (Zookeeper, Kafka, KSQLDB, MongoDB, Mongo Express, Kafka Connect, kstream-service, api-service, frontend, Grafana, Microcks, kafka-producer)
- [x] Helm charts for kstream-service and api-service
- [x] GitLab CI/CD pipeline
- [x] MongoDB authentication enabled
- [x] Docker health checks for all services
- [x] Grafana dashboard for API monitoring (8 panels)
- [x] Microcks for async API mocking
- [x] kafka-producer for realistic mock Kafka data (15 scenarios, 3s interval)
- [x] MongoDB Sink Connectors (sinistres-critiques, stats-contrat) via Kafka Connect
- [x] kafka-init creates topics + sink connectors

### Backend - kstream-service
- [x] KStreamApplication main entry point with CountDownLatch shutdown
- [x] AggregationService with 3 branches: KStream filter, KTable windowed aggregation, KGlobalTable join
- [x] Sinistre model with Jackson annotations
- [x] SinistreAggregator model with windowStart/windowEnd
- [x] Custom Serde implementations (SinistreSerde, SinistreAggregatorSerde)
- [x] KafkaStreamConfig with environment variables
- [x] Checkstyle: 0 violations
- [x] Java 17 migration
- [x] JaCoCo code coverage reporting
- [x] HealthController (/health endpoint)

### Backend - api-service
- [x] Spring Boot 3.1 REST API
- [x] StatsContratController with 6 endpoints (including paginated /critiques)
- [x] SinistreCritiqueRepository
- [x] StatsContratRepository (findFirstByContratIdOrderByWindowEndDesc for windowed data)
- [x] Dockerfile updated to Java 17
- [x] Prometheus metrics endpoint (/actuator/prometheus)
- [x] JaCoCo code coverage reporting
- [x] Rate limiting (Bucket4j, 100 req/min per IP)
- [x] CORS configuration (CorsConfig)
- [x] Health check endpoint (/actuator/health)

### Frontend
- [x] React 18 + TypeScript + Vite setup
- [x] StatCard, SinistresCritiquesTable, StatsChart components
- [x] Dashboard page with routing
- [x] ESLint configured and passing
- [x] Dockerfile with nginx (proxy /api to backend)
- [x] Build passing
- [x] ErrorBoundary component for error handling
- [x] LoadingSpinner component for loading states
- [x] ThemeContext with dark mode toggle (localStorage)
- [x] ExportButton for CSV/JSON export
- [x] Vitest unit tests (8 tests passing)
- [x] Code splitting (React.lazy + Suspense for Dashboard)

### Testing
- [x] 8 unit tests (KStreamApplicationTest) - TopologyTestDriver
- [x] 2 acceptance tests (KafkaStreamAcceptanceTest) - Testcontainers Kafka
- [x] 5 acceptance tests (ApiAcceptanceTest) - Testcontainers MongoDB
- [x] 3 E2E tests (EndToEndFlowTest) - Full pipeline scenarios
- [x] 8 frontend tests (StatCard, SinistresCritiquesTable)
- [x] **Total: 26 tests, all passing**

### Documentation
- [x] PRD.md - Product requirements
- [x] README.md - Project overview (updated with kafka-producer, 12 services)
- [x] docs/architecture/ - Architecture with PlantUML diagrams
- [x] docs/api/ - API reference (6 endpoints + health + prometheus)
- [x] docs/deployment/ - Docker + Kubernetes deployment guide
- [x] docs/testing/ - Testing strategy (26 tests documented)
- [x] docs/diagrams/plantuml/ - 11 PlantUML diagram files
- [x] CONTEXT.md - Project context and TODO list

### Code Quality
- [x] Checkstyle configured with custom checkstyle.xml
- [x] ESLint for frontend
- [x] All imports sorted
- [x] Javadoc on public classes/methods

### Monitoring
- [x] Grafana dashboard for API service (8 panels)
- [x] Prometheus datasource configuration
- [x] MongoDB indexes for query optimization

### Data Pipeline (verified end-to-end)
- [x] kafka-producer → raw-sinistres (15 scenarios, 3s interval)
- [x] kstream-service consumes raw-sinistres + contrats-ref
- [x] KStream filter → sinistres-critiques topic
- [x] KTable windowed aggregation (5min) → stats-contrat-5m topic
- [x] KGlobalTable join → sinistres-enrichis topic
- [x] MongoDB Sink Connectors persist to MongoDB
- [x] API serves data (301+ critiques, all types: AUTO, HABITATION, SANTE, INCENDIE)
- [x] Frontend displays data at http://localhost:3000

---

## TODO TASKS

### High Priority
- [ ] Configure SonarQube integration

### Medium Priority
- [ ] (none - all features implemented)

### Low Priority
- [ ] (none)

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
│       │       ├── controller/HealthController.java
│       │       ├── model/Sinistre.java
│       │       ├── model/SinistreAggregator.java
│       │       ├── service/AggregationService.java
│       │       └── serde/*.java
│       └── test/java/
│           └── com/caa/dammages/streaming/
│               ├── KStreamApplicationTest.java (8 unit tests)
│               ├── acceptance/KafkaStreamAcceptanceTest.java (2 tests)
│               └── e2e/EndToEndFlowTest.java (3 tests)
├── api-service/               # Spring Boot API (Java 17)
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
│       ├── context/ThemeContext.tsx
│       ├── pages/Dashboard.tsx (lazy loaded)
│       └── services/ (api.ts, export.ts)
├── grafana/                   # Grafana dashboards
│   ├── api-service-dashboard.json (8 panels)
│   └── datasources.yml
├── kafka-connect/             # MongoDB Sink Connector
│   ├── Dockerfile             # Downloads JARs from Maven Central
│   └── (JARs auto-downloaded at build time)
├── kafka-init/                # Topics + Connectors init
│   ├── Dockerfile             # Uses project root as build context
│   └── start.sh               # Creates topics + sink connectors
├── kafka-producer/            # Mock sinistres data
│   ├── Dockerfile
│   └── produce.sh             # 15 scenarios, kafka-console-producer
├── microcks/                  # AsyncAPI spec
│   ├── sinistres-asyncapi.yml (v1.1.0, 15 message examples)
│   └── config/
├── ksqldb/                    # KSQL scripts
├── helm/                      # Kubernetes charts
├── docs/                      # Documentation
│   ├── README.md
│   ├── api/README.md
│   ├── architecture/README.md
│   ├── deployment/README.md
│   ├── testing/README.md
│   └── diagrams/plantuml/*.puml
├── docker-compose.yml         # 12 services
├── .gitlab-ci.yml
├── .gitignore
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
| KAFKA_BOOTSTRAP_SERVERS | kafka:29092 | Kafka brokers |
| KSTREAM_APP_ID | sinistres-stream-app | App identifier |
| SEUIL_CRITIQUE | 10000.0 | Critical threshold EUR |
| WINDOW_SIZE_MINUTES | 5 | Aggregation window |

### api-service
| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_DATA_MONGODB_URI | mongodb://admin:password@mongodb:27017/dommages_db?authSource=admin | MongoDB URI |
| SPRING_DATA_MONGODB_DATABASE | dommages_db | Database name |

### kafka-producer
| Variable | Default | Description |
|----------|---------|-------------|
| KAFKA_BOOTSTRAP_SERVERS | kafka:29092 | Kafka brokers |
| KAFKA_TOPIC | raw-sinistres | Target topic |
| PRODUCE_FREQUENCY | 3 | Interval in seconds |

### Docker Compose
| Variable | Default | Description |
|----------|---------|-------------|
| MONGO_ROOT_USER | admin | MongoDB admin user |
| MONGO_ROOT_PASSWORD | password | MongoDB admin password |

---

## API ENDPOINTS

### GET /api/v1/stats/contrat/{contratId}
Get latest aggregation for a contract (most recent window).

### GET /api/v1/stats/contrat/{contratId}/historique
Get all aggregation windows for a contract.

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

### GET /health
kstream-service health check.
