// src/main/java/com/Transami/Transami/dao/ReparationDao.java
package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Reparation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReparationDao extends JpaRepository<Reparation, Long> {

    List<Reparation> findAllByAdminId(Long adminId);
    List<Reparation> findAllByCamionIdAndAdminId(Long camionId, Long adminId);
    List<Reparation> findAllByAdminIdAndDateBetween(Long adminId, LocalDate from, LocalDate to);
    List<Reparation> findAllByCamionIdAndAdminIdAndDateBetween(Long camionId, Long adminId, LocalDate from, LocalDate to);

    // NEW
    List<Reparation> findAllByCamionId(Long camionId);
    Optional<Reparation> findTopByCamionIdAndAdminIdOrderByDateDesc(Long camionId, Long adminId);
}