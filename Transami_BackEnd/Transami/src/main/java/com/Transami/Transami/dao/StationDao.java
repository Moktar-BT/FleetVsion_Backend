package com.Transami.Transami.dao;

import com.Transami.Transami.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationDao extends JpaRepository<Station, Long> {

    boolean existsByNomAndAdminId(String nom, Long adminId);

    List<Station> findAllByAdminId(Long adminId);

    Optional<Station> findByIdAndAdminId(Long id, Long adminId);
}
