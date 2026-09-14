# Document de Spécifications Produit (PRD)

## 1. Contexte & Objectifs

### 1.1 Contexte

Dans le cadre de la stratégie Data du groupe **CAA** (assurance Dommages), la tribu **Data Dammages** souhaite disposer d'un pipeline de streaming temps réel pour l'agrégation et le filtrage des sinistres.

Le socle Data existant est constitué :
- d'une chaîne d'ingestion basée sur **Kafka**, **KSQLDB**, **Kafka Streams**
- d'une capacité de stockage/exposition sous **MongoDB**
- de plusieurs API d'exposition développées en **Java** (Spring Boot)

### 1.2 Objectifs

Mettre en place un pipeline de streaming **tolérant aux pannes, scalable et à faible latence** qui :
1. Consomme les flux de sinistres Dommages depuis Kafka
2. Applique le filtrage et les agrégations en temps réel via KSQLDB et Kafka Streams
3. Persiste les résultats analytiques et alertes dans MongoDB
4. Expose les statistiques via une API REST (Spring Boot)

### 1.3 Cas d'usage métier

| Cas | Description |
|-----|-------------|
| **Filtrage sinistres critiques** | Détecter immédiatement tout sinistre avec un montant supérieur à **10 000 EUR** |
| **Agrégation temps réel par contrat** | Calculer la somme glissante des montants de sinistres par `contratId` sur des fenêtres de **5 minutes** |

---

## 2. Architecture globale

```
                        ┌──────────────────────────────────┐
                        │        KSQLDB Server              │
                        │  (Filtrage SQL des sinistres)     │
                        └─────────────┬────────────────────┘
                                      │
  Kafka Topic: raw-sinistres ─────────┤
                                      │
                        ┌─────────────▼────────────────────┐
                        │      Kafka Streams (Java)         │
                        │  Agrégation glissante 5min        │
                        │  par contratId                    │
                        └──────┬───────────────┬───────────┘
                               │               │
                ┌──────────────▼───┐   ┌───────▼───────────────┐
                │  Topic Kafka    │   │  Topic Kafka          │
                │  sinistres-     │   │  stats-contrat-5m     │
                │  critiques      │   │                       │
                └────────┬────────┘   └───────────┬───────────┘
                         │                        │
                         │  (Mongo Sink Connector) │
                         ▼                        ▼
                ┌────────────────────────────────────────────┐
                │              MongoDB                        │
                │  dommages_db                                │
                │  ├── sinistres_critiques                    │
                │  └── stats_contrat                          │
                └────────────────────────────────────────────┘
```

---

## 3. Spécifications Fonctionnelles & Flux de Données

### 3.1 Ingestion (raw-sinistres)

Réception continue de sinistres Dommages au format JSON.

### 3.2 Traitement KSQLDB (Branche 1 : Filtrage)

Filtrage SQL des sinistres critiques (montant > 10 000 EUR) via KSQLDB Server.

### 3.3 Traitement Kafka Streams (Branche 2 : Agrégation)

Calcul de la somme glissante des montants de sinistres par `contratId` sur les 5 dernières minutes. Les agrégations sont redirigées vers le topic `stats-contrat-5m`.

### 3.4 Persistance MongoDB

Kafka Connect (MongoDB Sink Connector) :
- Consomme `sinistres-critiques` → écrit dans la collection `sinistres_critiques`
- Consomme `stats-contrat-5m` → upsert dans la collection `stats_contrat`

### 3.5 Exposition API

API REST Spring Boot expose les statistiques agrégées par contrat.

---

## 4. Spécifications des Événements

### 4.1 Schéma d'entrée (raw-sinistres)

```json
{
  "sinistreId": "sin-2024-001",
  "contratId": "CTR-78945",
  "montantSinistre": 12500.00,
  "typeSinistre": "COLLISION",
  "dateDeclaration": 1773220000000
}
```

| Champ | Type | Description |
|-------|------|-------------|
| `sinistreId` | String | Identifiant unique du sinistre |
| `contratId` | String | Identifiant du contrat d'assurance |
| `montantSinistre` | Double | Montant du sinistre en EUR |
| `typeSinistre` | String | Type de sinistre (COLLISION, INCENDIE, VOL, etc.) |
| `dateDeclaration` | Long | Timestamp de déclaration (epoch ms) |

### 4.2 Schéma de sortie — sinistres critiques (sinistres-critiques)

```json
{
  "sinistreId": "sin-2024-001",
  "contratId": "CTR-78945",
  "montantSinistre": 12500.00,
  "typeSinistre": "COLLISION",
  "dateDeclaration": 1773220000000,
  "seuilDepasse": 10000.00
}
```

### 4.3 Schéma de sortie — stats agrégées (stats-contrat-5m)

```json
{
  "contratId": "CTR-78945",
  "totalMontant": 24350.00,
  "nbSinistres": 3,
  "windowStart": 1773219700000,
  "windowEnd": 1773220000000
}
```

---

## 5. Implémentation KSQLDB

### 5.1 Création du stream des sinistres bruts

```sql
CREATE STREAM stream_sinistres (
  sinistreId VARCHAR KEY,
  contratId VARCHAR,
  montantSinistre DOUBLE,
  typeSinistre VARCHAR,
  dateDeclaration BIGINT
) WITH (
  KAFKA_TOPIC = 'raw-sinistres',
  VALUE_FORMAT = 'JSON'
);
```

### 5.2 Table des sinistres critiques (montant > 10 000 EUR)

```sql
CREATE TABLE sinistres_critiques AS
  SELECT
    sinistreId,
    contratId,
    montantSinistre,
    typeSinistre,
    dateDeclaration
  FROM stream_sinistres
  WHERE montantSinistre > 10000
  EMIT CHANGES;
```

### 5.3 Table d'agrégation glissante par contrat (fenêtre 5 min)

```sql
CREATE TABLE stats_contrat_5m AS
  SELECT
    contratId,
    SUM(montantSinistre) AS totalMontant,
    COUNT(*) AS nbSinistres,
    WINDOWSTART AS debutFenetre,
    WINDOWEND AS finFenetre
  FROM stream_sinistres
  WINDOW TUMBLING (SIZE 5 MINUTES)
  GROUP BY contratId
  EMIT CHANGES;
```

---

## 6. Implémentation Kafka Streams (Java 8+)

### 6.1 Structure du projet

```
kstream-service/
├── pom.xml
├── src/main/java/com/caa/dammages/streaming/
│   ├── KStreamApplication.java
│   ├── config/
│   │   └── KafkaStreamConfig.java
│   ├── model/
│   │   └── Sinistre.java
│   ├── serde/
│   │   └── SinistreSerde.java
│   └── service/
│       └── AggregationService.java
├── src/test/java/com/caa/dammages/streaming/
│   ├── KStreamApplicationTest.java
│   └── AggregationServiceTest.java
└── src/main/resources/
    └── application.yml
```

### 6.2 Code principal — KStreamApplication.java

```java
package com.caa.dammages.streaming;

import com.caa.dammages.streaming.model.Sinistre;
import com.caa.dammages.streaming.serde.SinistreSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.common.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

public class KStreamApplication {

    private static final Logger log = LoggerFactory.getLogger(KStreamApplication.class);
    private static final double SEUIL_CRITIQUE = 10000.0;

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "sinistres-stream-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        SinistreSerde sinistreSerde = new SinistreSerde();

        KStream<String, String> rawSinistres = builder.stream("raw-sinistres");

        // Branche 1 : Sinistres critiques (montant > seuil)
        rawSinistres
            .filter((key, jsonValue) -> {
                try {
                    Sinistre s = sinistreSerde.deserializer().deserialize("raw-sinistres", jsonValue.getBytes());
                    return s != null && s.getMontantSinistre() > SEUIL_CRITIQUE;
                } catch (Exception e) {
                    log.error("Erreur filtrage sinistre critique: {}", e.getMessage());
                    return false;
                }
            })
            .mapValues((jsonValue) -> {
                try {
                    Sinistre s = sinistreSerde.deserializer().deserialize("raw-sinistres", jsonValue.getBytes());
                    return String.format(
                        "{\"sinistreId\":\"%s\",\"contratId\":\"%s\",\"montantSinistre\":%.2f,"
                        + "\"typeSinistre\":\"%s\",\"dateDeclaration\":%d,\"seuilDepasse\":%.2f}",
                        s.getSinistreId(), s.getContratId(), s.getMontantSinistre(),
                        s.getTypeSinistre(), s.getDateDeclaration(), SEUIL_CRITIQUE
                    );
                } catch (Exception e) {
                    log.error("Erreur formatage sinistre critique: {}", e.getMessage());
                    return null;
                }
            })
            .filter((key, value) -> value != null)
            .to("sinistres-critiques", Produced.with(Serdes.String(), Serdes.String()));

        log.info("Branche 1 (sinistres critiques) initialisée");

        // Branche 2 : Agrégation glissante 5min par contratId
        rawSinistres
            .filter((key, jsonValue) -> {
                try {
                    Sinistre s = sinistreSerde.deserializer().deserialize("raw-sinistres", jsonValue.getBytes());
                    return s != null && s.getContratId() != null && !s.getContratId().isEmpty();
                } catch (Exception e) {
                    log.error("Erreur validation contratId: {}", e.getMessage());
                    return false;
                }
            })
            .groupBy((key, jsonValue) -> {
                Sinistre s = sinistreSerde.deserializer().deserialize("raw-sinistres", jsonValue.getBytes());
                return new KeyValue<>(s.getContratId(), s);
            })
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
            .aggregate(
                () -> 0.0,
                (contratId, sinistre, aggregate) -> aggregate + sinistre.getMontantSinistre(),
                Materialized.as("stats-contrat-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(Serdes.Double())
            )
            .toStream()
            .map((windowedKey, total) -> {
                String contratId = windowedKey.key();
                String jsonResult = String.format(
                    "{\"contratId\":\"%s\",\"totalMontant\":%.2f,\"windowStart\":%d,\"windowEnd\":%d}",
                    contratId, total, windowedKey.window().start(), windowedKey.window().end()
                );
                return new KeyValue<>(contratId, jsonResult);
            })
            .to("stats-contrat-5m", Produced.with(Serdes.String(), Serdes.String()));

        log.info("Branche 2 (agrégation 5min) initialisée");

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Arrêt du KStream...");
            streams.close();
            log.info("KStream arrêté proprement");
        }));
    }
}
```

### 6.3 Modèle Sinistre.java

```java
package com.caa.dammages.streaming.model;

public class Sinistre {
    private String sinistreId;
    private String contratId;
    private double montantSinistre;
    private String typeSinistre;
    private long dateDeclaration;

    public Sinistre() {}

    public String getSinistreId() { return sinistreId; }
    public void setSinistreId(String sinistreId) { this.sinistreId = sinistreId; }

    public String getContratId() { return contratId; }
    public void setContratId(String contratId) { this.contratId = contratId; }

    public double getMontantSinistre() { return montantSinistre; }
    public void setMontantSinistre(double montantSinistre) { this.montantSinistre = montantSinistre; }

    public String getTypeSinistre() { return typeSinistre; }
    public void setTypeSinistre(String typeSinistre) { this.typeSinistre = typeSinistre; }

    public long getDateDeclaration() { return dateDeclaration; }
    public void setDateDeclaration(long dateDeclaration) { this.dateDeclaration = dateDeclaration; }
}
```

### 6.4 Serde SinistreSerde.java

```java
package com.caa.dammages.streaming.serde;

import com.caa.dammages.streaming.model.Sinistre;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class SinistreSerde implements Serde<Sinistre> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Serializer<Sinistre> serializer() {
        return (topic, data) -> {
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Erreur sérialisation sinistre", e);
            }
        };
    }

    @Override
    public Deserializer<Sinistre> deserializer() {
        return (topic, bytes) -> {
            if (bytes == null || bytes.length == 0) return null;
            try {
                return mapper.readValue(bytes, Sinistre.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur désérialisation sinistre", e);
            }
        };
    }
}
```

---

## 7. API Exposition Spring Boot

### 7.1 Structure

```
api-service/
├── pom.xml
├── src/main/java/com/caa/dammages/api/
│   ├── ApiApplication.java
│   ├── controller/
│   │   └── StatsContratController.java
│   ├── model/
│   │   ├── StatsContrat.java
│   │   └── SinistreCritique.java
│   └── repository/
│       └── StatsContratRepository.java
└── src/main/resources/
    └── application.yml
```

### 7.2 Controller — StatsContratController.java

```java
package com.caa.dammages.api.controller;

import com.caa.dammages.api.model.StatsContrat;
import com.caa.dammages.api.repository.StatsContratRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsContratController {

    private final StatsContratRepository repository;

    public StatsContratRepository(StatsContratRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/contrat/{contratId}")
    public StatsContrat getStatsByContrat(@PathVariable String contratId) {
        return repository.findByContratId(contratId);
    }

    @GetMapping("/critiques")
    public List<SinistreCritique> getSinistresCritiques() {
        return repository.findSinistresCritiques();
    }
}
```

### 7.3 Configuration application.yml

```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: dommages_db

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## 8. MongoDB Sink Connector

### 8.1 Configuration du connector

```json
{
  "name": "mongo-sink-dammages",
  "config": {
    "connector.class": "com.mongodb.kafka.connect.MongoSinkConnector",
    "tasks.max": "3",
    "topics": "sinistres-critiques,stats-contrat-5m",
    "connection.uri": "mongodb://localhost:27017",
    "database": "dommages_db",
    "topic.override.sinistres-critiques.collection": "sinistres_critiques",
    "topic.override.stats-contrat-5m.collection": "stats_contrat",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false"
  }
}
```

### 8.2 Schémas des Collections MongoDB

**Collection sinistres_critiques :**

```json
{
  "_id": ObjectId("65f1a2b3c4e5f67890123456"),
  "sinistreId": "sin-2024-001",
  "contratId": "CTR-78945",
  "montantSinistre": 12500.00,
  "typeSinistre": "COLLISION",
  "dateDeclaration": 1773220000000,
  "seuilDepasse": 10000.00
}
```

**Collection stats_contrat :**

```json
{
  "_id": ObjectId("65f1a2b3c4e5f67890123457"),
  "contratId": "CTR-78945",
  "totalMontant": 24350.00,
  "nbSinistres": 3,
  "windowStart": 1773219700000,
  "windowEnd": 1773220000000
}
```

---

## 9. Exigences Non-Fonctionnelles

| Critère | Exigence |
|---------|----------|
| **Latence** | < 300 ms entre publication Kafka et écriture MongoDB |
| **Résilience** | Reconstruction du state store via les changelog topics Kafka |
| **Scalabilité** | Augmentation du nombre de partitions et d'instances sans perte ni duplication |
| **Sécurité** | Authentification Kafka (SASL/SCRAM), TLS, auth MongoDB, chiffrement des données sensibles |
| **Monitoring** | Métriques Micrometer exposées via Spring Actuator (latence, throughput, erreurs) |
| **Logging** | Logs structurés (JSON) pour chaque étape du pipeline |

---

## 10. Tests Unitaires

### 10.1 Stratégie

| Composant | Outil | Couverture cible |
|-----------|-------|-----------------|
| KStream (Java) | JUnit 5 + TopologyTestDriver | 80% |
| KSQLDB | ksqlDB Integration Tests | Requêtes validées |
| API Spring Boot | JUnit 5 + MockMvc | 80% |
| Connector MongoDB | Tests d'intégration | Flux E2E |

### 10.2 Exemple — test KStream

```java
@Test
void shouldFilterSinistreCritique() {
    Sinistre s = new Sinistre();
    s.setSinistreId("sin-001");
    s.setContratId("CTR-001");
    s.setMontantSinistre(15000.0);

    // Avec TopologyTestDriver, vérifier que l'événement arrive sur sinistres-critiques
}
```

---

## 11. Déploiement & CI/CD

### 11.1 Pipeline GitLab CI/CD

```yaml
stages:
  - build
  - test
  - package
  - deploy

build:
  stage: build
  image: maven:3.9-eclipse-temurin-8
  script:
    - mvn clean compile -DskipTests

test:
  stage: test
  image: maven:3.9-eclipse-temurin-8
  script:
    - mvn test

package:
  stage: package
  image: maven:3.9-eclipse-temurin-8
  script:
    - mvn package -DskipTests
  artifacts:
    paths:
      - target/*.jar

deploy:
  stage: deploy
  image: alpine/helm:latest
  script:
    - helm upgrade --install kstream-service ./helm/kstream-service
  only:
    - main
```

### 11.2 Docker Compose (développement local)

```yaml
version: '3.8'
services:
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_NUM_PARTITIONS: 6

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  ksqldb:
    image: confluentinc/ksqldb-server:0.29.0
    environment:
      KSQL_BOOTSTRAP_SERVERS: kafka:9092
      KSQL_LISTENERS: http://0.0.0.0:8088

  mongodb:
    image: mongo:6.0
    ports:
      - "27017:27017"

  kstream:
    build: ./kstream-service
    depends_on:
      - kafka
      - ksqldb

  api:
    build: ./api-service
    ports:
      - "8080:8080"
    depends_on:
      - mongodb
```

---

## 12. Livrables Attendus

| Livrable | Format | Description |
|----------|--------|-------------|
| Code KSQLDB | Scripts `.sql` | Création streams, tables, agrégations |
| Code KStream | Java 8+ (Maven) | Pipeline d'agrégation glissante |
| API Spring Boot | Java 8+ (Maven) | Exposition REST des statistiques |
| Configuration connector | JSON | MongoSinkConnector config |
| Tests unitaires | JUnit 5 | Couverture ≥ 80% |
| Spécifications fonctionnelles | Markdown | Documentation métier |
| Spécifications techniques | Markdown | Architecture, schémas, flux |
| Modes opératoires | Markdown | Procédures de déploiement et maintenance |
| Pipeline CI/CD | `.gitlab-ci.yml` | Build, test, deploy |
| Docker Compose | `docker-compose.yml` | Environnement de développement local |
| Helm Charts | `helm/kstream-service/` | Déploiement Kubernetes |

---

## 13. Glossaire

| Terme | Définition |
|-------|------------|
| **Sinistre** | Événement déclaré par un assuré (accident, dommage, vol, etc.) |
| **Contrat** | Contrat d'assurance liant un assuré à CAA |
| **Montant sinistre** | Valeur financière du dommage déclaré |
| **Fenêtre glissante** | Période de temps pour le calcul d'agrégations (ici 5 min) |
| **State store** | Stockage local de KStream pour les agrégations |
| **Changelog topic** | Topic Kafka utilisé pour reconstruire le state store |
