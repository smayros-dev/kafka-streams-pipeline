# Guide Développeur - Data Dammages

## Prérequis

- Java 17+
- Maven 3.9+
- Node.js 20+
- Docker 24+ et Docker Compose v2

## Démarrage Rapide

### 1. Démarrer le stack complet

```bash
# Cloner
git clone https://gitlab.com/caa/kafka-streams-pipeline.git
cd kafka-streams-pipeline

# Configurer
echo "MONGO_ROOT_USER=admin" > .env
echo "MONGO_ROOT_PASSWORD=password" >> .env

# Démarrer (12 services, ~60s)
docker compose up -d --build

# Vérifier
curl -s http://localhost:8080/api/v1/stats/critiques/seuil/10000 | python3 -c "import sys,json; print(f'{len(json.load(sys.stdin))} sinistres critiques')"
```

### 2. Arrêter le stack

```bash
docker compose down      # Garde les données
docker compose down -v   # Reset complet (supprime volumes)
```

---

## Structure de l'Application

```
kafka-producer → raw-sinistres → kstream-service → sinistres-critiques ─┐
                                    │                                    │
                                    ├→ stats-contrat-5m ─────────────────┤
                                    │                                    │
                                    └→ sinistres-enrichis ───────────────┤
                                                                         ↓
                                                            MongoDB Sink Connectors
                                                                         ↓
                                                                    MongoDB
                                                                         ↓
                                                               Spring Boot API
                                                                         ↓
                                                                React Dashboard
```

### Rôles des services

| Service | Port | Rôle |
|---------|------|------|
| `zookeeper` | 2181 | Coordination Kafka |
| `kafka` | 9092 | Broker de messages |
| `ksqldb` | 8088 | Query language SQL |
| `mongodb` | 27017 | Base de données |
| `mongo-express` | 8082 | Admin UI MongoDB |
| `kafka-connect` | 8083 | Sink connectors MongoDB |
| `kafka-init` | - | Création topics + connectors (one-shot) |
| `kafka-producer` | - | Données mock (15 scénarios, 3s) |
| `kstream-service` | 8080 | Kafka Streams (filtrage, agrégation, join) |
| `api-service` | 8080 | API REST Spring Boot |
| `frontend` | 3000 | Dashboard React |
| `grafana` | 3001 | Monitoring |

---

## Développement Local

### kstream-service (Kafka Streams)

```bash
# Compiler
mvn clean compile -f kstream-service/pom.xml

# Lancer les tests
mvn test -f kstream-service/pom.xml

# Checkstyle
mvn checkstyle:check -f kstream-service/pom.xml

# Code coverage
mvn test jacoco:report -f kstream-service/pom.xml
```

### api-service (Spring Boot)

```bash
# Compiler
mvn clean compile -f api-service/pom.xml

# Lancer les tests
mvn test -f api-service/pom.xml

# Lancer en local ( nécessite MongoDB sur localhost:27017 )
mvn spring-boot:run -f api-service/pom.xml
```

### frontend (React)

```bash
cd frontend

# Installer les dépendances
npm install

# Lancer en dev
npm run dev

# Lint
npm run lint

# Tests
npm run test

# Build production
npm run build
```

---

## Topologie Kafka Streams

La topologie est définie dans `AggregationService.java` et contient 3 branches :

### Branche 1 : Filtrage (KStream)

```
raw-sinistres → filter(montant > 10000) → sinistres-critiques
```

- **Stateless** : pas d'état, traitement par événement
- **Output** : `sinistres-critiques` topic

### Branche 2 : Agrégation (KTable)

```
raw-sinistres → groupBy(contratId) → windowedBy(5min) → aggregate → stats-contrat-5m
```

- **Stateful** : materialized store dans Kafka
- **Fenêtre** : 5 minutes (tumbling, no grace)
- **Output** : `stats-contrat-5m` topic avec `windowStart`/`windowEnd`

### Branche 3 : Join (KGlobalTable)

```
raw-sinistres → selectKey(contratId) → leftJoin(contrats-ref) → sinistres-enrichis
```

- **GlobalKTable** : répliqué sur toutes les partitions
- **Output** : `sinistres-enrichis` topic

### Tester la topologie

```bash
# Les tests unitaires utilisent TopologyTestDriver (pas de Docker)
mvn test -Dtest=KStreamApplicationTest -f kstream-service/pom.xml
```

---

## Modèles de Données

### Sinistre (entrée)

```java
{
  "sinistreId": "SIN-721",
  "contratId": "CTR-200",
  "montantSinistre": 32000.0,
  "typeSinistre": "HABITATION",
  "dateDeclaration": 1789410154000
}
```

### SinistreCritique (sortie branche 1)

```java
{
  "sinistreId": "SIN-721",
  "contratId": "CTR-200",
  "montantSinistre": 32000.0,
  "typeSinistre": "HABITATION",
  "dateDeclaration": 1789410154000,
  "seuilDepasse": 10000.0
}
```

### SinistreAggregator (sortie branche 2)

```java
{
  "contratId": "CTR-200",
  "totalMontant": 96000.0,
  "nbSinistres": 3,
  "windowStart": 1789410000000,
  "windowEnd": 1789410300000
}
```

---

## Ajouter un Nouveau Type de Sinistre

### 1. Ajouter dans le producer

Éditer `kafka-producer/produce.sh` et ajouter un nouveau scénario :

```bash
"SIN-NEW|CTR-100|25000.0|NOUVEAU_TYPE|$(date +%s)000")
```

### 2. Mettre à jour l'AsyncAPI

Éditer `microcks/sinistres-asyncapi.yml` pour documenter le nouveau type.

### 3. Tester

```bash
docker compose restart kafka-producer
sleep 10
curl -s http://localhost:8080/api/v1/stats/critiques/all | python3 -c "
import sys, json
data = json.load(sys.stdin)
types = set(s['typeSinistre'] for s in data)
print('Types:', types)
"
```

---

## Ajouter un Nouvel Endpoint API

### 1. Ajouter la méthode au repository

Dans `api-service/src/main/java/com/caa/dammages/api/repository/SinistreCritiqueRepository.java` :

```java
List<SinistreCritique> findByTypeSinistreOrderByDateDeclarationDesc(String typeSinistre);
```

### 2. Ajouter le endpoint au controller

Dans `api-service/src/main/java/com/caa/dammages/api/controller/StatsContratController.java` :

```java
@GetMapping("/critiques/type/{type}")
public ResponseEntity<List<SinistreCritique>>
        getSinistresByType(@PathVariable String type) {
    List<SinistreCritique> critiques =
            sinistresRepository.findByTypeSinistreOrderByDateDeclarationDesc(type);
    return ResponseEntity.ok(critiques);
}
```

### 3. Tester

```bash
mvn test -f api-service/pom.xml
curl http://localhost:8080/api/v1/stats/critiques/type/AUTO
```

---

## Ajouter un Composant Frontend

### 1. Créer le composant

Dans `frontend/src/components/` :

```tsx
// MonComposant.tsx
import React from 'react';

interface MonComposantProps {
  data: string;
}

export const MonComposant: React.FC<MonComposantProps> = ({ data }) => {
  return (
    <div>
      <p>{data}</p>
    </div>
  );
};
```

### 2. Ajouter un test

Dans `frontend/src/components/__tests__/MonComposant.test.tsx` :

```tsx
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MonComposant } from '../MonComposant';

describe('MonComposant', () => {
  it('affiche les données', () => {
    render(<MonComposant data="Hello" />);
    expect(screen.getByText('Hello')).toBeInTheDocument();
  });
});
```

### 3. Lancer les tests

```bash
cd frontend && npm run test
```

---

## Debugging

### Voir les logs en temps réel

```bash
# Tous les services
docker compose logs -f

# Service spécifique
docker compose logs -f kstream-service
docker compose logs -f api-service
docker compose logs -f kafka-producer
```

### Vérifier les topics Kafka

```bash
# Lister les topics
docker compose exec kafka kafka-topics \
  --bootstrap-server localhost:9092 --list

# Voir les messages d'un topic
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic sinistres-critiques \
  --from-beginning \
  --max-messages 5
```

### Vérifier les connectors

```bash
# Lister
curl http://localhost:8083/connectors

# Statut
curl http://localhost:8083/connectors/mongo-sinistres-critiques/status
curl http://localhost:8083/connectors/mongo-stats-contrat/status
```

### Vérifier MongoDB

```bash
# Connexion
docker compose exec mongodb mongosh -u admin -p password

# Collections
use dommages_db
db.sinistres_critiques.countDocuments()
db.stats_contrat.countDocuments()
```

### Reset complet

```bash
docker compose down -v
docker compose up -d --build
```

---

## CI/CD Pipeline

Le pipeline GitLab CI (`.gitlab-ci.yml`) exécute :

| Stage | Commande |
|-------|----------|
| lint | `mvn checkstyle:check`, `npm run lint` |
| test | `mvn test` (kstream-service + api-service) |
| acceptance | `mvn test -Dtest=*AcceptanceTest` |
| e2e | `mvn test -Dtest=*EndToEndTest` |
| package | `docker build` |
| deploy | `helm upgrade --install` |

---

## Commandes Utiles

```bash
# === Build ===
mvn clean compile -f kstream-service/pom.xml
mvn clean compile -f api-service/pom.xml
cd frontend && npm run build

# === Tests ===
mvn test -f kstream-service/pom.xml
mvn test -f api-service/pom.xml
cd frontend && npm run test

# === Qualité ===
mvn checkstyle:check -f kstream-service/pom.xml
cd frontend && npm run lint

# === Coverage ===
mvn test jacoco:report -f kstream-service/pom.xml
mvn test jacoco:report -f api-service/pom.xml

# === Docker ===
docker compose up -d --build
docker compose down -v
docker compose ps
docker compose logs -f kstream-service

# === Kafka ===
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
docker compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic raw-sinistres --from-beginning --max-messages 3
```
