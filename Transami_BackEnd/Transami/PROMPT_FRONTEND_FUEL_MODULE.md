# Prompt pour Intégration du Module Carburant dans le Frontend

## 🎯 OBJECTIF

Intégrer le module de gestion du carburant dans le frontend React/Angular/Vue existant du projet Transami.

## 📋 CONTEXTE

Le backend Spring Boot a été enrichi avec un nouveau module de gestion du carburant comprenant:
- Gestion des stations de carburant
- Configuration des prix du carburant (Essence, Diesel, Diesel 50)
- Gestion des bons de carburant avec calcul automatique de consommation
- Statistiques de consommation par camion
- Isolation complète par admin (chaque admin voit uniquement ses données)

## 📁 FICHIERS DE RÉFÉRENCE À LIRE

### 1. Documentation API
- **`POSTMAN_FUEL_MODULE.md`** - Documentation complète des endpoints avec exemples
- **`ENDPOINTS_FUEL_MODULE.md`** - Guide rapide des endpoints
- **`Fuel_Module_Postman_Collection.json`** - Collection Postman importable
- **`FUEL_MODULE_README.md`** - Documentation technique du module

### 2. Backend (pour comprendre la structure)
- **Entities**: `src/main/java/com/Transami/Transami/entity/Station.java`, `PrixCarburant.java`, `BonCarburant.java`
- **DTOs**: Tous les fichiers dans `src/main/java/com/Transami/Transami/dto/` commençant par `Station`, `PrixCarburant`, `BonCarburant`, `CamionFuelStats`
- **Controllers**: `controller/StationController.java`, `PrixCarburantController.java`, `BonCarburantController.java`

## 🔧 TÂCHES À RÉALISER

### 1. MISE À JOUR DU FICHIER `api-client.ts` (ou équivalent)

Ajouter les endpoints suivants au service API existant:

```typescript
// ============================================
// PRIX CARBURANT
// ============================================

// Créer ou mettre à jour les prix du carburant (un seul enregistrement par admin)
createOrUpdatePrixCarburant(data: {
  prixEssence: number;
  prixDiesel: number;
  prixDiesel50: number;
}): Promise<PrixCarburantResponse>
// POST /api/prix-carburant

// Obtenir les prix configurés pour l'admin connecté
getPrixCarburant(): Promise<PrixCarburantResponse>
// GET /api/prix-carburant

// ============================================
// STATIONS
// ============================================

// Créer une nouvelle station
createStation(data: {
  nom: string;
  localisation: string;
}): Promise<StationResponse>
// POST /api/stations

// Lister toutes les stations de l'admin
getAllStations(): Promise<StationResponse[]>
// GET /api/stations

// Obtenir une station par ID
getStationById(id: number): Promise<StationResponse>
// GET /api/stations/{id}

// Modifier une station
updateStation(id: number, data: {
  nom: string;
  localisation: string;
}): Promise<StationResponse>
// PUT /api/stations/{id}

// Supprimer une station
deleteStation(id: number): Promise<void>
// DELETE /api/stations/{id}

// ============================================
// BONS DE CARBURANT
// ============================================

// Créer un bon de carburant
createBonCarburant(data: {
  date: string; // Format: "YYYY-MM-DD"
  camionId: number;
  stationId: number;
  kilometrageActuel: number;
  quantiteLitres: number;
  typCarburant: "DIESEL" | "DIESEL_50" | "ESSENCE";
  prixLitre: number;
  commentaire?: string;
}): Promise<BonCarburantResponse>
// POST /api/bons-carburant

// Lister tous les bons (avec filtres optionnels)
getAllBonsCarburant(filters?: {
  camionId?: number;
  stationId?: number;
}): Promise<BonCarburantResponse[]>
// GET /api/bons-carburant
// GET /api/bons-carburant?camionId=1
// GET /api/bons-carburant?stationId=1

// Obtenir un bon par ID
getBonCarburantById(id: number): Promise<BonCarburantResponse>
// GET /api/bons-carburant/{id}

// Modifier un bon de carburant
updateBonCarburant(id: number, data: {
  date: string;
  camionId: number;
  stationId: number;
  kilometrageActuel: number;
  quantiteLitres: number;
  typCarburant: "DIESEL" | "DIESEL_50" | "ESSENCE";
  prixLitre: number;
  commentaire?: string;
}): Promise<BonCarburantResponse>
// PUT /api/bons-carburant/{id}

// Supprimer un bon de carburant
deleteBonCarburant(id: number): Promise<void>
// DELETE /api/bons-carburant/{id}

// Obtenir les statistiques de consommation d'un camion
getCamionFuelStats(camionId: number): Promise<CamionFuelStatsResponse>
// GET /api/bons-carburant/camion/{camionId}/stats
```

### 2. TYPES TYPESCRIPT À CRÉER

Créer un fichier `types/fuel.types.ts` (ou ajouter au fichier types existant):

```typescript
// Types de carburant
export type FuelType = "DIESEL" | "DIESEL_50" | "ESSENCE";

// Statuts de consommation
export type ConsommationStatut = "BONNE" | "MOYENNE" | "MAUVAISE" | "INSUFFISANT";

// Prix Carburant
export interface PrixCarburantRequest {
  prixEssence: number;
  prixDiesel: number;
  prixDiesel50: number;
}

export interface PrixCarburantResponse {
  id: number;
  prixEssence: number;
  prixDiesel: number;
  prixDiesel50: number;
}

// Station
export interface StationRequest {
  nom: string;
  localisation: string;
}

export interface StationResponse {
  id: number;
  nom: string;
  localisation: string;
  totalAnnuelle: number;
  totalMensuelle: number;
  totalDieselMois: number;
  totalDiesel50Mois: number;
  totalEssenceMois: number;
}

// Bon de Carburant
export interface BonCarburantRequest {
  date: string; // Format ISO: "YYYY-MM-DD"
  camionId: number;
  stationId: number;
  kilometrageActuel: number;
  quantiteLitres: number;
  typCarburant: FuelType;
  prixLitre: number;
  commentaire?: string;
}

export interface BonCarburantResponse {
  id: number;
  date: string;
  camionId: number;
  camionMatricule: string;
  stationId: number;
  stationNom: string;
  kilometrageActuel: number;
  quantiteLitres: number;
  typCarburant: FuelType;
  prixLitre: number;
  montantTotal: number;
  distanceParcourue: number | null;
  consommationReelle: number | null;
  commentaire: string | null;
  consommationStatut: ConsommationStatut;
  consommationMessage: string;
}

// Statistiques Camion
export interface CamionFuelStatsResponse {
  matricule: string;
  nomChauffeur: string;
  consommationMoyenne: number | null;
  consommationDernier: number | null;
  coutTotalCarburant: number;
  coutMensuelCarburant: number;
  nombreBons: number;
  statut: ConsommationStatut;
  message: string;
}
```

### 3. MODIFICATION DE LA PAGE CAMIONS

**Fichier à modifier**: Page de détail d'un camion (ex: `CamionDetail.tsx`, `CamionView.vue`, etc.)

**Ajouts à faire**:

1. **Nouvel onglet "Carburant"** dans la page de détail du camion
2. **Afficher les statistiques de consommation**:
   - Consommation moyenne (L/100km)
   - Consommation du dernier plein
   - Coût total carburant
   - Coût mensuel carburant
   - Nombre de pleins
   - Statut avec badge coloré (BONNE=vert, MOYENNE=orange, MAUVAISE=rouge, INSUFFISANT=gris)
   - Message explicatif

3. **Tableau des bons de carburant du camion**:
   - Date
   - Station
   - Kilométrage
   - Quantité (L)
   - Type carburant
   - Prix/L
   - Montant total
   - Distance parcourue
   - Consommation (L/100km)
   - Statut avec badge
   - Actions (Modifier, Supprimer)

4. **Bouton "Ajouter un plein"** qui ouvre un modal/formulaire

### 4. NOUVELLE PAGE: GESTION DU CARBURANT

**Nom de la page**: "Carburant" ou "Fuel Management"

**Route**: `/carburant` ou `/fuel`

**Sections de la page**:

#### Section 1: Configuration des Prix (en haut)
- Card/Panel avec formulaire pour configurer les 3 prix
- Champs: Prix Essence, Prix Diesel, Prix Diesel 50
- Bouton "Enregistrer les prix"
- Affichage des prix actuels si déjà configurés

#### Section 2: Liste des Bons de Carburant (principal)
- **Filtres**:
  - Par camion (dropdown)
  - Par station (dropdown)
  - Par date (date range picker)
  - Bouton "Réinitialiser les filtres"

- **Tableau des bons**:
  - Colonnes: Date, Camion, Station, Kilométrage, Quantité, Type, Prix/L, Montant, Consommation, Statut
  - Tri par colonne
  - Pagination
  - Actions: Voir détails, Modifier, Supprimer

- **Boutons d'action**:
  - "Nouveau plein" (bouton principal)
  - "Exporter en Excel/PDF" (optionnel)

- **Statistiques globales** (cards en haut):
  - Nombre total de pleins ce mois
  - Coût total ce mois
  - Consommation moyenne de la flotte
  - Station la plus utilisée

#### Section 3: Graphiques (optionnel mais recommandé)
- Évolution de la consommation dans le temps
- Répartition des coûts par type de carburant
- Comparaison de consommation entre camions

### 5. NOUVELLE PAGE: STATIONS DE CARBURANT

**Nom de la page**: "Stations" ou "Gas Stations"

**Route**: `/stations` ou `/gas-stations`

**Contenu de la page**:

#### Vue en grille ou tableau
- **Card pour chaque station** contenant:
  - Nom de la station
  - Localisation
  - Total annuel (€)
  - Total mensuel (€)
  - Détails par type de carburant ce mois:
    - Diesel: XXX €
    - Diesel 50: XXX €
    - Essence: XXX €
  - Actions: Modifier, Supprimer

- **Bouton "Nouvelle station"** (bouton principal)

- **Statistiques globales** (en haut):
  - Nombre total de stations
  - Station avec le plus gros volume
  - Total dépensé ce mois toutes stations

### 6. FORMULAIRES/MODALS À CRÉER

#### Modal: Nouveau/Modifier Bon de Carburant
**Champs**:
- Date (date picker, défaut: aujourd'hui)
- Camion (dropdown avec recherche)
- Station (dropdown avec recherche)
- Kilométrage actuel (number, validation: > kilométrage précédent)
- Quantité en litres (number, validation: > 0)
- Type de carburant (radio buttons ou dropdown: Diesel, Diesel 50, Essence)
- Prix au litre (number, pré-rempli depuis la config, éditable)
- Commentaire (textarea, optionnel)

**Calculs automatiques à afficher**:
- Montant total = quantité × prix/litre
- Si pas le premier plein: distance parcourue et consommation estimée

**Validation**:
- Tous les champs obligatoires sauf commentaire
- Kilométrage doit être > au précédent
- Quantité et prix doivent être > 0

#### Modal: Nouvelle/Modifier Station
**Champs**:
- Nom (text, obligatoire)
- Localisation (text, obligatoire)

#### Modal: Configuration Prix Carburant
**Champs**:
- Prix Essence (number, 3 décimales, obligatoire)
- Prix Diesel (number, 3 décimales, obligatoire)
- Prix Diesel 50 (number, 3 décimales, obligatoire)

### 7. NAVIGATION / MENU

Ajouter dans le menu principal:
```
📊 Dashboard
🚛 Camions
⛽ Carburant          ← NOUVEAU
🏪 Stations          ← NOUVEAU
📦 Bons de Livraison
📄 Factures
👥 Clients
🏭 Fournisseurs
⚙️ Paramètres
```

### 8. TRADUCTIONS (i18n)

#### Français
```json
{
  "fuel": {
    "title": "Gestion du Carburant",
    "stations": "Stations",
    "newStation": "Nouvelle Station",
    "editStation": "Modifier la Station",
    "deleteStation": "Supprimer la Station",
    "stationName": "Nom de la station",
    "location": "Localisation",
    "totalAnnual": "Total Annuel",
    "totalMonthly": "Total Mensuel",
    
    "prices": "Prix du Carburant",
    "priceEssence": "Prix Essence",
    "priceDiesel": "Prix Diesel",
    "priceDiesel50": "Prix Diesel 50",
    "savePrices": "Enregistrer les prix",
    "pricesUpdated": "Prix mis à jour avec succès",
    
    "bons": "Bons de Carburant",
    "newBon": "Nouveau Plein",
    "editBon": "Modifier le Bon",
    "deleteBon": "Supprimer le Bon",
    "date": "Date",
    "truck": "Camion",
    "station": "Station",
    "mileage": "Kilométrage",
    "quantity": "Quantité (L)",
    "fuelType": "Type de Carburant",
    "pricePerLiter": "Prix/Litre",
    "totalAmount": "Montant Total",
    "distance": "Distance Parcourue",
    "consumption": "Consommation",
    "comment": "Commentaire",
    
    "fuelTypes": {
      "DIESEL": "Diesel",
      "DIESEL_50": "Diesel 50",
      "ESSENCE": "Essence"
    },
    
    "stats": {
      "title": "Statistiques de Consommation",
      "avgConsumption": "Consommation Moyenne",
      "lastConsumption": "Dernière Consommation",
      "totalCost": "Coût Total",
      "monthlyCost": "Coût Mensuel",
      "numberOfRefills": "Nombre de Pleins",
      "status": "Statut"
    },
    
    "status": {
      "BONNE": "Bonne",
      "MOYENNE": "Moyenne",
      "MAUVAISE": "Mauvaise",
      "INSUFFISANT": "Données Insuffisantes"
    },
    
    "filters": {
      "byTruck": "Par Camion",
      "byStation": "Par Station",
      "byDate": "Par Date",
      "reset": "Réinitialiser"
    },
    
    "messages": {
      "bonCreated": "Bon de carburant créé avec succès",
      "bonUpdated": "Bon de carburant modifié avec succès",
      "bonDeleted": "Bon de carburant supprimé avec succès",
      "stationCreated": "Station créée avec succès",
      "stationUpdated": "Station modifiée avec succès",
      "stationDeleted": "Station supprimée avec succès",
      "confirmDelete": "Êtes-vous sûr de vouloir supprimer ?",
      "mileageError": "Le kilométrage doit être supérieur au précédent"
    }
  }
}
```

#### Anglais
```json
{
  "fuel": {
    "title": "Fuel Management",
    "stations": "Stations",
    "newStation": "New Station",
    "editStation": "Edit Station",
    "deleteStation": "Delete Station",
    "stationName": "Station Name",
    "location": "Location",
    "totalAnnual": "Annual Total",
    "totalMonthly": "Monthly Total",
    
    "prices": "Fuel Prices",
    "priceEssence": "Gasoline Price",
    "priceDiesel": "Diesel Price",
    "priceDiesel50": "Diesel 50 Price",
    "savePrices": "Save Prices",
    "pricesUpdated": "Prices updated successfully",
    
    "bons": "Fuel Receipts",
    "newBon": "New Refill",
    "editBon": "Edit Receipt",
    "deleteBon": "Delete Receipt",
    "date": "Date",
    "truck": "Truck",
    "station": "Station",
    "mileage": "Mileage",
    "quantity": "Quantity (L)",
    "fuelType": "Fuel Type",
    "pricePerLiter": "Price/Liter",
    "totalAmount": "Total Amount",
    "distance": "Distance Traveled",
    "consumption": "Consumption",
    "comment": "Comment",
    
    "fuelTypes": {
      "DIESEL": "Diesel",
      "DIESEL_50": "Diesel 50",
      "ESSENCE": "Gasoline"
    },
    
    "stats": {
      "title": "Consumption Statistics",
      "avgConsumption": "Average Consumption",
      "lastConsumption": "Last Consumption",
      "totalCost": "Total Cost",
      "monthlyCost": "Monthly Cost",
      "numberOfRefills": "Number of Refills",
      "status": "Status"
    },
    
    "status": {
      "BONNE": "Good",
      "MOYENNE": "Average",
      "MAUVAISE": "Poor",
      "INSUFFISANT": "Insufficient Data"
    },
    
    "filters": {
      "byTruck": "By Truck",
      "byStation": "By Station",
      "byDate": "By Date",
      "reset": "Reset"
    },
    
    "messages": {
      "bonCreated": "Fuel receipt created successfully",
      "bonUpdated": "Fuel receipt updated successfully",
      "bonDeleted": "Fuel receipt deleted successfully",
      "stationCreated": "Station created successfully",
      "stationUpdated": "Station updated successfully",
      "stationDeleted": "Station deleted successfully",
      "confirmDelete": "Are you sure you want to delete?",
      "mileageError": "Mileage must be greater than previous"
    }
  }
}
```

## 🎨 DESIGN / UX

### Couleurs pour les Statuts
- **BONNE**: Vert (#10b981 ou success)
- **MOYENNE**: Orange (#f59e0b ou warning)
- **MAUVAISE**: Rouge (#ef4444 ou danger)
- **INSUFFISANT**: Gris (#6b7280 ou secondary)

### Icônes Recommandées
- ⛽ Carburant général
- 🏪 Stations
- 📊 Statistiques
- 🚛 Camions
- 📝 Bon de carburant
- 💰 Coûts
- 📈 Consommation

### Badges/Pills
Utiliser des badges colorés pour:
- Type de carburant (Diesel=bleu, Diesel 50=cyan, Essence=vert)
- Statut de consommation (selon couleurs ci-dessus)

## 📊 LOGIQUE MÉTIER IMPORTANTE

### Calcul de la Consommation
```
distanceParcourue = kilometrageActuel - kilometragePrecedent
consommationReelle = (quantiteLitres / distanceParcourue) × 100
```

**Affichage**: "28.5 L/100km"

### Évaluation du Statut
- ≤ 25 L/100km → BONNE (vert)
- 25-35 L/100km → MOYENNE (orange)
- > 35 L/100km → MAUVAISE (rouge)
- Pas de données → INSUFFISANT (gris)

### Montant Total
```
montantTotal = quantiteLitres × prixLitre
```

### Validation Kilométrage
Le kilométrage actuel doit TOUJOURS être supérieur au kilométrage du dernier bon pour ce camion.
Afficher un message d'erreur clair si ce n'est pas le cas.

## 🔐 SÉCURITÉ

- Tous les appels API nécessitent le token JWT dans le header `Authorization: Bearer {token}`
- L'isolation par admin est gérée automatiquement par le backend
- Pas besoin d'envoyer l'adminId dans les requêtes (extrait du JWT)

## 📱 RESPONSIVE

Assurer que toutes les pages sont responsive:
- Mobile: Tableaux en mode cards empilées
- Tablet: Vue hybride
- Desktop: Tableaux complets avec toutes les colonnes

## ✅ CHECKLIST DE VALIDATION

Avant de considérer l'intégration terminée, vérifier:

- [ ] Tous les endpoints sont ajoutés à `api-client.ts`
- [ ] Tous les types TypeScript sont créés
- [ ] Page Camions modifiée avec onglet Carburant
- [ ] Page Carburant créée et fonctionnelle
- [ ] Page Stations créée et fonctionnelle
- [ ] Tous les formulaires/modals créés
- [ ] Validation des formulaires implémentée
- [ ] Traductions FR/EN complètes
- [ ] Navigation/menu mis à jour
- [ ] Gestion des erreurs API
- [ ] Messages de succès/erreur affichés
- [ ] Design cohérent avec le reste de l'app
- [ ] Responsive testé (mobile, tablet, desktop)
- [ ] Statistiques affichées correctement
- [ ] Filtres fonctionnels
- [ ] Badges de statut colorés
- [ ] Calculs automatiques (montant, consommation)
- [ ] Tests manuels de tous les CRUD

## 🚀 ORDRE D'IMPLÉMENTATION RECOMMANDÉ

1. **Ajouter les types TypeScript**
2. **Mettre à jour `api-client.ts`**
3. **Créer la page Stations** (la plus simple)
4. **Créer la configuration des prix** (section dans page Carburant)
5. **Créer la page Carburant** (liste des bons)
6. **Ajouter l'onglet Carburant dans la page Camions**
7. **Ajouter les traductions**
8. **Tester l'ensemble**

## 📞 SUPPORT

En cas de questions sur les endpoints ou la logique métier, consulter:
- `POSTMAN_FUEL_MODULE.md` - Documentation complète
- `ENDPOINTS_FUEL_MODULE.md` - Guide rapide
- `FUEL_MODULE_README.md` - Documentation technique

## 🎯 RÉSULTAT ATTENDU

À la fin de l'intégration, l'utilisateur doit pouvoir:
1. Configurer les prix du carburant
2. Créer et gérer des stations
3. Enregistrer des pleins de carburant pour chaque camion
4. Voir automatiquement la consommation calculée
5. Consulter les statistiques par camion
6. Filtrer et rechercher les bons de carburant
7. Voir les totaux par station
8. Tout cela en français ET en anglais

Bonne intégration! 🚀
