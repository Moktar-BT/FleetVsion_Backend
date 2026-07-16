package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "remorques",
        uniqueConstraints = @UniqueConstraint(columnNames = {"matricule", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Remorque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @NotBlank
    @Column(nullable = false)
    private String matricule;

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "type_remorque")
    private String typeRemorque;

    @Column(name = "capacite_tonnes")
    private Double capaciteTonnes;

    @Column(name = "date_achat")
    private LocalDate dateAchat;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
