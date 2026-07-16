package com.Transami.Transami.service;

import com.Transami.Transami.dao.BonDeLivraisonDao;
import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dto.AdminProfileDto;
import com.Transami.Transami.dto.EtatBLRequest;
import com.Transami.Transami.entity.BonDeLivraison;
import com.Transami.Transami.enums.DeliveryStatus;
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
public class EtatBLPdfService {

    private final BonDeLivraisonDao bonDeLivraisonDao;
    private final CamionDao         camionDao;
    private final AdminService      adminService;
    private final TemplateEngine    templateEngine;
    private final AuthUtil          authUtil;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePdf(EtatBLRequest request) throws Exception {
        AdminProfileDto profile = adminService.getMyProfile();
        return generatePdfTx(request, profile);
    }

    @Transactional(readOnly = true)
    protected byte[] generatePdfTx(EtatBLRequest request, AdminProfileDto profile) throws Exception {
        Long adminId = authUtil.getCurrentAdminId();

        List<BonDeLivraison> bdls = bonDeLivraisonDao
                .findAllByCamionAdminIdAndDateBetween(adminId, request.getDateFrom(), request.getDateTo());

        if (request.getClientId() != null) {
            bdls = bdls.stream().filter(b -> b.getClient().getId().equals(request.getClientId())).collect(Collectors.toList());
        }
        if (request.getCamionId() != null) {
            bdls = bdls.stream().filter(b -> b.getCamion().getId().equals(request.getCamionId())).collect(Collectors.toList());
        }
        if (request.getFournisseurId() != null) {
            bdls = bdls.stream().filter(b -> b.getFournisseur().getId().equals(request.getFournisseurId())).collect(Collectors.toList());
        }
        if (request.getStatut() != null && !request.getStatut().isBlank()) {
            DeliveryStatus statutFilter = DeliveryStatus.valueOf(request.getStatut());
            bdls = bdls.stream().filter(b -> b.getStatut() == statutFilter).collect(Collectors.toList());
        }

        bdls.sort(Comparator.comparing(BonDeLivraison::getDate).thenComparing(BonDeLivraison::getNumero));

        List<Map<String, Object>> lignes = new ArrayList<>();
        for (BonDeLivraison bdl : bdls) {
            lignes.add(buildLigne(bdl));
        }

        double totalQty = bdls.stream().mapToDouble(b -> b.getQuantite() != null ? b.getQuantite() : 0.0).sum();
        BigDecimal totalHt = bdls.stream().map(b -> nvl(b.getMontantHt())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTtc = bdls.stream().map(b -> nvl(b.getMontantTtc())).reduce(BigDecimal.ZERO, BigDecimal::add);

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

        ctx.setVariable("lignes",         lignes);
        ctx.setVariable("nombreBL",       bdls.size());
        ctx.setVariable("totalQuantite",  fmt(BigDecimal.valueOf(totalQty)));
        ctx.setVariable("totalMontantHt", fmt(totalHt));
        ctx.setVariable("totalMontantTtc",fmt(totalTtc));

        return render("EtatBL", ctx);
    }

    private Map<String, Object> buildLigne(BonDeLivraison bdl) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("numero",           bdl.getNumero());
        row.put("date",             bdl.getDate().format(DATE_FMT));
        row.put("clientNom",        bdl.getClient().getNom());
        row.put("blNumFournisseur", bdl.getBlNumFournisseur() != null ? String.valueOf(bdl.getBlNumFournisseur()) : "");
        row.put("camionMatricule",  bdl.getCamion().getMatricule());
        row.put("quantite",         fmt(bdl.getQuantite() != null ? BigDecimal.valueOf(bdl.getQuantite()) : BigDecimal.ZERO));
        row.put("unite",            bdl.getCodeProduit().getUnit() != null ? bdl.getCodeProduit().getUnit() : "");
        row.put("prixUnitaire",     fmt(nvl(bdl.getCodeProduit().getUnitPrice())));
        row.put("montantHt",        fmt(nvl(bdl.getMontantHt())));
        row.put("montantTtc",       fmt(nvl(bdl.getMontantTtc())));

        if (bdl.getFacture() != null && bdl.getFacture().getNumero() != null) {
            row.put("numeroFacture", bdl.getFacture().getNumero());
            row.put("estFacture",    true);
        } else {
            row.put("numeroFacture", "Non facturé");
            row.put("estFacture",    false);
        }
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

    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}