// src/main/java/com/Transami/Transami/dto/MonthlyEntry.java
package com.Transami.Transami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyEntry {
    private int month;        // 1–12
    private BigDecimal amount;
}