package com.Transami.Transami.entity;

import com.Transami.Transami.enums.FuelType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bons_carburant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonCarburant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "numero", length = 50)
    private String numero;

    @NotNull(message = "La date est obligatoire")
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id", nullable = false)
    private Camion camion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @NotNull
    @Positive
    @Column(name = "kilometrage_actuel", nullable = false)
    private Double kilometrageActuel;

    @NotNull
    @Positive
    @Column(name = "quantite_litres", nullable = false)
    private Double quantiteLitres;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "typ_carburant", nullable = false)
    private FuelType typCarburant;

    @NotNull
    @Positive
    @Column(name = "prix_litre", nullable = false, precision = 10, scale = 3)
    private BigDecimal prixLitre;

    @NotNull
    @Column(name = "montant_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "distance_parcourue")
    private Double distanceParcourue;

    @Column(name = "consommation_reelle")
    private Double consommationReelle;
}