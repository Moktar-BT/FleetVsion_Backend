package com.Transami.Transami.service;

import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.RappelVidangeDao;
import com.Transami.Transami.dto.RappelVidangeAlerteSummary;
import com.Transami.Transami.dto.RappelVidangeRequest;
import com.Transami.Transami.dto.RappelVidangeResponse;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.RappelVidange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RappelVidangeService {

    private final RappelVidangeDao rappelVidangeDao;
    private final CamionDao camionDao;

    /**
     * Seuil d'alerte "PROCHE" en km.
     * Si le camion est à moins de 500 km de la prochaine vidange → alerte orange.
     */
    private static final double SEUIL_ALERTE_KM = 500.0;

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public RappelVidangeResponse create(RappelVidangeRequest request, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        // Un seul rappel actif par camion
        if (rappelVidangeDao.existsByCamionIdAndAdminId(request.getCamionId(), adminId)) {
            throw new RuntimeException(
                    "Un rappel de vidange existe déjà pour le camion " + camion.getMatricule()
                            + ". Modifiez-le ou supprimez-le avant d'en créer un nouveau.");
        }

        RappelVidange rappel = RappelVidange.builder()
                .adminId(adminId)
                .camion(camion)
                .kmDerniereVidange(request.getKmDerniereVidange())
                .intervalleKm(request.getIntervalleKm())
                .dateDerniereVidange(request.getDateDerniereVidange())
                .notes(request.getNotes())
                .actif(true)
                .build();

        // @PrePersist calcule kmProchaineVidange automatiquement
        return toResponse(rappelVidangeDao.save(rappel));
    }

    public List<RappelVidangeResponse> getAll(Long adminId) {
        return rappelVidangeDao.findAllByAdminId(adminId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RappelVidangeResponse getById(Long id, Long adminId) {
        RappelVidange r = rappelVidangeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));
        return toResponse(r);
    }

    @Transactional
    public RappelVidangeResponse update(Long id, RappelVidangeRequest request, Long adminId) {
        RappelVidange rappel = rappelVidangeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));

        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        // Si le camion change, vérifier qu'il n'a pas déjà un rappel
        if (!rappel.getCamion().getId().equals(request.getCamionId())
                && rappelVidangeDao.existsByCamionIdAndAdminId(request.getCamionId(), adminId)) {
            throw new RuntimeException(
                    "Un rappel de vidange existe déjà pour le camion " + camion.getMatricule());
        }

        rappel.setCamion(camion);
        rappel.setKmDerniereVidange(request.getKmDerniereVidange());
        rappel.setIntervalleKm(request.getIntervalleKm());
        rappel.setDateDerniereVidange(request.getDateDerniereVidange());
        rappel.setNotes(request.getNotes());
        // @PreUpdate recalcule kmProchaineVidange

        return toResponse(rappelVidangeDao.save(rappel));
    }

    /**
     * Réinitialise le rappel après une vidange effectuée.
     * L'admin saisit le nouveau kmDerniereVidange (= kilométrage actuel du camion).
     */
    @Transactional
    public RappelVidangeResponse reinitialiser(Long id, RappelVidangeRequest request, Long adminId) {
        RappelVidange rappel = rappelVidangeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));

        rappel.setKmDerniereVidange(request.getKmDerniereVidange());
        // L'intervalle peut être modifié aussi (ex: changement de type d'huile)
        if (request.getIntervalleKm() != null) {
            rappel.setIntervalleKm(request.getIntervalleKm());
        }
        if (request.getDateDerniereVidange() != null) {
            rappel.setDateDerniereVidange(request.getDateDerniereVidange());
        }
        if (request.getNotes() != null) {
            rappel.setNotes(request.getNotes());
        }
        // @PreUpdate recalcule kmProchaineVidange

        return toResponse(rappelVidangeDao.save(rappel));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        RappelVidange rappel = rappelVidangeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));
        rappelVidangeDao.delete(rappel);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Alertes dashboard
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne la liste des rappels en alerte (dépassés ou proches du seuil).
     * Endpoint léger utilisé par le badge dashboard.
     */
    public List<RappelVidangeAlerteSummary> getAlertes(Long adminId) {
        return rappelVidangeDao
                .findRappelsAlertes(adminId, SEUIL_ALERTE_KM)
                .stream()
                .map(this::toAlerteSummary)
                .collect(Collectors.toList());
    }

    /**
     * Vérifie les alertes pour un camion spécifique.
     * Appelé depuis BonCarburantService après chaque nouveau bon.
     */
    public void verifierAlertesCamion(Long camionId, Long adminId) {
        // Pour l'instant on ne fait rien de bloquant — le frontend poll /alertes.
        // On pourrait ici envoyer un email ou une notification push si besoin.
        rappelVidangeDao.findByCamionIdAndAdminId(camionId, adminId)
                .ifPresent(rappel -> {
                    if (rappel.getCamion().getMileage() != null
                            && rappel.getCamion().getMileage() >= rappel.getKmProchaineVidange()) {
                        // Log ou notification future
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mappers privés
    // ─────────────────────────────────────────────────────────────────────────

    private RappelVidangeResponse toResponse(RappelVidange r) {
        Double kmActuel = r.getCamion().getMileage();
        Double kmRestants = null;
        Double pourcentage = null;
        String statut = "INCONNU";

        if (kmActuel != null) {
            kmRestants = r.getKmProchaineVidange() - kmActuel;
            double parcourus = kmActuel - r.getKmDerniereVidange();
            pourcentage = Math.min((parcourus / r.getIntervalleKm()) * 100, 150); // cap à 150%

            if (kmActuel >= r.getKmProchaineVidange()) {
                statut = "DEPASSEE";
            } else if (kmRestants <= SEUIL_ALERTE_KM) {
                statut = "PROCHE";
            } else {
                statut = "OK";
            }
        }

        return RappelVidangeResponse.builder()
                .id(r.getId())
                .camionId(r.getCamion().getId())
                .camionMatricule(r.getCamion().getMatricule())
                .camionModele(r.getCamion().getTruckModel())
                .kmActuel(kmActuel)
                .kmDerniereVidange(r.getKmDerniereVidange())
                .intervalleKm(r.getIntervalleKm())
                .kmProchaineVidange(r.getKmProchaineVidange())
                .dateDerniereVidange(r.getDateDerniereVidange())
                .notes(r.getNotes())
                .actif(r.isActif())
                .statut(statut)
                .kmRestants(kmRestants)
                .pourcentageAvancement(pourcentage)
                .build();
    }

    private RappelVidangeAlerteSummary toAlerteSummary(RappelVidange r) {
        Double kmActuel = r.getCamion().getMileage();
        Double kmRestants = kmActuel != null ? r.getKmProchaineVidange() - kmActuel : null;
        String statut = kmActuel != null && kmActuel >= r.getKmProchaineVidange()
                ? "DEPASSEE" : "PROCHE";

        return RappelVidangeAlerteSummary.builder()
                .rappelId(r.getId())
                .camionId(r.getCamion().getId())
                .camionMatricule(r.getCamion().getMatricule())
                .kmActuel(kmActuel)
                .kmProchaineVidange(r.getKmProchaineVidange())
                .kmRestants(kmRestants)
                .statut(statut)
                .build();
    }
}