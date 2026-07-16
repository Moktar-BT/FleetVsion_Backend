package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClientResponse {

    private Long id;
    private String nom;
    private String localisation;
    private String matF;
    private BigDecimal monthlyTurnover;
    private BigDecimal annualTurnover;
}