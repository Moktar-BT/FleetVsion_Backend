package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO léger retourné par GET /rappels-vidange/alertes
 * Utilisé par le badge dashboard — contient uniquement ce dont le frontend a besoin.
 */
@Data
@Builder
public class RappelVidangeAlerteSummary {

    private Long rappelId;
    private Long camionId;
    private String camionMatricule;
    private Double kmActuel;
    private Double kmProchaineVidange;
    private Double kmRestants;         // négatif si dépassée
    private String statut;             // "DEPASSEE" ou "PROCHE"
}