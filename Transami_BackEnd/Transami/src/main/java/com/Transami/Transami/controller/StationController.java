package com.Transami.Transami.controller;

import com.Transami.Transami.dto.StationRequest;
import com.Transami.Transami.dto.StationResponse;
import com.Transami.Transami.service.StationService;
import com.Transami.Transami.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<StationResponse> create(@Valid @RequestBody StationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stationService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<StationResponse>> getAll() {
        return ResponseEntity.ok(stationService.getAll(authUtil.getCurrentAdminId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StationRequest request) {
        return ResponseEntity.ok(stationService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stationService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}
