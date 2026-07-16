package com.Transami.Transami.controller;

import com.Transami.Transami.dto.ChargeTemplateRequest;
import com.Transami.Transami.dto.ChargeTemplateResponse;
import com.Transami.Transami.enums.TypeCharge;
import com.Transami.Transami.service.ChargeTemplateService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/charges-templates")
@RequiredArgsConstructor
public class ChargeTemplateController {

    private final ChargeTemplateService chargeTemplateService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<ChargeTemplateResponse> create(@Valid @RequestBody ChargeTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chargeTemplateService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<ChargeTemplateResponse>> getAll(@RequestParam(required = false) TypeCharge type) {
        Long adminId = authUtil.getCurrentAdminId();
        if (type != null) {
            return ResponseEntity.ok(chargeTemplateService.getAllByType(adminId, type));
        }
        return ResponseEntity.ok(chargeTemplateService.getAll(adminId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargeTemplateResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(chargeTemplateService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChargeTemplateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ChargeTemplateRequest request) {
        return ResponseEntity.ok(chargeTemplateService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ChargeTemplateResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(chargeTemplateService.toggleActive(id, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chargeTemplateService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}
