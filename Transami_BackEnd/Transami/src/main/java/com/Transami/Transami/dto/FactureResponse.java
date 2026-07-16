package com.Transami.Transami.dto;

import com.Transami.Transami.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// com.Transami.Transami.dto.FactureResponse

@Data
@Builder
public class FactureResponse {

    private Long id;
    private String numero;   // new
    private LocalDate date;
    private Long clientId;
    private String clientNom;
    private BigDecimal montantHTVA;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private InvoiceStatus statut;
    private BigDecimal droitsTimbre;
    private List<BonDeLivraisonSummary> bonsDeLivraison;

    @Data
    @Builder
    public static class BonDeLivraisonSummary {
        private Long id;
        private String numero;
        private BigDecimal montantHt;
    }
}