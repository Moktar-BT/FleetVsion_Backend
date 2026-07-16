package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "rappels_vidange",
        uniqueConstraints = @UniqueConstraint(columnNames = {"camion_id", "admin_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RappelVidange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id", nullable = false)
    private Camion camion;

    /**
     * Kilométrage au moment de la dernière vidange (saisi par l'admin).
     */
    @NotNull
    @Positive
    @Column(name = "km_derniere_vidange", nullable = false)
    private Double kmDerniereVidange;

    /**
     * Intervalle en km saisi par l'admin (ex: 10 000).
     */
    @NotNull
    @Positive
    @Column(name = "intervalle_km", nullable = false)
    private Double intervalleKm;

    /**
     * Calculé automatiquement = kmDerniereVidange + intervalleKm.
     * Mis à jour à chaque modification.
     */
    @Column(name = "km_prochaine_vidange", nullable = false)
    private Double kmProchaineVidange;

    /**
     * Date de la dernière vidange effectuée (optionnelle, pour historique).
     */
    @Column(name = "date_derniere_vidange")
    private LocalDate dateDerniereVidange;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    // ── Helper : recalcule kmProchaineVidange automatiquement ────────────────
    @PrePersist
    @PreUpdate
    public void calculerProchaineVidange() {
        if (kmDerniereVidange != null && intervalleKm != null) {
            this.kmProchaineVidange = kmDerniereVidange + intervalleKm;
        }
    }
}