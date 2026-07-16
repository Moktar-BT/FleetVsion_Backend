package com.Transami.Transami.service;

import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.ChargeDao;
import com.Transami.Transami.dao.ChauffeurDao;
import com.Transami.Transami.dao.RemorqueDao;
import com.Transami.Transami.dto.AdminProfileDto;
import com.Transami.Transami.dto.EtatChargeRequest;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Charge;
import com.Transami.Transami.entity.ChargeTemplate;
import com.Transami.Transami.entity.Chauffeur;
import com.Transami.Transami.entity.Remorque;
import com.Transami.Transami.enums.StatutCharge;
import com.Transami.Transami.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtatChargePdfService {

    private final ChargeDao      chargeDao;
    private final CamionDao      camionDao;
    private final ChauffeurDao   chauffeurDao;
    private final RemorqueDao    remorqueDao;
    private final AdminService   adminService;
    private final TemplateEngine templateEngine;
    private final AuthUtil       authUtil;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String OTHER_VALUE = "__OTHER__"; // doit correspondre au frontend

    public byte[] generatePdf(EtatChargeRequest request) throws Exception {
        AdminProfileDto profile = adminService.getMyProfile();
        return generatePdfTx(request, profile);
    }

    @Transactional(readOnly = true)
    protected byte[] generatePdfTx(EtatChargeRequest request, AdminProfileDto profile) throws Exception {
        Long adminId = authUtil.getCurrentAdminId();

        List<Charge> charges = chargeDao.findAllByAdminIdAndDateBetween(
                adminId, request.getDateFrom(), request.getDateTo());

        if (request.getStatut() != null) {
            charges = charges.stream()
                    .filter(c -> c.getStatut() == request.getStatut())
                    .collect(Collectors.toList());
        }

        if (request.getType() != null) {
            charges = charges.stream()
                    .filter(c -> c.getTemplate().getType() == request.getType())
                    .collect(Collectors.toList());
        }

        // Filtre Camion — gère aussi le cas "sans camion associé"
        if (request.getCamionMatricule() != null && !request.getCamionMatricule().isBlank()) {
            if (OTHER_VALUE.equals(request.getCamionMatricule())) {
                charges = charges.stream()
                        .filter(c -> resolveCamionMatricule(c.getTemplate()) == null)
                        .collect(Collectors.toList());
            } else {
                charges = charges.stream()
                        .filter(c -> request.getCamionMatricule().equals(resolveCamionMatricule(c.getTemplate())))
                        .collect(Collectors.toList());
            }
        }

        // Filtre Chauffeur — gère aussi le cas "sans chauffeur associé"
        if (request.getChauffeurNom() != null && !request.getChauffeurNom().isBlank()) {
            if (OTHER_VALUE.equals(request.getChauffeurNom())) {
                charges = charges.stream()
                        .filter(c -> resolveChauffeurNom(c.getTemplate()) == null)
                        .collect(Collectors.toList());
            } else {
                charges = charges.stream()
                        .filter(c -> request.getChauffeurNom().equals(resolveChauffeurNom(c.getTemplate())))
                        .collect(Collectors.toList());
            }
        }

        // Filtre Remorque — gère aussi le cas "sans remorque associée"
        if (request.getRemorqueMatricule() != null && !request.getRemorqueMatricule().isBlank()) {
            if (OTHER_VALUE.equals(request.getRemorqueMatricule())) {
                charges = charges.stream()
                        .filter(c -> resolveRemorqueMatricule(c.getTemplate()) == null)
                        .collect(Collectors.toList());
            } else {
                charges = charges.stream()
                        .filter(c -> request.getRemorqueMatricule().equals(resolveRemorqueMatricule(c.getTemplate())))
                        .collect(Collectors.toList());
            }
        }

        charges.sort(Comparator.comparing(Charge::getDate)
                .thenComparing(c -> c.getTemplate().getLibelle()));

        List<Map<String, Object>> lignes = charges.stream()
                .map(this::buildLigne)
                .collect(Collectors.toList());

        BigDecimal totalMontant = charges.stream()
                .map(c -> c.getMontant() != null ? c.getMontant() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombrePayees = charges.stream().filter(c -> c.getStatut() == StatutCharge.PAYEE).count();
        long nombreEnAttente = charges.stream().filter(c -> c.getStatut() == StatutCharge.EN_ATTENTE).count();

        Context ctx = new Context();
        ctx.setVariable("companyName",    profile.getNomEntreprise());
        ctx.setVariable("companyAddress", profile.getAdresse());
        ctx.setVariable("companyPhone",   formatTelephones(profile.getTelephones()));
        ctx.setVariable("companyEmail",   profile.getEmail());
        ctx.setVariable("companyTva",     profile.getMatriculeFiscale() != null ? profile.getMatriculeFiscale() : "");
        ctx.setVariable("companyLogo",    logoToBase64(profile.getCheminLogoEntreprise()));

        ctx.setVariable("dateFrom",    request.getDateFrom().format(DATE_FMT));
        ctx.setVariable("dateTo",      request.getDateTo().format(DATE_FMT));
        ctx.setVariable("dateEdition", LocalDate.now().format(DATE_FMT));

        // Libellés des filtres actifs — traduits proprement pour le sentinel "Autre"
        ctx.setVariable("statutFiltre",    request.getStatut() != null ? libelleStatut(request.getStatut()) : null);
        ctx.setVariable("typeFiltre",      request.getType() != null ? request.getType().name() : null);
        ctx.setVariable("camionFiltre",    resolveFiltreLabel(request.getCamionMatricule(), "Sans camion"));
        ctx.setVariable("chauffeurFiltre", resolveFiltreLabel(request.getChauffeurNom(), "Sans chauffeur"));
        ctx.setVariable("remorqueFiltre",  resolveFiltreLabel(request.getRemorqueMatricule(), "Sans remorque"));

        ctx.setVariable("lignes",          lignes);
        ctx.setVariable("nombreCharges",   charges.size());
        ctx.setVariable("nombrePayees",    nombrePayees);
        ctx.setVariable("nombreEnAttente", nombreEnAttente);
        ctx.setVariable("totalMontant",    fmt(totalMontant));

        return render("EtatCharge", ctx);
    }

    private String resolveFiltreLabel(String value, String otherLabel) {
        if (value == null || value.isBlank()) return null;
        return OTHER_VALUE.equals(value) ? otherLabel : value;
    }

    private String resolveCamionMatricule(ChargeTemplate t) {
        if (t.getCamionId() == null) return null;
        return camionDao.findById(t.getCamionId()).map(Camion::getMatricule).orElse(null);
    }

    private String resolveChauffeurNom(ChargeTemplate t) {
        if (t.getChauffeurId() == null) return null;
        return chauffeurDao.findById(t.getChauffeurId())
                .map(ch -> ch.getPrenom() + " " + ch.getNom())
                .orElse(null);
    }

    private String resolveRemorqueMatricule(ChargeTemplate t) {
        if (t.getRemorqueId() == null) return null;
        return remorqueDao.findById(t.getRemorqueId()).map(Remorque::getMatricule).orElse(null);
    }

    private String libelleStatut(StatutCharge statut) {
        return statut == StatutCharge.PAYEE ? "Payée" : "En attente";
    }

    private Map<String, Object> buildLigne(Charge c) {
        ChargeTemplate template = c.getTemplate();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("date",      c.getDate().format(DATE_FMT));
        row.put("libelle",   template.getLibelle());
        row.put("categorie", template.getCategorie().name().replace("_", " "));
        row.put("type",      template.getType().name());
        row.put("camion",    Optional.ofNullable(resolveCamionMatricule(template)).orElse("—"));
        row.put("remorque",  Optional.ofNullable(resolveRemorqueMatricule(template)).orElse("—"));
        row.put("chauffeur", Optional.ofNullable(resolveChauffeurNom(template)).orElse("—"));
        row.put("montant",   fmt(c.getMontant() != null ? c.getMontant() : BigDecimal.ZERO));
        row.put("statut",    c.getStatut() == StatutCharge.PAYEE ? "Payée" : "En attente");
        row.put("estPayee",  c.getStatut() == StatutCharge.PAYEE);
        row.put("notes",     c.getNotes() != null ? c.getNotes() : "");
        return row;
    }

    private byte[] render(String templateName, Context ctx) throws Exception {
        String html = templateEngine.process(templateName, ctx);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    private String formatTelephones(List<String> telephones) {
        if (telephones == null || telephones.isEmpty()) return "";
        return String.join(" / ", telephones);
    }

    private String logoToBase64(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) return null;
        try (InputStream in = new URL(logoUrl).openStream()) {
            byte[] bytes = in.readAllBytes();
            String mime = logoUrl.toLowerCase().endsWith(".png") ? "image/png"
                    : logoUrl.toLowerCase().endsWith(".webp") ? "image/webp"
                    : "image/jpeg";
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static String fmt(BigDecimal v) {
        if (v == null) return "0,000";
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setGroupingSeparator('\u00A0');
        sym.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.000", sym).format(v.setScale(3, RoundingMode.HALF_UP));
    }
}