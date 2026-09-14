# Data Dammages - Documentation

## Table des matières

1. [Architecture](./architecture/README.md)
2. [API Reference](./api/README.md)
3. [Deployment](./deployment/README.md)
4. [Diagrams](./diagrams/README.md)

## Vue d'ensemble

Data Dammages est un pipeline de streaming temps réel pour l'agrégation et le filtrage des sinistres d'assurance du groupe CAA.

### Stack technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| Stream Processing | Kafka Streams | 3.5 |
| Query Language | KSQLDB | 0.29 |
| Database | MongoDB | 6.0 |
| API Backend | Spring Boot | 3.1 |
| Frontend | React + Vite | 18.2 |
| Container | Docker + Compose | 24.0 |
| CI/CD | GitLab CI | - |
| Orchestration | Kubernetes (Helm) | 1.13 |

### Métriques du projet

| Métrique | Valeur |
|----------|--------|
| Tests unitaires | 8 |
| Couverture de code | 80%+ |
| Temps de latence cible | < 300ms |
| Disponibilité cible | 99.9% |
