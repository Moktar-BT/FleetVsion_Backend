package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CamionFuelStatsResponse {

    private String matricule;
    private String nomChauffeur;
    private Double consommationMoyenne;
    private Double consommationDernier;
    private BigDecimal coutTotalCarburant;
    private BigDecimal coutMensuelCarburant;
    private Integer nombreBons;
    private String statut;
    private String message;
}
