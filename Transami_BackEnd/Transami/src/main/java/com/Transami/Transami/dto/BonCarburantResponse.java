package com.Transami.Transami.dto;

import com.Transami.Transami.enums.FuelType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BonCarburantResponse {

    private Long id;
    private LocalDate date;
    private Long camionId;
    private String camionMatricule;
    private String numero;
    private Long stationId;
    private String stationNom;
    private Double kilometrageActuel;
    private Double quantiteLitres;
    private FuelType typCarburant;
    private BigDecimal prixLitre;
    private BigDecimal montantTotal;
    private Double distanceParcourue;
    private Double consommationReelle;
    private String consommationStatut;
    private String consommationMessage;
}
