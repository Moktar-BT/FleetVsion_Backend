package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientDao extends JpaRepository<Client, Long> {

    // Simple filter by adminId
    List<Client> findAllByAdminId(Long adminId);

    // Find client by ID and adminId
    Optional<Client> findByIdAndAdminId(Long id, Long adminId);
}