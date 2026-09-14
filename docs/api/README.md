# API Reference

## Base URL

```
http://localhost:8080/api/v1
```

## Rate Limiting

L'API utilise Bucket4j pour le rate limiting : **100 requêtes minute par IP**.

## Endpoints

### 1. GET /stats/contrat/{contratId}

Récupère la dernière agrégation pour un contrat spécifique (fenêtre la plus récente).

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| contratId | string | Identifiant du contrat |

**Réponse 200 :**
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

**Codes de réponse :**
| Code | Description |
|------|-------------|
| 200 | Succès |
| 404 | Contrat non trouvé |

---

### 2. GET /stats/contrat/{contratId}/historique

Récupère l'historique des agrégations (toutes les fenêtres glissantes) pour un contrat.

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| contratId | string | Identifiant du contrat |

**Réponse 200 :**
```json
[
  {
    "id": "6aa8456420b6697cb9ccb1cd",
    "contratId": "CTR-100",
    "totalMontant": 3500.0,
    "nbSinistres": 1,
    "windowStart": 1789412700000,
    "windowEnd": 1789413000000
  },
  {
    "id": "6aa83d2a20b6697cb9ccb1a0",
    "contratId": "CTR-100",
    "totalMontant": 12000.0,
    "nbSinistres": 1,
    "windowStart": 1789412400000,
    "windowEnd": 1789412700000
  }
]
```

---

### 3. GET /stats/critiques

Récupère les sinistres critiques (montant > 10 000 EUR) avec pagination.

**Paramètres :**
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| page | int | 0 | Numéro de page |
| size | int | 20 | Taille de la page |

**Réponse 200 (Page) :**
```json
{
  "content": [
    {
      "id": "65f1a2b3c4e5f67890123456",
      "sinistreId": "sin-001",
      "contratId": "CTR-100",
      "montantSinistre": 15000.00,
      "typeSinistre": "AUTO",
      "dateDeclaration": 1773220000000,
      "seuilDepasse": 10000.00
    }
  ],
  "totalElements": 301,
  "totalPages": 16,
  "number": 0,
  "size": 20
}
```

---

### 4. GET /stats/critiques/all

Récupère tous les sinistres critiques (sans pagination).

**Réponse 200 :**
```json
[
  {
    "id": "65f1a2b3c4e5f67890123456",
    "sinistreId": "sin-001",
    "contratId": "CTR-100",
    "montantSinistre": 15000.00,
    "typeSinistre": "AUTO",
    "dateDeclaration": 1773220000000,
    "seuilDepasse": 10000.00
  }
]
```

---

### 5. GET /stats/critiques/contrat/{contratId}

Récupère les sinistres critiques pour un contrat spécifique.

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| contratId | string | Identifiant du contrat |

**Réponse 200 :** Même format que GET /stats/critiques/all

---

### 6. GET /stats/critiques/seuil/{seuil}

Récupère les sinistres avec un montant supérieur au seuil.

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| seuil | double | Seuil minimum en EUR |

**Réponse 200 :** Même format que GET /stats/critiques/all

---

### 7. GET /actuator/health

Health check Spring Boot.

**Réponse 200 :**
```json
{
  "status": "UP",
  "components": {
    "mongodb": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

### 8. GET /actuator/prometheus

Métriques Prometheus pour Grafana.

---

## Modèles de Données

### SinistreCritique

```typescript
{
  id: string;                    // ID MongoDB
  sinistreId: string;            // Identifiant unique du sinistre
  contratId: string;             // Identifiant du contrat
  montantSinistre: number;       // Montant en EUR
  typeSinistre: string;          // Type (AUTO, HABITATION, SANTE, COLLISION, VOL, INCENDIE)
  dateDeclaration: number;       // Timestamp epoch ms
  seuilDepasse: number;          // Seuil dépassé (10 000 EUR)
}
```

### StatsContrat

```typescript
{
  id: string;                    // ID MongoDB
  contratId: string;             // Identifiant du contrat
  totalMontant: number;          // Total des montants sur la fenêtre
  nbSinistres: number;           // Nombre de sinistres dans la fenêtre
  windowStart: number;           // Début de fenêtre (epoch ms)
  windowEnd: number;             // Fin de fenêtre (epoch ms)
}
```

### Types de sinistres supportés

| Type | Description | Montant typique |
|------|-------------|-----------------|
| AUTO | Accident automobile | 12 000 - 95 000 EUR |
| HABITATION | Dégât des eaux, vol | 18 000 - 32 000 EUR |
| SANTE | Frais médicaux | 15 000 EUR |
| COLLISION | Collision vehicle | 12 000 EUR |
| VOL | Vol de véhicule | 95 000 EUR |
| INCENDIE | Incendie habitation | 28 000 EUR |

## Erreurs

```json
{
  "timestamp": "2026-09-14T19:05:02.314+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/v1/stats/contrat/CTR-UNKNOWN"
}
```

## Exemples cURL

```bash
# Dernière agrégation pour CTR-100
curl http://localhost:8080/api/v1/stats/contrat/CTR-100

# Historique des fenêtres pour CTR-200
curl http://localhost:8080/api/v1/stats/contrat/CTR-200/historique

# Sinistres critiques paginés (page 1, taille 10)
curl "http://localhost:8080/api/v1/stats/critiques?page=1&size=10"

# Tous les sinistres critiques
curl http://localhost:8080/api/v1/stats/critiques/all

# Critiques pour CTR-300
curl http://localhost:8080/api/v1/stats/critiques/contrat/CTR-300

# Sinistres au-dessus de 50 000 EUR
curl http://localhost:8080/api/v1/stats/critiques/seuil/50000

# Health check
curl http://localhost:8080/actuator/health

# Métriques Prometheus
curl http://localhost:8080/actuator/prometheus
```
