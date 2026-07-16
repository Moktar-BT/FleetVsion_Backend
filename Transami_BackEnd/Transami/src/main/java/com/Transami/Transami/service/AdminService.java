package com.Transami.Transami.service;

import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.AdminDao;
import com.Transami.Transami.dto.AdminProfileDto;
import com.Transami.Transami.dto.ChangePasswordRequest;
import com.Transami.Transami.dto.RegisterRequest;
import com.Transami.Transami.entity.Admin;
import com.Transami.Transami.util.AuthUtil;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminDao adminDao;
    private final AuthUtil authUtil;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminProfileDto getMyProfile() {
        Admin admin = authUtil.getCurrentAdmin();
        // Force l'initialisation de la collection des téléphones
        Hibernate.initialize(admin.getTelephones());
        return mapToDto(admin);
    }

    @Transactional
    public AdminProfileDto updateProfile(RegisterRequest request) {
        Admin admin = authUtil.getCurrentAdmin();

        // 1. Mise à jour des champs basiques uniquement s'ils sont fournis (non nuls)
        if (request.getNom() != null) {
            admin.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            admin.setPrenom(request.getPrenom());
        }
        if (request.getAdresse() != null) {
            admin.setAdresse(request.getAdresse());
        }
        if (request.getNomEntreprise() != null) {
            admin.setNomEntreprise(request.getNomEntreprise());
        }
        if (request.getMatriculeFiscale() != null) {
            admin.setMatriculeFiscale(request.getMatriculeFiscale());
        }
        if (request.getCheminLogoEntreprise() != null) {
            admin.setCheminLogoEntreprise(request.getCheminLogoEntreprise());
        }

        // 2. Mise à jour sécurisée de la collection Hibernate des téléphones
        if (request.getTelephones() != null) {
            // Au lieu de remplacer la liste, on la vide et on la remplit à nouveau
            admin.getTelephones().clear();
            admin.getTelephones().addAll(request.getTelephones());
        }

        // 3. Mise à jour des préférences (Thème et Langue)
        if (request.getLanguage() != null) {
            admin.setLanguage(request.getLanguage());
        }
        if (request.getTheme() != null) {
            admin.setTheme(request.getTheme());
        }

        return mapToDto(adminDao.save(admin));
    }

    public String uploadLogo(MultipartFile file) throws IOException {
        Admin admin = authUtil.getCurrentAdmin();
        String urlImage = imageService.uploadImage(file);
        admin.setCheminLogoEntreprise(urlImage);
        adminDao.save(admin);
        return urlImage;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Admin admin = authUtil.getCurrentAdmin();

        // Vérifier que l'ancien mot de passe est correct
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getMotDePasse())) {
            throw new RuntimeException("L'ancien mot de passe est incorrect");
        }

        // Vérifier que le nouveau mot de passe et la confirmation correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Le nouveau mot de passe et la confirmation ne correspondent pas");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Encoder et sauvegarder le nouveau mot de passe
        admin.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        adminDao.save(admin);
    }

    private AdminProfileDto mapToDto(Admin admin) {
        return AdminProfileDto.builder()
                .id(admin.getId())
                .nom(admin.getNom())
                .prenom(admin.getPrenom())
                .email(admin.getEmail())
                .telephones(admin.getTelephones())
                .adresse(admin.getAdresse())
                .nomEntreprise(admin.getNomEntreprise())
                .cheminLogoEntreprise(admin.getCheminLogoEntreprise())
                .matriculeFiscale(admin.getMatriculeFiscale())
                .language(admin.getLanguage())
                .theme(admin.getTheme())
                .build();
    }
}