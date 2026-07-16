package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChauffeurResponse {

    private Long id;
    private Long adminId;
    private String nom;
    private String prenom;
    private String cin;
    private String telephone;
    private LocalDate dateEmbauche;
    private BigDecimal salaire;
    private boolean active;
    private String nomComplet;
    private Long camionId;
    private String camionMatricule;
}
