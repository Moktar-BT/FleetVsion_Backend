package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StationResponse {

    private Long id;
    private String nom;
    private String localisation;
    private BigDecimal totalAnnuelle;
    private BigDecimal totalMensuelle;
    private BigDecimal totalDieselMois;
    private BigDecimal totalDiesel50Mois;
    private BigDecimal totalEssenceMois;
}
