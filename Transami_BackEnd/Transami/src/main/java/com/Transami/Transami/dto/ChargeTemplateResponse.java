package com.Transami.Transami.dto;

import com.Transami.Transami.enums.CategorieCharge;
import com.Transami.Transami.enums.TypeCharge;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ChargeTemplateResponse {

    private Long id;
    private Long adminId;
    private String libelle;
    private TypeCharge type;
    private CategorieCharge categorie;
    private BigDecimal montantReference;
    private Long camionId;
    private String camionMatricule;
    private Long chauffeurId;
    private String chauffeurNom;
    private Long remorqueId;
    private String remorqueMatricule;
    private boolean active;
}
