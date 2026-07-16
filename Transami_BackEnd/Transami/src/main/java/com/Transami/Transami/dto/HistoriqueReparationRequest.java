package com.Transami.Transami.dto;
import lombok.Data;
import java.time.LocalDate;
@Data
public class HistoriqueReparationRequest{
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long camionId; // optionnel
}
