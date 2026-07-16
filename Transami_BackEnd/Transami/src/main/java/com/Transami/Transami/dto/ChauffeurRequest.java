package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChauffeurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le CIN est obligatoire")
    private String cin;

    private String telephone;

    private LocalDate dateEmbauche;
    private Long camionId; // optionnel

    @NotNull(message = "Le salaire est obligatoire")
    @Positive(message = "Le salaire doit être positif")
    private BigDecimal salaire;
}
