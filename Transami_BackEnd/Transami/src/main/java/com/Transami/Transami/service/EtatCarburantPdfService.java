package com.Transami.Transami.service;

import com.Transami.Transami.dao.BonCarburantDao;
import com.Transami.Transami.dto.AdminProfileDto;
import com.Transami.Transami.dto.EtatCarburantRequest;
import com.Transami.Transami.entity.BonCarburant;
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
public class EtatCarburantPdfService {

    private final BonCarburantDao bonCarburantDao;
    private final AdminService    adminService;
    private final TemplateEngine  templateEngine;
    private final AuthUtil        authUtil;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePdf(EtatCarburantRequest request) throws Exception {
        AdminProfileDto profile = adminService.getMyProfile();
        return generatePdfTx(request, profile);
    }

    @Transactional(readOnly = true)
    protected byte[] generatePdfTx(EtatCarburantRequest request, AdminProfileDto profile) throws Exception {
        Long adminId = authUtil.getCurrentAdminId();

        // Fetch all bons for this admin in the date range
        List<BonCarburant> bons = bonCarburantDao.findAllByAdminId(adminId).stream()
                .filter(b -> !b.getDate().isBefore(request.getDateFrom())
                        && !b.getDate().isAfter(request.getDateTo()))
                .collect(Collectors.toList());

        // Apply optional filters
        if (request.getStationId() != null) {
            bons = bons.stream()
                    .filter(b -> b.getStation().getId().equals(request.getStationId()))
                    .collect(Collectors.toList());
        }
        if (request.getCamionId() != null) {
            bons = bons.stream()
                    .filter(b -> b.getCamion().getId().equals(request.getCamionId()))
                    .collect(Collectors.toList());
        }

        // Sort by date then kilometrage
        bons.sort(Comparator.comparing(BonCarburant::getDate)
                .thenComparing(BonCarburant::getKilometrageActuel));

        // Build rows
        List<Map<String, Object>> lignes = bons.stream()
                .map(this::buildLigne)
                .collect(Collectors.toList());

        // Totals
        BigDecimal totalQuantite = bons.stream()
                .map(b -> BigDecimal.valueOf(b.getQuantiteLitres()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMontant = bons.stream()
                .map(b -> b.getMontantTotal() != null ? b.getMontantTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OptionalDouble avgConso = bons.stream()
                .filter(b -> b.getConsommationReelle() != null)
                .mapToDouble(BonCarburant::getConsommationReelle)
                .average();

        // Filter labels for header
        String stationNom = (request.getStationId() != null && !bons.isEmpty())
                ? bons.get(0).getStation().getNom() : null;
        String camionMatricule = (request.getCamionId() != null && !bons.isEmpty())
                ? bons.get(0).getCamion().getMatricule() : null;

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

        ctx.setVariable("stationNom",      stationNom);
        ctx.setVariable("camionMatricule", camionMatricule);

        ctx.setVariable("lignes",              lignes);
        ctx.setVariable("nombreBons",          bons.size());
        ctx.setVariable("totalQuantite",       fmt(totalQuantite));
        ctx.setVariable("totalMontant",        fmt(totalMontant));
        ctx.setVariable("consommationMoyenne", avgConso.isPresent()
                ? String.format("%.1f", avgConso.getAsDouble()).replace(".", ",") : "—");

        return render("EtatCarburant", ctx);
    }

    private Map<String, Object> buildLigne(BonCarburant b) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("numero",          b.getNumero() != null ? b.getNumero() : String.valueOf(b.getId()));
        row.put("date",            b.getDate().format(DATE_FMT));
        row.put("camionMatricule", b.getCamion().getMatricule());
        row.put("stationNom",      b.getStation().getNom());
        row.put("typCarburant",    b.getTypCarburant().name().replace("_", " "));
        row.put("kilometrage",     String.format("%,.0f", b.getKilometrageActuel()).replace(",", " "));
        row.put("quantite",        fmt(BigDecimal.valueOf(b.getQuantiteLitres())));
        row.put("prixLitre",       fmt(b.getPrixLitre()));
        row.put("montantTotal",    fmt(b.getMontantTotal() != null ? b.getMontantTotal() : BigDecimal.ZERO));
        row.put("consommation",    b.getConsommationReelle() != null
                ? String.format("%.1f", b.getConsommationReelle()).replace(".", ",") : "—");
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