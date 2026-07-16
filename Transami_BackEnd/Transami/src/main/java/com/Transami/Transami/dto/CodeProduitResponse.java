package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CodeProduitResponse {

    private Long id;
    private String code;
    private String description;
    private BigDecimal unitPrice;
    private String unit;
    private BigDecimal vat;
}