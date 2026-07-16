// src/main/java/com/Transami/Transami/dto/ReparationRequest.java
package com.Transami.Transami.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReparationRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private Long camionId;

    @NotBlank
    private String typeReparation;

    @NotNull
    @Positive
    private BigDecimal cout;

    private String notes;
}