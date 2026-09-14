# Data Dammages - Documentation

## Table des matières

1. [Architecture](./architecture/README.md)
2. [API Reference](./api/README.md)
3. [Deployment](./deployment/README.md)
4. [Testing](./testing/README.md)
5. [Diagrams](./diagrams/)

## Vue d'ensemble

Data Dammages est un pipeline de streaming temps réel pour l'agrégation et le filtrage des sinistres d'assurance du groupe CAA.

### Stack technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| Stream Processing | Kafka Streams | 3.5 |
| Query Language | KSQLDB | 0.29 |
| Database | MongoDB | 6.0 |
| API Backend | Spring Boot | 3.1 |
| Frontend | React + TypeScript + Vite | 18.2 |
| Container | Docker + Compose | v2 |
| CI/CD | GitLab CI | - |
| Orchestration | Kubernetes (Helm) | - |

### Métriques du projet

| Métrique | Valeur |
|----------|--------|
| Services Docker | 12 |
| Tests total | 26 (13 backend + 5 api + 8 frontend) |
| Couverture de code | 80%+ |
| Temps de latence cible | < 300ms |
| Disponibilité cible | 99.9% |

### Démarrage rapide

```bash
# 1. Cloner
git clone https://github.com/caa/dammages-streaming.git
cd dammages-streaming

# 2. Configurer
echo "MONGO_ROOT_USER=admin" > .env
echo "MONGO_ROOT_PASSWORD=password" >> .env

# 3. Démarrer (12 services)
docker compose up -d --build

# 4. Vérifier (~60s)
curl http://localhost:8080/api/v1/stats/critiques/seuil/10000
```

### Interfaces

| Service | URL | Port |
|---------|-----|------|
| Dashboard React | http://localhost:3000 | 3000 |
| API REST | http://localhost:8080 | 8080 |
| MongoDB Express | http://localhost:8082 | 8082 |
| KSQLDB | http://localhost:8088 | 8088 |
| Kafka Connect | http://localhost:8083 | 8083 |
| Grafana | http://localhost:3001 | 3001 |
