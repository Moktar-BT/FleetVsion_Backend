package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EtatBLRequest {

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateFrom;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateTo;

    // Filtres optionnels
    private Long clientId;       // null = tous les clients
    private Long camionId;       // null = tous les camions
    private Long fournisseurId;  // null = tous les fournisseurs
    private String statut;       // "FACTURE" | "NON_FACTURE" | null = tous
}