package com.Transami.Transami.dto;

import com.Transami.Transami.enums.StatutCharge;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChargeRequest {

    @NotNull(message = "Le template est obligatoire")
    private Long templateId;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    private StatutCharge statut;

    private String notes;
}
