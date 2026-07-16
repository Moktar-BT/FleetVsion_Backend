package com.Transami.Transami.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dto.FactureRequest;
import com.Transami.Transami.dto.FactureResponse;
import com.Transami.Transami.enums.InvoiceStatus;
import com.Transami.Transami.service.FacturePdfService;
import com.Transami.Transami.service.FactureService;
import com.Transami.Transami.util.AuthUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;
    private final FacturePdfService facturePdfService;
    private final AuthUtil authUtil;

    // ── CRUD ──────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<FactureResponse> create(@Valid @RequestBody FactureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(factureService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<FactureResponse>> getAll() {
        return ResponseEntity.ok(factureService.getAll(authUtil.getCurrentAdminId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.getById(id, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        factureService.deleteById(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<FactureResponse> updateStatut(
            @PathVariable Long id,
            @RequestParam InvoiceStatus statut) {
        return ResponseEntity.ok(factureService.updateStatut(id, statut, authUtil.getCurrentAdminId()));
    }

    // ── PDF Downloads ──────────────────────────────────────────────

    @GetMapping("/{id}/pdf/detaillee")
    public ResponseEntity<?> downloadDetaillee(@PathVariable Long id) {
        try {
            byte[] pdf = facturePdfService.generateDetaillee(id);
            return buildPdfResponse(pdf, "facture-detaillee-" + id + ".pdf");
        } catch (Exception e) {
            // Stack trace complet dans les logs Spring
            e.printStackTrace();
            // Message d'erreur visible dans le navigateur / Postman
            String msg = "ERREUR: " + e.getClass().getName() + ": " + e.getMessage();
            if (e.getCause() != null) {
                msg += "\nCAUSE: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
            }
            if (e.getCause() != null && e.getCause().getCause() != null) {
                msg += "\nROOT: " + e.getCause().getCause().getMessage();
            }
            return ResponseEntity.status(500).contentType(MediaType.TEXT_PLAIN).body(msg);
        }
    }

    @GetMapping("/{id}/pdf/comprimee")
    public ResponseEntity<?> downloadComprimee(@PathVariable Long id) {
        try {
            byte[] pdf = facturePdfService.generateComprimee(id);
            return buildPdfResponse(pdf, "facture-comprimee-" + id + ".pdf");
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "ERREUR: " + e.getClass().getName() + ": " + e.getMessage();
            if (e.getCause() != null) {
                msg += "\nCAUSE: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
            }
            return ResponseEntity.status(500).contentType(MediaType.TEXT_PLAIN).body(msg);
        }
    }
    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}