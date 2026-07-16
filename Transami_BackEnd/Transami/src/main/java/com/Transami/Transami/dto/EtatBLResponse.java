package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EtatBLResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private LocalDate dateEdition;

    private List<LigneBL> lignes;

    // Totaux
    private int    nombreBL;
    private Double totalQuantite;
    private BigDecimal totalMontantHt;
    private BigDecimal totalMontantTtc;

    // ── Ligne ────────────────────────────────────────────────────
    @Data
    @Builder
    public static class LigneBL {
        private String     numero;           // N° bon de livraison
        private LocalDate  date;
        private String     clientNom;
        private Long       blNumFournisseur; // null = ""
        private String     camionMatricule;
        private Double     quantite;
        private String     codeProduitUnit;  // unité (tonne, voyage…)
        private BigDecimal prixUnitaire;
        private BigDecimal montantHt;
        private BigDecimal montantTtc;
        private String     numeroFacture;    // null = "Non facturé"
    }
}