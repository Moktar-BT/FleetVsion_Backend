package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "codes_produit",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le code est obligatoire")
    @Column(nullable = false)
    private String code;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(columnDefinition = "TEXT")   // ← fix : illimité au lieu de VARCHAR(255)
    private String description;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    @Column(name = "prix_unitaire", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @NotBlank(message = "L'unité est obligatoire")
    @Column(name = "unite", nullable = false)
    private String unit;

    @NotNull(message = "La TVA est obligatoire")
    @Positive(message = "La TVA doit être positive")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal vat;
}