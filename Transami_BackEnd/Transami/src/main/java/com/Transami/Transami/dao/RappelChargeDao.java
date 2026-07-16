package com.Transami.Transami.dao;

import com.Transami.Transami.entity.RappelCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RappelChargeDao extends JpaRepository<RappelCharge, Long> {

    List<RappelCharge> findAllByAdminId(Long adminId);

    Optional<RappelCharge> findByIdAndAdminId(Long id, Long adminId);

    @Query("""
        SELECT r FROM RappelCharge r
        WHERE r.adminId = :adminId
          AND r.actif = true
          AND DATEDIFF(r.prochaineDate, CURRENT_DATE) <= r.joursAvant
    """)
    List<RappelCharge> findRappelsEnAlerte(@Param("adminId") Long adminId);

    @Modifying
    @Query("DELETE FROM RappelCharge r WHERE r.template.id = :templateId")
    void deleteAllByTemplateId(@Param("templateId") Long templateId);
}