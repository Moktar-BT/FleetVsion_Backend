// src/main/java/com/Transami/Transami/controller/ReparationController.java
package com.Transami.Transami.controller;

import com.Transami.Transami.dto.HistoriqueReparationRequest;
import com.Transami.Transami.dto.ReparationRequest;
import com.Transami.Transami.dto.ReparationResponse;
import com.Transami.Transami.service.HistoriqueReparationPdfService;
import com.Transami.Transami.service.ReparationService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reparations")
@RequiredArgsConstructor
public class ReparationController {

    private final ReparationService reparationService;
    private final HistoriqueReparationPdfService pdfService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<ReparationResponse> create(@Valid @RequestBody ReparationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reparationService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<ReparationResponse>> getAll(
            @RequestParam(required = false) Long camionId) {
        return ResponseEntity.ok(
                reparationService.getAll(authUtil.getCurrentAdminId(), camionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReparationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reparationService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReparationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ReparationRequest request) {
        return ResponseEntity.ok(
                reparationService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reparationService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/historique-pdf")
    public ResponseEntity<byte[]> downloadHistoriquePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long camionId) throws Exception {

        HistoriqueReparationRequest req = new HistoriqueReparationRequest();
        req.setDateFrom(dateFrom);
        req.setDateTo(dateTo);
        req.setCamionId(camionId);

        byte[] pdf = pdfService.generatePdf(req);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"historique-reparations-" + dateFrom + "-" + dateTo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}