package com.Transami.Transami.dto;

import com.Transami.Transami.enums.StatutCharge;
import com.Transami.Transami.enums.TypeCharge;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EtatChargeRequest {

    private LocalDate dateFrom;
    private LocalDate dateTo;

    // Filtres optionnels — correspondent exactement aux paramètres
    // envoyés par chargeApi.downloadEtat() côté frontend
    private StatutCharge statut;
    private TypeCharge type;
    private String camionMatricule;
    private String chauffeurNom;
    private String remorqueMatricule;
}