package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrixCarburantRequest {

    @NotNull(message = "Le prix de l'essence est obligatoire")
    @Positive(message = "Le prix de l'essence doit être positif")
    private BigDecimal prixEssence;

    @NotNull(message = "Le prix du diesel est obligatoire")
    @Positive(message = "Le prix du diesel doit être positif")
    private BigDecimal prixDiesel;

    @NotNull(message = "Le prix du diesel 50 est obligatoire")
    @Positive(message = "Le prix du diesel 50 doit être positif")
    private BigDecimal prixDiesel50;
}
