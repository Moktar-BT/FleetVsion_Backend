package com.Transami.Transami.dto;

import com.Transami.Transami.enums.Language;
import com.Transami.Transami.enums.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileDto {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private List<String> telephones;        // ← Liste de téléphones
    private String adresse;
    private String nomEntreprise;
    private String cheminLogoEntreprise;
    private String matriculeFiscale;
    private Language language;
    private Theme theme;
}