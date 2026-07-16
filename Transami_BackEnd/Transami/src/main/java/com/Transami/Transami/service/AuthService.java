package com.Transami.Transami.service;

import com.Transami.Transami.enums.Language;
import com.Transami.Transami.enums.Theme;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.AdminDao;
import com.Transami.Transami.dto.AuthRequest;
import com.Transami.Transami.dto.AuthResponse;
import com.Transami.Transami.dto.RegisterRequest;
import com.Transami.Transami.entity.Admin;
import com.Transami.Transami.enums.Role;
import com.Transami.Transami.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (adminDao.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un admin avec cet email existe déjà : " + request.getEmail());
        }

        var admin = Admin.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephones(request.getTelephones())   // liste
                .adresse(request.getAdresse())
                .nomEntreprise(request.getNomEntreprise())
                .cheminLogoEntreprise(request.getCheminLogoEntreprise())
                .matriculeFiscale(request.getMatriculeFiscale())
                .language(request.getLanguage() != null ? request.getLanguage() : Language.FRANCAIS)
                .theme(request.getTheme() != null ? request.getTheme() : Theme.LIGHT)
                .role(Role.ADMIN)
                .active(true)
                .build();

        var savedAdmin = adminDao.save(admin);
        var accessToken  = jwtService.generateToken(savedAdmin);
        var refreshToken = jwtService.generateRefreshToken(savedAdmin);

        return buildResponse(savedAdmin, accessToken, refreshToken);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        var admin = adminDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        var accessToken  = jwtService.generateToken(admin);
        var refreshToken = jwtService.generateRefreshToken(admin);

        return buildResponse(admin, accessToken, refreshToken);
    }

    private AuthResponse buildResponse(Admin admin, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(admin.getEmail())
                .nom(admin.getNom())
                .prenom(admin.getPrenom())
                .nomEntreprise(admin.getNomEntreprise())
                .cheminLogoEntreprise(admin.getCheminLogoEntreprise())
                .matriculeFiscale(admin.getMatriculeFiscale())
                .language(admin.getLanguage())
                .theme(admin.getTheme())
                .telephones(admin.getTelephones())
                .adresse(admin.getAdresse())
                .build();
    }
}