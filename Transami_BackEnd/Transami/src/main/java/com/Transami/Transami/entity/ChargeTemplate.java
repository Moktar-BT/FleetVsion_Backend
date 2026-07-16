package com.Transami.Transami.entity;

import com.Transami.Transami.enums.CategorieCharge;
import com.Transami.Transami.enums.TypeCharge;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "charges_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"libelle", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @NotBlank
    @Column(nullable = false)
    private String libelle;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCharge type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieCharge categorie;

    @Column(name = "montant_reference", precision = 19, scale = 3)
    private BigDecimal montantReference;

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "chauffeur_id")
    private Long chauffeurId;

    @Column(name = "remorque_id")
    private Long remorqueId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
