package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "fournisseurs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nom", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {

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

    @OneToMany(mappedBy = "fournisseur", fetch = FetchType.LAZY)
    private List<BonDeLivraison> bonsDeLivraison;
}