package com.Transami.Transami.entity;

import com.Transami.Transami.enums.FuelType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "camions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"matricule", "admin_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"chauffeur", "admin", "bonsDeLivraison", "bonsCarburant", "reparations"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Le matricule est obligatoire")
    @Column(name = "matricule", nullable = false)
    private String matricule;

    @NotBlank(message = "Le nom du chauffeur est obligatoire")
    @Column(name = "nom_chauffeur", nullable = false)
    private String nomChauffeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id")
    private Chauffeur chauffeur;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private boolean status = true;

    // Kilométrage calculé automatiquement depuis les bons carburant — optionnel
    @Column(nullable = true)
    private Double mileage;

    @NotBlank(message = "Le modèle est obligatoire")
    @Column(name = "modele_camion", nullable = false)
    private String truckModel;

    @Column(name = "capacite_litres")
    private Double capacityLiters;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_carburant")
    @Builder.Default
    private FuelType fuelType = FuelType.DIESEL;

    @Column(name = "date_achat")
    private LocalDate purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BonDeLivraison> bonsDeLivraison;

    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BonCarburant> bonsCarburant;

    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reparation> reparations;
}