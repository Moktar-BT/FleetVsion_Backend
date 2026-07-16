package com.Transami.Transami.controller;

import com.Transami.Transami.dto.PrixCarburantRequest;
import com.Transami.Transami.dto.PrixCarburantResponse;
import com.Transami.Transami.service.PrixCarburantService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prix-carburant")
@RequiredArgsConstructor
public class PrixCarburantController {

    private final PrixCarburantService prixCarburantService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<PrixCarburantResponse> createOrUpdate(@Valid @RequestBody PrixCarburantRequest request) {
        return ResponseEntity.ok(prixCarburantService.createOrUpdate(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<PrixCarburantResponse> getByAdmin() {
        return ResponseEntity.ok(prixCarburantService.getByAdmin(authUtil.getCurrentAdminId()));
    }
}
