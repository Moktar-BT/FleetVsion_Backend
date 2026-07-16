package com.Transami.Transami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FournisseurResponse {

    private Long id;
    private String nom;
    private String localisation;
}