package com.Transami.Transami.dto;

import com.Transami.Transami.enums.FuelType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BonCarburantRequest {

    private String numero;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le camion est obligatoire")
    private Long camionId;

    @NotNull(message = "La station est obligatoire")
    private Long stationId;

    @NotNull
    @Positive
    private Double kilometrageActuel;

    @NotNull
    @Positive
    private Double quantiteLitres;

    @NotNull
    private FuelType typCarburant;

    @NotNull
    @Positive
    private BigDecimal prixLitre;
}