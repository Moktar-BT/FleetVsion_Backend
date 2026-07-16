package com.Transami.Transami.service;

import com.Transami.Transami.dao.*;
import com.Transami.Transami.dto.ChargeTemplateRequest;
import com.Transami.Transami.dto.ChargeTemplateResponse;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.ChargeTemplate;
import com.Transami.Transami.entity.Remorque;
import com.Transami.Transami.enums.TypeCharge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeTemplateService {

    private final ChargeTemplateDao chargeTemplateDao;
    private final CamionDao camionDao;
    private final ChauffeurDao chauffeurDao;
    private final RemorqueDao remorqueDao;
    private final ChargeDao chargeDao;
    private final RappelChargeDao rappelChargeDao;
    private final ChargeService chargeService;

    @Transactional
    public ChargeTemplateResponse create(ChargeTemplateRequest request, Long adminId) {
        if (chargeTemplateDao.existsByLibelleAndAdminId(request.getLibelle(), adminId)) {
            throw new RuntimeException("Un template avec ce libellé existe déjà");
        }

        ChargeTemplate template = ChargeTemplate.builder()
                .adminId(adminId)
                .libelle(request.getLibelle())
                .type(request.getType())
                .categorie(request.getCategorie())
                .montantReference(request.getMontantReference())
                .camionId(request.getCamionId())
                .chauffeurId(request.getChauffeurId())
                .remorqueId(request.getRemorqueId())
                .build();

        return toResponse(chargeTemplateDao.save(template));
    }

    @Transactional(readOnly = true)
    public List<ChargeTemplateResponse> getAll(Long adminId) {
        return chargeTemplateDao.findAllByAdminId(adminId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChargeTemplateResponse> getAllByType(Long adminId, TypeCharge type) {
        return chargeTemplateDao.findAllByAdminIdAndType(adminId, type).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChargeTemplateResponse getById(Long id, Long adminId) {
        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));
        return toResponse(template);
    }

    @Transactional
    public ChargeTemplateResponse update(Long id, ChargeTemplateRequest request, Long adminId) {
        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));

        if (!template.getLibelle().equals(request.getLibelle()) &&
                chargeTemplateDao.existsByLibelleAndAdminId(request.getLibelle(), adminId)) {
            throw new RuntimeException("Un template avec ce libellé existe déjà");
        }

        template.setLibelle(request.getLibelle());
        template.setType(request.getType());
        template.setCategorie(request.getCategorie());
        template.setMontantReference(request.getMontantReference());
        template.setCamionId(request.getCamionId());
        template.setChauffeurId(request.getChauffeurId());
        template.setRemorqueId(request.getRemorqueId());

        return toResponse(chargeTemplateDao.save(template));
    }

    @Transactional
    public ChargeTemplateResponse toggleActive(Long id, Long adminId) {
        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));

        template.setActive(!template.isActive());
        return toResponse(chargeTemplateDao.save(template));
    }

    /**
     * Suppression manuelle d'un seul template (depuis l'écran "Templates de charge").
     * Les rappels liés sont supprimés ; les charges déjà générées sont conservées (détachées du template),
     * avec leur snapshot figé.
     */
    @Transactional
    public void delete(Long id, Long adminId) {
        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));

        supprimerTemplateEtDependances(template);
    }

    /**
     * Appelé lors de la suppression d'un Camion : supprime tous les templates de charge liés
     * à ce camion, ainsi que leurs rappels. Les charges historiques sont conservées.
     */
    @Transactional
    public void deleteAllByCamion(Long camionId, Long adminId) {
        List<ChargeTemplate> templates = chargeTemplateDao.findAllByCamionIdAndAdminId(camionId, adminId);
        templates.forEach(this::supprimerTemplateEtDependances);
    }

    /**
     * Appelé lors de la suppression d'un Chauffeur : supprime tous les templates de charge liés
     * à ce chauffeur, ainsi que leurs rappels. Les charges historiques sont conservées.
     */
    @Transactional
    public void deleteAllByChauffeur(Long chauffeurId, Long adminId) {
        List<ChargeTemplate> templates = chargeTemplateDao.findAllByChauffeurIdAndAdminId(chauffeurId, adminId);
        templates.forEach(this::supprimerTemplateEtDependances);
    }

    /**
     * Appelé lors de la suppression d'une Remorque : supprime tous les templates de charge liés
     * à cette remorque, ainsi que leurs rappels. Les charges historiques sont conservées.
     */
    @Transactional
    public void deleteAllByRemorque(Long remorqueId, Long adminId) {
        List<ChargeTemplate> templates = chargeTemplateDao.findAllByRemorqueIdAndAdminId(remorqueId, adminId);
        templates.forEach(this::supprimerTemplateEtDependances);
    }

    /**
     * Supprime un template :
     *  1) fige le snapshot (libellé, type, catégorie, matricule camion, nom chauffeur,
     *     matricule remorque) sur TOUTES les charges liées, pour garantir la traçabilité,
     *  2) supprime les rappels liés,
     *  3) détache les charges du template (sans les supprimer),
     *  4) supprime le template.
     */
    private void supprimerTemplateEtDependances(ChargeTemplate template) {
        chargeService.figerSnapshotPourTemplate(template.getId());
        rappelChargeDao.deleteAllByTemplateId(template.getId());
        chargeDao.detachTemplateById(template.getId());
        chargeTemplateDao.delete(template);
    }

    private ChargeTemplateResponse toResponse(ChargeTemplate t) {
        String camionMatricule = null;
        if (t.getCamionId() != null) {
            camionMatricule = camionDao.findById(t.getCamionId())
                    .map(Camion::getMatricule)
                    .orElse(null);
        }

        String chauffeurNom = null;
        if (t.getChauffeurId() != null) {
            chauffeurNom = chauffeurDao.findById(t.getChauffeurId())
                    .map(c -> c.getPrenom() + " " + c.getNom())
                    .orElse(null);
        }

        String remorqueMatricule = null;
        if (t.getRemorqueId() != null) {
            remorqueMatricule = remorqueDao.findById(t.getRemorqueId())
                    .map(Remorque::getMatricule)
                    .orElse(null);
        }

        return ChargeTemplateResponse.builder()
                .id(t.getId())
                .adminId(t.getAdminId())
                .libelle(t.getLibelle())
                .type(t.getType())
                .categorie(t.getCategorie())
                .montantReference(t.getMontantReference())
                .camionId(t.getCamionId())
                .camionMatricule(camionMatricule)
                .chauffeurId(t.getChauffeurId())
                .chauffeurNom(chauffeurNom)
                .remorqueId(t.getRemorqueId())
                .remorqueMatricule(remorqueMatricule)
                .active(t.isActive())
                .build();
    }
}