// src/main/java/com/Transami/Transami/entity/Reparation.java
package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reparations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reparation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id", nullable = false)
    private Camion camion;

    @NotBlank
    @Column(name = "type_reparation", nullable = false, length = 150)
    private String typeReparation;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal cout;

    @Column(length = 500)
    private String notes;
}