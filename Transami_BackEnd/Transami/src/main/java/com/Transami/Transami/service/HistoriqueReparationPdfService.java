package com.Transami.Transami.service;

import com.Transami.Transami.dao.ReparationDao;
import com.Transami.Transami.dto.AdminProfileDto;
import com.Transami.Transami.dto.HistoriqueReparationRequest;
import com.Transami.Transami.entity.Reparation;
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
public class HistoriqueReparationPdfService {

    private final ReparationDao  reparationDao;
    private final AdminService   adminService;
    private final TemplateEngine templateEngine;
    private final AuthUtil       authUtil;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePdf(HistoriqueReparationRequest request) throws Exception {
        AdminProfileDto profile = adminService.getMyProfile();
        return generatePdfTx(request, profile);
    }

    @Transactional(readOnly = true)
    protected byte[] generatePdfTx(HistoriqueReparationRequest request,
                                   AdminProfileDto profile) throws Exception {

        Long adminId = authUtil.getCurrentAdminId();

        // ── Fetch & filter ────────────────────────────────────────────────
        List<Reparation> reparations = reparationDao
                .findAllByAdminIdAndDateBetween(adminId, request.getDateFrom(), request.getDateTo());

        if (request.getCamionId() != null) {
            reparations = reparations.stream()
                    .filter(r -> r.getCamion().getId().equals(request.getCamionId()))
                    .collect(Collectors.toList());
        }

        // ── Sort by date then truck ───────────────────────────────────────
        reparations.sort(Comparator.comparing(Reparation::getDate)
                .thenComparing(r -> r.getCamion().getMatricule()));

        // ── Build rows ────────────────────────────────────────────────────
        List<Map<String, Object>> lignes = reparations.stream()
                .map(this::buildLigne)
                .collect(Collectors.toList());

        // ── Totals ────────────────────────────────────────────────────────
        BigDecimal totalCout = reparations.stream()
                .map(Reparation::getCout)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Filter label for header
        String camionMatricule = (request.getCamionId() != null && !reparations.isEmpty())
                ? reparations.get(0).getCamion().getMatricule()
                : null;

        // ── Thymeleaf context ─────────────────────────────────────────────
        Context ctx = new Context();

        ctx.setVariable("companyName",    profile.getNomEntreprise());
        ctx.setVariable("companyAddress", profile.getAdresse());
        ctx.setVariable("companyPhone",   formatTelephones(profile.getTelephones()));
        ctx.setVariable("companyEmail",   profile.getEmail());
        ctx.setVariable("companyTva",     profile.getMatriculeFiscale() != null
                ? profile.getMatriculeFiscale() : "");
        ctx.setVariable("companyLogo",    logoToBase64(profile.getCheminLogoEntreprise()));

        ctx.setVariable("dateFrom",        request.getDateFrom().format(DATE_FMT));
        ctx.setVariable("dateTo",          request.getDateTo().format(DATE_FMT));
        ctx.setVariable("dateEdition",     LocalDate.now().format(DATE_FMT));
        ctx.setVariable("camionMatricule", camionMatricule);

        ctx.setVariable("lignes",              lignes);
        ctx.setVariable("nombreReparations",   reparations.size());
        ctx.setVariable("totalCout",           fmt(totalCout));

        return render("HistoriqueReparation", ctx);
    }

    // ── Row builder ───────────────────────────────────────────────────────────

    private Map<String, Object> buildLigne(Reparation r) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("date",            r.getDate().format(DATE_FMT));
        row.put("camionMatricule", r.getCamion().getMatricule());
        row.put("typeReparation",  r.getTypeReparation());
        row.put("cout",            fmt(r.getCout()));
        row.put("notes",           r.getNotes() != null && !r.getNotes().isBlank()
                ? r.getNotes() : "—");
        return row;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    private byte[] render(String templateName, Context ctx) throws Exception {
        String html = templateEngine.process(templateName, ctx);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String formatTelephones(List<String> telephones) {
        if (telephones == null || telephones.isEmpty()) return "";
        return String.join(" / ", telephones);
    }

    private String logoToBase64(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) return null;
        try (InputStream in = new URL(logoUrl).openStream()) {
            byte[] bytes = in.readAllBytes();
            String mime = logoUrl.toLowerCase().endsWith(".png")  ? "image/png"
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