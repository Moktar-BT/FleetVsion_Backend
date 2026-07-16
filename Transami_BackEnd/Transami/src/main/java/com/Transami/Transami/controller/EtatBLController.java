package com.Transami.Transami.controller;

import com.Transami.Transami.dto.EtatBLRequest;
import com.Transami.Transami.service.EtatBLPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/etat-bl")
@RequiredArgsConstructor
public class EtatBLController {

    private final EtatBLPdfService etatBLPdfService;

    /**
     * GET /api/etat-bl/pdf?dateFrom=2026-01-01&dateTo=2026-03-31
     *                      &clientId=1&camionId=2&fournisseurId=3&statut=NON_FACTURE
     *
     * All params except dateFrom / dateTo are optional.
     */
    @GetMapping("/pdf")
    public ResponseEntity<?> generatePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long camionId,
            @RequestParam(required = false) Long fournisseurId,
            @RequestParam(required = false) String statut) {

        try {
            EtatBLRequest request = new EtatBLRequest();
            request.setDateFrom(dateFrom);
            request.setDateTo(dateTo);
            request.setClientId(clientId);
            request.setCamionId(camionId);
            request.setFournisseurId(fournisseurId);
            request.setStatut(statut);

            byte[] pdf = etatBLPdfService.generatePdf(request);

            String filename = "etat-bl-" + dateFrom + "-" + dateTo + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdf.length)
                    .body(pdf);

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "ERREUR: " + e.getClass().getName() + ": " + e.getMessage();
            if (e.getCause() != null) {
                msg += "\nCAUSE: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
            }
            return ResponseEntity.status(500).contentType(MediaType.TEXT_PLAIN).body(msg);
        }
    }
}