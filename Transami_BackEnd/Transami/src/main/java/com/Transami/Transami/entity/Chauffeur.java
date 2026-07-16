package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "chauffeurs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cin", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chauffeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @NotBlank
    @Column(nullable = false)
    private String prenom;

    @NotBlank
    @Column(nullable = false, unique = false)
    private String cin;

    @Column
    private String telephone;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal salaire;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
