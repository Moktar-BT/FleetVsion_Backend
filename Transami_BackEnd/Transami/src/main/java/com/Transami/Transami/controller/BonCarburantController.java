package com.Transami.Transami.controller;

import com.Transami.Transami.dto.BonCarburantRequest;
import com.Transami.Transami.dto.BonCarburantResponse;
import com.Transami.Transami.dto.CamionFuelStatsResponse;
import com.Transami.Transami.dto.EtatCarburantRequest;
import com.Transami.Transami.service.BonCarburantService;
import com.Transami.Transami.service.EtatCarburantPdfService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bons-carburant")
@RequiredArgsConstructor
public class BonCarburantController {

    private final BonCarburantService bonCarburantService;
    private final EtatCarburantPdfService etatCarburantPdfService;
    private final AuthUtil authUtil;

    // POST /api/bons-carburant
    @PostMapping
    public ResponseEntity<BonCarburantResponse> create(
            @Valid @RequestBody BonCarburantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bonCarburantService.create(request, authUtil.getCurrentAdminId()));
    }

    // GET /api/bons-carburant
    // GET /api/bons-carburant?camionId=1
    // GET /api/bons-carburant?stationId=1
    @GetMapping
    public ResponseEntity<List<BonCarburantResponse>> getAll(
            @RequestParam(required = false) Long camionId,
            @RequestParam(required = false) Long stationId) {
        return ResponseEntity.ok(
                bonCarburantService.getAll(authUtil.getCurrentAdminId(), camionId, stationId));
    }

    // GET /api/bons-carburant/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BonCarburantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                bonCarburantService.getById(id, authUtil.getCurrentAdminId()));
    }

    // PUT /api/bons-carburant/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BonCarburantResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BonCarburantRequest request) {
        return ResponseEntity.ok(
                bonCarburantService.update(id, request, authUtil.getCurrentAdminId()));
    }

    // DELETE /api/bons-carburant/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bonCarburantService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    // GET /api/bons-carburant/camion/{camionId}/stats
    @GetMapping("/camion/{camionId}/stats")
    public ResponseEntity<CamionFuelStatsResponse> getCamionFuelStats(
            @PathVariable Long camionId) {
        return ResponseEntity.ok(
                bonCarburantService.getCamionFuelStats(camionId, authUtil.getCurrentAdminId()));
    }

    // GET /api/bons-carburant/etat-pdf?dateFrom=...&dateTo=...&stationId=...&camionId=...
    @GetMapping("/etat-pdf")
    public ResponseEntity<byte[]> downloadEtatPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) Long camionId) throws Exception {

        EtatCarburantRequest request = new EtatCarburantRequest();
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);
        request.setStationId(stationId);
        request.setCamionId(camionId);

        byte[] pdf = etatCarburantPdfService.generatePdf(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"etat-carburant-" + dateFrom + "-" + dateTo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}