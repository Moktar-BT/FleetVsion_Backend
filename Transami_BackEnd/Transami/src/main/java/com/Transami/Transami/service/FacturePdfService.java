package com.Transami.Transami.service;

import com.Transami.Transami.dao.FactureDao;
import com.Transami.Transami.dto.AdminProfileDto;
import com.Transami.Transami.entity.BonDeLivraison;
import com.Transami.Transami.entity.CodeProduit;
import com.Transami.Transami.entity.Facture;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FacturePdfService {

    private final FactureDao factureDao;
    private final TemplateEngine templateEngine;
    private final AdminService adminService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    public byte[] generateDetaillee(Long factureId) throws Exception {
        AdminProfileDto profile = adminService.getMyProfile();
        return generateDetailleeTx(factureId, profile);
    }

    public byte[] generateComprimee(Long factureId) throws Exception {
        AdminProfileDto profile = adminService.getMyProfile();
        return generateComprimeeeTx(factureId, profile);
    }

    @Transactional(readOnly = true)
    protected byte[] generateDetailleeTx(Long factureId, AdminProfileDto profile) throws Exception {
        Facture facture = findFacture(factureId);
        Context ctx = buildBaseContext(facture, profile);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (BonDeLivraison bdl : facture.getBonsDeLivraison()) {
            rows.add(buildDetailRow(bdl));
        }
        ctx.setVariable("rows", rows);
        ctx.setVariable("isDetaillee", true);
        ctx.setVariable("tvaRecap", buildTvaRecap(facture.getBonsDeLivraison()));

        return render("facture-detaillee", ctx);
    }

    @Transactional(readOnly = true)
    protected byte[] generateComprimeeeTx(Long factureId, AdminProfileDto profile) throws Exception {
        Facture facture = findFacture(factureId);
        Context ctx = buildBaseContext(facture, profile);

        Map<Long, List<BonDeLivraison>> grouped = new LinkedHashMap<>();
        for (BonDeLivraison bdl : facture.getBonsDeLivraison()) {
            grouped.computeIfAbsent(bdl.getCodeProduit().getId(), k -> new ArrayList<>()).add(bdl);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (List<BonDeLivraison> bdls : grouped.values()) {
            rows.add(buildCompressedRow(bdls));
        }
        ctx.setVariable("rows", rows);
        ctx.setVariable("isDetaillee", false);
        ctx.setVariable("tvaRecap", buildTvaRecap(facture.getBonsDeLivraison()));

        return render("facture-detaillee", ctx);
    }

    private Facture findFacture(Long id) {
        return factureDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée : " + id));
    }

    private String formatTelephones(List<String> telephones) {
        if (telephones == null || telephones.isEmpty()) return "";
        return String.join(" / ", telephones);
    }

    private Context buildBaseContext(Facture facture, AdminProfileDto profile) {
        Context ctx = new Context();

        ctx.setVariable("factureId",         facture.getId());
        ctx.setVariable("factureNumero",     facture.getNumero());
        ctx.setVariable("factureDate",       facture.getDate().format(DATE_FMT));
        ctx.setVariable("clientNom",         facture.getClient().getNom());
        ctx.setVariable("clientLocalisation", facture.getClient().getLocalisation());
        ctx.setVariable("clientMatF",        facture.getClient().getMatF());

        ctx.setVariable("montantHTVA",   fmt(facture.getMontantHTVA()));
        ctx.setVariable("montantTVA",    fmt(facture.getMontantTVA()));
        ctx.setVariable("droitsTimbre",  fmt(facture.getDroitsTimbre()));
        ctx.setVariable("montantTTC",    fmt(facture.getMontantTTC()));

        ctx.setVariable("companyName",    profile.getNomEntreprise());
        ctx.setVariable("companyAddress", profile.getAdresse());
        ctx.setVariable("companyPhone",   formatTelephones(profile.getTelephones()));
        ctx.setVariable("companyEmail",   profile.getEmail());
        ctx.setVariable("companyTva",     profile.getMatriculeFiscale() != null ? profile.getMatriculeFiscale() : "");
        ctx.setVariable("companyLogo",    logoToBase64(profile.getCheminLogoEntreprise()));

        return ctx;
    }

    private Map<String, Object> buildDetailRow(BonDeLivraison bdl) {
        CodeProduit cp = bdl.getCodeProduit();
        BigDecimal qty = bdl.getQuantite() != null ? BigDecimal.valueOf(bdl.getQuantite()) : BigDecimal.ZERO;
        BigDecimal unitPrice = nvl(cp.getUnitPrice());
        BigDecimal totalHT = nvl(bdl.getMontantHt());
        BigDecimal totalTTC = nvl(bdl.getMontantTtc());
        String unite = cp.getUnit() != null ? cp.getUnit() : "";

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reference",    cp.getCode());
        row.put("designation",  cp.getDescription() != null ? cp.getDescription() : cp.getCode());
        row.put("unite",        unite);
        row.put("quantite",     fmt(qty));
        row.put("prixUnitaire", fmt(unitPrice));
        row.put("tva",          cp.getVat().stripTrailingZeros().toPlainString() + "%");
        row.put("totalHT",      fmt(totalHT));
        row.put("totalTTC",     fmt(totalTTC));
        return row;
    }

    private Map<String, Object> buildCompressedRow(List<BonDeLivraison> bdls) {
        CodeProduit cp = bdls.get(0).getCodeProduit();
        BigDecimal totalQty = bdls.stream()
                .map(b -> b.getQuantite() != null ? BigDecimal.valueOf(b.getQuantite()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unitPrice = nvl(cp.getUnitPrice());
        BigDecimal totalHT = bdls.stream().map(b -> nvl(b.getMontantHt())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTTC = bdls.stream().map(b -> nvl(b.getMontantTtc())).reduce(BigDecimal.ZERO, BigDecimal::add);
        String unite = cp.getUnit() != null ? cp.getUnit() : "";

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reference",    cp.getCode());
        row.put("designation",  cp.getDescription() != null ? cp.getDescription() : cp.getCode());
        row.put("unite",        unite);
        row.put("quantite",     fmt(totalQty));
        row.put("prixUnitaire", fmt(unitPrice));
        row.put("tva",          cp.getVat().stripTrailingZeros().toPlainString() + "%");
        row.put("totalHT",      fmt(totalHT));
        row.put("totalTTC",     fmt(totalTTC));
        return row;
    }

    private List<Map<String, Object>> buildTvaRecap(List<BonDeLivraison> bdls) {
        Map<String, BigDecimal[]> recapMap = new LinkedHashMap<>();
        for (BonDeLivraison bdl : bdls) {
            CodeProduit cp = bdl.getCodeProduit();
            String tauxKey = cp.getVat().stripTrailingZeros().toPlainString();
            BigDecimal ht = nvl(bdl.getMontantHt());
            BigDecimal ttc = nvl(bdl.getMontantTtc());
            BigDecimal tva = ttc.subtract(ht);
            recapMap.compute(tauxKey, (k, arr) -> {
                if (arr == null) arr = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
                arr[0] = arr[0].add(ht);
                arr[1] = arr[1].add(tva);
                arr[2] = arr[2].add(ttc);
                return arr;
            });
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> entry : recapMap.entrySet()) {
            BigDecimal[] arr = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("taux",       entry.getKey());
            row.put("baseHT",     fmt(arr[0]));
            row.put("montantTVA", fmt(arr[1]));
            row.put("totalTTC",   fmt(arr[2]));
            result.add(row);
        }
        return result;
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