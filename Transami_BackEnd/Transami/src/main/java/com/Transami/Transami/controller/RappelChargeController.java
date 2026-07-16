package com.Transami.Transami.controller;

import com.Transami.Transami.dto.ChargeAlerteSummary;
import com.Transami.Transami.dto.RappelChargeRequest;
import com.Transami.Transami.dto.RappelChargeResponse;
import com.Transami.Transami.service.RappelChargeService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rappels-charge")
@RequiredArgsConstructor
public class RappelChargeController {

    private final RappelChargeService rappelChargeService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<RappelChargeResponse> create(@Valid @RequestBody RappelChargeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rappelChargeService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<RappelChargeResponse>> getAll() {
        return ResponseEntity.ok(rappelChargeService.getAll(authUtil.getCurrentAdminId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RappelChargeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rappelChargeService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RappelChargeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RappelChargeRequest request) {
        return ResponseEntity.ok(rappelChargeService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @PatchMapping("/{id}/actif")
    public ResponseEntity<RappelChargeResponse> toggleActif(
            @PathVariable Long id,
            @RequestParam boolean actif) {
        return ResponseEntity.ok(rappelChargeService.toggleActif(id, actif, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rappelChargeService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<ChargeAlerteSummary>> getAlertes() {
        return ResponseEntity.ok(rappelChargeService.getAlertes(authUtil.getCurrentAdminId()));
    }

    @PatchMapping("/{id}/avancer")
    public ResponseEntity<RappelChargeResponse> avancerProchainRappel(@PathVariable Long id) {
        return ResponseEntity.ok(rappelChargeService.avancerProchainRappel(id, authUtil.getCurrentAdminId()));
    }
}
