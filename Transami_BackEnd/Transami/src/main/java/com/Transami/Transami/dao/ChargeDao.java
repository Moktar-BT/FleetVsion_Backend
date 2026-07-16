package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Charge;
import com.Transami.Transami.enums.StatutCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChargeDao extends JpaRepository<Charge, Long> {

    List<Charge> findAllByAdminId(Long adminId);

    List<Charge> findAllByAdminIdAndTemplateId(Long adminId, Long templateId);

    List<Charge> findAllByTemplateId(Long templateId);

    List<Charge> findAllByAdminIdAndDateBetween(Long adminId, LocalDate from, LocalDate to);

    List<Charge> findAllByAdminIdAndStatut(Long adminId, StatutCharge statut);

    // Toutes les charges liées à un camion précis (via le snapshot camionId)
    List<Charge> findAllByCamionIdAndAdminId(Long camionId, Long adminId);

    Optional<Charge> findByIdAndAdminId(Long id, Long adminId);

    @Query("SELECT COALESCE(SUM(c.montant), 0) FROM Charge c WHERE c.adminId = :adminId AND c.date BETWEEN :from AND :to")
    BigDecimal sumMontantByAdminIdAndDateBetween(@Param("adminId") Long adminId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT c.templateCategorie, COALESCE(SUM(c.montant), 0) FROM Charge c WHERE c.adminId = :adminId AND YEAR(c.date) = :year GROUP BY c.templateCategorie")
    List<Object[]> sumByCategorie(@Param("adminId") Long adminId, @Param("year") int year);

    @Modifying
    @Query("UPDATE Charge c SET c.template = null WHERE c.template.id = :templateId")
    void detachTemplateById(@Param("templateId") Long templateId);
}