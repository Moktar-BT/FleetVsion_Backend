package com.Transami.Transami.dao;

import com.Transami.Transami.entity.PrixCarburant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrixCarburantDao extends JpaRepository<PrixCarburant, Long> {

    Optional<PrixCarburant> findByAdminId(Long adminId);
}
