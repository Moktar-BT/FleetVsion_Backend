package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CodeProduitRequest {

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    private String description;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    private BigDecimal unitPrice;

    @NotBlank(message = "L'unité est obligatoire")
    private String unit;

    @NotNull(message = "La TVA est obligatoire")
    @Positive
    private BigDecimal vat;
}