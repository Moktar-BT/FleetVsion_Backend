package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Remorque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RemorqueDao extends JpaRepository<Remorque, Long> {

    List<Remorque> findAllByAdminId(Long adminId);

    List<Remorque> findAllByAdminIdAndCamionId(Long adminId, Long camionId);

    Optional<Remorque> findByIdAndAdminId(Long id, Long adminId);

    Optional<Remorque> findByCamionIdAndAdminId(Long camionId, Long adminId);

    boolean existsByMatriculeAndAdminId(String matricule, Long adminId);
}
