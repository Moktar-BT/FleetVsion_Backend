# Collection Postman - Module Gestion du Carburant

## ⚠️ IMPORTANT - Configuration de Base

**Base URL :** `http://localhost:8080/api`

**ATTENTION:** Le contexte `/api` est obligatoire! Tous les endpoints commencent par `/api`.

**Headers requis pour tous les endpoints :**
```
Content-Type: application/json
Authorization: Bearer {votre_token_jwt}
```

**Exemples d'URLs complètes:**
- ✅ `http://localhost:8080/api/stations`
- ✅ `http://localhost:8080/api/prix-carburant`
- ✅ `http://localhost:8080/api/bons-carburant`
- ❌ `http://localhost:8080/stations` (INCORRECT - manque /api)

---

## 1. PRIX CARBURANT

### 1.1 Créer/Mettre à jour les prix du carburant
```
POST http://localhost:8080/api/prix-carburant
```

**Body :**
```json
{
  "prixEssence": 2.150,
  "prixDiesel": 1.950,
  "prixDiesel50": 2.050
}
```

**Réponse attendue (200 OK) :**
```json
{
  "id": 1,
  "prixEssence": 2.150,
  "prixDiesel": 1.950,
  "prixDiesel50": 2.050
}
```

---

### 1.2 Obtenir les prix configurés
```
GET http://localhost:8080/api/prix-carburant
```

**Pas de body**

**Réponse attendue (200 OK) :**
```json
{
  "id": 1,
  "prixEssence": 2.150,
  "prixDiesel": 1.950,
  "prixDiesel50": 2.050
}
```

---

## 2. STATIONS

### 2.1 Créer une station
```
POST {{baseUrl}}/stations
```

**Body :**
```json
{
  "nom": "Station Total Tunis Centre",
  "localisation": "Avenue Habib Bourguiba, Tunis"
}
```

**Réponse attendue (201 CREATED) :**
```json
{
  "id": 1,
  "nom": "Station Total Tunis Centre",
  "localisation": "Avenue Habib Bourguiba, Tunis",
  "totalAnnuelle": 0.00,
  "totalMensuelle": 0.00,
  "totalDieselMois": 0.00,
  "totalDiesel50Mois": 0.00,
  "totalEssenceMois": 0.00
}
```

---

### 2.2 Créer une deuxième station
```
POST {{baseUrl}}/stations
```

**Body :**
```json
{
  "nom": "Station Agil Ariana",
  "localisation": "Route de Bizerte, Ariana"
}
```

---

### 2.3 Créer une troisième station
```
POST {{baseUrl}}/stations
```

**Body :**
```json
{
  "nom": "Station Shell Lac 2",
  "localisation": "Les Berges du Lac, Tunis"
}
```

---

### 2.4 Obtenir toutes les stations
```
GET {{baseUrl}}/stations
```

**Pas de body**

**Réponse attendue (200 OK) :**
```json
[
  {
    "id": 1,
    "nom": "Station Total Tunis Centre",
    "localisation": "Avenue Habib Bourguiba, Tunis",
    "totalAnnuelle": 0.00,
    "totalMensuelle": 0.00,
    "totalDieselMois": 0.00,
    "totalDiesel50Mois": 0.00,
    "totalEssenceMois": 0.00
  },
  {
    "id": 2,
    "nom": "Station Agil Ariana",
    "localisation": "Route de Bizerte, Ariana",
    "totalAnnuelle": 0.00,
    "totalMensuelle": 0.00,
    "totalDieselMois": 0.00,
    "totalDiesel50Mois": 0.00,
    "totalEssenceMois": 0.00
  }
]
```

---

### 2.5 Obtenir une station par ID
```
GET {{baseUrl}}/stations/1
```

**Pas de body**

---

### 2.6 Modifier une station
```
PUT {{baseUrl}}/stations/1
```

**Body :**
```json
{
  "nom": "Station Total Tunis Centre Ville",
  "localisation": "Avenue Habib Bourguiba, Tunis 1000"
}
```

---

### 2.7 Supprimer une station
```
DELETE {{baseUrl}}/stations/3
```

**Pas de body**

**Réponse attendue (204 NO CONTENT)**

---

## 3. BONS CARBURANT

### 3.1 Créer le premier bon de carburant (pas de consommation calculée)
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-01",
  "camionId": 1,
  "stationId": 1,
  "kilometrageActuel": 120000,
  "quantiteLitres": 85.5,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "commentaire": "Premier plein du mois"
}
```

**Réponse attendue (201 CREATED) :**
```json
{
  "id": 1,
  "date": "2026-05-01",
  "camionId": 1,
  "camionMatricule": "123TU456",
  "stationId": 1,
  "stationNom": "Station Total Tunis Centre",
  "kilometrageActuel": 120000.0,
  "quantiteLitres": 85.5,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "montantTotal": 166.73,
  "distanceParcourue": null,
  "consommationReelle": null,
  "commentaire": "Premier plein du mois",
  "consommationStatut": "INSUFFISANT",
  "consommationMessage": "Données insuffisantes pour calculer la consommation"
}
```

---

### 3.2 Créer un deuxième bon (consommation calculée)
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-05",
  "camionId": 1,
  "stationId": 1,
  "kilometrageActuel": 120450,
  "quantiteLitres": 80.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "commentaire": "Plein après livraison Sfax"
}
```

**Réponse attendue (201 CREATED) :**
```json
{
  "id": 2,
  "date": "2026-05-05",
  "camionId": 1,
  "camionMatricule": "123TU456",
  "stationId": 1,
  "stationNom": "Station Total Tunis Centre",
  "kilometrageActuel": 120450.0,
  "quantiteLitres": 80.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "montantTotal": 156.00,
  "distanceParcourue": 450.0,
  "consommationReelle": 17.78,
  "commentaire": "Plein après livraison Sfax",
  "consommationStatut": "BONNE",
  "consommationMessage": "Consommation excellente : 17.8 L/100km"
}
```

---

### 3.3 Créer un troisième bon (consommation moyenne)
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-10",
  "camionId": 1,
  "stationId": 2,
  "kilometrageActuel": 120800,
  "quantiteLitres": 95.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.980,
  "commentaire": "Plein en ville - trafic dense"
}
```

**Réponse attendue :**
```json
{
  "id": 3,
  "date": "2026-05-10",
  "camionId": 1,
  "camionMatricule": "123TU456",
  "stationId": 2,
  "stationNom": "Station Agil Ariana",
  "kilometrageActuel": 120800.0,
  "quantiteLitres": 95.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.980,
  "montantTotal": 188.10,
  "distanceParcourue": 350.0,
  "consommationReelle": 27.14,
  "commentaire": "Plein en ville - trafic dense",
  "consommationStatut": "MOYENNE",
  "consommationMessage": "Consommation normale : 27.1 L/100km"
}
```

---

### 3.4 Créer un bon avec consommation élevée
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-15",
  "camionId": 1,
  "stationId": 1,
  "kilometrageActuel": 121000,
  "quantiteLitres": 75.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "commentaire": "Consommation anormale - à vérifier"
}
```

**Réponse attendue :**
```json
{
  "id": 4,
  "date": "2026-05-15",
  "camionId": 1,
  "camionMatricule": "123TU456",
  "stationId": 1,
  "stationNom": "Station Total Tunis Centre",
  "kilometrageActuel": 121000.0,
  "quantiteLitres": 75.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "montantTotal": 146.25,
  "distanceParcourue": 200.0,
  "consommationReelle": 37.50,
  "commentaire": "Consommation anormale - à vérifier",
  "consommationStatut": "MAUVAISE",
  "consommationMessage": "Consommation élevée : 37.5 L/100km, vérifiez le véhicule"
}
```

---

### 3.5 Créer un bon avec DIESEL_50
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-20",
  "camionId": 1,
  "stationId": 2,
  "kilometrageActuel": 121500,
  "quantiteLitres": 90.0,
  "typCarburant": "DIESEL_50",
  "prixLitre": 2.050,
  "commentaire": "Test diesel 50"
}
```

---

### 3.6 Créer un bon avec ESSENCE (pour un autre camion)
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-22",
  "camionId": 2,
  "stationId": 1,
  "kilometrageActuel": 85000,
  "quantiteLitres": 60.0,
  "typCarburant": "ESSENCE",
  "prixLitre": 2.150,
  "commentaire": "Premier plein camion 2"
}
```

---

### 3.7 Obtenir tous les bons de carburant
```
GET {{baseUrl}}/bons-carburant
```

**Pas de body**

---

### 3.8 Filtrer les bons par camion
```
GET {{baseUrl}}/bons-carburant?camionId=1
```

**Pas de body**

---

### 3.9 Filtrer les bons par station
```
GET {{baseUrl}}/bons-carburant?stationId=1
```

**Pas de body**

---

### 3.10 Obtenir un bon par ID
```
GET {{baseUrl}}/bons-carburant/2
```

**Pas de body**

---

### 3.11 Modifier un bon de carburant
```
PUT {{baseUrl}}/bons-carburant/4
```

**Body :**
```json
{
  "date": "2026-05-15",
  "camionId": 1,
  "stationId": 1,
  "kilometrageActuel": 121000,
  "quantiteLitres": 75.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "commentaire": "Consommation vérifiée - filtre à air changé"
}
```

---

### 3.12 Supprimer un bon de carburant
```
DELETE {{baseUrl}}/bons-carburant/6
```

**Pas de body**

**Réponse attendue (204 NO CONTENT)**

---

### 3.13 Obtenir les statistiques carburant d'un camion
```
GET {{baseUrl}}/bons-carburant/camion/1/stats
```

**Pas de body**

**Réponse attendue (200 OK) :**
```json
{
  "matricule": "123TU456",
  "nomChauffeur": "Ahmed Ben Ali",
  "consommationMoyenne": 27.47,
  "consommationDernier": 18.00,
  "coutTotalCarburant": 841.08,
  "coutMensuelCarburant": 841.08,
  "nombreBons": 5,
  "statut": "MOYENNE",
  "message": "Consommation normale : 27.5 L/100km"
}
```

---

## 4. TESTS D'ERREURS

### 4.1 Kilométrage invalide (inférieur au précédent)
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-25",
  "camionId": 1,
  "stationId": 1,
  "kilometrageActuel": 120000,
  "quantiteLitres": 80.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "commentaire": "Test erreur kilométrage"
}
```

**Réponse attendue (500 ERROR) :**
```json
{
  "message": "Le kilométrage actuel (120000) est inférieur au kilométrage précédent (121500). Veuillez vérifier."
}
```

---

### 4.2 Station en doublon
```
POST {{baseUrl}}/stations
```

**Body :**
```json
{
  "nom": "Station Total Tunis Centre",
  "localisation": "Autre adresse"
}
```

**Réponse attendue (500 ERROR) :**
```json
{
  "message": "Une station avec ce nom existe déjà : Station Total Tunis Centre"
}
```

---

### 4.3 Camion inexistant
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-25",
  "camionId": 9999,
  "stationId": 1,
  "kilometrageActuel": 125000,
  "quantiteLitres": 80.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950
}
```

**Réponse attendue (500 ERROR) :**
```json
{
  "message": "Camion non trouvé ou accès refusé"
}
```

---

### 4.4 Station inexistante
```
POST {{baseUrl}}/bons-carburant
```

**Body :**
```json
{
  "date": "2026-05-25",
  "camionId": 1,
  "stationId": 9999,
  "kilometrageActuel": 125000,
  "quantiteLitres": 80.0,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950
}
```

**Réponse attendue (500 ERROR) :**
```json
{
  "message": "Station non trouvée ou accès refusé"
}
```

---

### 4.5 Prix carburant non configurés
```
GET {{baseUrl}}/prix-carburant
```

**Réponse attendue (500 ERROR) si aucun prix n'est configuré :**
```json
{
  "message": "Prix du carburant non configurés pour cet admin"
}
```

---

## 5. SCÉNARIO COMPLET DE TEST

### Ordre recommandé :

1. **Configurer les prix** (1.1)
2. **Créer 2-3 stations** (2.1, 2.2, 2.3)
3. **Créer le premier bon** (3.1) - Pas de consommation
4. **Créer le deuxième bon** (3.2) - Consommation calculée
5. **Créer plusieurs bons** (3.3, 3.4, 3.5) - Différents types
6. **Consulter les statistiques** (3.13)
7. **Vérifier les totaux de la station** (2.4 ou 2.5)
8. **Tester les filtres** (3.8, 3.9)
9. **Tester les erreurs** (4.1, 4.2, 4.3, 4.4)

---

## 6. VARIABLES POSTMAN

Créez ces variables dans Postman :

```
baseUrl: http://localhost:8080
token: {votre_token_jwt_après_login}
camionId: 1
stationId: 1
bonCarburantId: 1
```

---

## 7. NOTES IMPORTANTES

1. **Authentification requise** : Tous les endpoints nécessitent un token JWT valide
2. **Types de carburant** : `DIESEL`, `DIESEL_50`, `ESSENCE` (sensible à la casse)
3. **Format de date** : `YYYY-MM-DD` (ex: `2026-05-24`)
4. **Calculs automatiques** : 
   - `montantTotal` = `quantiteLitres` × `prixLitre`
   - `distanceParcourue` = `kilometrageActuel` - `kilometragePrecedent`
   - `consommationReelle` = (`quantiteLitres` / `distanceParcourue`) × 100
5. **Mise à jour automatique** : Le kilométrage du camion est mis à jour automatiquement
6. **Recalcul des totaux** : Les totaux de la station sont recalculés après chaque opération

---

## 8. EXPORT POSTMAN COLLECTION

Pour importer dans Postman, créez une nouvelle collection et ajoutez ces requêtes avec les bodies ci-dessus.
