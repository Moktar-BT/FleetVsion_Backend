package com.Transami.Transami.service;

import com.Transami.Transami.dao.BonCarburantDao;
import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.StationDao;
import com.Transami.Transami.dto.BonCarburantRequest;
import com.Transami.Transami.dto.BonCarburantResponse;
import com.Transami.Transami.dto.CamionFuelStatsResponse;
import com.Transami.Transami.entity.BonCarburant;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BonCarburantService {

    private final BonCarburantDao bonCarburantDao;
    private final CamionDao camionDao;
    private final StationDao stationDao;
    private final StationService stationService;

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public BonCarburantResponse create(BonCarburantRequest request, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        Station station = stationDao.findByIdAndAdminId(request.getStationId(), adminId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée ou accès refusé"));

        // ── Validation kilométrage ─────────────────────────────────────────────
        // Si mileage est null (premier bon du camion), on accepte sans comparaison.
        if (camion.getMileage() != null && request.getKilometrageActuel() <= camion.getMileage()) {
            throw new RuntimeException(
                    "Le kilométrage saisi (" + request.getKilometrageActuel() +
                            " km) doit être supérieur au kilométrage actuel du camion (" +
                            camion.getMileage() + " km)");
        }

        // ── Validation type de carburant ──────────────────────────────────────
        if (camion.getFuelType() != null &&
                !camion.getFuelType().equals(request.getTypCarburant())) {
            throw new RuntimeException(
                    "Le type de carburant « " + request.getTypCarburant() +
                            " » ne correspond pas au type du camion « " + camion.getFuelType() + " »");
        }

        // ── Unicité du numéro par camion + admin ──────────────────────────────
        if (request.getNumero() != null && !request.getNumero().isBlank()) {
            if (bonCarburantDao.existsByNumeroAndCamionIdAndAdminId(
                    request.getNumero(), request.getCamionId(), adminId)) {
                throw new RuntimeException(
                        "Le numéro de bon « " + request.getNumero() +
                                " » existe déjà pour ce camion.");
            }
        }

        // ── Calcul montantTotal ───────────────────────────────────────────────
        BigDecimal montantTotal = BigDecimal.valueOf(request.getQuantiteLitres())
                .multiply(request.getPrixLitre())
                .setScale(2, RoundingMode.HALF_UP);

        BonCarburant bon = BonCarburant.builder()
                .adminId(adminId)
                .numero(request.getNumero())
                .date(request.getDate())
                .camion(camion)
                .station(station)
                .kilometrageActuel(request.getKilometrageActuel())
                .quantiteLitres(request.getQuantiteLitres())
                .typCarburant(request.getTypCarburant())
                .prixLitre(request.getPrixLitre())
                .montantTotal(montantTotal)
                .distanceParcourue(0.0)
                .consommationReelle(null)
                .build();

        BonCarburant saved = bonCarburantDao.save(bon);

        // ── Calcul distance + consommation du bon nouvellement créé ───────────
        // distance = km(bon actuel) - km(bon précédent du même camion)
        // consommation = litres(bon actuel) / distance * 100
        updatePreviousBon(saved, adminId);

        // ── Si le nouveau bon s'insère AVANT un bon déjà existant           ──
        // ── (kilométrage du nouveau bon < kilométrage d'un bon existant),   ──
        // ── alors ce bon existant a désormais un nouveau "bon précédent"    ──
        // ── (le bon qu'on vient de créer) : il faut recalculer sa distance  ──
        // ── et sa consommation.                                             ──
        BonCarburant nextExisting = findNextBonOf(saved, adminId);
        if (nextExisting != null) {
            updatePreviousBon(nextExisting, adminId);
        }

        // ── Mise à jour kilométrage camion ────────────────────────────────────
        // On ne met à jour le kilométrage du camion que si le nouveau bon est
        // bien le plus récent (kilométrage le plus élevé) ; sinon on garde le
        // kilométrage du bon le plus élevé existant.
        bonCarburantDao.findAllByCamionIdAndAdminId(camion.getId(), adminId)
                .stream()
                .max(Comparator.comparingDouble(BonCarburant::getKilometrageActuel))
                .ifPresent(dernierBon -> {
                    camion.setMileage(dernierBon.getKilometrageActuel());
                    camionDao.save(camion);
                });

        stationService.recalculerTotaux(station.getId());

        return toResponse(bonCarburantDao.findById(saved.getId()).orElse(saved));
    }

    public List<BonCarburantResponse> getAll(Long adminId, Long camionId, Long stationId) {
        List<BonCarburant> bons;
        if (camionId != null) {
            bons = bonCarburantDao.findAllByCamionIdAndAdminId(camionId, adminId);
        } else if (stationId != null) {
            bons = bonCarburantDao.findAllByStationIdAndAdminId(stationId, adminId);
        } else {
            bons = bonCarburantDao.findAllByAdminId(adminId);
        }
        return bons.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BonCarburantResponse getById(Long id, Long adminId) {
        BonCarburant bon = bonCarburantDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de carburant non trouvé"));
        if (!bon.getAdminId().equals(adminId)) throw new RuntimeException("Accès refusé");
        return toResponse(bon);
    }

    @Transactional
    public BonCarburantResponse update(Long id, BonCarburantRequest request, Long adminId) {
        BonCarburant bon = bonCarburantDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de carburant non trouvé"));
        if (!bon.getAdminId().equals(adminId)) throw new RuntimeException("Accès refusé");

        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        Station station = stationDao.findByIdAndAdminId(request.getStationId(), adminId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée ou accès refusé"));

        // ── Validation type de carburant ──────────────────────────────────────
        if (camion.getFuelType() != null &&
                !camion.getFuelType().equals(request.getTypCarburant())) {
            throw new RuntimeException(
                    "Le type de carburant « " + request.getTypCarburant() +
                            " » ne correspond pas au type du camion « " + camion.getFuelType() + " »");
        }

        // ── Unicité numéro (excluant le bon courant) ──────────────────────────
        if (request.getNumero() != null && !request.getNumero().isBlank()) {
            if (bonCarburantDao.existsByNumeroAndCamionIdAndAdminIdAndIdNot(
                    request.getNumero(), request.getCamionId(), adminId, id)) {
                throw new RuntimeException(
                        "Le numéro de bon « " + request.getNumero() +
                                " » existe déjà pour ce camion.");
            }
        }

        Long oldStationId = bon.getStation().getId();
        Double oldKilometrage = bon.getKilometrageActuel();

        // ── Validation kilométrage : doit rester dans l'intervalle des bons voisins ──
        // On cherche les voisins du bon À SA POSITION ACTUELLE (avant modification),
        // en excluant le bon lui-même, pour définir la plage [precedent, suivant]
        // dans laquelle le nouveau kilométrage doit obligatoirement se situer.
        bonCarburantDao.findPreviousBon(camion.getId(), adminId, oldKilometrage, id)
                .ifPresent(prev -> {
                    if (request.getKilometrageActuel() <= prev.getKilometrageActuel()) {
                        throw new RuntimeException(
                                "Le kilométrage saisi (" + request.getKilometrageActuel() +
                                        " km) doit être supérieur au kilométrage du bon précédent « " +
                                        (prev.getNumero() != null ? prev.getNumero() : prev.getId()) +
                                        " » (" + prev.getKilometrageActuel() + " km)");
                    }
                });

        bonCarburantDao.findNextBon(camion.getId(), adminId, oldKilometrage, id)
                .ifPresent(next -> {
                    if (request.getKilometrageActuel() >= next.getKilometrageActuel()) {
                        throw new RuntimeException(
                                "Le kilométrage saisi (" + request.getKilometrageActuel() +
                                        " km) doit être inférieur au kilométrage du bon suivant « " +
                                        (next.getNumero() != null ? next.getNumero() : next.getId()) +
                                        " » (" + next.getKilometrageActuel() + " km)");
                    }
                });

        BigDecimal montantTotal = BigDecimal.valueOf(request.getQuantiteLitres())
                .multiply(request.getPrixLitre())
                .setScale(2, RoundingMode.HALF_UP);

        bon.setDate(request.getDate());
        bon.setCamion(camion);
        bon.setStation(station);
        bon.setKilometrageActuel(request.getKilometrageActuel());
        bon.setQuantiteLitres(request.getQuantiteLitres());
        bon.setTypCarburant(request.getTypCarburant());
        bon.setPrixLitre(request.getPrixLitre());
        bon.setNumero(request.getNumero());
        bon.setMontantTotal(montantTotal);
        bon.setDistanceParcourue(0.0);
        bon.setConsommationReelle(null);

        BonCarburant saved = bonCarburantDao.save(bon);

        // ── Recalculer le bon précédent du bon mis à jour ─────────────────────
        updatePreviousBon(saved, adminId);

        // ── Si kilométrage a changé, recalculer aussi l'ancien bon précédent ──
        if (!oldKilometrage.equals(request.getKilometrageActuel())) {
            bonCarburantDao.findPreviousBon(
                            camion.getId(), adminId, oldKilometrage, id)
                    .ifPresent(prev -> updatePreviousBon(findNextBonOf(prev, adminId), adminId));
        }

        // ── Recalculer le bon qui suit désormais le bon mis à jour (si besoin) ─
        BonCarburant nextExisting = findNextBonOf(saved, adminId);
        if (nextExisting != null) {
            updatePreviousBon(nextExisting, adminId);
        }

        // ── Mise à jour kilométrage camion ────────────────────────────────────
        // Recalculer depuis le bon le plus récent (kilométrage le plus élevé)
        bonCarburantDao.findAllByCamionIdAndAdminId(camion.getId(), adminId)
                .stream()
                .max(Comparator.comparingDouble(BonCarburant::getKilometrageActuel))
                .ifPresent(dernierBon -> {
                    camion.setMileage(dernierBon.getKilometrageActuel());
                    camionDao.save(camion);
                });

        stationService.recalculerTotaux(station.getId());
        if (!oldStationId.equals(station.getId())) {
            stationService.recalculerTotaux(oldStationId);
        }

        return toResponse(bonCarburantDao.findById(saved.getId()).orElse(saved));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        BonCarburant bon = bonCarburantDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de carburant non trouvé"));
        if (!bon.getAdminId().equals(adminId)) throw new RuntimeException("Accès refusé");

        Long stationId = bon.getStation().getId();
        Long camionId = bon.getCamion().getId();
        Double kilometrage = bon.getKilometrageActuel();
        Camion camion = bon.getCamion();

        bonCarburantDao.delete(bon);

        // ── Après suppression : recalculer le bon précédent du bon supprimé ───
        bonCarburantDao.findPreviousBon(camionId, adminId, kilometrage, null)
                .ifPresent(prev -> {
                    BonCarburant nextOfDeleted = bonCarburantDao
                            .findNextBon(camionId, adminId, kilometrage, null)
                            .orElse(null);
                    if (nextOfDeleted != null) {
                        updatePreviousBon(nextOfDeleted, adminId);
                    } else {
                        prev.setDistanceParcourue(0.0);
                        prev.setConsommationReelle(null);
                        bonCarburantDao.save(prev);
                    }
                });

        // ── Kilométrage camion : dernier bon restant ou null si aucun ─────────
        bonCarburantDao.findAllByCamionIdAndAdminId(camionId, adminId)
                .stream()
                .max(Comparator.comparingDouble(BonCarburant::getKilometrageActuel))
                .ifPresentOrElse(
                        dernierBon -> {
                            camion.setMileage(dernierBon.getKilometrageActuel());
                            camionDao.save(camion);
                        },
                        () -> {
                            camion.setMileage(null);   // null = aucun bon, pas 0
                            camionDao.save(camion);
                        }
                );

        stationService.recalculerTotaux(stationId);
    }

    // ── Réparation / recalcul des données existantes ────────────────────────────

    /**
     * Recalcule distanceParcourue + consommationReelle pour TOUS les bons
     * d'un camion, dans l'ordre croissant de kilométrage. À utiliser une fois
     * pour corriger des données historiques calculées avec l'ancienne logique
     * buggée (ex: bons insérés dans le désordre).
     *
     * Le premier bon (le plus petit kilométrage) reçoit toujours
     * distance = 0 et consommation = null, car il n'a pas de prédécesseur.
     */
    @Transactional
    public void recalculerHistoriqueCamion(Long camionId, Long adminId) {
        camionDao.findByIdAndAdminId(camionId, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        List<BonCarburant> bons = bonCarburantDao.findAllByCamionIdAndAdminId(camionId, adminId)
                .stream()
                .sorted(Comparator.comparingDouble(BonCarburant::getKilometrageActuel))
                .collect(Collectors.toList());

        BonCarburant prev = null;
        for (BonCarburant current : bons) {
            if (prev == null) {
                current.setDistanceParcourue(0.0);
                current.setConsommationReelle(null);
            } else {
                double distance = current.getKilometrageActuel() - prev.getKilometrageActuel();
                current.setDistanceParcourue(distance);
                current.setConsommationReelle(distance > 0
                        ? (current.getQuantiteLitres() / distance) * 100
                        : null);
            }
            bonCarburantDao.save(current);
            prev = current;
        }

        // Recaler le kilométrage du camion sur le bon le plus récent
        if (!bons.isEmpty()) {
            Camion camion = camionDao.findByIdAndAdminId(camionId, adminId).orElseThrow();
            camion.setMileage(bons.get(bons.size() - 1).getKilometrageActuel());
            camionDao.save(camion);
        }
    }

    /**
     * Recalcule l'historique de TOUS les camions d'un admin.
     * Pratique pour une réparation globale en une seule fois.
     */
    @Transactional
    public void recalculerHistoriqueTousCamions(Long adminId) {
        camionDao.findAllByAdminId(adminId)
                .forEach(camion -> recalculerHistoriqueCamion(camion.getId(), adminId));
    }

    // ── Stats par camion ──────────────────────────────────────────────────────

    public CamionFuelStatsResponse getCamionFuelStats(Long camionId, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(camionId, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        List<BonCarburant> bons = bonCarburantDao.findAllByCamionIdAndAdminId(camionId, adminId);

        if (bons.isEmpty()) {
            return CamionFuelStatsResponse.builder()
                    .matricule(camion.getMatricule())
                    .nomChauffeur(camion.getNomChauffeur())
                    .consommationMoyenne(null)
                    .consommationDernier(null)
                    .coutTotalCarburant(BigDecimal.ZERO)
                    .coutMensuelCarburant(BigDecimal.ZERO)
                    .nombreBons(0)
                    .statut("INSUFFISANT")
                    .message("Aucune donnée de carburant disponible")
                    .build();
        }

        List<Double> consommations = bons.stream()
                .map(BonCarburant::getConsommationReelle)
                .filter(c -> c != null)
                .collect(Collectors.toList());

        Double consommationMoyenne = consommations.isEmpty() ? null
                : consommations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        Double consommationDernier = bons.stream()
                .filter(b -> b.getConsommationReelle() != null)
                .max((b1, b2) -> {
                    int dc = b1.getDate().compareTo(b2.getDate());
                    return dc != 0 ? dc : b1.getKilometrageActuel().compareTo(b2.getKilometrageActuel());
                })
                .map(BonCarburant::getConsommationReelle)
                .orElse(null);

        BigDecimal coutTotal = bons.stream()
                .map(BonCarburant::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        BigDecimal coutMensuel = bons.stream()
                .filter(b -> !b.getDate().isBefore(startOfMonth))
                .map(BonCarburant::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String statut;
        String message;
        if (consommationMoyenne == null) {
            statut = "INSUFFISANT";
            message = "Données insuffisantes pour calculer la consommation";
        } else if (consommationMoyenne <= 42) {
            statut = "BONNE";
            message = String.format("Consommation excellente : %.1f L/100km", consommationMoyenne);
        } else if (consommationMoyenne <= 46) {
            statut = "MOYENNE";
            message = String.format("Consommation normale : %.1f L/100km", consommationMoyenne);
        } else {
            statut = "MAUVAISE";
            message = String.format("Consommation élevée : %.1f L/100km, vérifiez le véhicule", consommationMoyenne);
        }

        return CamionFuelStatsResponse.builder()
                .matricule(camion.getMatricule())
                .nomChauffeur(camion.getNomChauffeur())
                .consommationMoyenne(consommationMoyenne)
                .consommationDernier(consommationDernier)
                .coutTotalCarburant(coutTotal)
                .coutMensuelCarburant(coutMensuel)
                .nombreBons(bons.size())
                .statut(statut)
                .message(message)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privés
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcule la distance parcourue et la consommation réelle d'un bon (nextBon)
     * par rapport à SON bon précédent (kilométrage juste en dessous, même camion,
     * même admin).
     *
     * distance      = km(nextBon) - km(bon précédent)
     * consommation  = litres(nextBon) / distance * 100     (L/100km)
     *
     * Logique métier : le camion fait le plein à chaque bon. La consommation
     * associée à un bon représente donc la conso réalisée DEPUIS le plein
     * précédent JUSQU'À ce bon, avec la quantité de litres reversée à CE bon.
     */
    private void updatePreviousBon(BonCarburant nextBon, Long adminId) {
        if (nextBon == null) return;
        bonCarburantDao.findPreviousBon(
                        nextBon.getCamion().getId(),
                        adminId,
                        nextBon.getKilometrageActuel(),
                        nextBon.getId())
                .ifPresentOrElse(prev -> {
                    double distance = nextBon.getKilometrageActuel() - prev.getKilometrageActuel();
                    nextBon.setDistanceParcourue(distance);
                    if (distance > 0) {
                        nextBon.setConsommationReelle((nextBon.getQuantiteLitres() / distance) * 100);
                    } else {
                        nextBon.setConsommationReelle(null);
                    }
                    bonCarburantDao.save(nextBon);
                }, () -> {
                    // Aucun bon précédent (c'est le premier bon du camion)
                    nextBon.setDistanceParcourue(0.0);
                    nextBon.setConsommationReelle(null);
                    bonCarburantDao.save(nextBon);
                });
    }

    private BonCarburant findNextBonOf(BonCarburant bon, Long adminId) {
        if (bon == null) return null;
        return bonCarburantDao.findNextBon(
                        bon.getCamion().getId(),
                        adminId,
                        bon.getKilometrageActuel(),
                        bon.getId())
                .orElse(null);
    }

    private BonCarburantResponse toResponse(BonCarburant bon) {
        String statut;
        String message;
        if (bon.getConsommationReelle() == null) {
            statut = "INSUFFISANT";
            message = "Données insuffisantes pour calculer la consommation";
        } else {
            double c = bon.getConsommationReelle();
            if (c <= 42) {
                statut = "BONNE";
                message = String.format("Consommation excellente : %.1f L/100km", c);
            } else if (c <= 46) {
                statut = "MOYENNE";
                message = String.format("Consommation normale : %.1f L/100km", c);
            } else {
                statut = "MAUVAISE";
                message = String.format("Consommation élevée : %.1f L/100km, vérifiez le véhicule", c);
            }
        }

        return BonCarburantResponse.builder()
                .id(bon.getId())
                .date(bon.getDate())
                .camionId(bon.getCamion().getId())
                .camionMatricule(bon.getCamion().getMatricule())
                .stationId(bon.getStation().getId())
                .stationNom(bon.getStation().getNom())
                .kilometrageActuel(bon.getKilometrageActuel())
                .quantiteLitres(bon.getQuantiteLitres())
                .typCarburant(bon.getTypCarburant())
                .prixLitre(bon.getPrixLitre())
                .montantTotal(bon.getMontantTotal())
                .distanceParcourue(bon.getDistanceParcourue())
                .consommationReelle(bon.getConsommationReelle())
                .numero(bon.getNumero())
                .consommationStatut(statut)
                .consommationMessage(message)
                .build();
    }
}