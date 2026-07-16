package com.Transami.Transami.controller;

import com.Transami.Transami.dto.RappelVidangeAlerteSummary;
import com.Transami.Transami.dto.RappelVidangeRequest;
import com.Transami.Transami.dto.RappelVidangeResponse;
import com.Transami.Transami.service.RappelVidangeService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rappels-vidange")
@RequiredArgsConstructor
public class RappelVidangeController {

    private final RappelVidangeService rappelVidangeService;
    private final AuthUtil authUtil;

    // POST /api/rappels-vidange
    @PostMapping
    public ResponseEntity<RappelVidangeResponse> create(
            @Valid @RequestBody RappelVidangeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rappelVidangeService.create(request, authUtil.getCurrentAdminId()));
    }

    // GET /api/rappels-vidange
    @GetMapping
    public ResponseEntity<List<RappelVidangeResponse>> getAll() {
        return ResponseEntity.ok(
                rappelVidangeService.getAll(authUtil.getCurrentAdminId()));
    }

    // GET /api/rappels-vidange/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RappelVidangeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                rappelVidangeService.getById(id, authUtil.getCurrentAdminId()));
    }

    // PUT /api/rappels-vidange/{id}
    @PutMapping("/{id}")
    public ResponseEntity<RappelVidangeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RappelVidangeRequest request) {
        return ResponseEntity.ok(
                rappelVidangeService.update(id, request, authUtil.getCurrentAdminId()));
    }

    /**
     * PATCH /api/rappels-vidange/{id}/reinitialiser
     *
     * Appelé quand l'admin effectue une vidange.
     * Body : { kmDerniereVidange, intervalleKm (optionnel), dateDerniereVidange (optionnel) }
     */
    @PatchMapping("/{id}/reinitialiser")
    public ResponseEntity<RappelVidangeResponse> reinitialiser(
            @PathVariable Long id,
            @Valid @RequestBody RappelVidangeRequest request) {
        return ResponseEntity.ok(
                rappelVidangeService.reinitialiser(id, request, authUtil.getCurrentAdminId()));
    }

    // DELETE /api/rappels-vidange/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rappelVidangeService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/rappels-vidange/alertes
     *
     * Endpoint léger pour le badge dashboard.
     * Retourne uniquement les rappels en statut DEPASSEE ou PROCHE.
     */
    @GetMapping("/alertes")
    public ResponseEntity<List<RappelVidangeAlerteSummary>> getAlertes() {
        return ResponseEntity.ok(
                rappelVidangeService.getAlertes(authUtil.getCurrentAdminId()));
    }
}