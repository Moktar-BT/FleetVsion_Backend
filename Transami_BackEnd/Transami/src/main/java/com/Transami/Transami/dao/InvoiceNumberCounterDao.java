package com.Transami.Transami.dao;

import com.Transami.Transami.entity.InvoiceNumberCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface InvoiceNumberCounterDao extends JpaRepository<InvoiceNumberCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM InvoiceNumberCounter c WHERE c.year = :year AND c.adminId = :adminId")
    Optional<InvoiceNumberCounter> findByYearAndAdminIdWithLock(@Param("year") Integer year, @Param("adminId") Long adminId);
}