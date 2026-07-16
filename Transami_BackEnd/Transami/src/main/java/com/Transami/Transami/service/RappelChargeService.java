package com.Transami.Transami.service;

import com.Transami.Transami.dao.*;
import com.Transami.Transami.dto.ChargeAlerteSummary;
import com.Transami.Transami.dto.RappelChargeRequest;
import com.Transami.Transami.dto.RappelChargeResponse;
import com.Transami.Transami.entity.*;
import com.Transami.Transami.enums.FrequenceRappel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RappelChargeService {

    private final RappelChargeDao rappelChargeDao;
    private final ChargeTemplateDao chargeTemplateDao;
    private final CamionDao camionDao;
    private final ChauffeurDao chauffeurDao;

    @Transactional
    public RappelChargeResponse create(RappelChargeRequest request, Long adminId) {
        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(request.getTemplateId(), adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));

        Integer joursAvant = request.getJoursAvant() != null ? request.getJoursAvant() : 15;

        RappelCharge rappel = RappelCharge.builder()
                .adminId(adminId)
                .template(template)
                .frequence(request.getFrequence())
                .prochaineDate(request.getProchaineDate())
                .joursAvant(joursAvant)
                .build();

        return toResponse(rappelChargeDao.save(rappel));
    }

    @Transactional(readOnly = true)
    public List<RappelChargeResponse> getAll(Long adminId) {
        return rappelChargeDao.findAllByAdminId(adminId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RappelChargeResponse getById(Long id, Long adminId) {
        RappelCharge rappel = rappelChargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));
        return toResponse(rappel);
    }

    @Transactional
    public RappelChargeResponse update(Long id, RappelChargeRequest request, Long adminId) {
        RappelCharge rappel = rappelChargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));

        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(request.getTemplateId(), adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));

        rappel.setTemplate(template);
        rappel.setFrequence(request.getFrequence());
        rappel.setProchaineDate(request.getProchaineDate());
        if (request.getJoursAvant() != null) {
            rappel.setJoursAvant(request.getJoursAvant());
        }

        return toResponse(rappelChargeDao.save(rappel));
    }

    @Transactional
    public RappelChargeResponse toggleActif(Long id, boolean actif, Long adminId) {
        RappelCharge rappel = rappelChargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));

        rappel.setActif(actif);
        return toResponse(rappelChargeDao.save(rappel));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        RappelCharge rappel = rappelChargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));
        rappelChargeDao.delete(rappel);
    }

    @Transactional(readOnly = true)
    public List<ChargeAlerteSummary> getAlertes(Long adminId) {
        List<RappelCharge> rappels = rappelChargeDao.findRappelsEnAlerte(adminId);
        return rappels.stream().map(this::toAlerteSummary).collect(Collectors.toList());
    }

    @Transactional
    public RappelChargeResponse avancerProchainRappel(Long id, Long adminId) {
        RappelCharge rappel = rappelChargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Rappel non trouvé ou accès refusé"));

        LocalDate nouvelleDateRef = rappel.getProchaineDate();

        switch (rappel.getFrequence()) {
            case MENSUEL -> nouvelleDateRef = nouvelleDateRef.plusMonths(1);
            case TRIMESTRIEL -> nouvelleDateRef = nouvelleDateRef.plusMonths(3);
            case SEMESTRIEL -> nouvelleDateRef = nouvelleDateRef.plusMonths(6);
            case ANNUEL -> nouvelleDateRef = nouvelleDateRef.plusYears(1);
        }

        rappel.setProchaineDate(nouvelleDateRef);
        return toResponse(rappelChargeDao.save(rappel));
    }

    private RappelChargeResponse toResponse(RappelCharge r) {
        ChargeTemplate template = r.getTemplate();

        String camionMatricule = null;
        if (template.getCamionId() != null) {
            camionMatricule = camionDao.findById(template.getCamionId())
                    .map(Camion::getMatricule)
                    .orElse(null);
        }

        String chauffeurNom = null;
        if (template.getChauffeurId() != null) {
            chauffeurNom = chauffeurDao.findById(template.getChauffeurId())
                    .map(c -> c.getPrenom() + " " + c.getNom())
                    .orElse(null);
        }

        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), r.getProchaineDate());
        String statut;
        if (joursRestants < 0) {
            statut = "DEPASSE";
        } else if (joursRestants <= r.getJoursAvant()) {
            statut = "PROCHE";
        } else {
            statut = "OK";
        }

        return RappelChargeResponse.builder()
                .id(r.getId())
                .adminId(r.getAdminId())
                .frequence(r.getFrequence())
                .prochaineDate(r.getProchaineDate())
                .joursAvant(r.getJoursAvant())
                .actif(r.isActif())
                .templateId(template.getId())
                .templateLibelle(template.getLibelle())
                .templateCategorie(template.getCategorie())
                .templateType(template.getType())
                .montantReference(template.getMontantReference())
                .camionMatricule(camionMatricule)
                .chauffeurNom(chauffeurNom)
                .joursRestants(joursRestants)
                .statut(statut)
                .build();
    }

    private ChargeAlerteSummary toAlerteSummary(RappelCharge r) {
        ChargeTemplate template = r.getTemplate();

        String camionMatricule = null;
        if (template.getCamionId() != null) {
            camionMatricule = camionDao.findById(template.getCamionId())
                    .map(Camion::getMatricule)
                    .orElse(null);
        }

        String chauffeurNom = null;
        if (template.getChauffeurId() != null) {
            chauffeurNom = chauffeurDao.findById(template.getChauffeurId())
                    .map(c -> c.getPrenom() + " " + c.getNom())
                    .orElse(null);
        }

        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), r.getProchaineDate());
        String statut = joursRestants < 0 ? "DEPASSE" : "PROCHE";

        return ChargeAlerteSummary.builder()
                .rappelId(r.getId())
                .templateLibelle(template.getLibelle())
                .templateCategorie(template.getCategorie())
                .prochaineDate(r.getProchaineDate())
                .joursRestants(joursRestants)
                .montantReference(template.getMontantReference())
                .camionMatricule(camionMatricule)
                .chauffeurNom(chauffeurNom)
                .statut(statut)
                .build();
    }
}
