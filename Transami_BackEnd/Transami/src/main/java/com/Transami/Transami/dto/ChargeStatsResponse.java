package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ChargeStatsResponse {

    private BigDecimal totalAnnee;
    private BigDecimal totalMois;
    private Map<String, BigDecimal> totalParCategorie;
}
