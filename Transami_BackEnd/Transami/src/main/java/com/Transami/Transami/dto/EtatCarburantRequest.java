package com.Transami.Transami.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EtatCarburantRequest {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long stationId;   // nullable → all stations
    private Long camionId;    // nullable → all trucks
}