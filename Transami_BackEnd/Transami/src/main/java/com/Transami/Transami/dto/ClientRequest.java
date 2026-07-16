package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "La localisation est obligatoire")
    private String localisation;

    private String matF;

    // monthlyTurnover and annualTurnover are now calculated, not accepted in request
}