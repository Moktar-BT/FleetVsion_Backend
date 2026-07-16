package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Admin;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminDao extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Admin> findActiveByEmail(String email);
    Optional<Admin> findByMatriculeFiscale(String matriculeFiscale);
    boolean existsByMatriculeFiscale(String matriculeFiscale);

    @EntityGraph(attributePaths = {"telephones"})
    Optional<Admin> findWithTelephonesByEmail(String email);
}