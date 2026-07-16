// src/main/java/com/Transami/Transami/dto/ReparationResponse.java
package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ReparationResponse {
    private Long id;
    private LocalDate date;
    private Long camionId;
    private String camionMatricule;
    private String typeReparation;
    private BigDecimal cout;
    private String notes;
}