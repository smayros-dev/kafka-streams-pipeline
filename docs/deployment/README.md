# Deployment Guide

## Prérequis

- Docker 24.0+
- Docker Compose 2.20+
- Java 8+ (pour build local)
- Node.js 20+ (pour frontend)

## Déploiement Local (Docker Compose)

### 1. Cloner le repository

```bash
git clone https://github.com/caa/dammages-streaming.git
cd dammages-streaming
```

### 2. Configurer les variables d'environnement

```bash
# Créer le fichier .env
cat > .env << EOF
MONGO_ROOT_USER=admin
MONGO_ROOT_PASSWORD=password
EOF
```

### 3. Démarrer les services

```bash
# Build et démarrage
docker-compose up -d --build

# Vérifier les logs
docker-compose logs -f
```

### 4. Vérifier les services

| Service | URL | Port |
|---------|-----|------|
| Frontend | http://localhost:3000 | 3000 |
| API Backend | http://localhost:8080 | 8080 |
| MongoDB Express | http://localhost:8081 | 8081 |
| KSQLDB | http://localhost:8088 | 8088 |
| Kafka Connect | http://localhost:8083 | 8083 |

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

## Variables d'Environnement

### KStream Service

| Variable | Description | Défaut |
|----------|-------------|--------|
| KAFKA_BOOTSTRAP_SERVERS | Serveurs Kafka | localhost:9092 |
| KSTREAM_APP_ID | ID de l'application | sinistres-stream-app |
| SEUIL_CRITIQUE | Seuil sinistre critique | 10000.0 |
| WINDOW_SIZE_MINUTES | Taille fenêtre agrégation | 5 |

### API Service

| Variable | Description | Défaut |
|----------|-------------|--------|
| SPRING_DATA_MONGODB_URI | URI MongoDB | mongodb://localhost:27017 |
| SPRING_DATA_MONGODB_DATABASE | Base de données | dommages_db |

## Monitoring

### Health Checks

```bash
# KStream Service
curl http://localhost:8080/actuator/health

# API Service
curl http://localhost:8080/actuator/health
```

### Métriques Prometheus

```bash
curl http://localhost:8080/actuator/prometheus
```

## Troubleshooting

### Problème : Kafka ne démarre pas

```bash
# Vérifier les logs
docker-compose logs kafka

# Redémarrer
docker-compose restart kafka
```

### Problème : MongoDB connection refused

```bash
# Vérifier l'authentification
docker-compose exec mongodb mongo -u admin -p password

# Vérifier les variables d'environnement
docker-compose config
```

### Problème : KStream rebalance infini

```bash
# Vérifier les logs
docker-compose logs kstream-service

# Augmenter les ressources
docker-compose up -d --scale kstream-service=2
```
