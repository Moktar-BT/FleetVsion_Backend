package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class FactureRequest {

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Les droits de timbre sont obligatoires")
    @PositiveOrZero(message = "Les droits de timbre doivent être positifs ou zéro")
    private BigDecimal droitsTimbre;

    @NotNull(message = "La liste des bons de livraison est obligatoire")
    private List<Long> bonDeLivraisonIds;
}