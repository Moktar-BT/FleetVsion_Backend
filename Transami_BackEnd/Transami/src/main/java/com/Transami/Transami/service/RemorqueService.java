package com.Transami.Transami.service;

import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.RemorqueDao;
import com.Transami.Transami.dto.RemorqueRequest;
import com.Transami.Transami.dto.RemorqueResponse;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Remorque;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RemorqueService {

    private final RemorqueDao remorqueDao;
    private final CamionDao camionDao;
    private final ChargeTemplateService chargeTemplateService;

    @Transactional
    public RemorqueResponse create(RemorqueRequest request, Long adminId) {
        if (remorqueDao.existsByMatriculeAndAdminId(request.getMatricule(), adminId)) {
            throw new RuntimeException("Une remorque avec ce matricule existe déjà");
        }

        if (request.getCamionId() != null) {
            if (!camionDao.existsByIdAndAdminId(request.getCamionId(), adminId)) {
                throw new RuntimeException("Camion non trouvé ou accès refusé");
            }
            // Un camion ne peut avoir qu'une seule remorque : on détache l'ancienne si elle existe
            detachExistingTrailer(request.getCamionId(), adminId, null);
        }

        Remorque remorque = Remorque.builder()
                .adminId(adminId)
                .matricule(request.getMatricule())
                .camionId(request.getCamionId())
                .typeRemorque(request.getTypeRemorque())
                .capaciteTonnes(request.getCapaciteTonnes())
                .dateAchat(request.getDateAchat())
                .build();

        return toResponse(remorqueDao.save(remorque));
    }

    @Transactional(readOnly = true)
    public List<RemorqueResponse> getAll(Long adminId, Long camionId) {
        List<Remorque> remorques = camionId != null
                ? remorqueDao.findAllByAdminIdAndCamionId(adminId, camionId)
                : remorqueDao.findAllByAdminId(adminId);
        return remorques.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RemorqueResponse getById(Long id, Long adminId) {
        Remorque remorque = remorqueDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Remorque non trouvée ou accès refusé"));
        return toResponse(remorque);
    }

    @Transactional
    public RemorqueResponse update(Long id, RemorqueRequest request, Long adminId) {
        Remorque remorque = remorqueDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Remorque non trouvée ou accès refusé"));

        if (!remorque.getMatricule().equals(request.getMatricule()) &&
                remorqueDao.existsByMatriculeAndAdminId(request.getMatricule(), adminId)) {
            throw new RuntimeException("Une remorque avec ce matricule existe déjà");
        }

        if (request.getCamionId() != null) {
            if (!camionDao.existsByIdAndAdminId(request.getCamionId(), adminId)) {
                throw new RuntimeException("Camion non trouvé ou accès refusé");
            }
            // Un camion ne peut avoir qu'une seule remorque : on détache l'ancienne si elle existe
            // (en excluant la remorque courante, au cas où camionId n'a pas changé)
            detachExistingTrailer(request.getCamionId(), adminId, id);
        }

        remorque.setMatricule(request.getMatricule());
        remorque.setCamionId(request.getCamionId());
        remorque.setTypeRemorque(request.getTypeRemorque());
        remorque.setCapaciteTonnes(request.getCapaciteTonnes());
        remorque.setDateAchat(request.getDateAchat());

        return toResponse(remorqueDao.save(remorque));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        Remorque remorque = remorqueDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Remorque non trouvée ou accès refusé"));

        // Supprimer les templates de charge liés à cette remorque + leurs rappels
        // (les charges déjà générées sont conservées avec leur snapshot pour la traçabilité)
        chargeTemplateService.deleteAllByRemorque(id, adminId);

        remorqueDao.delete(remorque);
    }

    /**
     * Garantit la relation 1 camion ↔ 1 remorque.
     * Si le camion donné a déjà une remorque assignée (différente de excludeRemorqueId),
     * on la détache (camionId = null).
     */
    private void detachExistingTrailer(Long camionId, Long adminId, Long excludeRemorqueId) {
        remorqueDao.findByCamionIdAndAdminId(camionId, adminId).ifPresent(existing -> {
            if (excludeRemorqueId == null || !existing.getId().equals(excludeRemorqueId)) {
                existing.setCamionId(null);
                remorqueDao.save(existing);
            }
        });
    }

    private RemorqueResponse toResponse(Remorque r) {
        String camionMatricule = null;
        if (r.getCamionId() != null) {
            camionMatricule = camionDao.findById(r.getCamionId())
                    .map(Camion::getMatricule)
                    .orElse(null);
        }

        return RemorqueResponse.builder()
                .id(r.getId())
                .adminId(r.getAdminId())
                .matricule(r.getMatricule())
                .camionId(r.getCamionId())
                .camionMatricule(camionMatricule)
                .typeRemorque(r.getTypeRemorque())
                .capaciteTonnes(r.getCapaciteTonnes())
                .dateAchat(r.getDateAchat())
                .active(r.isActive())
                .build();
    }
}