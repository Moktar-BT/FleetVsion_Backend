package com.Transami.Transami.dto;

import com.Transami.Transami.enums.Language;
import com.Transami.Transami.enums.Theme;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    private List<String> telephones;   // devient une liste

    private String adresse;
    private String nomEntreprise;
    private String cheminLogoEntreprise;

    @NotBlank(message = "Le matricule fiscale est obligatoire")
    private String matriculeFiscale;

    @Builder.Default
    private Language language = Language.FRANCAIS;

    @Builder.Default
    private Theme theme = Theme.LIGHT;
}