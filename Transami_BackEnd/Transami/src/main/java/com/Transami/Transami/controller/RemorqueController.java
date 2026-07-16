package com.Transami.Transami.controller;

import com.Transami.Transami.dto.RemorqueRequest;
import com.Transami.Transami.dto.RemorqueResponse;
import com.Transami.Transami.service.RemorqueService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/remorques")
@RequiredArgsConstructor
public class RemorqueController {

    private final RemorqueService remorqueService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<RemorqueResponse> create(@Valid @RequestBody RemorqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(remorqueService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<RemorqueResponse>> getAll(@RequestParam(required = false) Long camionId) {
        return ResponseEntity.ok(remorqueService.getAll(authUtil.getCurrentAdminId(), camionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RemorqueResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(remorqueService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RemorqueResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RemorqueRequest request) {
        return ResponseEntity.ok(remorqueService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        remorqueService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}
