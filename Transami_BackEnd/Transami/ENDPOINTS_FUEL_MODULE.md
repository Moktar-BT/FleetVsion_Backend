# Endpoints du Module Carburant - Guide Rapide

## ⚠️ URL DE BASE: `http://localhost:8080/api`

**Tous les endpoints nécessitent:**
- Header: `Authorization: Bearer {votre_jwt_token}`
- Header: `Content-Type: application/json`

---

## 1. PRIX CARBURANT (`/api/prix-carburant`)

### Créer/Mettre à jour les prix
```http
POST http://localhost:8080/api/prix-carburant
```
**Body:**
```json
{
  "prixEssence": 2.150,
  "prixDiesel": 1.950,
  "prixDiesel50": 2.050
}
```

### Obtenir les prix
```http
GET http://localhost:8080/api/prix-carburant
```

---

## 2. STATIONS (`/api/stations`)

### Créer une station
```http
POST http://localhost:8080/api/stations
```
**Body:**
```json
{
  "nom": "Station Shell Centre",
  "localisation": "Avenue Habib Bourguiba, Tunis"
}
```

### Lister toutes les stations
```http
GET http://localhost:8080/api/stations
```

### Obtenir une station par ID
```http
GET http://localhost:8080/api/stations/1
```

### Modifier une station
```http
PUT http://localhost:8080/api/stations/1
```
**Body:**
```json
{
  "nom": "Station Shell Centre Ville",
  "localisation": "Avenue Habib Bourguiba, Tunis"
}
```

### Supprimer une station
```http
DELETE http://localhost:8080/api/stations/1
```

---

## 3. BONS DE CARBURANT (`/api/bons-carburant`)

### Créer un bon de carburant
```http
POST http://localhost:8080/api/bons-carburant
```
**Body:**
```json
{
  "date": "2024-01-15",
  "camionId": 1,
  "stationId": 1,
  "kilometrageActuel": 15000,
  "quantiteLitres": 80,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "commentaire": "Plein complet"
}
```

**Types de carburant valides:** `DIESEL`, `DIESEL_50`, `ESSENCE`

### Lister tous les bons
```http
GET http://localhost:8080/api/bons-carburant
```

### Filtrer par camion
```http
GET http://localhost:8080/api/bons-carburant?camionId=1
```

### Filtrer par station
```http
GET http://localhost:8080/api/bons-carburant?stationId=1
```

### Obtenir un bon par ID
```http
GET http://localhost:8080/api/bons-carburant/1
```

### Modifier un bon
```http
PUT http://localhost:8080/api/bons-carburant/1
```
**Body:** (même structure que la création)

### Supprimer un bon
```http
DELETE http://localhost:8080/api/bons-carburant/1
```

### Statistiques carburant d'un camion
```http
GET http://localhost:8080/api/bons-carburant/camion/1/stats
```

**Réponse:**
```json
{
  "matricule": "123 TUN 456",
  "nomChauffeur": "Ahmed Ben Ali",
  "consommationMoyenne": 28.5,
  "consommationDernier": 27.8,
  "coutTotalCarburant": 5420.50,
  "coutMensuelCarburant": 850.00,
  "nombreBons": 12,
  "statut": "MOYENNE",
  "message": "Consommation normale : 28.5 L/100km"
}
```

---

## 4. ORDRE DE TEST RECOMMANDÉ

1. **Authentification** (obtenir le JWT token)
   ```http
   POST http://localhost:8080/api/auth/login
   ```
   Body:
   ```json
   {
     "username": "votre_username",
     "password": "votre_password"
   }
   ```

2. **Configurer les prix du carburant**
   ```http
   POST http://localhost:8080/api/prix-carburant
   ```

3. **Créer une station**
   ```http
   POST http://localhost:8080/api/stations
   ```

4. **Vérifier que vous avez un camion** (ou en créer un)
   ```http
   GET http://localhost:8080/api/camions
   ```

5. **Créer un premier bon de carburant**
   ```http
   POST http://localhost:8080/api/bons-carburant
   ```

6. **Créer un deuxième bon** (pour voir le calcul de consommation)
   - Utiliser un kilométrage supérieur au premier
   - La consommation sera calculée automatiquement

7. **Consulter les statistiques**
   ```http
   GET http://localhost:8080/api/bons-carburant/camion/{camionId}/stats
   ```

---

## 5. ERREURS COURANTES

### 404 Not Found
- ❌ Vous utilisez `http://localhost:8080/stations`
- ✅ Utilisez `http://localhost:8080/api/stations`

### 401 Unauthorized
- Vérifiez que le token JWT est présent dans le header
- Vérifiez que le token n'est pas expiré

### 403 Forbidden
- Vérifiez que vous êtes authentifié en tant qu'admin

### 400 Bad Request
- Vérifiez le format JSON
- Vérifiez que tous les champs requis sont présents
- Vérifiez les types de données (nombres, dates, etc.)

---

## 6. NOTES IMPORTANTES

### Calcul automatique de la consommation
- Le premier bon de carburant d'un camion n'aura pas de consommation calculée
- À partir du deuxième bon, la consommation est calculée automatiquement:
  - `distanceParcourue = kilometrageActuel - kilometragePrecedent`
  - `consommationReelle = (quantiteLitres / distanceParcourue) * 100`

### Statuts de consommation
- **BONNE**: ≤ 25 L/100km
- **MOYENNE**: 25-35 L/100km
- **MAUVAISE**: > 35 L/100km
- **INSUFFISANT**: Pas assez de données

### Mise à jour automatique
- Le kilométrage du camion est mis à jour automatique
- 
- ment
- Les totaux de la station sont recalculés automatiquement
- Les totaux mensuels sont basés sur le mois en cours

### Isolation par admin
- Chaque admin voit uniquement ses propres données
- Les stations, prix et bons sont isolés par admin
- Pas besoin de passer l'adminId dans les requêtes (extrait du JWT)
