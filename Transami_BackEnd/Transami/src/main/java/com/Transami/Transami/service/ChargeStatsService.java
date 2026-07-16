package com.Transami.Transami.service;

import com.Transami.Transami.dao.ChargeDao;
import com.Transami.Transami.dto.MonthlyEntry;
import com.Transami.Transami.dto.YearlyBreakdown;
import com.Transami.Transami.entity.Charge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChargeStatsService {

    private final ChargeDao chargeDao;

    /**
     * Répartition mensuelle/annuelle de TOUTES les charges de l'admin
     * (indépendamment du camion/chauffeur/remorque lié), pour le chart
     * global "Charges" du dashboard.
     */
    @Transactional(readOnly = true)
    public List<YearlyBreakdown> buildGlobalChargeBreakdown(Long adminId) {
        List<Charge> charges = chargeDao.findAllByAdminId(adminId);
        Map<Integer, Map<Integer, BigDecimal>> byYearMonth = new TreeMap<>();
        for (Charge c : charges) {
            int y = c.getDate().getYear();
            int m = c.getDate().getMonthValue();
            byYearMonth
                    .computeIfAbsent(y, k -> new TreeMap<>())
                    .merge(m, c.getMontant(), BigDecimal::add);
        }
        return toBreakdownList(byYearMonth);
    }

    /**
     * Total des charges de l'admin pour un mois précis (du 1er au dernier jour du mois).
     */
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyTotal(Long adminId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return chargeDao.sumMontantByAdminIdAndDateBetween(adminId, start, end);
    }

    /**
     * Total des charges de l'admin pour une année précise (1er janvier → 31 décembre).
     */
    @Transactional(readOnly = true)
    public BigDecimal getAnnualTotal(Long adminId, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return chargeDao.sumMontantByAdminIdAndDateBetween(adminId, start, end);
    }

    private List<YearlyBreakdown> toBreakdownList(
            Map<Integer, Map<Integer, BigDecimal>> byYearMonth) {

        List<YearlyBreakdown> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, BigDecimal>> yearEntry : byYearMonth.entrySet()) {
            int year = yearEntry.getKey();
            Map<Integer, BigDecimal> monthMap = yearEntry.getValue();

            List<MonthlyEntry> months = new ArrayList<>();
            BigDecimal annual = BigDecimal.ZERO;
            for (int m = 1; m <= 12; m++) {
                BigDecimal amt = monthMap.getOrDefault(m, BigDecimal.ZERO);
                months.add(new MonthlyEntry(m, amt));
                annual = annual.add(amt);
            }
            result.add(new YearlyBreakdown(year, months, annual));
        }
        return result;
    }
}