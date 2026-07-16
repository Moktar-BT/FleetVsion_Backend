package com.Transami.Transami.dao;

import com.Transami.Transami.entity.RappelVidange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RappelVidangeDao extends JpaRepository<RappelVidange, Long> {

    List<RappelVidange> findAllByAdminId(Long adminId);

    Optional<RappelVidange> findByIdAndAdminId(Long id, Long adminId);

    // Un seul rappel actif par camion par admin
    Optional<RappelVidange> findByCamionIdAndAdminId(Long camionId, Long adminId);

    boolean existsByCamionIdAndAdminId(Long camionId, Long adminId);

    /**
     * Récupère tous les rappels actifs dont le km du camion dépasse ou approche
     * le km de prochaine vidange (pour afficher les alertes dashboard).
     *
     * "approche" = le camion est à moins de [seuilKm] km de la prochaine vidange.
     */
    @Query("""
        SELECT r FROM RappelVidange r
        WHERE r.adminId = :adminId
          AND r.actif = true
          AND (
              r.camion.mileage >= r.kmProchaineVidange
              OR (r.camion.mileage IS NOT NULL
                  AND (r.kmProchaineVidange - r.camion.mileage) <= :seuilKm)
          )
    """)
    List<RappelVidange> findRappelsAlertes(
            @Param("adminId") Long adminId,
            @Param("seuilKm") Double seuilKm
    );

    /**
     * Tous les rappels actifs d'un admin avec leur camion chargé.
     */
    @Query("""
        SELECT r FROM RappelVidange r
        JOIN FETCH r.camion
        WHERE r.adminId = :adminId
          AND r.actif = true
    """)
    List<RappelVidange> findAllActifsByAdminId(@Param("adminId") Long adminId);
}