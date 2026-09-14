# Guide Utilisateur - Data Dammages

## Accéder à l'application

### Dashboard React

Ouvrez votre navigateur et allez sur :

```
http://localhost:3000
```

Le dashboard se charge automatiquement et se rafraîchit toutes les 30 secondes.

---

## Tableau de Bord

### Les 4 cartes de synthèse

En haut du dashboard, 4 cartes affichent les métriques clés :

| Carte | Description | Exemple |
|-------|-------------|---------|
| **Sinistres Critiques** | Nombre total de sinistres avec montant > 10 000 EUR | 301 |
| **Montant Total** | Somme de tous les montants critiques | 12 450 000 EUR |
| **Montant Moyen** | Montant moyen par sinistre | 41 362 EUR |
| **Contrats Affectés** | Nombre de contrats uniques touchés | 3 |

### Graphiques

#### Barres : Montant par contrat
Montre le total des sinistres critiques pour chaque contrat (CTR-100, CTR-200, CTR-300).

#### Camembert : Répartition
Affiche la répartition en pourcentage des montants par contrat.

### Tableau des sinistres critiques

Le tableau liste tous les sinistres critiques avec :

| Colonne | Description |
|---------|-------------|
| **ID** | Identifiant du sinistre (ex: SIN-721) |
| **Contrat** | Identifiant du contrat (ex: CTR-100) |
| **Montant** | Montant en EUR (en rouge si > 10 000) |
| **Type** | Type de sinistre (AUTO, HABITATION, SANTE, INCENDIE) |
| **Date** | Date de déclaration (format français) |

### Exporter les données

Cliquez sur le bouton **Exporter** en haut à droite :

| Format | Usage |
|--------|-------|
| **CSV** | Ouvrir dans Excel, Google Sheets |
| **JSON** | Intégration technique, API |
| **PDF** | Rapport imprimable (via HTML) |

---

## Types de Sinistres

| Type | Description | Montant typique |
|------|-------------|-----------------|
| **AUTO** | Accident automobile | 12 000 - 95 000 EUR |
| **HABITATION** | Dégât des eaux, incendie | 18 000 - 32 000 EUR |
| **SANTE** | Frais médicaux | 15 000 EUR |
| **INCENDIE** | Incendie habitation | 28 000 EUR |

---

## API REST (pour développeurs)

### Base URL

```
http://localhost:8080/api/v1
```

### Requêtes courantes

#### Dernière agrégation pour un contrat

```bash
curl http://localhost:8080/api/v1/stats/contrat/CTR-100
```

Réponse :
```json
{
  "id": "6aa8456420b6697cb9ccb1cd",
  "contratId": "CTR-100",
  "totalMontant": 3500.0,
  "nbSinistres": 1,
  "windowStart": 1789412700000,
  "windowEnd": 1789413000000
}
```

#### Historique des fenêtres glissantes

```bash
curl http://localhost:8080/api/v1/stats/contrat/CTR-100/historique
```

#### Tous les sinistres critiques

```bash
curl http://localhost:8080/api/v1/stats/critiques/all
```

#### Sinistres critiques paginés

```bash
curl "http://localhost:8080/api/v1/stats/critiques?page=0&size=10"
```

#### Filtrer par seuil

```bash
# Sinistres au-dessus de 50 000 EUR
curl http://localhost:8080/api/v1/stats/critiques/seuil/50000
```

#### Filtrer par contrat

```bash
curl http://localhost:8080/api/v1/stats/critiques/contrat/CTR-300
```

### Codes de réponse

| Code | Signification |
|------|---------------|
| 200 | Succès |
| 404 | Ressource non trouvée |
| 500 | Erreur serveur |

### Rate Limiting

L'API limite à **100 requêtes minute par IP**. Si vous atteignez la limite, vous recevrez un code 429.

---

## MongoDB Express (Admin)

### Accès

```
http://localhost:8082
Identifiants : admin / password
```

### Collections

| Collection | Contenu |
|------------|---------|
| `sinistres_critiques` | Tous les sinistres avec montant > 10 000 EUR |
| `stats_contrat` | Agrégations par contrat et fenêtre glissante |

### Requêtes utiles dans Mongo Express

#### Compter les sinistres critiques

```javascript
db.sinistres_critiques.countDocuments()
```

#### Trouver les sinistres d'un contrat

```javascript
db.sinistres_critiques.find({ "contratId": "CTR-100" }).limit(10)
```

#### Dernière agrégation d'un contrat

```javascript
db.stats_contrat.find({ "contratId": "CTR-100" })
  .sort({ "windowEnd": -1 })
  .limit(1)
```

#### Agrégation par type de sinistre

```javascript
db.sinistres_critiques.aggregate([
  { $group: {
    _id: "$typeSinistre",
    count: { $sum: 1 },
    totalMontant: { $sum: "$montantSinistre" }
  }},
  { $sort: { totalMontant: -1 } }
])
```

---

## Grafana (Monitoring)

### Accès

```
http://localhost:3001
Identifiants : admin / admin
```

### Dashboard API Service

Le dashboard pré-chargé contient 8 panneaux :

| Panneau | Description |
|---------|-------------|
| API Requests | Taux de requêtes par seconde |
| Response Time | Latence p50 et p95 |
| JVM Memory | Utilisation heap mémoire |
| JVM Threads | Nombre de threads actifs |
| MongoDB Operations | Opérations MongoDB |
| HTTP Status Codes | Répartition 2xx, 4xx, 5xx |
| System CPU Usage | Utilisation CPU |
| GC Pause Time | Temps de pause garbage collector |

### Ajouter un panneau personnalisé

1. Cliquez sur **+** → **Create** → **Dashboard**
2. Cliquez sur **Add visualization**
3. Sélectionnez la datasource **Prometheus**
4. Entrez une requête PromQL, par exemple :

```promql
# Taux de requêtes HTTP
rate(http_server_requests_seconds_count[5m])

# Latence p99
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))
```

---

## KSQLDB (Analyse Avancée)

### Accès

```bash
# CLI
docker compose exec ksqldb ksql http://localhost:8088

# Ou via curl
curl -X POST http://localhost:8088/query \
  -H "Content-Type: application/vnd.ksql.v1+json" \
  -d '{"ksql": "SHOW STREAMS;"}'
```

### Requêtes utiles

#### Voir les streams

```sql
SHOW STREAMS;
```

#### Voir les tables

```sql
SHOW TABLES;
```

#### Compter les sinistres par type

```sql
SELECT typeSinistre, COUNT(*)
FROM stream_sinistres
GROUP BY typeSinistre
EMIT CHANGES;
```

---

## Kafka Connect (Voir les connectors)

### Lister les connectors

```bash
curl http://localhost:8083/connectors
```

### Statut d'un connector

```bash
curl http://localhost:8083/connectors/mongo-sinistres-critiques/status
curl http://localhost:8083/connectors/mongo-stats-contrat/status
```

---

## Dépannage

### Le dashboard affiche "Erreur de connexion"

1. Vérifiez que api-service est démarré : `docker compose ps`
2. Testez l'API : `curl http://localhost:8080/actuator/health`
3. Redémarrez : `docker compose restart api-service`

### Les données ne se mettent pas à jour

1. Vérifiez kafka-producer : `docker compose logs kafka-producer`
2. Vérifiez kstream-service : `docker compose logs kstream-service`
3. Vérifiez MongoDB : `docker compose exec mongodb mongosh -u admin -p password`

### Le tableau est vide

1. Les données mock mettent ~30 secondes à apparaître
2. Vérifiez que les topics existent : `docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list`
3. Vérifiez les connectors : `curl http://localhost:8083/connectors`
