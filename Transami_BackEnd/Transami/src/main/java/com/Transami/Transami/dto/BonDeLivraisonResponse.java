package com.Transami.Transami.dto;

import com.Transami.Transami.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BonDeLivraisonResponse {

    private Long id;
    private String numero;
    private Long blNumFournisseur;
    private LocalDate date;
    private Double quantite;
    private BigDecimal montantHt;
    private BigDecimal montantTtc;
    private DeliveryStatus statut;

    private Long camionId;
    private String camionModele;

    private Long codeProduitId;
    private String codeProduitCode;
    private String codeProduitUnit;   // unité du produit (pour affichage)

    private Long clientId;
    private String clientNom;

    private Long fournisseurId;
    private String fournisseurNom;
}