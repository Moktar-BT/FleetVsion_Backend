package com.Transami.Transami.dto;

import com.Transami.Transami.enums.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BonDeLivraisonRequest {

    @NotBlank(message = "Le numéro est obligatoire")
    private String numero;

    private Long blNumFournisseur;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    // Quantité générique
    private Double quantite;

    @NotNull(message = "Le montant HT est obligatoire")
    @Positive(message = "Le montant HT doit être positif")
    private BigDecimal montantHt;

    @NotNull(message = "Le montant TTC est obligatoire")
    @Positive(message = "Le montant TTC doit être positif")
    private BigDecimal montantTtc;

    private DeliveryStatus statut;

    @NotNull(message = "Le camion est obligatoire")
    private Long camionId;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le code produit est obligatoire")
    private Long codeProduitId;

    private Long fournisseurId;
}