package com.Transami.Transami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.Transami.Transami.enums.Language;
import com.Transami.Transami.enums.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String email;
    private String nom;
    private String prenom;
    private String nomEntreprise;
    private String cheminLogoEntreprise;
    private String matriculeFiscale;
    private Language language;
    private Theme theme;
    private List<String> telephones;   // devient une liste
    private String adresse;
}