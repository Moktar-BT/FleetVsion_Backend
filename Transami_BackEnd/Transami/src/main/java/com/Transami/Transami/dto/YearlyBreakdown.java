// src/main/java/com/Transami/Transami/dto/YearlyBreakdown.java
package com.Transami.Transami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class YearlyBreakdown {
    private int year;
    private List<MonthlyEntry> months;   // always 12 entries
    private BigDecimal annualTotal;      // sum of the 12 months
}