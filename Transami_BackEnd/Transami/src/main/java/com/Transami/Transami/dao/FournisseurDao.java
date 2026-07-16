package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FournisseurDao extends JpaRepository<Fournisseur, Long> {

    // Simple filter by adminId
    List<Fournisseur> findAllByAdminId(Long adminId);

    // Find by ID and adminId
    Optional<Fournisseur> findByIdAndAdminId(Long id, Long adminId);
}