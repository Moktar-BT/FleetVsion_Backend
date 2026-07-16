package com.Transami.Transami.dto;

import com.Transami.Transami.enums.CategorieCharge;
import com.Transami.Transami.enums.StatutCharge;
import com.Transami.Transami.enums.TypeCharge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeResponse {
    private Long id;
    private Long adminId;
    private LocalDate date;
    private BigDecimal montant;
    private StatutCharge statut;
    private String notes;

    private Long templateId; // null si le template a été supprimé
    private String templateLibelle;
    private TypeCharge templateType;
    private CategorieCharge templateCategorie;

    private Long camionId;
    private String camionMatricule;

    private Long chauffeurId;
    private String chauffeurNom;

    private Long remorqueId;
    private String remorqueMatricule;
}