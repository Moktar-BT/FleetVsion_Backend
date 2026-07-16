package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PrixCarburantResponse {

    private Long id;
    private BigDecimal prixEssence;
    private BigDecimal prixDiesel;
    private BigDecimal prixDiesel50;
}
