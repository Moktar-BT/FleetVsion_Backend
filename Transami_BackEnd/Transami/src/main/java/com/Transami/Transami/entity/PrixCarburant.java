package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "prix_carburant",
       uniqueConstraints = @UniqueConstraint(columnNames = {"admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrixCarburant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false, unique = true)
    private Long adminId;

    @NotNull(message = "Le prix de l'essence est obligatoire")
    @Positive(message = "Le prix de l'essence doit être positif")
    @Column(name = "prix_essence", nullable = false, precision = 10, scale = 3)
    private BigDecimal prixEssence;

    @NotNull(message = "Le prix du diesel est obligatoire")
    @Positive(message = "Le prix du diesel doit être positif")
    @Column(name = "prix_diesel", nullable = false, precision = 10, scale = 3)
    private BigDecimal prixDiesel;

    @NotNull(message = "Le prix du diesel 50 est obligatoire")
    @Positive(message = "Le prix du diesel 50 doit être positif")
    @Column(name = "prix_diesel50", nullable = false, precision = 10, scale = 3)
    private BigDecimal prixDiesel50;
}
