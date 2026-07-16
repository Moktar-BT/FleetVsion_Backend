package com.Transami.Transami.dto;

import com.Transami.Transami.enums.CategorieCharge;
import com.Transami.Transami.enums.FrequenceRappel;
import com.Transami.Transami.enums.TypeCharge;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RappelChargeResponse {

    private Long id;
    private Long adminId;
    private FrequenceRappel frequence;
    private LocalDate prochaineDate;
    private Integer joursAvant;
    private boolean actif;
    private Long templateId;
    private String templateLibelle;
    private CategorieCharge templateCategorie;
    private TypeCharge templateType;
    private BigDecimal montantReference;
    private String camionMatricule;
    private String chauffeurNom;
    private Long joursRestants;
    private String statut;
}
