package com.Transami.Transami.controller;

import com.Transami.Transami.dto.*;
import com.Transami.Transami.enums.StatutCharge;
import com.Transami.Transami.enums.TypeCharge;
import com.Transami.Transami.service.ChargeService;
import com.Transami.Transami.service.ChargeStatsService;
import com.Transami.Transami.service.EtatChargePdfService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/charges")
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService chargeService;
    private final EtatChargePdfService etatChargePdfService;
    private final ChargeStatsService chargeStatsService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<ChargeResponse> create(@Valid @RequestBody ChargeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chargeService.create(request, authUtil.getCurrentAdminId()));
    }
    @GetMapping("/breakdown")
    public ResponseEntity<List<YearlyBreakdown>> getGlobalBreakdown() {
        return ResponseEntity.ok(chargeStatsService.buildGlobalChargeBreakdown(authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<ChargeResponse>> getAll(
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) StatutCharge statut) {

        Long adminId = authUtil.getCurrentAdminId();

        if (templateId != null) {
            return ResponseEntity.ok(chargeService.getAllByTemplate(adminId, templateId));
        }

        if (dateFrom != null && dateTo != null) {
            return ResponseEntity.ok(chargeService.getAllByPeriode(adminId, dateFrom, dateTo));
        }

        if (statut != null) {
            return ResponseEntity.ok(chargeService.getAllByStatut(adminId, statut));
        }

        return ResponseEntity.ok(chargeService.getAll(adminId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(chargeService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChargeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ChargeRequest request) {
        return ResponseEntity.ok(chargeService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<ChargeResponse> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutCharge statut) {
        return ResponseEntity.ok(chargeService.updateStatut(id, statut, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chargeService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<ChargeStatsResponse> getStats(@RequestParam int year) {
        return ResponseEntity.ok(chargeService.getStats(authUtil.getCurrentAdminId(), year));
    }

    /**
     * Export PDF de l'état des dépenses filtré.
     * Correspond exactement aux paramètres envoyés par chargeApi.downloadEtat()
     * du frontend : dateFrom, dateTo (obligatoires), statut, type,
     * camionMatricule, chauffeurNom, remorqueMatricule (optionnels).
     */
    @GetMapping("/etat-pdf")
    public ResponseEntity<byte[]> getEtatPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) StatutCharge statut,
            @RequestParam(required = false) TypeCharge type,
            @RequestParam(required = false) String camionMatricule,
            @RequestParam(required = false) String chauffeurNom,
            @RequestParam(required = false) String remorqueMatricule
    ) throws Exception {

        EtatChargeRequest request = new EtatChargeRequest();
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);
        request.setStatut(statut);
        request.setType(type);
        request.setCamionMatricule(camionMatricule);
        request.setChauffeurNom(chauffeurNom);
        request.setRemorqueMatricule(remorqueMatricule);

        byte[] pdf = etatChargePdfService.generatePdf(request);

        String filename = "etat-depenses-" + dateFrom + "_" + dateTo + ".pdf";
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}