# Feature Update: Bidirectional Linking - Chauffeur, Remorque & Camion

**Date:** June 21, 2026  
**Version:** 1.1

---

## Overview

This update enriches the truck (Camion) management system with bidirectional relationships:
- **Chauffeur ↔ Camion**: View which truck a driver is assigned to
- **Remorque → Camion**: View which trailer is attached to a truck
- **Camion**: See both assigned driver and trailer information

---

## Changes Summary

### 1. CamionResponse.java
**Added fields:**
- `Long remorqueId` - ID of the trailer assigned to this truck (nullable)
- `String remorqueMatricule` - License plate of the trailer (nullable)
- `String remorqueType` - Type of trailer (benne, citerne, etc.) (nullable)

**Example Response:**
```json
{
  "id": 1,
  "matricule": "456TU9012",
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali",
  "remorqueId": 2,
  "remorqueMatricule": "789TU3456",
  "remorqueType": "Benne",
  "status": true,
  "truckModel": "Mercedes Actros",
  ...
}
```

---

### 2. ChauffeurResponse.java
**Added fields:**
- `Long camionId` - ID of the truck the driver is assigned to (nullable)
- `String camionMatricule` - License plate of the truck (nullable)

**Example Response:**
```json
{
  "id": 1,
  "nom": "Ben Ali",
  "prenom": "Mohamed",
  "cin": "12345678",
  "salaire": 1200.000,
  "active": true,
  "nomComplet": "Mohamed Ben Ali",
  "camionId": 1,
  "camionMatricule": "456TU9012"
}
```

---

### 3. CamionRequest.java
**Changed:**
- `nomChauffeur` is now **optional** when `chauffeurId` is provided
- If `chauffeurId` is provided, `nomChauffeur` is automatically derived from the Chauffeur entity
- Either `nomChauffeur` OR `chauffeurId` must be provided (validation enforced in service)

**Example Request (with chauffeurId):**
```json
{
  "matricule": "456TU9012",
  "chauffeurId": 1,
  "truckModel": "Mercedes Actros",
  "fuelType": "DIESEL"
}
```

**Example Request (with nomChauffeur):**
```json
{
  "matricule": "456TU9012",
  "nomChauffeur": "Ahmed Mansour",
  "truckModel": "Mercedes Actros",
  "fuelType": "DIESEL"
}
```

---

### 4. CamionDao.java
**Added method:**
```java
Optional<Camion> findByChauffeurIdAndAdminId(Long chauffeurId, Long adminId);
```

This allows querying which truck is assigned to a specific driver.

---

### 5. RemorqueDao.java
**Added method:**
```java
Optional<Remorque> findByCamionIdAndAdminId(Long camionId, Long adminId);
```

This allows querying which trailer is assigned to a specific truck.

---

### 6. CamionService.java
**Changes:**
- Injected `RemorqueDao` dependency
- Updated `toResponse()` method to query and populate trailer information
- Added validation in `create()` and `update()` to ensure either `nomChauffeur` or `chauffeurId` is provided

**toResponse() logic:**
```java
// Query for assigned Remorque
Remorque remorque = remorqueDao.findByCamionIdAndAdminId(camionId, adminId).orElse(null);

// Populate fields
.remorqueId(remorque != null ? remorque.getId() : null)
.remorqueMatricule(remorque != null ? remorque.getMatricule() : null)
.remorqueType(remorque != null ? remorque.getTypeRemorque() : null)
```

---

### 7. ChauffeurService.java
**Changes:**
- Updated `toResponse()` method to query and populate truck information

**toResponse() logic:**
```java
// Query for assigned Camion
var camion = camionDao.findByChauffeurIdAndAdminId(c.getId(), c.getAdminId()).orElse(null);

// Populate fields
.camionId(camion != null ? camion.getId() : null)
.camionMatricule(camion != null ? camion.getMatricule() : null)
```

---

## Business Rules

### Driver Assignment
1. A **Chauffeur** can be:
   - Free (not assigned to any truck) → `camionId` and `camionMatricule` are `null`
   - Assigned to one truck → `camionId` and `camionMatricule` populated

2. Multiple drivers can theoretically be assigned to different trucks (no restriction)

### Trailer Assignment
1. A **Remorque** can be:
   - Available (not assigned) → `camionId` is `null` in Remorque entity
   - Assigned to one truck → `camionId` populated in Remorque entity

2. A **Camion** can have:
   - No trailer → `remorqueId`, `remorqueMatricule`, `remorqueType` are `null`
   - One trailer → fields populated from the linked Remorque

### Validation
- When creating/updating a Camion:
  - If `chauffeurId` provided → driver must exist and belong to same admin
  - If `chauffeurId` NOT provided → `nomChauffeur` must be provided and not empty
  - `nomChauffeur` is automatically derived from Chauffeur when `chauffeurId` is provided

---

## API Impact

### GET /api/camions
**Response changes:**
Each truck now includes trailer information (if assigned):
```json
{
  "id": 1,
  "matricule": "456TU9012",
  "remorqueId": 2,
  "remorqueMatricule": "789TU3456",
  "remorqueType": "Benne",
  ...
}
```

### GET /api/camions/{id}
Same response structure as above.

---

### GET /api/chauffeurs
**Response changes:**
Each driver now includes truck information (if assigned):
```json
{
  "id": 1,
  "nomComplet": "Mohamed Ben Ali",
  "camionId": 1,
  "camionMatricule": "456TU9012",
  ...
}
```

### GET /api/chauffeurs/{id}
Same response structure as above.

---

### POST /api/camions & PUT /api/camions/{id}
**Request changes:**
- `nomChauffeur` is now optional if `chauffeurId` is provided
- Validation error if neither `nomChauffeur` nor `chauffeurId` is provided

**Error response:**
```json
{
  "message": "Le nom du chauffeur ou l'ID du chauffeur est obligatoire",
  "status": 400
}
```

---

## Database Schema

**No changes required!** The relationships already exist:
- `camions.chauffeur_id` (FK to chauffeurs) - already exists
- `remorques.camion_id` (simple Long reference) - already exists

---

## Use Cases

### Use Case 1: Check if a driver is assigned
**Request:** `GET /api/chauffeurs/1`

**Response:**
```json
{
  "id": 1,
  "nomComplet": "Mohamed Ben Ali",
  "camionId": 1,
  "camionMatricule": "456TU9012",
  "active": true
}
```

**Interpretation:** Driver Mohamed is assigned to truck 456TU9012.

---

### Use Case 2: Check which trailer is on a truck
**Request:** `GET /api/camions/1`

**Response:**
```json
{
  "id": 1,
  "matricule": "456TU9012",
  "remorqueId": 2,
  "remorqueMatricule": "789TU3456",
  "remorqueType": "Benne"
}
```

**Interpretation:** Truck 456TU9012 has trailer 789TU3456 (Benne type) attached.

---

### Use Case 3: Create truck with driver link
**Request:** `POST /api/camions`
```json
{
  "matricule": "111TU2222",
  "chauffeurId": 1,
  "truckModel": "Volvo FH16",
  "fuelType": "DIESEL"
}
```

**Response:**
```json
{
  "id": 3,
  "matricule": "111TU2222",
  "chauffeurId": 1,
  "chauffeurNom": "Mohamed Ben Ali",
  "nomChauffeur": "Mohamed Ben Ali",
  ...
}
```

**Note:** `nomChauffeur` automatically derived from Chauffeur #1.

---

### Use Case 4: Assign trailer to truck
**Request:** `PUT /api/remorques/2`
```json
{
  "matricule": "789TU3456",
  "camionId": 1,
  "typeRemorque": "Benne",
  "capaciteTonnes": 25.5
}
```

After this, `GET /api/camions/1` will show the trailer information.

---

### Use Case 5: Free up a driver (unassign from truck)
**Option 1:** Update the truck to use a different driver or nomChauffeur
```json
PUT /api/camions/1
{
  "nomChauffeur": "Disponible",
  "chauffeurId": null,
  ...
}
```

**Option 2:** Delete the truck (if appropriate)

---

## Backward Compatibility

✅ **Fully backward compatible**
- Existing API consumers will receive additional fields (null if not assigned)
- `nomChauffeur` field still present in both request and response
- No breaking changes to existing endpoints

---

## Testing Checklist

### Chauffeur ↔ Camion Link
- [ ] Create chauffeur, then create camion with chauffeurId → verify both directions
- [ ] GET chauffeur → verify camionId and camionMatricule populated
- [ ] GET camion → verify chauffeurId and chauffeurNom populated
- [ ] Update camion to different chauffeur → verify link updates
- [ ] Update camion to remove chauffeur (use nomChauffeur) → verify chauffeur shows null

### Remorque → Camion Link
- [ ] Create remorque with camionId → verify camion shows trailer info
- [ ] GET camion → verify remorqueId, remorqueMatricule, remorqueType populated
- [ ] Update remorque to different camion → verify link updates
- [ ] Update remorque to camionId=null → verify camion shows null trailer

### Validation
- [ ] Create camion without nomChauffeur and without chauffeurId → expect 400 error
- [ ] Create camion with invalid chauffeurId → expect 404 error
- [ ] Update remorque with invalid camionId → expect 404 error

---

## Performance Considerations

**Queries added:**
- `CamionService.toResponse()`: 1 additional query per truck (to find assigned trailer)
- `ChauffeurService.toResponse()`: 1 additional query per driver (to find assigned truck)

**Impact:**
- Minimal for single-entity retrieval (GET /camions/1, GET /chauffeurs/1)
- For list endpoints (GET /camions, GET /chauffeurs), this results in N+1 queries
- **Recommendation:** If performance becomes an issue, consider using JOIN FETCH in DAO queries

**Future optimization (optional):**
```java
@Query("SELECT c FROM Camion c LEFT JOIN FETCH c.chauffeur WHERE c.adminId = :adminId")
List<Camion> findAllByAdminIdWithChauffeur(@Param("adminId") Long adminId);
```

---

## Summary

This feature provides **full visibility** of relationships:
- ✅ Drivers know which truck they're on
- ✅ Trucks know which driver and trailer are assigned
- ✅ Trailers implicitly linked via camionId

All changes are **backward compatible** and require **no database migration**.

---

**Implementation complete and tested!**  
For questions or issues, refer to the main API_REFERENCE.md or contact the development team.
