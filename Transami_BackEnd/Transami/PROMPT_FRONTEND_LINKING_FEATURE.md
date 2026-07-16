# Prompt Frontend: Intégration de la Fonctionnalité de Liaison Chauffeur-Camion-Remorque

## 🎯 OBJECTIF

Enrichir les pages **Trucks**, **Chauffeurs**, et **Remorques** du frontend pour afficher les relations bidirectionnelles entre ces entités, en utilisant les données fournies par le backend mis à jour.

## 📋 CONTEXTE

Le backend a été enrichi avec une fonctionnalité de **liaison bidirectionnelle**:
- Un **Chauffeur** peut être assigné à un **Camion** → Le chauffeur voit quel camion lui est assigné
- Un **Camion** a un **Chauffeur** assigné → Le camion affiche les infos du chauffeur
- Un **Camion** peut avoir une **Remorque** attachée → Le camion affiche les infos de la remorque
- Une **Remorque** peut être assignée à un **Camion** → La remorque affiche quel camion l'utilise

**Documentation backend à lire et à attacher au chat Kiro:**
- `API_REFERENCE.md` - Documentation complète de l'API
- `LINKING_FEATURE_UPDATE.md` - Détails de la fonctionnalité de liaison
- `QUICK_REFERENCE_LINKING.md` - Guide rapide

---

## 📊 STRUCTURE DES DONNÉES DE L'API

### CamionResponse (Réponse GET /api/camions ou GET /api/camions/{id})

```json
{
  "id": 1,
  "matricule": "456TU9012",
  "chauffeurId": 1,
  "nomChauffeur": "Mohamed Ben Ali",
  
  // ⬇️ NOUVEAUX CHAMPS (nullable si pas de remorque assignée)
  "remorqueId": 2,
  "remorqueMatricule": "789TU3456",
  "remorqueType": "Benne",
  
  "status": true,
  "truckModel": "Mercedes Actros",
  "fuelType": "DIESEL",
  "adminId": 1
}
```

**Champs clés:**
- `chauffeurId`, `nomChauffeur` → Infos du chauffeur assigné (déjà existants)
- `remorqueId`, `remorqueMatricule`, `remorqueType` → **NOUVEAUX** infos de la remorque attachée

---

### ChauffeurResponse (Réponse GET /api/chauffeurs ou GET /api/chauffeurs/{id})

```json
{
  "id": 1,
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "telephone": "+216 20 123 456",
  "dateEmbauche": "2024-01-15",
  "salaire": 1200.000,
  "active": true,
  "nomComplet": "Mohamed Ben Ali",
  
  // ⬇️ NOUVEAUX CHAMPS (nullable si pas assigné à un camion)
  "camionId": 1,
  "camionMatricule": "456TU9012",
  
  "adminId": 1
}
```

**Champs clés:**
- `camionId`, `camionMatricule` → **NOUVEAUX** infos du camion assigné

---

### RemorqueResponse (Réponse GET /api/remorques ou GET /api/remorques/{id})

```json
{
  "id": 2,
  "matricule": "789TU3456",
  "typeRemorque": "Benne",
  "capaciteTonnes": 25.5,
  "dateAchat": "2023-03-15",
  
  // ⬇️ CHAMPS EXISTANTS
  "camionId": 1,
  "camionMatricule": "456TU9012",
  
  "active": true,
  "adminId": 1
}
```

**Champs clés:**
- `camionId`, `camionMatricule` → Infos du camion auquel elle est attachée

---

## 🔧 MODIFICATIONS À EFFECTUER

### 1️⃣ PAGE TRUCKS (Camions)

**Fichier à modifier:** `app/trucks/page.tsx` (ou équivalent selon votre structure)

#### Modifications demandées:

#### A. Remplacement du champ texte "Nom du chauffeur" par un dropdown

**Avant:**
```tsx
<Input
  label="Nom du chauffeur"
  name="nomChauffeur"
  value={formData.nomChauffeur}
  onChange={handleChange}
  required
/>
```

**Après:**
```tsx
<Select
  label="Chauffeur"
  name="chauffeurId"
  value={formData.chauffeurId}
  onChange={handleChauffeurChange}
  required
>
  <option value="">Sélectionner un chauffeur</option>
  {chauffeurs.map(c => (
    <option key={c.id} value={c.id}>
      {c.nomComplet} {c.camionId ? `(Assigné à ${c.camionMatricule})` : '(Disponible)'}
    </option>
  ))}
</Select>
```

**Logique:**
- Charger la liste des chauffeurs via `GET /api/chauffeurs`
- Afficher `nomComplet` de chaque chauffeur
- Indiquer si le chauffeur est déjà assigné à un camion
- Lors de la sélection, envoyer `chauffeurId` dans le body de la requête (POST ou PUT)

---
#### B. Statut automatique "Maintenance" si pas de chauffeur

**Règle métier:**
> Si un camion n'a PAS de chauffeur assigné (`chauffeurId === null`), son statut doit automatiquement être `status: false` (Maintenance).

**Implémentation:**
```tsx
const handleChauffeurChange = (e) => {
  const chauffeurId = e.target.value ? Number(e.target.value) : null;
  
  setFormData({
    ...formData,
    chauffeurId,
    status: chauffeurId ? formData.status : false  // ← Auto-désactiver si pas de chauffeur
  });
};
```

**Affichage dans le formulaire:**
```tsx
{!formData.chauffeurId && (
  <Alert type="warning">
    ⚠️ Attention: Ce camion sera automatiquement mis en maintenance car aucun chauffeur n'est assigné.
  </Alert>
)}

<Switch
  label="Statut"
  checked={formData.status}
  onChange={(checked) => setFormData({ ...formData, status: checked })}
  disabled={!formData.chauffeurId}  // ← Désactiver si pas de chauffeur
/>
```

---

#### C. Affichage des badges Chauffeur et Remorque sur chaque carte de camion

**Dans la liste des camions:**

Chaque carte de camion doit afficher:
1. **Badge Chauffeur** avec le nom du chauffeur
2. **Badge Remorque** avec le matricule et type de la remorque (si présente)

**Exemple de rendu:**

```tsx
<Card>
  <h3>{truck.matricule}</h3>
  <p>Modèle: {truck.truckModel}</p>
  
  {/* Badge Chauffeur */}
  <Badge color="blue" icon={<User />}>
    👤 {truck.nomChauffeur || 'Aucun chauffeur'}
  </Badge>
  
  {/* Badge Remorque (si présente) */}
  {truck.remorqueId && (
    <Badge color="green" icon={<Truck />}>
      🚛 {truck.remorqueMatricule} ({truck.remorqueType})
    </Badge>
  )}
  
  {/* Badge Statut */}
  <Badge color={truck.status ? 'green' : 'red'}>
    {truck.status ? 'Opérationnel' : 'Maintenance'}
  </Badge>
</Card>
```

**Style des badges:**
- **Chauffeur**: Bleu (#3b82f6) avec icône utilisateur 👤
- **Remorque**: Vert (#10b981) avec icône camion 🚛
- **Statut Opérationnel**: Vert (#10b981) ✅
- **Statut Maintenance**: Rouge (#ef4444) ⚠️

---

### 2️⃣ PAGE CHAUFFEURS (Drivers)

**Fichier à modifier:** `app/chauffeurs/page.tsx` (ou équivalent)

#### Modifications demandées:

#### A. Affichage du badge "Camion assigné" sur chaque carte de chauffeur

**Dans la liste des chauffeurs:**

Chaque carte de chauffeur doit afficher un badge indiquant le camion assigné (ou "Disponible").

**Exemple de rendu:**

```tsx
<Card>
  <h3>{chauffeur.nomComplet}</h3>
  <p>CIN: {chauffeur.cin}</p>
  <p>Téléphone: {chauffeur.telephone}</p>
  <p>Salaire: {chauffeur.salaire.toFixed(3)} TND</p>
  
  {/* Badge Camion assigné */}
  {chauffeur.camionId ? (
    <Badge color="blue" icon={<Truck />}>
      🚛 Assigné au camion: {chauffeur.camionMatricule}
    </Badge>
  ) : (
    <Badge color="gray" icon={<Clock />}>
      ⏸️ Disponible
    </Badge>
  )}
  
  {/* Badge Statut */}
  <Badge color={chauffeur.active ? 'green' : 'red'}>
    {chauffeur.active ? 'Actif' : 'Inactif'}
  </Badge>
</Card>
```

**Style des badges:**
- **Assigné**: Bleu (#3b82f6) avec icône camion 🚛
- **Disponible**: Gris (#6b7280) avec icône horloge ⏸️
- **Actif**: Vert (#10b981) ✅
- **Inactif**: Rouge (#ef4444) ❌

---
#### B. Filtre par statut d'assignation

**Ajouter un filtre pour afficher:**
- **Tous les chauffeurs**
- **Chauffeurs disponibles** (pas de camion assigné)
- **Chauffeurs assignés** (ont un camion)

**Exemple d'implémentation:**

```tsx
const [assignmentFilter, setAssignmentFilter] = useState<'all' | 'assigned' | 'available'>('all');

const filteredChauffeurs = useMemo(() => {
  return chauffeurs.filter(c => {
    if (assignmentFilter === 'assigned') return c.camionId !== null;
    if (assignmentFilter === 'available') return c.camionId === null;
    return true;  // 'all'
  });
}, [chauffeurs, assignmentFilter]);

// UI
<div className="filters">
  <Button 
    variant={assignmentFilter === 'all' ? 'primary' : 'secondary'}
    onClick={() => setAssignmentFilter('all')}
  >
    Tous
  </Button>
  <Button 
    variant={assignmentFilter === 'available' ? 'primary' : 'secondary'}
    onClick={() => setAssignmentFilter('available')}
  >
    Disponibles
  </Button>
  <Button 
    variant={assignmentFilter === 'assigned' ? 'primary' : 'secondary'}
    onClick={() => setAssignmentFilter('assigned')}
  >
    Assignés
  </Button>
</div>
```

---

### 3️⃣ PAGE REMORQUES (Trailers)

**Fichier à modifier:** `app/remorques/page.tsx` (ou équivalent)

#### Modifications demandées:

#### A. Meilleur affichage du statut d'assignation

**Dans la liste des remorques:**

Chaque carte de remorque doit clairement afficher si elle est assignée à un camion ou disponible.

**Exemple de rendu:**

```tsx
<Card>
  <h3>{remorque.matricule}</h3>
  <p>Type: {remorque.typeRemorque}</p>
  <p>Capacité: {remorque.capaciteTonnes} tonnes</p>
  
  {/* Badge Assignation */}
  {remorque.camionId ? (
    <Badge color="blue" icon={<Link />}>
      🔗 Attachée au camion: {remorque.camionMatricule}
    </Badge>
  ) : (
    <Badge color="green" icon={<Check />}>
      ✅ Disponible
    </Badge>
  )}
  
  {/* Badge Statut */}
  <Badge color={remorque.active ? 'green' : 'red'}>
    {remorque.active ? 'Active' : 'Inactive'}
  </Badge>
</Card>
```

**Style des badges:**
- **Attachée**: Bleu (#3b82f6) avec icône lien 🔗
- **Disponible**: Vert (#10b981) avec icône check ✅
- **Active**: Vert (#10b981) ✅
- **Inactive**: Rouge (#ef4444) ❌

---

#### B. Filtres améliorés

**Ajouter des filtres pour afficher:**
- **Toutes les remorques**
- **Remorques disponibles** (pas assignées)
- **Remorques assignées** (attachées à un camion)
- **Remorques par type** (Benne, Citerne, etc.)

**Exemple d'implémentation:**

```tsx
const [availabilityFilter, setAvailabilityFilter] = useState<'all' | 'assigned' | 'available'>('all');
const [typeFilter, setTypeFilter] = useState<string>('all');

const filteredRemorques = useMemo(() => {
  return remorques.filter(r => {
    // Filtre par disponibilité
    if (availabilityFilter === 'assigned' && !r.camionId) return false;
    if (availabilityFilter === 'available' && r.camionId) return false;
    
    // Filtre par type
    if (typeFilter !== 'all' && r.typeRemorque !== typeFilter) return false;
    
    return true;
  });
}, [remorques, availabilityFilter, typeFilter]);

// UI
<div className="filters">
  <Select 
    label="Disponibilité"
    value={availabilityFilter}
    onChange={(e) => setAvailabilityFilter(e.target.value as any)}
  >
    <option value="all">Toutes</option>
    <option value="available">Disponibles</option>
    <option value="assigned">Assignées</option>
  </Select>
  
  <Select 
    label="Type"
    value={typeFilter}
    onChange={(e) => setTypeFilter(e.target.value)}
  >
    <option value="all">Tous les types</option>
    <option value="Benne">Benne</option>
    <option value="Citerne">Citerne</option>
    <option value="Plateau">Plateau</option>
    {/* Autres types selon vos besoins */}
  </Select>
</div>
```

---
## 📊 TYPES TYPESCRIPT À METTRE À JOUR

### Mettre à jour le fichier `types/api.types.ts` (ou équivalent)

```typescript
// Camion (Truck)
export interface CamionResponse {
  id: number;
  matricule: string;
  chauffeurId: number | null;
  nomChauffeur: string;
  
  // ⬇️ NOUVEAUX CHAMPS
  remorqueId: number | null;
  remorqueMatricule: string | null;
  remorqueType: string | null;
  
  status: boolean;
  truckModel: string;
  fuelType: string;
  adminId: number;
}

export interface CamionRequest {
  matricule: string;
  chauffeurId?: number | null;  // ← Optionnel si nomChauffeur fourni
  nomChauffeur?: string;         // ← Optionnel si chauffeurId fourni
  truckModel: string;
  fuelType: string;
  status?: boolean;
}

// Chauffeur (Driver)
export interface ChauffeurResponse {
  id: number;
  nom: string;
  prenom: string;
  cin: string;
  telephone: string;
  dateEmbauche: string;
  salaire: number;
  active: boolean;
  nomComplet: string;
  
  // ⬇️ NOUVEAUX CHAMPS
  camionId: number | null;
  camionMatricule: string | null;
  
  adminId: number;
}

// Remorque (Trailer)
export interface RemorqueResponse {
  id: number;
  matricule: string;
  typeRemorque: string;
  capaciteTonnes: number;
  dateAchat: string;
  camionId: number | null;
  camionMatricule: string | null;
  active: boolean;
  adminId: number;
}
```

---
## 🎨 DESIGN SYSTEM / STYLES

### Couleurs recommandées (Tailwind CSS)

```typescript
const statusColors = {
  // Badges Chauffeur
  chauffeurAssigned: 'bg-blue-100 text-blue-800 border-blue-300',
  chauffeurAvailable: 'bg-gray-100 text-gray-800 border-gray-300',
  
  // Badges Remorque
  remorqueAttached: 'bg-blue-100 text-blue-800 border-blue-300',
  remorqueAvailable: 'bg-green-100 text-green-800 border-green-300',
  
  // Badges Statut
  active: 'bg-green-100 text-green-800 border-green-300',
  inactive: 'bg-red-100 text-red-800 border-red-300',
  maintenance: 'bg-red-100 text-red-800 border-red-300',
  operational: 'bg-green-100 text-green-800 border-green-300',
};
```

### Composant Badge réutilisable

```tsx
interface BadgeProps {
  color: 'blue' | 'green' | 'gray' | 'red';
  icon?: React.ReactNode;
  children: React.ReactNode;
}

const Badge: React.FC<BadgeProps> = ({ color, icon, children }) => {
  const colorClasses = {
    blue: 'bg-blue-100 text-blue-800 border-blue-300',
    green: 'bg-green-100 text-green-800 border-green-300',
    gray: 'bg-gray-100 text-gray-800 border-gray-300',
    red: 'bg-red-100 text-red-800 border-red-300',
  };
  
  return (
    <span className={`
      inline-flex items-center gap-1 px-3 py-1 rounded-full 
      text-sm font-medium border ${colorClasses[color]}
    `}>
      {icon}
      {children}
    </span>
  );
};
```

### Icônes (lucide-react)

```tsx
import { 
  User,        // Chauffeur
  Truck,       // Camion/Remorque
  Link,        // Attachée
  Check,       // Disponible
  Clock,       // En attente
  AlertTriangle // Maintenance
} from 'lucide-react';
```

---
## 🔐 RÈGLES MÉTIER IMPORTANTES

### 1. Création/Modification d'un Camion

**Validation côté frontend:**
- ✅ Soit `chauffeurId` OU `nomChauffeur` doit être fourni (au moins l'un des deux)
- ✅ Si `chauffeurId` fourni → `nomChauffeur` est automatiquement résolu par le backend
- ✅ Si pas de `chauffeurId` → le camion est automatiquement en maintenance (`status: false`)

**Exemple de requête POST:**

```json
// Option 1: Avec chauffeurId (recommandé)
{
  "matricule": "123TU4567",
  "chauffeurId": 1,
  "truckModel": "Mercedes Actros",
  "fuelType": "DIESEL",
  "status": true
}

// Option 2: Avec nomChauffeur (texte libre)
{
  "matricule": "123TU4567",
  "nomChauffeur": "Ahmed Mansour",
  "truckModel": "Mercedes Actros",
  "fuelType": "DIESEL",
  "status": false  // Automatiquement false
}
```

---

### 2. Assignation d'une Remorque

**L'assignation se fait via l'API des remorques:**

```typescript
// PUT /api/remorques/{id}
const assignRemorque = async (remorqueId: number, camionId: number | null) => {
  await api.put(`/api/remorques/${remorqueId}`, {
    ...remorqueData,
    camionId  // null pour détacher
  });
};
```

**UI recommandée:**
- Ajouter un bouton "Assigner une remorque" dans la page de détail du camion
- Modal avec dropdown de sélection des remorques disponibles
- Afficher uniquement les remorques où `camionId === null`

---

### 3. Libérer un Chauffeur

Pour libérer un chauffeur d'un camion:

**Option 1:** Modifier le camion pour utiliser un texte libre
```json
PUT /api/camions/1
{
  "nomChauffeur": "Disponible",
  "chauffeurId": null,
  ...
}
```

**Option 2:** Assigner un autre chauffeur au camion

---
