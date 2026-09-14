# Deployment Guide

## Prérequis

- Docker 24.0+ et Docker Compose v2
- Java 17+ (pour build local)
- Node.js 20+ (pour frontend)
- macOS ARM64 nécessite Rancher Desktop ou Docker Desktop avec Rosetta

## Déploiement Local (Docker Compose)

### 1. Cloner le repository

```bash
git clone https://gitlab.com/caa/kafka-streams-pipeline.git
cd kafka-streams-pipeline
```

### 2. Configurer les variables d'environnement

```bash
cat > .env << EOF
MONGO_ROOT_USER=admin
MONGO_ROOT_PASSWORD=password
EOF
```

### 3. Démarrer les services

```bash
# Build et démarrage (12 services)
docker compose up -d --build

# Vérifier la progression (~60s)
docker compose ps
```

### 4. Ordre de démarrage

Les services démarrent dans cet ordre (via health checks + depends_on) :

```
zookeeper → kafka → ksqldb, kafka-connect, mongodb
                           ↓
                     kafka-init (topics + connectors)
                           ↓
               kafka-producer, kstream-service
                           ↓
                     api-service, frontend
```

### 5. Vérifier les services

| Service | URL | Port | Health Check |
|---------|-----|------|--------------|
| Frontend React | http://localhost:3000 | 3000 | `curl http://localhost:3000` |
| API Backend | http://localhost:8080 | 8080 | `curl http://localhost:8080/actuator/health` |
| API Prometheus | http://localhost:8080/actuator/prometheus | 8080 | - |
| MongoDB Express | http://localhost:8082 | 8082 | - |
| KSQLDB | http://localhost:8088 | 8088 | `curl http://localhost:8088/info` |
| Kafka Connect | http://localhost:8083 | 8083 | `curl http://localhost:8083/connectors` |
| Grafana | http://localhost:3001 | 3001 | `curl http://localhost:3001/api/health` |
| Microcks | http://localhost:8585 | 8585 | `curl http://localhost:8585/api/health` |
| Zookeeper | localhost:2181 | 2181 | - |
| Kafka | localhost:9092 | 9092 | - |

### 6. Vérifier le pipeline

```bash
# Vérifier que des données circulent
curl -s http://localhost:8080/api/v1/stats/critiques/seuil/10000 | python3 -c "
import sys, json
data = json.load(sys.stdin)
print(f'Sinistres critiques trouvés: {len(data)}')
types = {}
for s in data:
    t = s['typeSinistre']
    types[t] = types.get(t, 0) + 1
for t, c in sorted(types.items()):
    print(f'  {t}: {c}')
"

# Vérifier les stats agrégées
curl -s http://localhost:8080/api/v1/stats/contrat/CTR-100 | python3 -m json.tool
```

### 7. Voir les logs

```bash
# Tous les services
docker compose logs -f

# Service spécifique
docker compose logs -f kstream-service
docker compose logs -f kafka-producer
docker compose logs -f api-service

# Dernières 100 lignes
docker compose logs --tail 100 kstream-service
```

### 8. Arrêter le stack

```bash
# Arrêter sans supprimer les données
docker compose down

# Arrêter et supprimer les volumes (reset complet)
docker compose down -v
```

## Variables d'Environnement

### kstream-service

| Variable | Description | Défaut |
|----------|-------------|--------|
| KAFKA_BOOTSTRAP_SERVERS | Serveurs Kafka | kafka:29092 |
| KSTREAM_APP_ID | ID de l'application | sinistres-stream-app |
| SEUIL_CRITIQUE | Seuil sinistre critique | 10000.0 |
| WINDOW_SIZE_MINUTES | Taille fenêtre agrégation | 5 |

### api-service

| Variable | Description | Défaut |
|----------|-------------|--------|
| SPRING_DATA_MONGODB_URI | URI MongoDB | mongodb://admin:password@mongodb:27017/dommages_db?authSource=admin |
| SPRING_DATA_MONGODB_DATABASE | Base de données | dommages_db |

### kafka-producer

| Variable | Description | Défaut |
|----------|-------------|--------|
| KAFKA_BOOTSTRAP_SERVERS | Serveurs Kafka | kafka:29092 |
| KAFKA_TOPIC | Topic cible | raw-sinistres |
| PRODUCE_FREQUENCY | Intervalle secondes | 3 |

### Docker Compose

| Variable | Description | Défaut |
|----------|-------------|--------|
| MONGO_ROOT_USER | Utilisateur MongoDB | admin |
| MONGO_ROOT_PASSWORD | Mot de passe MongoDB | password |

## Architecture Docker

```
                    ┌─────────────┐
                    │  Zookeeper  │
                    │   :2181     │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │    Kafka    │
                    │   :9092     │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐ ┌──▼───┐ ┌──────▼──────┐
       │   KSQLDB    │ │ ksqldb│ │Kafka Connect│
       │   :8088     │ │       │ │   :8083     │
       └─────────────┘ └──────┘ └──────┬──────┘
                                       │
                    ┌───────────────────┤
                    │                   │
             ┌──────▼──────┐    ┌──────▼──────┐
             │ kafka-init  │    │   MongoDB   │
             │ (one-shot)  │    │   :27017    │
             └──────┬──────┘    └──────┬──────┘
                    │                   │
              ┌─────┼───────────────────┤
              │     │                   │
       ┌──────▼────┐ ┌─────────┐ ┌─────▼──────┐
       │  kstream  │ │  kafka  │ │ api-service│
       │  service  │ │producer │ │   :8080    │
       └───────────┘ └─────────┘ └─────┬──────┘
                                       │
                              ┌────────▼────────┐
                              │    Frontend     │
                              │     :3000       │
                              └─────────────────┘
```

## Déploiement Kubernetes (Helm)

### Prérequis

- Kubernetes 1.24+
- Helm 3.12+
- Kubectl configuré

### 1. Installer les charts

```bash
# KStream Service
helm install kstream-service ./helm/kstream-service \
  --namespace dammages \
  --create-namespace \
  --set image.tag=latest

# API Service
helm install api-service ./helm/api-service \
  --namespace dammages \
  --set image.tag=latest
```

### 2. Vérifier le déploiement

```bash
kubectl get pods -n dammages
kubectl get services -n dammages
```

### 3. Mettre à jour

```bash
helm upgrade kstream-service ./helm/kstream-service \
  --namespace dammages \
  --set image.tag=<new-version>
```

## Monitoring

### Health Checks

```bash
# kstream-service
curl http://localhost:8080/health

# API Service
curl http://localhost:8080/actuator/health

# Kafka Connect
curl http://localhost:8083/connectors

# KSQLDB
curl http://localhost:8088/info
```

### Métriques Prometheus

```bash
curl http://localhost:8080/actuator/prometheus
```

### Grafana Dashboard

1. Ouvrir http://localhost:3001 (admin/admin)
2. Les datasources Prometheus sont pré-configurées
3. Le dashboard API Service est pré-chargé avec 8 panneaux

## Troubleshooting

### Problème : kstream-service redémarre en boucle

```bash
# Cause probable : state store incompatible après changement de topologie
# Solution : reset complet
docker compose down -v
docker compose up -d --build
```

### Problème : Kafka ne démarre pas

```bash
docker compose logs kafka
docker compose restart kafka
```

### Problème : MongoDB connection refused

```bash
# Vérifier l'authentification
docker compose exec mongodb mongosh -u admin -p password

# Vérifier les variables d'environnement
docker compose config
```

### Problème : kafka-producer ne produit pas

```bash
# Vérifier que kafka-init a bien créé les topics
docker compose logs kafka-init

# Vérifier les logs du producer
docker compose logs kafka-producer
```

### Problème : API retourne 500 sur /stats/contrat

```bash
# Cause probable : plusieurs documents par contratId (fenêtres glissantes)
# Vérifier que le repository utilise findFirstByContratIdOrderByWindowEndDesc
docker compose logs api-service | grep "Exception"
```

### Problème : Frontend ne charge pas

```bash
# Vérifier nginx proxy
docker compose logs frontend

# Vérifier que api-service est healthy
curl http://localhost:8080/actuator/health
```
