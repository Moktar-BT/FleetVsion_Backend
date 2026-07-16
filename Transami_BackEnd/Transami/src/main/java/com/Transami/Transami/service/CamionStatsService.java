package com.Transami.Transami.service;

import com.Transami.Transami.dao.BonCarburantDao;
import com.Transami.Transami.dao.BonDeLivraisonDao;
import com.Transami.Transami.dao.ChargeDao;
import com.Transami.Transami.dao.ReparationDao;
import com.Transami.Transami.dto.MonthlyEntry;
import com.Transami.Transami.dto.YearlyBreakdown;
import com.Transami.Transami.entity.BonCarburant;
import com.Transami.Transami.entity.BonDeLivraison;
import com.Transami.Transami.entity.Charge;
import com.Transami.Transami.entity.Reparation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CamionStatsService {

    private final BonDeLivraisonDao bonDeLivraisonDao;
    private final BonCarburantDao   bonCarburantDao;
    private final ReparationDao     reparationDao;
    private final ChargeDao         chargeDao;

    // ── Revenue breakdown ────────────────────────────────────────────────────

    public List<YearlyBreakdown> buildRevenueBreakdown(Long camionId) {
        List<BonDeLivraison> bdls = bonDeLivraisonDao.findAllByCamionId(camionId);
        Map<Integer, Map<Integer, BigDecimal>> byYearMonth = new TreeMap<>();
        for (BonDeLivraison b : bdls) {
            int y = b.getDate().getYear();
            int m = b.getDate().getMonthValue();
            byYearMonth
                    .computeIfAbsent(y, k -> new TreeMap<>())
                    .merge(m, b.getMontantTtc(), BigDecimal::add);
        }
        return toBreakdownList(byYearMonth);
    }

    // ── Fuel cost breakdown ──────────────────────────────────────────────────

    public List<YearlyBreakdown> buildFuelCostBreakdown(Long camionId, Long adminId) {
        List<BonCarburant> bons = bonCarburantDao.findAllByCamionIdAndAdminId(camionId, adminId);
        Map<Integer, Map<Integer, BigDecimal>> byYearMonth = new TreeMap<>();
        for (BonCarburant b : bons) {
            int y = b.getDate().getYear();
            int m = b.getDate().getMonthValue();
            byYearMonth
                    .computeIfAbsent(y, k -> new TreeMap<>())
                    .merge(m, b.getMontantTotal(), BigDecimal::add);
        }
        return toBreakdownList(byYearMonth);
    }

    // ── Repair cost breakdown ────────────────────────────────────────────────

    public List<YearlyBreakdown> buildRepairCostBreakdown(Long camionId, Long adminId) {
        List<Reparation> reps = reparationDao.findAllByCamionIdAndAdminId(camionId, adminId);
        Map<Integer, Map<Integer, BigDecimal>> byYearMonth = new TreeMap<>();
        for (Reparation r : reps) {
            int y = r.getDate().getYear();
            int m = r.getDate().getMonthValue();
            byYearMonth
                    .computeIfAbsent(y, k -> new TreeMap<>())
                    .merge(m, r.getCout(), BigDecimal::add);
        }
        return toBreakdownList(byYearMonth);
    }

    // ── Charge cost breakdown (dépenses liées à ce camion) ──────────────────

    public List<YearlyBreakdown> buildChargeCostBreakdown(Long camionId, Long adminId) {
        List<Charge> charges = chargeDao.findAllByCamionIdAndAdminId(camionId, adminId);
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

    // ── Last maintenance date ────────────────────────────────────────────────

    public LocalDate getLastMaintenanceDate(Long camionId, Long adminId) {
        return reparationDao
                .findTopByCamionIdAndAdminIdOrderByDateDesc(camionId, adminId)
                .map(Reparation::getDate)
                .orElse(null);
    }

    // ── Fuel consumption (L/100km) from second-to-last BonCarburant ─────────

    public Double getFuelConsumption(Long camionId, Long adminId) {
        List<BonCarburant> top2 = bonCarburantDao
                .findTop2ByCamionIdOrderByKilometrageDesc(camionId, adminId);
        if (top2.size() >= 2) {
            return top2.get(1).getConsommationReelle();
        }
        if (top2.size() == 1) {
            return top2.get(0).getConsommationReelle();
        }
        return null;
    }

    // ── Private helper ───────────────────────────────────────────────────────

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