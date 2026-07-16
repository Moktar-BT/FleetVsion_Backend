package com.Transami.Transami.controller;

import com.Transami.Transami.dto.ChauffeurRequest;
import com.Transami.Transami.dto.ChauffeurResponse;
import com.Transami.Transami.service.ChauffeurService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chauffeurs")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurService chauffeurService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<ChauffeurResponse> create(@Valid @RequestBody ChauffeurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chauffeurService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<ChauffeurResponse>> getAll(@RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(chauffeurService.getAll(authUtil.getCurrentAdminId(), active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChauffeurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(chauffeurService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChauffeurResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ChauffeurRequest request) {
        return ResponseEntity.ok(chauffeurService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ChauffeurResponse> toggleActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(chauffeurService.toggleActive(id, active, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chauffeurService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}
