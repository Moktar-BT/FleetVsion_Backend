# Module de Gestion du Carburant - Transami

## Vue d'ensemble

Ce module permet de gérer complètement le carburant pour une flotte de camions :
- Gestion des stations-service
- Configuration des prix du carburant par admin
- Enregistrement des bons de carburant avec calcul automatique de la consommation
- Statistiques détaillées par camion

## Entités

### 1. Station
Représente une station-service où les camions font le plein.

**Champs :**
- `nom` : Nom de la station (unique par admin)
- `localisation` : Adresse de la station
- `totalAnnuelle` : Total des dépenses annuelles
- `totalMensuelle` : Total des dépenses du mois en cours
- `totalDieselMois`, `totalDiesel50Mois`, `totalEssenceMois` : Totaux par type de carburant

### 2. PrixCarburant
Configuration des prix du carburant pour un admin (un seul enregistrement par admin).

**Champs :**
- `prixEssence` : Prix de l'essence (TND/L)
- `prixDiesel` : Prix du diesel (TND/L)
- `prixDiesel50` : Prix du diesel 50 (TND/L)

### 3. BonCarburant
Enregistrement d'un plein de carburant.

**Champs principaux :**
- `date` : Date du plein
- `camion` : Camion concerné
- `station` : Station où le plein a été fait
- `kilometrageActuel` : Kilométrage au moment du plein
- `quantiteLitres` : Quantité de carburant ajoutée
- `typCarburant` : DIESEL, DIESEL_50 ou ESSENCE
- `prixLitre` : Prix au litre (snapshot)
- `montantTotal` : Coût total (calculé automatiquement)

**Champs calculés automatiquement :**
- `distanceParcourue` : Distance depuis le dernier plein
- `consommationReelle` : Consommation en L/100km

## Logique métier

### Calcul automatique de la consommation

Lors de la création ou modification d'un bon de carburant :

1. **Recherche du bon précédent** pour le même camion
2. **Calcul de la distance** : `kilometrageActuel - kilometragePrecedent`
3. **Calcul de la consommation** : `(quantiteLitres / distanceParcourue) * 100`
4. **Mise à jour du kilométrage** du camion si nécessaire
5. **Recalcul des totaux** de la station

### Évaluation de la consommation

- **BONNE** : ≤ 25 L/100km
- **MOYENNE** : 25-35 L/100km
- **MAUVAISE** : > 35 L/100km
- **INSUFFISANT** : Pas assez de données

## API Endpoints

### Stations (`/stations`)

```
POST   /stations           - Créer une station
GET    /stations           - Liste des stations
GET    /stations/{id}      - Détails d'une station
PUT    /stations/{id}      - Modifier une station
DELETE /stations/{id}      - Supprimer une station
```

### Prix Carburant (`/prix-carburant`)

```
POST   /prix-carburant     - Créer/Mettre à jour les prix (upsert)
GET    /prix-carburant     - Obtenir les prix configurés
```

### Bons Carburant (`/bons-carburant`)

```
POST   /bons-carburant                    - Créer un bon
GET    /bons-carburant                    - Liste des bons (filtrable par camionId ou stationId)
GET    /bons-carburant/{id}               - Détails d'un bon
PUT    /bons-carburant/{id}               - Modifier un bon
DELETE /bons-carburant/{id}               - Supprimer un bon
GET    /bons-carburant/camion/{id}/stats  - Statistiques carburant d'un camion
```

## Exemples d'utilisation

### 1. Configurer les prix du carburant

```json
POST /prix-carburant
{
  "prixEssence": 2.150,
  "prixDiesel": 1.950,
  "prixDiesel50": 2.050
}
```

### 2. Créer une station

```json
POST /stations
{
  "nom": "Station Total Tunis",
  "localisation": "Avenue Habib Bourguiba, Tunis"
}
```

### 3. Enregistrer un plein

```json
POST /bons-carburant
{
  "date": "2026-05-24",
  "camionId": 1,
  "stationId": 1,
  "kilometrageActuel": 125000,
  "quantiteLitres": 80.5,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "commentaire": "Plein complet"
}
```

**Réponse :**
```json
{
  "id": 1,
  "date": "2026-05-24",
  "camionId": 1,
  "camionMatricule": "123TU456",
  "stationId": 1,
  "stationNom": "Station Total Tunis",
  "kilometrageActuel": 125000,
  "quantiteLitres": 80.5,
  "typCarburant": "DIESEL",
  "prixLitre": 1.950,
  "montantTotal": 156.98,
  "distanceParcourue": 450.0,
  "consommationReelle": 17.89,
  "commentaire": "Plein complet",
  "consommationStatut": "BONNE",
  "consommationMessage": "Consommation excellente : 17.9 L/100km"
}
```

### 4. Obtenir les statistiques d'un camion

```
GET /bons-carburant/camion/1/stats
```

**Réponse :**
```json
{
  "matricule": "123TU456",
  "nomChauffeur": "Ahmed Ben Ali",
  "consommationMoyenne": 28.5,
  "consommationDernier": 27.8,
  "coutTotalCarburant": 15420.50,
  "coutMensuelCarburant": 2340.00,
  "nombreBons": 45,
  "statut": "MOYENNE",
  "message": "Consommation normale : 28.5 L/100km"
}
```

## Validation et gestion d'erreurs

### Erreurs courantes

1. **Kilométrage invalide**
   ```
   "Le kilométrage actuel (124500) est inférieur au kilométrage précédent (125000). Veuillez vérifier."
   ```

2. **Station non trouvée**
   ```
   "Station non trouvée ou accès refusé"
   ```

3. **Camion non trouvé**
   ```
   "Camion non trouvé ou accès refusé"
   ```

4. **Station en doublon**
   ```
   "Une station avec ce nom existe déjà : Station Total Tunis"
   ```

## Installation

1. **Exécuter le script SQL** :
   ```bash
   mysql -u votre_user -p votre_database < create_fuel_tables.sql
   ```

2. **Redémarrer l'application** Spring Boot

3. **Tester les endpoints** avec Postman ou curl

## Isolation par admin

Toutes les données sont isolées par admin :
- Chaque admin a ses propres stations
- Chaque admin configure ses propres prix
- Les bons de carburant sont filtrés par admin
- Impossible d'accéder aux données d'un autre admin

## Notes techniques

- Les calculs de consommation sont automatiques
- Les totaux des stations sont recalculés après chaque opération
- Le kilométrage du camion est mis à jour automatiquement
- Les prix sont stockés comme snapshot dans chaque bon (historique)
- Toutes les validations sont côté serveur avec Jakarta Validation
