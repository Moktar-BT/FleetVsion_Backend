package com.Transami.Transami.dto;

import com.Transami.Transami.enums.FrequenceRappel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RappelChargeRequest {

    @NotNull(message = "Le template est obligatoire")
    private Long templateId;

    @NotNull(message = "La fréquence est obligatoire")
    private FrequenceRappel frequence;

    @NotNull(message = "La prochaine date est obligatoire")
    private LocalDate prochaineDate;

    private Integer joursAvant;
}
