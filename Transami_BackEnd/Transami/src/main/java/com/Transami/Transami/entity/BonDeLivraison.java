package com.Transami.Transami.entity;

import com.Transami.Transami.enums.DeliveryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bons_de_livraison", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"numero", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonDeLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro est obligatoire")
    @Column(nullable = false)
    private String numero;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "bl_num_fournisseur")
    private Long blNumFournisseur;

    @NotNull(message = "La date est obligatoire")
    @Column(nullable = false)
    private LocalDate date;

    // Quantité générique (tonnes, heures, jours, m³, voyages...)
    @Column(name = "quantite", nullable = false)
    private Double quantite;

    @NotNull(message = "Le prix total HT est obligatoire")
    @Positive(message = "Le prix total HT doit être positif")
    @Column(name = "montant_ht", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantHt;

    @NotNull(message = "Le montant TTC est obligatoire")
    @Column(name = "montant_ttc", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantTtc;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus statut = DeliveryStatus.NON_FACTURE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id", nullable = false)
    private Camion camion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_produit_id", nullable = false)
    private CodeProduit codeProduit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "facture_id")
    private Facture facture;
}