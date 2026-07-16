// src/main/java/com/Transami/Transami/dto/CamionRequest.java
package com.Transami.Transami.dto;

import com.Transami.Transami.enums.FuelType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CamionRequest {

    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;

    // Optional if chauffeurId is provided (will be derived from Chauffeur entity)
    private String nomChauffeur;

    private Long chauffeurId;

    @NotBlank(message = "Le modèle est obligatoire")
    private String truckModel;

    // Optionnel — calculé automatiquement depuis les bons carburant
    private Double mileage;

    private Boolean status;

    private Double capacityLiters;

    // Défaut DIESEL si non fourni
    private FuelType fuelType = FuelType.DIESEL;

    private LocalDate purchaseDate;
}