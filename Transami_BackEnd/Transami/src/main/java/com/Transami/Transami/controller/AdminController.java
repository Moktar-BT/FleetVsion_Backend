package com.Transami.Transami.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dto.AdminProfileDto;
import com.Transami.Transami.dto.ChangePasswordRequest;
import com.Transami.Transami.dto.RegisterRequest;
import com.Transami.Transami.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/profile")
    public ResponseEntity<AdminProfileDto> getProfile() {
        return ResponseEntity.ok(adminService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<AdminProfileDto> updateProfile(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(adminService.updateProfile(request));
    }

    @PostMapping("/upload-logo")
    public ResponseEntity<String> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(adminService.uploadLogo(file));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        adminService.changePassword(request);
        return ResponseEntity.ok("Mot de passe modifié avec succès");
    }
}
