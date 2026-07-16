package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "clients",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nom", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "La localisation est obligatoire")
    @Column(nullable = false)
    private String localisation;

    @Column(name = "mat_f")
    private String matF;

    @NotNull(message = "Le chiffre d'affaire mensuel est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le chiffre d'affaire mensuel doit être non négatif")
    @Column(name = "chiffre_affaire_mensuel", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyTurnover;

    @NotNull(message = "Le chiffre d'affaire annuel est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le chiffre d'affaire annuel doit être non négatif")
    @Column(name = "chiffre_affaire_annuel", nullable = false, precision = 19, scale = 2)
    private BigDecimal annualTurnover;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<BonDeLivraison> bonsDeLivraison;
}