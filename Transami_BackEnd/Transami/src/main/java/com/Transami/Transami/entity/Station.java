package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nom", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "La localisation est obligatoire")
    @Column(nullable = false)
    private String localisation;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Builder.Default
    @Column(name = "total_annuelle", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAnnuelle = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_mensuelle", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMensuelle = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_diesel_mois", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDieselMois = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_diesel50_mois", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDiesel50Mois = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_essence_mois", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalEssenceMois = BigDecimal.ZERO;
}
