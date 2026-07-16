package com.Transami.Transami.dao;

import com.Transami.Transami.entity.ChargeTemplate;
import com.Transami.Transami.enums.TypeCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChargeTemplateDao extends JpaRepository<ChargeTemplate, Long> {

    List<ChargeTemplate> findAllByAdminId(Long adminId);

    List<ChargeTemplate> findAllByAdminIdAndType(Long adminId, TypeCharge type);

    List<ChargeTemplate> findAllByAdminIdAndActive(Long adminId, boolean active);

    Optional<ChargeTemplate> findByIdAndAdminId(Long id, Long adminId);

    boolean existsByLibelleAndAdminId(String libelle, Long adminId);

    List<ChargeTemplate> findAllByCamionIdAndAdminId(Long camionId, Long adminId);

    List<ChargeTemplate> findAllByChauffeurIdAndAdminId(Long chauffeurId, Long adminId);

    List<ChargeTemplate> findAllByRemorqueIdAndAdminId(Long remorqueId, Long adminId);
}