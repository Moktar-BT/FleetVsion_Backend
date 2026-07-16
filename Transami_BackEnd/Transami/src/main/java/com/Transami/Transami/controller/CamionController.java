package com.Transami.Transami.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dto.CamionRequest;
import com.Transami.Transami.dto.CamionResponse;
import com.Transami.Transami.service.CamionService;
import com.Transami.Transami.util.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/camions")
@RequiredArgsConstructor
public class CamionController {

    private final CamionService camionService;
    private final AuthUtil authUtil;

    // POST /api/camions
    @PostMapping
    public ResponseEntity<CamionResponse> create(
            @Valid @RequestBody CamionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(camionService.create(request, authUtil.getCurrentAdminId()));
    }

    // GET /api/camions
    // GET /api/camions?status=true  → uniquement les actifs
    // GET /api/camions?status=false → uniquement en maintenance
    @GetMapping
    public ResponseEntity<List<CamionResponse>> getAll(
            @RequestParam(required = false) Boolean status) {
        Long adminId = authUtil.getCurrentAdminId();
        if (status != null) {
            return ResponseEntity.ok(camionService.getAllByStatus(adminId, status));
        }
        return ResponseEntity.ok(camionService.getAll(adminId));
    }

    // GET /api/camions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CamionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(camionService.getById(id, authUtil.getCurrentAdminId()));
    }

    // PUT /api/camions/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CamionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CamionRequest request) {
        return ResponseEntity.ok(camionService.update(id, request, authUtil.getCurrentAdminId()));
    }

    // PATCH /api/camions/{id}/status?status=false
    @PatchMapping("/{id}/status")
    public ResponseEntity<CamionResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean status) {
        return ResponseEntity.ok(camionService.updateStatus(id, status, authUtil.getCurrentAdminId()));
    }

    // DELETE /api/camions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        camionService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}