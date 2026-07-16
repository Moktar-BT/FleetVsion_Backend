# Transami — Référence API (nouveaux modules)

**Base URL:** `http://localhost:8080/api`

**Authentification:** Bearer Token (JWT) dans le header `Authorization`

---

## Authentification

Toutes les routes nécessitent un token JWT obtenu via `POST /api/auth/login`.

Le token doit être inclus dans chaque requête :
```
Authorization: Bearer <votre_token_jwt>
```

---

## Module Chauffeur

### POST /api/chauffeurs

Crée un nouveau chauffeur.

**Request Body:**
```json
{
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "telephone": "+216 20 123 456",
  "dateEmbauche": "2024-01-15",
  "salaire": 1200.000
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "adminId": 1,
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "telephone": "+216 20 123 456",
  "dateEmbauche": "2024-01-15",
  "salaire": 1200.000,
  "active": true,
  "nomComplet": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant (nom, prenom, cin, salaire)
- `409 Conflict` : CIN déjà utilisé pour cet admin
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Le CIN doit être unique par admin
- Le salaire doit être positif
- Le champ `nomComplet` est calculé automatiquement (prenom + nom)

---

### GET /api/chauffeurs

Liste tous les chauffeurs de l'admin connecté.

**Query Parameters:**
- `active` (optionnel) : `true` ou `false` pour filtrer par statut

**Exemples d'URL:**
- `/api/chauffeurs` → tous les chauffeurs
- `/api/chauffeurs?active=true` → seulement les chauffeurs actifs
- `/api/chauffeurs?active=false` → seulement les chauffeurs inactifs

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "adminId": 1,
    "nom": "Ben Ali",
    "prenom": "Mohamed",
    "cin": "12345678",
    "telephone": "+216 20 123 456",
    "dateEmbauche": "2024-01-15",
    "salaire": 1200.000,
    "active": true,
    "nomComplet": "Mohamed Ben Ali"
  },
  {
    "id": 2,
    "adminId": 1,
    "nom": "Trabelsi",
    "prenom": "Fatma",
    "cin": "87654321",
    "telephone": "+216 98 765 432",
    "dateEmbauche": "2023-06-01",
    "salaire": 1500.000,
    "active": true,
    "nomComplet": "Fatma Trabelsi"
  }
]
```

**Erreurs possibles:**
- `401 Unauthorized` : Token manquant ou invalide

---

### GET /api/chauffeurs/{id}

Récupère les détails d'un chauffeur spécifique.

**Path Parameters:**
- `id` (requis) : ID du chauffeur

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "telephone": "+216 20 123 456",
  "dateEmbauche": "2024-01-15",
  "salaire": 1200.000,
  "active": true,
  "nomComplet": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `404 Not Found` : Chauffeur non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Le chauffeur doit appartenir à l'admin connecté

---

### PUT /api/chauffeurs/{id}

Modifie un chauffeur existant.

**Path Parameters:**
- `id` (requis) : ID du chauffeur

**Request Body:**
```json
{
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "telephone": "+216 20 999 888",
  "dateEmbauche": "2024-01-15",
  "salaire": 1350.000
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "telephone": "+216 20 999 888",
  "dateEmbauche": "2024-01-15",
  "salaire": 1350.000,
  "active": true,
  "nomComplet": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant
- `404 Not Found` : Chauffeur non trouvé ou accès refusé
- `409 Conflict` : CIN déjà utilisé par un autre chauffeur
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Tous les champs doivent être fournis (même si non modifiés)

---

### PATCH /api/chauffeurs/{id}/active

Active ou désactive un chauffeur.

**Path Parameters:**
- `id` (requis) : ID du chauffeur

**Query Parameters:**
- `active` (requis) : `true` pour activer, `false` pour désactiver

**Exemple d'URL:**
- `/api/chauffeurs/1/active?active=false`

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "telephone": "+216 20 123 456",
  "dateEmbauche": "2024-01-15",
  "salaire": 1200.000,
  "active": false,
  "nomComplet": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `404 Not Found` : Chauffeur non trouvé ou accès refusé
- `400 Bad Request` : Paramètre active manquant ou invalide
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Cette opération ne supprime pas le chauffeur, elle change seulement son statut
- Un chauffeur inactif peut être réactivé

---

### DELETE /api/chauffeurs/{id}

Supprime un chauffeur.

**Path Parameters:**
- `id` (requis) : ID du chauffeur

**Response 204 No Content:**
(Pas de corps de réponse)

**Erreurs possibles:**
- `404 Not Found` : Chauffeur non trouvé ou accès refusé
- `409 Conflict` : Le chauffeur est assigné à un camion actif
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Impossible de supprimer un chauffeur assigné à un camion actif
- Utiliser PATCH /active pour désactiver plutôt que supprimer

---

## Module Remorque

### POST /api/remorques

Crée une nouvelle remorque.

**Request Body:**
```json
{
  "matricule": "123TU5678",
  "camionId": 1,
  "typeRemorque": "Benne",
  "capaciteTonnes": 25.5,
  "dateAchat": "2023-03-15"
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "adminId": 1,
  "matricule": "123TU5678",
  "camionId": 1,
  "camionMatricule": "456TU9012",
  "typeRemorque": "Benne",
  "capaciteTonnes": 25.5,
  "dateAchat": "2023-03-15",
  "active": true
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant (matricule)
- `404 Not Found` : Camion non trouvé ou accès refusé (si camionId fourni)
- `409 Conflict` : Matricule déjà utilisé pour cet admin
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Le matricule doit être unique par admin
- Le champ `camionId` est optionnel (remorque peut ne pas être assignée)
- Si camionId fourni, le camion doit appartenir au même admin
- Le champ `camionMatricule` est résolu automatiquement si camionId est fourni

---

### GET /api/remorques

Liste toutes les remorques de l'admin connecté.

**Query Parameters:**
- `camionId` (optionnel) : Filtre par camion tracteur

**Exemples d'URL:**
- `/api/remorques` → toutes les remorques
- `/api/remorques?camionId=1` → remorques du camion 1

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "adminId": 1,
    "matricule": "123TU5678",
    "camionId": 1,
    "camionMatricule": "456TU9012",
    "typeRemorque": "Benne",
    "capaciteTonnes": 25.5,
    "dateAchat": "2023-03-15",
    "active": true
  },
  {
    "id": 2,
    "adminId": 1,
    "matricule": "789TU3456",
    "camionId": null,
    "camionMatricule": null,
    "typeRemorque": "Citerne",
    "capaciteTonnes": 30.0,
    "dateAchat": "2024-01-20",
    "active": true
  }
]
```

**Erreurs possibles:**
- `401 Unauthorized` : Token manquant ou invalide

---

### GET /api/remorques/{id}

Récupère les détails d'une remorque spécifique.

**Path Parameters:**
- `id` (requis) : ID de la remorque

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "matricule": "123TU5678",
  "camionId": 1,
  "camionMatricule": "456TU9012",
  "typeRemorque": "Benne",
  "capaciteTonnes": 25.5,
  "dateAchat": "2023-03-15",
  "active": true
}
```

**Erreurs possibles:**
- `404 Not Found` : Remorque non trouvée ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- La remorque doit appartenir à l'admin connecté

---

### PUT /api/remorques/{id}

Modifie une remorque existante.

**Path Parameters:**
- `id` (requis) : ID de la remorque

**Request Body:**
```json
{
  "matricule": "123TU5678",
  "camionId": 2,
  "typeRemorque": "Benne basculante",
  "capaciteTonnes": 28.0,
  "dateAchat": "2023-03-15"
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "matricule": "123TU5678",
  "camionId": 2,
  "camionMatricule": "789TU1234",
  "typeRemorque": "Benne basculante",
  "capaciteTonnes": 28.0,
  "dateAchat": "2023-03-15",
  "active": true
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant
- `404 Not Found` : Remorque ou camion non trouvé(e) ou accès refusé
- `409 Conflict` : Matricule déjà utilisé par une autre remorque
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Tous les champs doivent être fournis
- camionId peut être null pour détacher la remorque

---

### DELETE /api/remorques/{id}

Supprime une remorque.

**Path Parameters:**
- `id` (requis) : ID de la remorque

**Response 204 No Content:**
(Pas de corps de réponse)

**Erreurs possibles:**
- `404 Not Found` : Remorque non trouvée ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

---

## Module ChargeTemplate (Modèles de charges)

### POST /api/charges-templates

Crée un nouveau modèle de charge.

**Request Body:**
```json
{
  "libelle": "Salaire chauffeur Mohamed",
  "type": "FIXE",
  "categorie": "SALAIRE",
  "montantReference": 1200.000,
  "camionId": null,
  "chauffeurId": 1,
  "remorqueId": null
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "adminId": 1,
  "libelle": "Salaire chauffeur Mohamed",
  "type": "FIXE",
  "categorie": "SALAIRE",
  "montantReference": 1200.000,
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali",
  "remorqueId": null,
  "remorqueMatricule": null,
  "active": true
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant (libelle, type, categorie)
- `404 Not Found` : Camion, chauffeur ou remorque non trouvé(e) (si ID fourni)
- `409 Conflict` : Libellé déjà utilisé pour cet admin
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Le libellé doit être unique par admin
- Type : `FIXE` ou `VARIABLE`
- Catégories disponibles : `SALAIRE`, `CNSS`, `ASSURANCE`, `VIGNETTE`, `VISITE_TECHNIQUE`, `VIDANGE`, `LAVAGE`, `REPARATION`, `ASSURANCE_REMORQUE`, `VIGNETTE_REMORQUE`, `AUTRE`
- Les champs camionId, chauffeurId, remorqueId sont optionnels
- Les noms (camion, chauffeur, remorque) sont résolus automatiquement si les IDs sont fournis

---

### GET /api/charges-templates

Liste tous les modèles de charges de l'admin connecté.

**Query Parameters:**
- `type` (optionnel) : `FIXE` ou `VARIABLE`

**Exemples d'URL:**
- `/api/charges-templates` → tous les templates
- `/api/charges-templates?type=FIXE` → seulement les charges fixes
- `/api/charges-templates?type=VARIABLE` → seulement les charges variables

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "adminId": 1,
    "libelle": "Salaire chauffeur Mohamed",
    "type": "FIXE",
    "categorie": "SALAIRE",
    "montantReference": 1200.000,
    "camionId": null,
    "camionMatricule": null,
    "chauffeurId": 1,
    "chauffeurNom": "Mohamed Ben Ali",
    "remorqueId": null,
    "remorqueMatricule": null,
    "active": true
  },
  {
    "id": 2,
    "adminId": 1,
    "libelle": "Assurance camion 456TU9012",
    "type": "FIXE",
    "categorie": "ASSURANCE",
    "montantReference": 800.000,
    "camionId": 1,
    "camionMatricule": "456TU9012",
    "chauffeurId": null,
    "chauffeurNom": null,
    "remorqueId": null,
    "remorqueMatricule": null,
    "active": true
  }
]
```

**Erreurs possibles:**
- `401 Unauthorized` : Token manquant ou invalide

---

### GET /api/charges-templates/{id}

Récupère les détails d'un modèle de charge spécifique.

**Path Parameters:**
- `id` (requis) : ID du template

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "libelle": "Salaire chauffeur Mohamed",
  "type": "FIXE",
  "categorie": "SALAIRE",
  "montantReference": 1200.000,
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali",
  "remorqueId": null,
  "remorqueMatricule": null,
  "active": true
}
```

**Erreurs possibles:**
- `404 Not Found` : Template non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Le template doit appartenir à l'admin connecté

---

### PUT /api/charges-templates/{id}

Modifie un modèle de charge existant.

**Path Parameters:**
- `id` (requis) : ID du template

**Request Body:**
```json
{
  "libelle": "Salaire chauffeur Mohamed (augmenté)",
  "type": "FIXE",
  "categorie": "SALAIRE",
  "montantReference": 1350.000,
  "camionId": null,
  "chauffeurId": 1,
  "remorqueId": null
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "libelle": "Salaire chauffeur Mohamed (augmenté)",
  "type": "FIXE",
  "categorie": "SALAIRE",
  "montantReference": 1350.000,
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali",
  "remorqueId": null,
  "remorqueMatricule": null,
  "active": true
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant
- `404 Not Found` : Template non trouvé ou accès refusé
- `409 Conflict` : Libellé déjà utilisé par un autre template
- `401 Unauthorized` : Token manquant ou invalide

---

### PATCH /api/charges-templates/{id}/active

Active ou désactive un modèle de charge.

**Path Parameters:**
- `id` (requis) : ID du template

**Query Parameters:**
- `active` (requis) : `true` pour activer, `false` pour désactiver

**Exemple d'URL:**
- `/api/charges-templates/1/active?active=false`

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "libelle": "Salaire chauffeur Mohamed",
  "type": "FIXE",
  "categorie": "SALAIRE",
  "montantReference": 1200.000,
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali",
  "remorqueId": null,
  "remorqueMatricule": null,
  "active": false
}
```

**Erreurs possibles:**
- `404 Not Found` : Template non trouvé ou accès refusé
- `400 Bad Request` : Paramètre active manquant ou invalide
- `401 Unauthorized` : Token manquant ou invalide

---

### DELETE /api/charges-templates/{id}

Supprime un modèle de charge.

**Path Parameters:**
- `id` (requis) : ID du template

**Response 204 No Content:**
(Pas de corps de réponse)

**Erreurs possibles:**
- `404 Not Found` : Template non trouvé ou accès refusé
- `409 Conflict` : Des charges actives référencent ce template
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Impossible de supprimer un template si des charges l'utilisent
- Utiliser PATCH /active pour désactiver plutôt que supprimer

---

## Module Charge (Dépenses réelles)

### POST /api/charges

Enregistre une nouvelle dépense.

**Request Body:**
```json
{
  "templateId": 1,
  "date": "2026-01-15",
  "montant": 1200.000,
  "statut": "PAYEE",
  "notes": "Salaire janvier 2026"
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "adminId": 1,
  "date": "2026-01-15",
  "montant": 1200.000,
  "statut": "PAYEE",
  "notes": "Salaire janvier 2026",
  "templateId": 1,
  "templateLibelle": "Salaire chauffeur Mohamed",
  "templateType": "FIXE",
  "templateCategorie": "SALAIRE",
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant (templateId, date, montant)
- `404 Not Found` : Template non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Le montant doit être positif
- Statut par défaut : `EN_ATTENTE`
- Statuts disponibles : `EN_ATTENTE`, `PAYEE`
- Les informations du template (libellé, catégorie, camion, chauffeur) sont automatiquement résolues

---

### GET /api/charges

Liste toutes les charges de l'admin connecté.

**Query Parameters:**
- `templateId` (optionnel) : Filtre par template
- `dateFrom` (optionnel) : Date de début (format YYYY-MM-DD)
- `dateTo` (optionnel) : Date de fin (format YYYY-MM-DD)
- `statut` (optionnel) : `EN_ATTENTE` ou `PAYEE`

**Exemples d'URL:**
- `/api/charges` → toutes les charges
- `/api/charges?templateId=1` → charges du template 1
- `/api/charges?dateFrom=2026-01-01&dateTo=2026-12-31` → charges de l'année 2026
- `/api/charges?statut=EN_ATTENTE` → charges en attente
- `/api/charges?dateFrom=2026-06-01&dateTo=2026-06-30&statut=PAYEE` → charges payées en juin 2026

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "adminId": 1,
    "date": "2026-01-15",
    "montant": 1200.000,
    "statut": "PAYEE",
    "notes": "Salaire janvier 2026",
    "templateId": 1,
    "templateLibelle": "Salaire chauffeur Mohamed",
    "templateType": "FIXE",
    "templateCategorie": "SALAIRE",
    "camionId": null,
    "camionMatricule": null,
    "chauffeurId": 1,
    "chauffeurNom": "Mohamed Ben Ali"
  },
  {
    "id": 2,
    "adminId": 1,
    "date": "2026-02-10",
    "montant": 350.000,
    "statut": "EN_ATTENTE",
    "notes": "Réparation moteur",
    "templateId": 3,
    "templateLibelle": "Réparation camion 456TU9012",
    "templateType": "VARIABLE",
    "templateCategorie": "REPARATION",
    "camionId": 1,
    "camionMatricule": "456TU9012",
    "chauffeurId": null,
    "chauffeurNom": null
  }
]
```

**Erreurs possibles:**
- `401 Unauthorized` : Token manquant ou invalide
- `400 Bad Request` : Format de date invalide

---

### GET /api/charges/{id}

Récupère les détails d'une charge spécifique.

**Path Parameters:**
- `id` (requis) : ID de la charge

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "date": "2026-01-15",
  "montant": 1200.000,
  "statut": "PAYEE",
  "notes": "Salaire janvier 2026",
  "templateId": 1,
  "templateLibelle": "Salaire chauffeur Mohamed",
  "templateType": "FIXE",
  "templateCategorie": "SALAIRE",
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `404 Not Found` : Charge non trouvée ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

---

### PUT /api/charges/{id}

Modifie une charge existante.

**Path Parameters:**
- `id` (requis) : ID de la charge

**Request Body:**
```json
{
  "templateId": 1,
  "date": "2026-01-15",
  "montant": 1250.000,
  "statut": "PAYEE",
  "notes": "Salaire janvier 2026 avec prime"
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "date": "2026-01-15",
  "montant": 1250.000,
  "statut": "PAYEE",
  "notes": "Salaire janvier 2026 avec prime",
  "templateId": 1,
  "templateLibelle": "Salaire chauffeur Mohamed",
  "templateType": "FIXE",
  "templateCategorie": "SALAIRE",
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant
- `404 Not Found` : Charge ou template non trouvé(e) ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

---

### PATCH /api/charges/{id}/statut

Change le statut d'une charge.

**Path Parameters:**
- `id` (requis) : ID de la charge

**Query Parameters:**
- `statut` (requis) : `EN_ATTENTE` ou `PAYEE`

**Exemple d'URL:**
- `/api/charges/1/statut?statut=PAYEE`

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "date": "2026-01-15",
  "montant": 1200.000,
  "statut": "PAYEE",
  "notes": "Salaire janvier 2026",
  "templateId": 1,
  "templateLibelle": "Salaire chauffeur Mohamed",
  "templateType": "FIXE",
  "templateCategorie": "SALAIRE",
  "camionId": null,
  "camionMatricule": null,
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali"
}
```

**Erreurs possibles:**
- `404 Not Found` : Charge non trouvée ou accès refusé
- `400 Bad Request` : Paramètre statut manquant ou invalide
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Permet de marquer une charge comme payée ou en attente

---

### DELETE /api/charges/{id}

Supprime une charge.

**Path Parameters:**
- `id` (requis) : ID de la charge

**Response 204 No Content:**
(Pas de corps de réponse)

**Erreurs possibles:**
- `404 Not Found` : Charge non trouvée ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

---

### GET /api/charges/stats

Récupère les statistiques des charges.

**Query Parameters:**
- `year` (requis) : Année pour les statistiques (ex: 2026)

**Exemple d'URL:**
- `/api/charges/stats?year=2026`

**Response 200 OK:**
```json
{
  "totalAnnee": 15600.000,
  "totalMois": 2400.000,
  "totalParCategorie": {
    "SALAIRE": 7200.000,
    "ASSURANCE": 2400.000,
    "REPARATION": 3500.000,
    "VIDANGE": 1200.000,
    "VIGNETTE": 800.000,
    "CNSS": 500.000
  }
}
```

**Erreurs possibles:**
- `400 Bad Request` : Paramètre year manquant ou invalide
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- `totalAnnee` : somme de toutes les charges de l'année spécifiée
- `totalMois` : somme des charges du mois en cours
- `totalParCategorie` : répartition par catégorie pour l'année

---

## Module RappelCharge (Rappels de charges récurrentes)

### POST /api/rappels-charge

Crée un nouveau rappel de charge.

**Request Body:**
```json
{
  "templateId": 2,
  "frequence": "ANNUEL",
  "prochaineDate": "2027-01-15",
  "joursAvant": 15
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "adminId": 1,
  "frequence": "ANNUEL",
  "prochaineDate": "2027-01-15",
  "joursAvant": 15,
  "actif": true,
  "templateId": 2,
  "templateLibelle": "Assurance camion 456TU9012",
  "templateCategorie": "ASSURANCE",
  "templateType": "FIXE",
  "montantReference": 800.000,
  "camionMatricule": "456TU9012",
  "chauffeurNom": null,
  "joursRestants": 214,
  "statut": "OK"
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant (templateId, frequence, prochaineDate)
- `404 Not Found` : Template non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Fréquences disponibles : `MENSUEL`, `TRIMESTRIEL`, `SEMESTRIEL`, `ANNUEL`
- `joursAvant` : nombre de jours avant l'échéance pour déclencher l'alerte (défaut: 15)
- `joursRestants` : calculé automatiquement (différence entre prochaineDate et aujourd'hui)
- `statut` : calculé automatiquement
  - `DEPASSE` si joursRestants < 0
  - `PROCHE` si joursRestants <= joursAvant
  - `OK` sinon

---

### GET /api/rappels-charge

Liste tous les rappels de charges de l'admin connecté.

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "adminId": 1,
    "frequence": "ANNUEL",
    "prochaineDate": "2027-01-15",
    "joursAvant": 15,
    "actif": true,
    "templateId": 2,
    "templateLibelle": "Assurance camion 456TU9012",
    "templateCategorie": "ASSURANCE",
    "templateType": "FIXE",
    "montantReference": 800.000,
    "camionMatricule": "456TU9012",
    "chauffeurNom": null,
    "joursRestants": 214,
    "statut": "OK"
  },
  {
    "id": 2,
    "adminId": 1,
    "frequence": "MENSUEL",
    "prochaineDate": "2026-07-01",
    "joursAvant": 10,
    "actif": true,
    "templateId": 1,
    "templateLibelle": "Salaire chauffeur Mohamed",
    "templateCategorie": "SALAIRE",
    "templateType": "FIXE",
    "montantReference": 1200.000,
    "camionMatricule": null,
    "chauffeurNom": "Mohamed Ben Ali",
    "joursRestants": 16,
    "statut": "OK"
  }
]
```

**Erreurs possibles:**
- `401 Unauthorized` : Token manquant ou invalide

---

### GET /api/rappels-charge/{id}

Récupère les détails d'un rappel spécifique.

**Path Parameters:**
- `id` (requis) : ID du rappel

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "frequence": "ANNUEL",
  "prochaineDate": "2027-01-15",
  "joursAvant": 15,
  "actif": true,
  "templateId": 2,
  "templateLibelle": "Assurance camion 456TU9012",
  "templateCategorie": "ASSURANCE",
  "templateType": "FIXE",
  "montantReference": 800.000,
  "camionMatricule": "456TU9012",
  "chauffeurNom": null,
  "joursRestants": 214,
  "statut": "OK"
}
```

**Erreurs possibles:**
- `404 Not Found` : Rappel non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

---

### PUT /api/rappels-charge/{id}

Modifie un rappel existant.

**Path Parameters:**
- `id` (requis) : ID du rappel

**Request Body:**
```json
{
  "templateId": 2,
  "frequence": "ANNUEL",
  "prochaineDate": "2027-02-01",
  "joursAvant": 20
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "frequence": "ANNUEL",
  "prochaineDate": "2027-02-01",
  "joursAvant": 20,
  "actif": true,
  "templateId": 2,
  "templateLibelle": "Assurance camion 456TU9012",
  "templateCategorie": "ASSURANCE",
  "templateType": "FIXE",
  "montantReference": 800.000,
  "camionMatricule": "456TU9012",
  "chauffeurNom": null,
  "joursRestants": 231,
  "statut": "OK"
}
```

**Erreurs possibles:**
- `400 Bad Request` : Champ obligatoire manquant
- `404 Not Found` : Rappel ou template non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

---

### PATCH /api/rappels-charge/{id}/actif

Active ou désactive un rappel.

**Path Parameters:**
- `id` (requis) : ID du rappel

**Query Parameters:**
- `actif` (requis) : `true` pour activer, `false` pour désactiver

**Exemple d'URL:**
- `/api/rappels-charge/1/actif?actif=false`

**Response 200 OK:**
```json
{
  "id": 1,
  "adminId": 1,
  "frequence": "ANNUEL",
  "prochaineDate": "2027-01-15",
  "joursAvant": 15,
  "actif": false,
  "templateId": 2,
  "templateLibelle": "Assurance camion 456TU9012",
  "templateCategorie": "ASSURANCE",
  "templateType": "FIXE",
  "montantReference": 800.000,
  "camionMatricule": "456TU9012",
  "chauffeurNom": null,
  "joursRestants": 214,
  "statut": "OK"
}
```

**Erreurs possibles:**
- `404 Not Found` : Rappel non trouvé ou accès refusé
- `400 Bad Request` : Paramètre actif manquant ou invalide
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Un rappel inactif ne déclenchera pas d'alertes

---

### DELETE /api/rappels-charge/{id}

Supprime un rappel.

**Path Parameters:**
- `id` (requis) : ID du rappel

**Response 204 No Content:**
(Pas de corps de réponse)

**Erreurs possibles:**
- `404 Not Found` : Rappel non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

---

### GET /api/rappels-charge/alertes

Liste les rappels en alerte (proches ou dépassés).

**Response 200 OK:**
```json
[
  {
    "rappelId": 3,
    "templateLibelle": "Vignette camion 456TU9012",
    "templateCategorie": "VIGNETTE",
    "prochaineDate": "2026-06-20",
    "joursRestants": 5,
    "montantReference": 150.000,
    "camionMatricule": "456TU9012",
    "chauffeurNom": null,
    "statut": "PROCHE"
  },
  {
    "rappelId": 4,
    "templateLibelle": "Visite technique remorque 123TU5678",
    "templateCategorie": "VISITE_TECHNIQUE",
    "prochaineDate": "2026-06-10",
    "joursRestants": -5,
    "montantReference": 80.000,
    "camionMatricule": null,
    "chauffeurNom": null,
    "statut": "DEPASSE"
  }
]
```

**Erreurs possibles:**
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Retourne uniquement les rappels actifs dont :
  - `statut = "PROCHE"` : joursRestants <= joursAvant ET joursRestants >= 0
  - `statut = "DEPASSE"` : joursRestants < 0
- Utile pour afficher un dashboard d'alertes
- Les rappels avec `statut = "OK"` ne sont pas inclus

---

### PATCH /api/rappels-charge/{id}/avancer

Décale la prochaine date selon la fréquence du rappel.

**Path Parameters:**
- `id` (requis) : ID du rappel

**Response 200 OK:**
```json
{
  "id": 2,
  "adminId": 1,
  "frequence": "MENSUEL",
  "prochaineDate": "2026-08-01",
  "joursAvant": 10,
  "actif": true,
  "templateId": 1,
  "templateLibelle": "Salaire chauffeur Mohamed",
  "templateCategorie": "SALAIRE",
  "templateType": "FIXE",
  "montantReference": 1200.000,
  "camionMatricule": null,
  "chauffeurNom": "Mohamed Ben Ali",
  "joursRestants": 47,
  "statut": "OK"
}
```

**Erreurs possibles:**
- `404 Not Found` : Rappel non trouvé ou accès refusé
- `401 Unauthorized` : Token manquant ou invalide

**Notes:**
- Calcule automatiquement la nouvelle date selon la fréquence :
  - `MENSUEL` → +1 mois
  - `TRIMESTRIEL` → +3 mois
  - `SEMESTRIEL` → +6 mois
  - `ANNUEL` → +12 mois
- Utilisé après validation d'une charge PAYEE pour programmer la prochaine échéance
- Exemple workflow :
  1. Créer une charge avec statut PAYEE (salaire payé)
  2. Appeler PATCH /rappels-charge/{id}/avancer pour décaler au mois suivant

---

## Tableau récapitulatif des endpoints

| Méthode | Endpoint | Description | Auth requise |
|---------|----------|-------------|--------------|
| **Module Chauffeur** | | | |
| POST | `/api/chauffeurs` | Créer un chauffeur | ✓ |
| GET | `/api/chauffeurs` | Liste des chauffeurs (filtre: active) | ✓ |
| GET | `/api/chauffeurs/{id}` | Détail d'un chauffeur | ✓ |
| PUT | `/api/chauffeurs/{id}` | Modifier un chauffeur | ✓ |
| PATCH | `/api/chauffeurs/{id}/active` | Activer/désactiver un chauffeur | ✓ |
| DELETE | `/api/chauffeurs/{id}` | Supprimer un chauffeur | ✓ |
| **Module Remorque** | | | |
| POST | `/api/remorques` | Créer une remorque | ✓ |
| GET | `/api/remorques` | Liste des remorques (filtre: camionId) | ✓ |
| GET | `/api/remorques/{id}` | Détail d'une remorque | ✓ |
| PUT | `/api/remorques/{id}` | Modifier une remorque | ✓ |
| DELETE | `/api/remorques/{id}` | Supprimer une remorque | ✓ |
| **Module ChargeTemplate** | | | |
| POST | `/api/charges-templates` | Créer un modèle de charge | ✓ |
| GET | `/api/charges-templates` | Liste des templates (filtre: type) | ✓ |
| GET | `/api/charges-templates/{id}` | Détail d'un template | ✓ |
| PUT | `/api/charges-templates/{id}` | Modifier un template | ✓ |
| PATCH | `/api/charges-templates/{id}/active` | Activer/désactiver un template | ✓ |
| DELETE | `/api/charges-templates/{id}` | Supprimer un template | ✓ |
| **Module Charge** | | | |
| POST | `/api/charges` | Créer une charge | ✓ |
| GET | `/api/charges` | Liste des charges (filtres multiples) | ✓ |
| GET | `/api/charges/{id}` | Détail d'une charge | ✓ |
| PUT | `/api/charges/{id}` | Modifier une charge | ✓ |
| PATCH | `/api/charges/{id}/statut` | Changer le statut d'une charge | ✓ |
| DELETE | `/api/charges/{id}` | Supprimer une charge | ✓ |
| GET | `/api/charges/stats` | Statistiques des charges (param: year) | ✓ |
| **Module RappelCharge** | | | |
| POST | `/api/rappels-charge` | Créer un rappel | ✓ |
| GET | `/api/rappels-charge` | Liste des rappels | ✓ |
| GET | `/api/rappels-charge/{id}` | Détail d'un rappel | ✓ |
| PUT | `/api/rappels-charge/{id}` | Modifier un rappel | ✓ |
| PATCH | `/api/rappels-charge/{id}/actif` | Activer/désactiver un rappel | ✓ |
| DELETE | `/api/rappels-charge/{id}` | Supprimer un rappel | ✓ |
| GET | `/api/rappels-charge/alertes` | Liste des rappels en alerte | ✓ |
| PATCH | `/api/rappels-charge/{id}/avancer` | Décaler la prochaine date | ✓ |

---

## Enums utilisés

### TypeCharge
- `FIXE` : Charges fixes (ex: salaires, assurances)
- `VARIABLE` : Charges variables (ex: réparations, lavages)

### CategorieCharge
- `SALAIRE` : Salaires des chauffeurs
- `CNSS` : Cotisations sociales
- `ASSURANCE` : Assurances camions
- `VIGNETTE` : Vignettes
- `VISITE_TECHNIQUE` : Visites techniques
- `VIDANGE` : Vidanges
- `LAVAGE` : Lavages
- `REPARATION` : Réparations
- `ASSURANCE_REMORQUE` : Assurances remorques
- `VIGNETTE_REMORQUE` : Vignettes remorques
- `AUTRE` : Autres charges

### StatutCharge
- `EN_ATTENTE` : Charge en attente de paiement
- `PAYEE` : Charge payée

### FrequenceRappel
- `MENSUEL` : Rappel tous les mois
- `TRIMESTRIEL` : Rappel tous les 3 mois
- `SEMESTRIEL` : Rappel tous les 6 mois
- `ANNUEL` : Rappel tous les 12 mois

---

## Codes d'erreur HTTP courants

| Code | Description |
|------|-------------|
| 200 | OK - Requête réussie |
| 201 | Created - Ressource créée avec succès |
| 204 | No Content - Suppression réussie |
| 400 | Bad Request - Données invalides ou champs manquants |
| 401 | Unauthorized - Token JWT manquant ou invalide |
| 403 | Forbidden - Accès interdit |
| 404 | Not Found - Ressource non trouvée ou accès refusé |
| 409 | Conflict - Conflit (ex: doublon sur champ unique) |
| 500 | Internal Server Error - Erreur serveur |

---

## Format des dates

Toutes les dates utilisent le format ISO 8601 : `YYYY-MM-DD`

**Exemples:**
- `2026-01-15`
- `2026-06-15`
- `2027-12-31`

---

## Format des montants

Tous les montants sont en TND (Dinar Tunisien) avec 3 décimales.

**Exemples:**
- `1200.000` → 1200 TND
- `350.500` → 350,5 TND
- `15.750` → 15,75 TND

---

## Règles de sécurité et multi-tenant

- Chaque entité appartient à un admin spécifique via le champ `adminId`
- L'admin connecté est déterminé automatiquement via le token JWT
- Les requêtes filtrent automatiquement par `adminId` pour garantir l'isolation des données
- Un admin ne peut jamais accéder aux données d'un autre admin
- Les relations entre entités (ex: remorque → camion) doivent appartenir au même admin

---

## Exemples d'utilisation

### Workflow 1 : Gestion d'un chauffeur

```bash
# 1. Créer un nouveau chauffeur
POST /api/chauffeurs
{
  "nom": "Mansour",
  "prenom": "Ahmed",
  "cin": "11223344",
  "telephone": "+216 22 334 455",
  "dateEmbauche": "2026-01-10",
  "salaire": 1300.000
}

# 2. Lister tous les chauffeurs actifs
GET /api/chauffeurs?active=true

# 3. Modifier le salaire d'un chauffeur
PUT /api/chauffeurs/1
{
  "nom": "Mansour",
  "prenom": "Ahmed",
  "cin": "11223344",
  "telephone": "+216 22 334 455",
  "dateEmbauche": "2026-01-10",
  "salaire": 1450.000
}

# 4. Désactiver temporairement un chauffeur
PATCH /api/chauffeurs/1/active?active=false
```

---

### Workflow 2 : Gestion des charges récurrentes

```bash
# 1. Créer un template pour le salaire mensuel
POST /api/charges-templates
{
  "libelle": "Salaire Ahmed Mansour",
  "type": "FIXE",
  "categorie": "SALAIRE",
  "montantReference": 1300.000,
  "chauffeurId": 1
}

# 2. Créer un rappel mensuel pour ce salaire
POST /api/rappels-charge
{
  "templateId": 1,
  "frequence": "MENSUEL",
  "prochaineDate": "2026-07-01",
  "joursAvant": 5
}

# 3. Vérifier les alertes de paiement
GET /api/rappels-charge/alertes
```

# 4. Enregistrer le paiement effectif
POST /api/charges
{
  "templateId": 1,
  "date": "2026-07-01",
  "montant": 1300.000,
  "statut": "PAYEE",
  "notes": "Salaire juillet 2026"
}

# 5. Décaler le rappel au mois suivant
PATCH /api/rappels-charge/2/avancer
```

---

### Workflow 3 : Suivi des réparations

```bash
# 1. Créer un template pour les réparations d'un camion
POST /api/charges-templates
{
  "libelle": "Réparations camion 456TU9012",
  "type": "VARIABLE",
  "categorie": "REPARATION",
  "montantReference": 500.000,
  "camionId": 1
}

# 2. Enregistrer une réparation effectuée
POST /api/charges
{
  "templateId": 3,
  "date": "2026-06-10",
  "montant": 425.000,
  "statut": "PAYEE",
  "notes": "Changement plaquettes de frein"
}

# 3. Consulter l'historique des réparations
GET /api/charges?templateId=3&dateFrom=2026-01-01&dateTo=2026-12-31

# 4. Obtenir les statistiques annuelles
GET /api/charges/stats?year=2026
```

---

### Workflow 4 : Gestion d'une remorque

```bash
# 1. Créer une remorque
POST /api/remorques
{
  "matricule": "789TU4567",
  "camionId": 1,
  "typeRemorque": "Citerne",
  "capaciteTonnes": 35.0,
  "dateAchat": "2025-08-20"
}
```

# 2. Créer un template pour l'assurance de la remorque
POST /api/charges-templates
{
  "libelle": "Assurance remorque 789TU4567",
  "type": "FIXE",
  "categorie": "ASSURANCE_REMORQUE",
  "montantReference": 600.000,
  "remorqueId": 1
}

# 3. Créer un rappel annuel pour l'assurance
POST /api/rappels-charge
{
  "templateId": 4,
  "frequence": "ANNUEL",
  "prochaineDate": "2027-08-20",
  "joursAvant": 30
}

# 4. Détacher la remorque du camion
PUT /api/remorques/1
{
  "matricule": "789TU4567",
  "camionId": null,
  "typeRemorque": "Citerne",
  "capaciteTonnes": 35.0,
  "dateAchat": "2025-08-20"
}

# 5. Lister toutes les remorques non assignées
GET /api/remorques
```

---

## Notes importantes

### Contraintes d'unicité
- **Chauffeur** : CIN unique par admin
- **Remorque** : Matricule unique par admin
- **ChargeTemplate** : Libellé unique par admin

### Suppressions
- **Chauffeur** : Impossible si assigné à un camion actif
- **ChargeTemplate** : Impossible si des charges actives le référencent
- Privilégier la désactivation (PATCH /active ou /actif) à la suppression

### Relations
- Un chauffeur peut être assigné à plusieurs camions
- Une remorque ne peut être assignée qu'à un seul camion à la fois
- Un template de charge peut être lié à un camion, un chauffeur ou une remorque
- Une charge est toujours liée à un template

---

## Bonnes pratiques

### Gestion des erreurs
Toutes les erreurs retournent un message descriptif en français. Exemple :
```json
{
  "message": "Chauffeur non trouvé ou accès refusé",
  "timestamp": "2026-06-15T14:30:00",
  "status": 404
}
```

### Pagination
Les endpoints de liste ne sont actuellement pas paginés. Pour de grandes quantités de données, utilisez les filtres disponibles (dates, statuts, etc.).

### Performance
- Utilisez les filtres pour limiter les résultats (ex: `?active=true`, `?statut=EN_ATTENTE`)
- Pour les stats, précisez toujours l'année pour éviter de charger trop de données
- Les rappels d'alertes filtrent automatiquement uniquement les rappels pertinents

### Sécurité
- Ne jamais exposer le token JWT dans les logs ou URLs
- Le token expire après 24 heures (configurable)
- Toujours utiliser HTTPS en production

---

## Support et contact

Pour toute question sur l'API ou signaler un bug, contactez l'équipe de développement Transami.

**Version de l'API:** 1.0  
**Dernière mise à jour:** 15 juin 2026

---

## Changelog

### Version 1.0 (15 juin 2026)
- ✅ Module Chauffeur : CRUD complet + activation/désactivation
- ✅ Module Remorque : CRUD complet avec liaison au camion tracteur
- ✅ Module ChargeTemplate : Modèles de charges fixes et variables
- ✅ Module Charge : Enregistrement des dépenses réelles
- ✅ Module RappelCharge : Système de rappels automatiques avec alertes
- ✅ Statistiques : Agrégations par période et par catégorie
- ✅ Multi-tenant : Isolation complète des données par admin
- ✅ Validation : Contraintes d'unicité et règles métier

---

## Annexe : Structure de la base de données

### Tables créées

#### Table `chauffeurs`
```sql
CREATE TABLE chauffeurs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    cin VARCHAR(255) NOT NULL,
    telephone VARCHAR(255),
    date_embauche DATE,
    salaire DECIMAL(19,3) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_chauffeur_cin_admin (cin, admin_id)
);
```

#### Table `remorques`
```sql
CREATE TABLE remorques (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    matricule VARCHAR(255) NOT NULL,
    camion_id BIGINT,
    type_remorque VARCHAR(255),
    capacite_tonnes DOUBLE,
    date_achat DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_remorque_matricule_admin (matricule, admin_id)
);
```

#### Table `charges_templates`
```sql
CREATE TABLE charges_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    categorie VARCHAR(50) NOT NULL,
    montant_reference DECIMAL(19,3),
    camion_id BIGINT,
    chauffeur_id BIGINT,
    remorque_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_template_libelle_admin (libelle, admin_id)
);
```

#### Table `charges`
```sql
CREATE TABLE charges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    date DATE NOT NULL,
    montant DECIMAL(19,3) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    notes VARCHAR(500),
    FOREIGN KEY (template_id) REFERENCES charges_templates(id)
);
```

#### Table `rappels_charge`
```sql
CREATE TABLE rappels_charge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    frequence VARCHAR(50) NOT NULL,
    prochaine_date DATE NOT NULL,
    jours_avant INT NOT NULL DEFAULT 15,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (template_id) REFERENCES charges_templates(id)
);
```

#### Modification de la table `camions`
```sql
-- Colonne ajoutée (existante conservée)
ALTER TABLE camions ADD COLUMN chauffeur_id BIGINT;
-- Note: la colonne nom_chauffeur VARCHAR(255) existe toujours
```

---

## Annexe : Script SQL de migration

Pour créer toutes les tables nécessaires, exécutez le script suivant :

```sql
-- Création de la table chauffeurs
CREATE TABLE IF NOT EXISTS chauffeurs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    cin VARCHAR(255) NOT NULL,
    telephone VARCHAR(255),
    date_embauche DATE,
    salaire DECIMAL(19,3) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_chauffeur_cin_admin UNIQUE (cin, admin_id)
);
```

-- Création de la table remorques
CREATE TABLE IF NOT EXISTS remorques (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    matricule VARCHAR(255) NOT NULL,
    camion_id BIGINT,
    type_remorque VARCHAR(255),
    capacite_tonnes DOUBLE,
    date_achat DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_remorque_matricule_admin UNIQUE (matricule, admin_id)
);

-- Création de la table charges_templates
CREATE TABLE IF NOT EXISTS charges_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    categorie VARCHAR(50) NOT NULL,
    montant_reference DECIMAL(19,3),
    camion_id BIGINT,
    chauffeur_id BIGINT,
    remorque_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_template_libelle_admin UNIQUE (libelle, admin_id)
);

-- Création de la table charges
CREATE TABLE IF NOT EXISTS charges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    date DATE NOT NULL,
    montant DECIMAL(19,3) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    notes VARCHAR(500),
    CONSTRAINT fk_charge_template FOREIGN KEY (template_id) REFERENCES charges_templates(id)
);

-- Création de la table rappels_charge
CREATE TABLE IF NOT EXISTS rappels_charge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    frequence VARCHAR(50) NOT NULL,
    prochaine_date DATE NOT NULL,
    jours_avant INT NOT NULL DEFAULT 15,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_rappel_template FOREIGN KEY (template_id) REFERENCES charges_templates(id)
);
```

-- Modification de la table camions (si elle existe déjà)
-- Ajouter la colonne chauffeur_id si elle n'existe pas
ALTER TABLE camions ADD COLUMN IF NOT EXISTS chauffeur_id BIGINT;

-- Index pour améliorer les performances
CREATE INDEX idx_chauffeur_admin ON chauffeurs(admin_id);
CREATE INDEX idx_chauffeur_active ON chauffeurs(active);
CREATE INDEX idx_remorque_admin ON remorques(admin_id);
CREATE INDEX idx_remorque_camion ON remorques(camion_id);
CREATE INDEX idx_template_admin ON charges_templates(admin_id);
CREATE INDEX idx_template_type ON charges_templates(type);
CREATE INDEX idx_charge_admin ON charges(admin_id);
CREATE INDEX idx_charge_template ON charges(template_id);
CREATE INDEX idx_charge_date ON charges(date);
CREATE INDEX idx_charge_statut ON charges(statut);
CREATE INDEX idx_rappel_admin ON rappels_charge(admin_id);
CREATE INDEX idx_rappel_prochaine_date ON rappels_charge(prochaine_date);
CREATE INDEX idx_rappel_actif ON rappels_charge(actif);
```

---

## Glossaire

| Terme | Description |
|-------|-------------|
| **Admin** | Utilisateur principal qui gère son entreprise de transport |
| **Multi-tenant** | Architecture permettant l'isolation des données entre différents admins |
| **Chauffeur** | Conducteur de camion employé par l'entreprise |
| **Remorque** | Véhicule tracté par un camion tracteur |
| **Charge** | Dépense ou coût d'exploitation (salaire, assurance, réparation, etc.) |
| **Template** | Modèle de charge réutilisable pour créer des dépenses récurrentes |
| **Rappel** | Alerte automatique pour une charge récurrente à venir |
| **Charge fixe** | Dépense régulière et prévisible (ex: salaire, assurance) |
| **Charge variable** | Dépense ponctuelle et variable (ex: réparation, lavage) |
| **Statut** | État d'une charge ou d'un rappel (calculé pour les rappels) |

---

## FAQ (Questions fréquentes)

**Q: Puis-je supprimer un chauffeur assigné à un camion ?**  
R: Non, vous devez d'abord désassigner le chauffeur du camion ou utiliser PATCH /active pour le désactiver.

**Q: Que se passe-t-il si je supprime un template de charge ?**  
R: La suppression échoue si des charges actives référencent ce template. Désactivez-le avec PATCH /active.

**Q: Comment fonctionne le calcul du statut des rappels ?**  
R: Le statut est calculé automatiquement :
- `DEPASSE` : la date est passée (joursRestants < 0)
- `PROCHE` : la date approche dans le délai configuré (0 <= joursRestants <= joursAvant)
- `OK` : la date est lointaine (joursRestants > joursAvant)

**Q: Puis-je assigner plusieurs remorques au même camion ?**  
R: Non, une remorque ne peut être assignée qu'à un seul camion. Modifiez la remorque pour changer son camion tracteur.

**Q: Les montants sont-ils TTC ou HT ?**  
R: Les montants sont saisis tels quels, l'application ne gère pas la TVA automatiquement.

**Q: Comment obtenir toutes les charges d'un chauffeur ?**  
R: Créez un template lié au chauffeur, puis utilisez GET /api/charges?templateId={id}.

**Q: Que signifie "accès refusé" dans une erreur 404 ?**  
R: La ressource existe mais appartient à un autre admin. Pour des raisons de sécurité, on retourne 404 plutôt que 403.

**Q: Puis-je changer le type d'un template (FIXE → VARIABLE) ?**  
R: Oui, utilisez PUT /api/charges-templates/{id} avec le nouveau type.

**Q: Comment savoir quand payer une charge récurrente ?**  
R: Consultez GET /api/rappels-charge/alertes pour voir les paiements à venir ou en retard.

**Q: Puis-je avoir plusieurs templates avec le même nom ?**  
R: Non, le libellé doit être unique par admin pour éviter les confusions.

---

**Fin de la documentation API**
