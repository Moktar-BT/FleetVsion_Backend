package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FournisseurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "La localisation est obligatoire")
    private String localisation;
}