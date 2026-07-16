# Quick Reference: Chauffeur-Camion-Remorque Linking

## 🔗 What Changed?

### CamionResponse (Truck)
**NEW:** Now shows assigned trailer information
```json
{
  "id": 1,
  "matricule": "456TU9012",
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali",
  "remorqueId": 2,              // ← NEW
  "remorqueMatricule": "789TU3456",  // ← NEW
  "remorqueType": "Benne"       // ← NEW
}
```

### ChauffeurResponse (Driver)
**NEW:** Now shows assigned truck information
```json
{
  "id": 1,
  "nomComplet": "Mohamed Ben Ali",
  "camionId": 1,                // ← NEW
  "camionMatricule": "456TU9012" // ← NEW
}
```

### CamionRequest (Create/Update Truck)
**CHANGED:** `nomChauffeur` is now optional when `chauffeurId` is provided
```json
// Option 1: Link to existing driver
{
  "matricule": "456TU9012",
  "chauffeurId": 1,           // ← nomChauffeur auto-derived
  "truckModel": "Mercedes"
}

// Option 2: Use free-text name
{
  "matricule": "456TU9012",
  "nomChauffeur": "Ahmed",    // ← No driver link
  "truckModel": "Mercedes"
}
```

---

## 📊 Relationship Flow

```
Chauffeur (Driver)
    ↓ Can be assigned to
Camion (Truck)
    ↓ Can have attached
Remorque (Trailer)
```

### Query Patterns

**"Which truck is this driver on?"**
```http
GET /api/chauffeurs/1
→ Response includes: camionId, camionMatricule
```

**"Which trailer is on this truck?"**
```http
GET /api/camions/1
→ Response includes: remorqueId, remorqueMatricule, remorqueType
```

**"Which driver is on this truck?"**
```http
GET /api/camions/1
→ Response includes: chauffeurId, chauffeurNom
```

---

## 🔧 Common Operations

### Assign Driver to Truck
```http
POST /api/camions
{
  "matricule": "123TU4567",
  "chauffeurId": 1,
  "truckModel": "Volvo"
}
```

### Assign Trailer to Truck
```http
PUT /api/remorques/2
{
  "matricule": "789TU3456",
  "camionId": 1,
  "typeRemorque": "Benne"
}
```

### Unassign Driver (use free text instead)
```http
PUT /api/camions/1
{
  "matricule": "123TU4567",
  "nomChauffeur": "Disponible",
  "chauffeurId": null,
  "truckModel": "Volvo"
}
```

### Unassign Trailer
```http
PUT /api/remorques/2
{
  "matricule": "789TU3456",
  "camionId": null,
  "typeRemorque": "Benne"
}
```

---

## ⚠️ Validation Rules

| Scenario | Validation |
|----------|------------|
| Create truck without `nomChauffeur` AND without `chauffeurId` | ❌ Error 400 |
| Create truck with invalid `chauffeurId` | ❌ Error 404 |
| Assign trailer with invalid `camionId` | ❌ Error 404 |
| Create truck with `chauffeurId` → `nomChauffeur` auto-filled | ✅ Success |

---

## 🎯 Use Case Examples

### Case 1: Check Free Drivers
```http
GET /api/chauffeurs
```
Look for drivers where `camionId: null` → they're available!

### Case 2: Check Available Trailers
```http
GET /api/remorques
```
Look for trailers where `camionId: null` → they're available!

### Case 3: Full Truck Configuration
```http
# Step 1: Create driver
POST /api/chauffeurs { "nom": "Ben Ali", "prenom": "Mohamed", ... }
→ Returns id: 1

# Step 2: Create truck linked to driver
POST /api/camions { "matricule": "123TU", "chauffeurId": 1, ... }
→ Returns id: 1

# Step 3: Create trailer linked to truck
POST /api/remorques { "matricule": "789TU", "camionId": 1, ... }
→ Returns id: 1

# Step 4: Verify everything
GET /api/camions/1
→ Shows driver AND trailer info!
```

---

## 📂 Files Modified

| File | Change |
|------|--------|
| `CamionResponse.java` | ✅ Added 3 fields (remorque info) |
| `ChauffeurResponse.java` | ✅ Added 2 fields (camion info) |
| `CamionRequest.java` | ✅ Made nomChauffeur optional |
| `CamionDao.java` | ✅ Added findByChauffeurIdAndAdminId |
| `RemorqueDao.java` | ✅ Added findByCamionIdAndAdminId |
| `CamionService.java` | ✅ Updated toResponse + validation |
| `ChauffeurService.java` | ✅ Updated toResponse |

**Total:** 7 files modified, 0 new files, 0 breaking changes

---

## ✨ Benefits

1. **Frontend can now:**
   - Show "Truck: 456TU9012" on driver cards
   - Show "Driver: Mohamed Ben Ali" on truck cards
   - Show "Trailer: 789TU3456 (Benne)" on truck cards
   - Filter free drivers/trailers easily

2. **Business logic:**
   - Track which resources are in use vs available
   - Prevent double-booking (future feature)
   - Better resource management

3. **No migration needed:**
   - Uses existing database columns
   - Fully backward compatible
   - Just queries additional data

---

## 🚀 Ready to Use!

All endpoints now return enriched data automatically. No additional API calls needed!

**Test it:**
```bash
# Get a truck with all relationships
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/camions/1

# Expected response includes:
# - chauffeurId, chauffeurNom
# - remorqueId, remorqueMatricule, remorqueType
```

---

**Version:** 1.1  
**Status:** ✅ Implemented & Tested  
**Breaking Changes:** None  
**Migration Required:** None
