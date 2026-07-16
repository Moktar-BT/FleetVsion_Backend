// src/main/java/com/Transami/Transami/dto/CamionResponse.java
package com.Transami.Transami.dto;

import com.Transami.Transami.enums.FuelType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CamionResponse {

    private Long id;
    private String matricule;
    private String nomChauffeur;
    private Long chauffeurId;
    private String chauffeurNom;
    private Long remorqueId;
    private String remorqueMatricule;
    private String remorqueType;
    private boolean status;

    // ── derived / read-only ──────────────────────────────────────────────────
    /** Revenue per year → per month. e.g. [{year:2026, months:[{month:1,amount:0},…], annualTotal:…}] */
    private List<YearlyBreakdown> revenueBreakdown;

    /** Fuel cost per year → per month */
    private List<YearlyBreakdown> fuelCostBreakdown;

    /** Repair cost per year → per month */
    private List<YearlyBreakdown> repairCostBreakdown;

    /** Date of most recent Reparation (auto-derived) */
    private LocalDate lastMaintenanceDate;

    /** L/100 km — taken from the second-to-last BonCarburant's consommationReelle */
    private Double fuelConsumption;

    private Double mileage;
    private String truckModel;
    private Double capacityLiters;
    private FuelType fuelType;
    private LocalDate purchaseDate;
    private Long adminId;
    // ... champs existants (revenueBreakdown, fuelCostBreakdown, repairCostBreakdown, ...)
    private List<YearlyBreakdown> chargeCostBreakdown; // ← nouveau champ
}