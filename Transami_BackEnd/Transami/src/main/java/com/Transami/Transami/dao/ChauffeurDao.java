package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Chauffeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChauffeurDao extends JpaRepository<Chauffeur, Long> {

    List<Chauffeur> findAllByAdminId(Long adminId);

    List<Chauffeur> findAllByAdminIdAndActive(Long adminId, boolean active);

    Optional<Chauffeur> findByIdAndAdminId(Long id, Long adminId);

    boolean existsByCinAndAdminId(String cin, Long adminId);
}
