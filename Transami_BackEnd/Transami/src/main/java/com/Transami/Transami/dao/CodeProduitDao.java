package com.Transami.Transami.dao;

import com.Transami.Transami.entity.CodeProduit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeProduitDao extends JpaRepository<CodeProduit, Long> {

    boolean existsByCodeAndAdminId(String code, Long adminId);

    Optional<CodeProduit> findByCodeAndAdminId(String code, Long adminId);
    
    List<CodeProduit> findAllByAdminId(Long adminId);
    
    Optional<CodeProduit> findByIdAndAdminId(Long id, Long adminId);
}