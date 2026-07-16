package com.Transami.Transami.dto;

import com.Transami.Transami.enums.CategorieCharge;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChargeAlerteSummary {

    private Long rappelId;
    private String templateLibelle;
    private CategorieCharge templateCategorie;
    private LocalDate prochaineDate;
    private Long joursRestants;
    private BigDecimal montantReference;
    private String camionMatricule;
    private String chauffeurNom;
    private String statut;
}
