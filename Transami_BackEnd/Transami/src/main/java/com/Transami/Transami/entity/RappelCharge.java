package com.Transami.Transami.entity;

import com.Transami.Transami.enums.FrequenceRappel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "rappels_charge")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RappelCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ChargeTemplate template;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequenceRappel frequence;

    @NotNull
    @Column(name = "prochaine_date", nullable = false)
    private LocalDate prochaineDate;

    @Column(name = "jours_avant", nullable = false)
    @Builder.Default
    private Integer joursAvant = 15;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;
}
