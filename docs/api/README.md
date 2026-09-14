# API Reference

## Base URL

```
http://localhost:8080/api/v1
```

## Endpoints

### 1. GET /stats/contrat/{contratId}

Récupère les statistiques agrégées pour un contrat spécifique.

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| contratId | string | Identifiant du contrat |

**Réponse :**
```json
{
  "id": "65f1a2b3c4e5f67890123456",
  "contratId": "CTR-001",
  "totalMontant": 24350.00,
  "nbSinistres": 3,
  "windowStart": 1773219700000,
  "windowEnd": 1773220000000
}
```

**Codes de réponse :**
| Code | Description |
|------|-------------|
| 200 | Succès |
| 404 | Contrat non trouvé |

---

### 2. GET /stats/contrat/{contratId}/historique

Récupère l'historique des statistiques pour un contrat.

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| contratId | string | Identifiant du contrat |

**Réponse :**
```json
[
  {
    "id": "65f1a2b3c4e5f67890123456",
    "contratId": "CTR-001",
    "totalMontant": 24350.00,
    "nbSinistres": 3,
    "windowStart": 1773219700000,
    "windowEnd": 1773220000000
  }
]
```

---

### 3. GET /stats/critiques

Récupère tous les sinistres critiques (montant > 10 000 EUR).

**Réponse :**
```json
[
  {
    "id": "65f1a2b3c4e5f67890123456",
    "sinistreId": "sin-001",
    "contratId": "CTR-001",
    "montantSinistre": 15000.00,
    "typeSinistre": "COLLISION",
    "dateDeclaration": 1773220000000,
    "seuilDepasse": 10000.00
  }
]
```

---

### 4. GET /stats/critiques/contrat/{contratId}

Récupère les sinistres critiques pour un contrat spécifique.

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| contratId | string | Identifiant du contrat |

**Réponse :** Même format que GET /stats/critiques

---

### 5. GET /stats/critiques/seuil/{seuil}

Récupère les sinistres avec un montant supérieur au seuil.

**Paramètres :**
| Paramètre | Type | Description |
|-----------|------|-------------|
| seuil | double | Seuil minimum en EUR |

**Réponse :** Même format que GET /stats/critiques

---

## Modèles de Données

### SinistreCritique

```typescript
{
  id: string;                    // ID MongoDB
  sinistreId: string;            // Identifiant unique du sinistre
  contratId: string;             // Identifiant du contrat
  montantSinistre: number;       // Montant en EUR
  typeSinistre: string;          // Type (COLLISION, INCENDIE, VOL, etc.)
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
  nbSinistres: number;           // Nombre de sinistres
  windowStart: number;           // Début de fenêtre (epoch ms)
  windowEnd: number;             // Fin de fenêtre (epoch ms)
}
```

## Erreurs

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Contrat non trouvé: CTR-UNKNOWN",
  "path": "/api/v1/stats/contrat/CTR-UNKNOWN"
}
```
