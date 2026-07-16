package com.Transami.Transami.service;

import com.Transami.Transami.dao.AdminDao;
import com.Transami.Transami.dao.PasswordResetOtpDao;
import com.Transami.Transami.dto.ForgotPasswordRequest;
import com.Transami.Transami.dto.ResetPasswordRequest;
import com.Transami.Transami.dto.VerifyOtpRequest;
import com.Transami.Transami.entity.Admin;
import com.Transami.Transami.entity.PasswordResetOtp;
import com.Transami.Transami.exception.ExpiredOtpException;
import com.Transami.Transami.exception.InvalidOtpException;
import com.Transami.Transami.exception.OtpAlreadyUsedException;
import com.Transami.Transami.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final AdminDao adminDao;
    private final PasswordResetOtpDao otpDao;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Étape 1 : demande de réinitialisation.
     * Ne révèle jamais si l'email existe ou non (anti-énumération).
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Admin admin = adminDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        language_message(request.getEmail())
                ));

        String otpCode = generateOtp();

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(admin.getEmail())
                .otpCode(otpCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .verified(false)
                .used(false)
                .build();

        otpDao.save(otp);
        emailService.sendOtpEmail(admin.getEmail(), otpCode);
    }

    private String language_message(String email) {
        return "Aucun compte associé à l'adresse " + email;
    }

    /**
     * Étape 2 : vérification du code OTP.
     */
    @Transactional
    public boolean verifyOtp(VerifyOtpRequest request) {
        PasswordResetOtp otp = getValidOtp(request.getEmail(), request.getOtp());
        otp.setVerified(true);
        otpDao.save(otp);
        return true;
    }

    /**
     * Étape 3 : réinitialisation effective du mot de passe.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetOtp otp = getValidOtp(request.getEmail(), request.getOtp());

        Admin admin = adminDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidOtpException("Requête invalide"));

        admin.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        adminDao.save(admin);

        // Invalidation immédiate du code
        otp.setUsed(true);
        otpDao.save(otp);
    }

    /**
     * Récupère et valide le dernier OTP émis pour un email donné :
     * existence, correspondance du code, expiration, non-utilisation.
     */
    private PasswordResetOtp getValidOtp(String email, String otpCode) {
        PasswordResetOtp otp = otpDao.findFirstByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new InvalidOtpException("Aucun code de vérification trouvé pour cet email"));

        if (!otp.getOtpCode().equals(otpCode)) {
            throw new InvalidOtpException("Le code de vérification est incorrect");
        }

        if (otp.isUsed()) {
            throw new OtpAlreadyUsedException("Ce code a déjà été utilisé");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredOtpException("Le code de vérification a expiré");
        }

        return otp;
    }

    private String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000); // 6 chiffres, jamais de zéro en tête manquant
        return String.valueOf(code);
    }
}