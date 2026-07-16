package com.Transami.Transami.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dto.BonDeLivraisonRequest;
import com.Transami.Transami.dto.BonDeLivraisonResponse;
import com.Transami.Transami.enums.DeliveryStatus;
import com.Transami.Transami.service.BonDeLivraisonService;
import com.Transami.Transami.util.AuthUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bons-livraison")
@RequiredArgsConstructor
public class BonDeLivraisonController {

    private final BonDeLivraisonService bonDeLivraisonService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<BonDeLivraisonResponse> create(
            @Valid @RequestBody BonDeLivraisonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bonDeLivraisonService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<BonDeLivraisonResponse>> getAll() {
        return ResponseEntity.ok(bonDeLivraisonService.getAll(authUtil.getCurrentAdminId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonDeLivraisonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeLivraisonService.getById(id, authUtil.getCurrentAdminId()));
    }

    @GetMapping("/camion/{camionId}")
    public ResponseEntity<List<BonDeLivraisonResponse>> getAllByCamion(
            @PathVariable Long camionId) {
        return ResponseEntity.ok(bonDeLivraisonService.getAllByCamion(camionId, authUtil.getCurrentAdminId()));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<BonDeLivraisonResponse>> getAllByStatut(
            @PathVariable DeliveryStatus statut) {
        return ResponseEntity.ok(bonDeLivraisonService.getAllByStatut(authUtil.getCurrentAdminId(), statut));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<BonDeLivraisonResponse>> getAllByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(bonDeLivraisonService.getAllByClient(authUtil.getCurrentAdminId(), clientId));
    }

    @GetMapping("/periode")
    public ResponseEntity<List<BonDeLivraisonResponse>> getAllByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(bonDeLivraisonService.getAllByDateRange(authUtil.getCurrentAdminId(), from, to));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BonDeLivraisonResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BonDeLivraisonRequest request) {
        return ResponseEntity.ok(bonDeLivraisonService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<BonDeLivraisonResponse> updateStatut(
            @PathVariable Long id,
            @RequestParam DeliveryStatus statut) {
        return ResponseEntity.ok(bonDeLivraisonService.updateStatut(id, statut, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bonDeLivraisonService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}