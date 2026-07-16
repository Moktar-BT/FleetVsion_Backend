package com.Transami.Transami.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dto.FournisseurRequest;
import com.Transami.Transami.dto.FournisseurResponse;
import com.Transami.Transami.service.FournisseurService;
import com.Transami.Transami.util.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<FournisseurResponse> create(
            @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fournisseurService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<FournisseurResponse>> getAll() {
        return ResponseEntity.ok(fournisseurService.getAll(authUtil.getCurrentAdminId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.ok(fournisseurService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fournisseurService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}