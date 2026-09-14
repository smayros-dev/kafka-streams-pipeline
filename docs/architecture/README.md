# Architecture

## Vue d'ensemble

L'architecture Data Dammages est basée sur un pipeline de streaming event-driven utilisant Kafka comme bus d'événements central.

## Diagramme d'architecture

Voir : [`diagrams/plantuml/architecture-system.puml`](../diagrams/plantuml/architecture-system.puml)

```
@startuml architecture-system
package "Ingestion" {
  [Applications Métier] --> [Kafka Topic: raw-sinistres]
}

package "Traitement" {
  [Kafka Topic: raw-sinistres] --> [KStream: Filtrage Critique]
  [Kafka Topic: raw-sinistres] --> [KTable: Agrégation 5min]
  [Kafka Topic: raw-sinistres] --> [KGlobalTable: Join Contrats]
  
  [KStream: Filtrage Critique] --> [Kafka Topic: sinistres-critiques]
  [KTable: Agrégation 5min] --> [Kafka Topic: stats-contrat-5m]
  [KGlobalTable: Join Contrats] --> [Kafka Topic: sinistres-enrichis]
}

package "Persistance" {
  [Kafka Topic: sinistres-critiques] --> [MongoDB Sink Connector]
  [Kafka Topic: stats-contrat-5m] --> [MongoDB Sink Connector]
  [Kafka Topic: sinistres-enrichis] --> [MongoDB Sink Connector]
  [MongoDB Sink Connector] --> [(MongoDB: dommages_db)]
}

package "Exposition" {
  [(MongoDB: dommages_db)] --> [Spring Boot API]
  [Spring Boot API] --> [Frontend Dashboard]
}

package "Monitoring" {
  [Spring Boot API] --> [Prometheus]
  [Prometheus] --> [Grafana]
}
@enduml
```

## Composants

### 1. KStream - Filtrage Temps Réel

**Usage :** Filtrage event-by-event des sinistres critiques (montant > 10 000 EUR)

Voir : [`diagrams/plantuml/data-flow.puml`](../diagrams/plantuml/data-flow.puml)

**Caractéristiques :**
- Stateless (pas d'état)
- Faible latence
- Traitement par événement

### 2. KTable - Agrégation Materialisée

**Usage :** Agrégation glissante des montants par contrat sur fenêtre de 5 minutes

Voir : [`diagrams/plantuml/topology-diagram.puml`](../diagrams/plantuml/topology-diagram.puml)

**Caractéristiques :**
- Stateful (état materialisé)
- Changelog topic pour récupération
- Requétisable (state store)

### 3. KGlobalTable - Join Données Référence

**Usage :** Enrichissement des sinistres avec les informations contrat

Voir : [`diagrams/plantuml/topology-diagram.puml`](../diagrams/plantuml/topology-diagram.puml)

**Caractéristiques :**
- Répliqué sur toutes les partitions
- Pas de repartitionnement
- Idéal pour petites tables de référence

## Schéma des Topics

| Topic | Partitions | Description |
|-------|------------|-------------|
| raw-sinistres | 6 | Données brutes des sinistres |
| sinistres-critiques | 6 | Sinistres avec montant > 10k |
| stats-contrat-5m | 6 | Agrégation par contrat (5 min) |
| contrats-ref | 6 | Table de référence contrats |
| sinistres-enrichis | 6 | Sinistres enrichis avec contrat |

## Flux de Données

Voir : [`diagrams/plantuml/sequence-diagram.puml`](../diagrams/plantuml/sequence-diagram.puml)

1. Application métier publie sinistre dans `raw-sinistres`
2. KStream filtre et publie dans `sinistres-critiques` si montant > 10k
3. KTable agrège par contrat et publie dans `stats-contrat-5m`
4. KGlobalTable join avec `contrats-ref` et publie dans `sinistres-enrichis`
5. MongoDB Sink Connector persiste les résultats
6. Spring Boot API expose les données
7. Frontend Dashboard affiche les métriques

## Patterns de Conception

### 1. Event Sourcing
Chaque modification d'état est capturée comme un événement immuable dans Kafka.

### 2. CQRS (Command Query Responsibility Segregation)
- **Write side :** KStream/KTable écrivent dans Kafka
- **Read side :** MongoDB expose les données pour les requêtes

### 3. Materialized View
La KTable matérialise automatiquement une vue agrégée des données.

## Tolérance aux Pannes

| Scénario | Mécanisme |
|----------|-----------|
| Arrêt application | State store reconstruit via changelog topic |
| Perte partition | Rebalance automatique |
| MongoDB indisponible | Kafka buffer les messages |
| Network partition | At-least-once delivery |
