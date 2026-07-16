package com.Transami.Transami.service;

import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.ChargeDao;
import com.Transami.Transami.dao.ChargeTemplateDao;
import com.Transami.Transami.dao.ChauffeurDao;
import com.Transami.Transami.dao.RemorqueDao;
import com.Transami.Transami.dto.ChargeRequest;
import com.Transami.Transami.dto.ChargeResponse;
import com.Transami.Transami.dto.ChargeStatsResponse;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Charge;
import com.Transami.Transami.entity.ChargeTemplate;
import com.Transami.Transami.entity.Remorque;
import com.Transami.Transami.enums.CategorieCharge;
import com.Transami.Transami.enums.StatutCharge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeService {

    private final ChargeDao chargeDao;
    private final ChargeTemplateDao chargeTemplateDao;
    private final CamionDao camionDao;
    private final ChauffeurDao chauffeurDao;
    private final RemorqueDao remorqueDao;

    @Transactional
    public ChargeResponse create(ChargeRequest request, Long adminId) {
        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(request.getTemplateId(), adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));

        StatutCharge statut = request.getStatut() != null ? request.getStatut() : StatutCharge.EN_ATTENTE;

        Charge charge = Charge.builder()
                .adminId(adminId)
                .template(template)
                .date(request.getDate())
                .montant(request.getMontant())
                .statut(statut)
                .notes(request.getNotes())
                .build();

        appliquerSnapshot(charge, template);

        return toResponse(chargeDao.save(charge));
    }

    @Transactional(readOnly = true)
    public List<ChargeResponse> getAll(Long adminId) {
        return chargeDao.findAllByAdminId(adminId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChargeResponse> getAllByTemplate(Long adminId, Long templateId) {
        return chargeDao.findAllByAdminIdAndTemplateId(adminId, templateId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChargeResponse> getAllByPeriode(Long adminId, LocalDate dateFrom, LocalDate dateTo) {
        return chargeDao.findAllByAdminIdAndDateBetween(adminId, dateFrom, dateTo).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChargeResponse> getAllByStatut(Long adminId, StatutCharge statut) {
        return chargeDao.findAllByAdminIdAndStatut(adminId, statut).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChargeResponse getById(Long id, Long adminId) {
        Charge charge = chargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Charge non trouvée ou accès refusé"));
        return toResponse(charge);
    }

    @Transactional
    public ChargeResponse update(Long id, ChargeRequest request, Long adminId) {
        Charge charge = chargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Charge non trouvée ou accès refusé"));

        ChargeTemplate template = chargeTemplateDao.findByIdAndAdminId(request.getTemplateId(), adminId)
                .orElseThrow(() -> new RuntimeException("Template non trouvé ou accès refusé"));

        charge.setTemplate(template);
        charge.setDate(request.getDate());
        charge.setMontant(request.getMontant());
        if (request.getStatut() != null) {
            charge.setStatut(request.getStatut());
        }
        charge.setNotes(request.getNotes());

        appliquerSnapshot(charge, template);

        return toResponse(chargeDao.save(charge));
    }

    @Transactional
    public ChargeResponse updateStatut(Long id, StatutCharge statut, Long adminId) {
        Charge charge = chargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Charge non trouvée ou accès refusé"));

        charge.setStatut(statut);
        return toResponse(chargeDao.save(charge));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        Charge charge = chargeDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Charge non trouvée ou accès refusé"));
        chargeDao.delete(charge);
    }

    @Transactional(readOnly = true)
    public ChargeStatsResponse getStats(Long adminId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        LocalDate startOfMonth = LocalDate.of(year, LocalDate.now().getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        BigDecimal totalAnnee = chargeDao.sumMontantByAdminIdAndDateBetween(adminId, startOfYear, endOfYear);
        BigDecimal totalMois = chargeDao.sumMontantByAdminIdAndDateBetween(adminId, startOfMonth, endOfMonth);

        List<Object[]> categoryData = chargeDao.sumByCategorie(adminId, year);
        Map<String, BigDecimal> totalParCategorie = new HashMap<>();
        for (Object[] row : categoryData) {
            CategorieCharge categorie = (CategorieCharge) row[0];
            BigDecimal montant = (BigDecimal) row[1];
            if (categorie != null) {
                totalParCategorie.put(categorie.name(), montant);
            }
        }

        return ChargeStatsResponse.builder()
                .totalAnnee(totalAnnee)
                .totalMois(totalMois)
                .totalParCategorie(totalParCategorie)
                .build();
    }

    /**
     * Garantit que toutes les charges liées à ce template ont bien leur snapshot
     * (nom chauffeur, matricule camion, matricule remorque, libellé...) rempli,
     * AVANT que le template soit détaché/supprimé.
     */
    @Transactional
    public void figerSnapshotPourTemplate(Long templateId) {
        ChargeTemplate template = chargeTemplateDao.findById(templateId).orElse(null);
        if (template == null) return;

        List<Charge> charges = chargeDao.findAllByTemplateId(templateId);
        for (Charge c : charges) {
            appliquerSnapshot(c, template);
        }
        chargeDao.saveAll(charges);
    }

    /**
     * Copie les infos du template (et du camion/chauffeur/remorque lié) directement sur la Charge,
     * afin qu'elle reste lisible même si le template (ou le camion/chauffeur/remorque) est supprimé plus tard.
     */
    private void appliquerSnapshot(Charge charge, ChargeTemplate template) {
        charge.setTemplateLibelle(template.getLibelle());
        charge.setTemplateType(template.getType());
        charge.setTemplateCategorie(template.getCategorie());

        Long camionId = template.getCamionId();
        String camionMatricule = null;
        if (camionId != null) {
            camionMatricule = camionDao.findById(camionId).map(Camion::getMatricule).orElse(null);
        }
        charge.setCamionId(camionId);
        charge.setCamionMatricule(camionMatricule);

        Long chauffeurId = template.getChauffeurId();
        String chauffeurNom = null;
        if (chauffeurId != null) {
            chauffeurNom = chauffeurDao.findById(chauffeurId)
                    .map(ch -> ch.getPrenom() + " " + ch.getNom())
                    .orElse(null);
        }
        charge.setChauffeurId(chauffeurId);
        charge.setChauffeurNom(chauffeurNom);

        Long remorqueId = template.getRemorqueId();
        String remorqueMatricule = null;
        if (remorqueId != null) {
            remorqueMatricule = remorqueDao.findById(remorqueId).map(Remorque::getMatricule).orElse(null);
        }
        charge.setRemorqueId(remorqueId);
        charge.setRemorqueMatricule(remorqueMatricule);
    }

    /**
     * Construit la réponse à partir du snapshot stocké sur la Charge (fonctionne même si le
     * template, le camion, le chauffeur ou la remorque ont été supprimés depuis).
     */
    private ChargeResponse toResponse(Charge c) {
        ChargeTemplate template = c.getTemplate();

        return ChargeResponse.builder()
                .id(c.getId())
                .adminId(c.getAdminId())
                .date(c.getDate())
                .montant(c.getMontant())
                .statut(c.getStatut())
                .notes(c.getNotes())
                .templateId(template != null ? template.getId() : null)
                .templateLibelle(c.getTemplateLibelle())
                .templateType(c.getTemplateType())
                .templateCategorie(c.getTemplateCategorie())
                .camionId(c.getCamionId())
                .camionMatricule(c.getCamionMatricule())
                .chauffeurId(c.getChauffeurId())
                .chauffeurNom(c.getChauffeurNom())
                .remorqueId(c.getRemorqueId())
                .remorqueMatricule(c.getRemorqueMatricule())
                .build();
    }
}