package com.Transami.Transami.dao;

import com.Transami.Transami.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpDao extends JpaRepository<PasswordResetOtp, Long> {

    // Récupère le dernier OTP créé pour un email donné (le plus récent)
    Optional<PasswordResetOtp> findFirstByEmailOrderByCreatedAtDesc(String email);

    Optional<PasswordResetOtp> findByEmailAndOtpCode(String email, String otpCode);
}