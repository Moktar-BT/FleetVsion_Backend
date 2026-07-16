package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RappelVidangeResponse {

    private Long id;

    // ── Camion info ───────────────────────────────────────────────────────────
    private Long camionId;
    private String camionMatricule;
    private String camionModele;

    /**
     * Kilométrage actuel du camion (depuis Camion.mileage).
     * Peut être null si aucun bon carburant n'a encore été enregistré.
     */
    private Double kmActuel;

    // ── Vidange info ──────────────────────────────────────────────────────────
    private Double kmDerniereVidange;
    private Double intervalleKm;
    private Double kmProchaineVidange;
    private LocalDate dateDerniereVidange;
    private String notes;
    private boolean actif;

    // ── Statut calculé ────────────────────────────────────────────────────────
    /**
     * DEPASSEE   : kmActuel >= kmProchaineVidange
     * PROCHE     : (kmProchaineVidange - kmActuel) <= seuilAlerte (500 km par défaut)
     * OK         : tout va bien
     * INCONNU    : kmActuel est null
     */
    private String statut;

    /**
     * Km restants avant la prochaine vidange.
     * Négatif si dépassée. Null si kmActuel inconnu.
     */
    private Double kmRestants;

    /**
     * Pourcentage d'avancement depuis la dernière vidange (0–100+).
     * Null si kmActuel inconnu.
     */
    private Double pourcentageAvancement;
}