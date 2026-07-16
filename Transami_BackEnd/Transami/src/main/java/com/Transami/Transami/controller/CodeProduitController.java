package com.Transami.Transami.controller;

import com.Transami.Transami.service.AuthService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dto.CodeProduitRequest;
import com.Transami.Transami.dto.CodeProduitResponse;
import com.Transami.Transami.service.CodeProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/codes-produit")
@RequiredArgsConstructor
public class CodeProduitController {

    private final CodeProduitService codeProduitService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<CodeProduitResponse> create(
            @Valid @RequestBody CodeProduitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(codeProduitService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<CodeProduitResponse>> getAll() {
        return ResponseEntity.ok(codeProduitService.getAll(authUtil.getCurrentAdminId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeProduitResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(codeProduitService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CodeProduitResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CodeProduitRequest request) {
        return ResponseEntity.ok(codeProduitService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        codeProduitService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}