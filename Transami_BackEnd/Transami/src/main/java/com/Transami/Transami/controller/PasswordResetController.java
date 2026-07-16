package com.Transami.Transami.controller;

import com.Transami.Transami.dto.*;
import com.Transami.Transami.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * POST /auth/forgot-password
     * Envoie un code OTP par e-mail si le compte existe.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("If the email exists, a verification code has been sent.")
                        .build()
        );
    }

    /**
     * POST /auth/verify-otp
     * Vérifie la validité du code OTP.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        boolean verified = passwordResetService.verifyOtp(request);
        return ResponseEntity.ok(VerifyOtpResponse.builder().verified(verified).build());
    }

    /**
     * POST /auth/reset-password
     * Réinitialise le mot de passe si le code OTP est valide.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("Mot de passe réinitialisé avec succès")
                        .build()
        );
    }
}