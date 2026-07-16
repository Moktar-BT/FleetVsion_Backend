package com.Transami.Transami.dao;

import com.Transami.Transami.entity.BonCarburant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BonCarburantDao extends JpaRepository<BonCarburant, Long> {

    List<BonCarburant> findAllByAdminId(Long adminId);

    List<BonCarburant> findAllByCamionIdAndAdminId(Long camionId, Long adminId);

    List<BonCarburant> findAllByStationId(Long stationId);

    List<BonCarburant> findAllByStationIdAndAdminId(Long stationId, Long adminId);

    // Ancien (gardé pour compatibilité update)
    Optional<BonCarburant> findTopByCamionIdAndAdminIdAndIdNotOrderByDateDescKilometrageActuelDesc(
            Long camionId, Long adminId, Long excludeId);

    // Bon précédent (kilométrage strictement inférieur, excluant le bon courant)
    @Query("""
        SELECT b FROM BonCarburant b
        WHERE b.camion.id = :camionId
          AND b.adminId = :adminId
          AND (:excludeId IS NULL OR b.id <> :excludeId)
          AND b.kilometrageActuel < :kilometrage
        ORDER BY b.kilometrageActuel DESC
        LIMIT 1
    """)
    Optional<BonCarburant> findPreviousBon(
            @Param("camionId") Long camionId,
            @Param("adminId") Long adminId,
            @Param("kilometrage") Double kilometrage,
            @Param("excludeId") Long excludeId);

    // Bon suivant (kilométrage strictement supérieur, excluant le bon courant)
    @Query("""
        SELECT b FROM BonCarburant b
        WHERE b.camion.id = :camionId
          AND b.adminId = :adminId
          AND (:excludeId IS NULL OR b.id <> :excludeId)
          AND b.kilometrageActuel > :kilometrage
        ORDER BY b.kilometrageActuel ASC
        LIMIT 1
    """)
    Optional<BonCarburant> findNextBon(
            @Param("camionId") Long camionId,
            @Param("adminId") Long adminId,
            @Param("kilometrage") Double kilometrage,
            @Param("excludeId") Long excludeId);

    // Unicité numéro par camion + admin
    boolean existsByNumeroAndCamionIdAndAdminId(String numero, Long camionId, Long adminId);

    boolean existsByNumeroAndCamionIdAndAdminIdAndIdNot(
            String numero, Long camionId, Long adminId, Long excludeId);
    /** Two most-recent bons for a camion ordered by kilometrage DESC (to get second-to-last). */
    @Query("""
    SELECT b FROM BonCarburant b
    WHERE b.camion.id = :camionId
      AND b.adminId  = :adminId
    ORDER BY b.kilometrageActuel DESC
    LIMIT 2
""")
    List<BonCarburant> findTop2ByCamionIdOrderByKilometrageDesc(
            @Param("camionId") Long camionId,
            @Param("adminId")  Long adminId);
}