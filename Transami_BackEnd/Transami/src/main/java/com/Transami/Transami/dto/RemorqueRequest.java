package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RemorqueRequest {

    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;

    private Long camionId;

    private String typeRemorque;

    private Double capaciteTonnes;

    private LocalDate dateAchat;
}
