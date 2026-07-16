package com.Transami.Transami.dao;

import com.Transami.Transami.entity.BonDeLivraison;
import com.Transami.Transami.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BonDeLivraisonDao extends JpaRepository<BonDeLivraison, Long> {

    List<BonDeLivraison> findAllByCamionAdminId(Long adminId);
    Optional<BonDeLivraison> findByIdAndCamionAdminId(Long id, Long adminId);
    boolean existsByNumeroAndAdminId(String numero, Long adminId);
    List<BonDeLivraison> findAllByCamionAdminIdAndStatut(Long adminId, DeliveryStatus statut);
    List<BonDeLivraison> findAllByCamionAdminIdAndClientId(Long adminId, Long clientId);
    List<BonDeLivraison> findAllByCamionAdminIdAndFournisseurId(Long adminId, Long fournisseurId);
    List<BonDeLivraison> findAllByCamionAdminIdAndDateBetween(Long adminId, LocalDate from, LocalDate to);
    // inside BonDeLivraisonDao
    /** All BDL for a camion grouped by year+month (used for breakdown). */
    @Query("""
    SELECT b FROM BonDeLivraison b
    WHERE b.camion.id = :camionId
""")
    List<BonDeLivraison> findAllByCamionId(@Param("camionId") Long camionId);
// (already exists — no change needed)

    @Query("SELECT COALESCE(SUM(b.montantTtc), 0) FROM BonDeLivraison b WHERE b.client.id = :clientId AND b.date BETWEEN :start AND :end")
    BigDecimal sumPrixTotalByClientAndDateRange(@Param("clientId") Long clientId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

}