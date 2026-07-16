package com.Transami.Transami.entity;

import com.Transami.Transami.enums.CategorieCharge;
import com.Transami.Transami.enums.StatutCharge;
import com.Transami.Transami.enums.TypeCharge;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "charges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    // Le template peut être supprimé (camion/chauffeur/remorque supprimé) :
    // la relation devient optionnelle, on garde uniquement les infos "snapshot" ci-dessous.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ChargeTemplate template;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCharge statut = StatutCharge.EN_ATTENTE;

    @Column(length = 500)
    private String notes;

    // ── Snapshot conservé pour la traçabilité même si le template/camion/chauffeur/remorque est supprimé ──

    @Column(name = "template_libelle")
    private String templateLibelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type")
    private TypeCharge templateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_categorie")
    private CategorieCharge templateCategorie;

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "camion_matricule")
    private String camionMatricule;

    @Column(name = "chauffeur_id")
    private Long chauffeurId;

    @Column(name = "chauffeur_nom")
    private String chauffeurNom;

    @Column(name = "remorque_id")
    private Long remorqueId;

    @Column(name = "remorque_matricule")
    private String remorqueMatricule;
}