package com.Transami.Transami.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dto.ClientRequest;
import com.Transami.Transami.dto.ClientResponse;
import com.Transami.Transami.service.ClientService;
import com.Transami.Transami.util.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<ClientResponse> create(
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.create(request, authUtil.getCurrentAdminId()));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(clientService.getAll(authUtil.getCurrentAdminId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id, authUtil.getCurrentAdminId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request, authUtil.getCurrentAdminId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id, authUtil.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }
}