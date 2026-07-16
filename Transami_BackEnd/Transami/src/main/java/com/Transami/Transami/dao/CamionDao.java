package com.Transami.Transami.dao;

import com.Transami.Transami.entity.BonDeLivraison;
import com.Transami.Transami.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CamionDao extends JpaRepository<Camion, Long> {

    List<Camion> findAllByAdminId(Long adminId);

    List<Camion> findAllByAdminIdAndStatus(Long adminId, boolean status);

    Optional<Camion> findByIdAndAdminId(Long id, Long adminId);

    boolean existsByIdAndAdminId(Long id, Long adminId);

    boolean existsByMatriculeAndAdminId(String matricule, Long adminId);

    Optional<Camion> findByMatriculeAndAdminId(String matricule, Long adminId);

    boolean existsByChauffeurIdAndAdminId(Long chauffeurId, Long adminId);

    Optional<Camion> findByChauffeurIdAndAdminId(Long chauffeurId, Long adminId);
}