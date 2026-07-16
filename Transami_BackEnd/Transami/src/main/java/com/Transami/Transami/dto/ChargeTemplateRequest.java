package com.Transami.Transami.dto;

import com.Transami.Transami.enums.CategorieCharge;
import com.Transami.Transami.enums.TypeCharge;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeTemplateRequest {

    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;

    @NotNull(message = "Le type est obligatoire")
    private TypeCharge type;

    @NotNull(message = "La catégorie est obligatoire")
    private CategorieCharge categorie;

    private BigDecimal montantReference;

    private Long camionId;

    private Long chauffeurId;

    private Long remorqueId;
}
