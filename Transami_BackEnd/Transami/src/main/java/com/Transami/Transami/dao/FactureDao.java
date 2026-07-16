package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactureDao extends JpaRepository<Facture, Long> {

    List<Facture> findAllByAdminId(Long adminId);
    
    Optional<Facture> findByIdAndAdminId(Long id, Long adminId);
}