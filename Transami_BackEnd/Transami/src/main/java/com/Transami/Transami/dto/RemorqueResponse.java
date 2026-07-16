package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RemorqueResponse {

    private Long id;
    private Long adminId;
    private String matricule;
    private Long camionId;
    private String camionMatricule;
    private String typeRemorque;
    private Double capaciteTonnes;
    private LocalDate dateAchat;
    private boolean active;
}
