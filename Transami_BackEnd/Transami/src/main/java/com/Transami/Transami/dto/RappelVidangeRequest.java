package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RappelVidangeRequest {

    @NotNull(message = "Le camion est obligatoire")
    private Long camionId;

    @NotNull(message = "Le kilométrage de la dernière vidange est obligatoire")
    @Positive(message = "Le kilométrage doit être positif")
    private Double kmDerniereVidange;

    @NotNull(message = "L'intervalle en km est obligatoire")
    @Positive(message = "L'intervalle doit être positif")
    private Double intervalleKm;

    /**
     * Date optionnelle de la dernière vidange (pour l'historique).
     */
    private LocalDate dateDerniereVidange;

    private String notes;
}