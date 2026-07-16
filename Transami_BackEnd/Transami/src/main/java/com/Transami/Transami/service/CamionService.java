package com.Transami.Transami.service;

import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.AdminDao;
import com.Transami.Transami.dao.BonCarburantDao;
import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.ChauffeurDao;
import com.Transami.Transami.dao.RemorqueDao;
import com.Transami.Transami.dto.CamionRequest;
import com.Transami.Transami.dto.CamionResponse;
import com.Transami.Transami.entity.Admin;
import com.Transami.Transami.entity.BonCarburant;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Chauffeur;
import com.Transami.Transami.entity.Remorque;
import com.Transami.Transami.enums.FuelType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CamionService {

    private final CamionDao          camionDao;
    private final AdminDao           adminDao;
    private final BonCarburantDao    bonCarburantDao;
    private final CamionStatsService statsService;
    private final ClientService      clientService;
    private final ChauffeurDao       chauffeurDao;
    private final RemorqueDao        remorqueDao;
    private final ChargeTemplateService chargeTemplateService;

    public CamionResponse create(CamionRequest request, Long adminId) {
        Admin admin = adminDao.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        if (camionDao.existsByMatriculeAndAdminId(request.getMatricule(), adminId)) {
            throw new RuntimeException("Un camion avec ce matricule existe déjà : " + request.getMatricule());
        }

        FuelType fuelType = request.getFuelType() != null ? request.getFuelType() : FuelType.DIESEL;

        Chauffeur chauffeur = null;
        String nomChauffeur = request.getNomChauffeur();

        if (request.getChauffeurId() != null) {
            chauffeur = chauffeurDao.findByIdAndAdminId(request.getChauffeurId(), adminId)
                    .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé ou accès refusé"));
            nomChauffeur = chauffeur.getPrenom() + " " + chauffeur.getNom();
        } else if (nomChauffeur == null || nomChauffeur.trim().isEmpty()) {
            throw new RuntimeException("Le nom du chauffeur ou l'ID du chauffeur est obligatoire");
        }

        Camion camion = Camion.builder()
                .matricule(request.getMatricule())
                .nomChauffeur(nomChauffeur)
                .chauffeur(chauffeur)
                .status(request.getStatus() != null ? request.getStatus() : true)
                .mileage(request.getMileage())
                .truckModel(request.getTruckModel())
                .capacityLiters(request.getCapacityLiters())
                .fuelType(fuelType)
                .purchaseDate(request.getPurchaseDate())
                .admin(admin)
                .build();

        return toResponse(camionDao.save(camion), adminId);
    }

    public List<CamionResponse> getAll(Long adminId) {
        return camionDao.findAllByAdminId(adminId).stream()
                .map(c -> toResponse(c, adminId))
                .collect(Collectors.toList());
    }

    public List<CamionResponse> getAllByStatus(Long adminId, boolean status) {
        return camionDao.findAllByAdminIdAndStatus(adminId, status).stream()
                .map(c -> toResponse(c, adminId))
                .collect(Collectors.toList());
    }

    public CamionResponse getById(Long id, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));
        return toResponse(camion, adminId);
    }

    public CamionResponse update(Long id, CamionRequest request, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        if (!camion.getMatricule().equals(request.getMatricule())
                && camionDao.existsByMatriculeAndAdminId(request.getMatricule(), adminId)) {
            throw new RuntimeException("Un camion avec ce matricule existe déjà : " + request.getMatricule());
        }

        FuelType fuelType = request.getFuelType() != null ? request.getFuelType() : FuelType.DIESEL;

        Chauffeur chauffeur = null;
        String nomChauffeur = request.getNomChauffeur();

        if (request.getChauffeurId() != null) {
            chauffeur = chauffeurDao.findByIdAndAdminId(request.getChauffeurId(), adminId)
                    .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé ou accès refusé"));
            nomChauffeur = chauffeur.getPrenom() + " " + chauffeur.getNom();
        } else if (nomChauffeur == null || nomChauffeur.trim().isEmpty()) {
            throw new RuntimeException("Le nom du chauffeur ou l'ID du chauffeur est obligatoire");
        }

        camion.setMatricule(request.getMatricule());
        camion.setNomChauffeur(nomChauffeur);
        camion.setChauffeur(chauffeur);
        if (request.getStatus() != null) camion.setStatus(request.getStatus());
        camion.setTruckModel(request.getTruckModel());
        camion.setCapacityLiters(request.getCapacityLiters());
        camion.setFuelType(fuelType);
        camion.setPurchaseDate(request.getPurchaseDate());

        return toResponse(camionDao.save(camion), adminId);
    }

    public CamionResponse updateStatus(Long id, boolean status, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));
        camion.setStatus(status);
        return toResponse(camionDao.save(camion), adminId);
    }

    public void syncMileageFromLatestBon(Long camionId, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(camionId, adminId)
                .orElse(null);
        if (camion == null) return;

        List<BonCarburant> top1 = bonCarburantDao
                .findTop2ByCamionIdOrderByKilometrageDesc(camionId, adminId);
        if (!top1.isEmpty() && top1.get(0).getKilometrageActuel() != null) {
            camion.setMileage(top1.get(0).getKilometrageActuel().doubleValue());
            camionDao.save(camion);
        }
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        List<Long> affectedClientIds = camion.getBonsDeLivraison().stream()
                .map(b -> b.getClient().getId())
                .distinct()
                .collect(Collectors.toList());

        // Supprimer les templates de charge liés à ce camion + leurs rappels
        // (les charges déjà générées sont conservées avec leur snapshot pour la traçabilité)
        chargeTemplateService.deleteAllByCamion(id, adminId);

        camionDao.delete(camion);
        camionDao.flush();

        affectedClientIds.forEach(clientService::recalculateTurnovers);
    }

    public CamionResponse toResponse(Camion camion, Long adminId) {
        Long camionId = camion.getId();

        Remorque remorque = remorqueDao.findByCamionIdAndAdminId(camionId, adminId).orElse(null);

        return CamionResponse.builder()
                .id(camionId)
                .matricule(camion.getMatricule())
                .nomChauffeur(camion.getNomChauffeur())
                .chauffeurId(camion.getChauffeur() != null ? camion.getChauffeur().getId() : null)
                .chauffeurNom(camion.getChauffeur() != null ? camion.getChauffeur().getPrenom() + " " + camion.getChauffeur().getNom() : null)
                .remorqueId(remorque != null ? remorque.getId() : null)
                .remorqueMatricule(remorque != null ? remorque.getMatricule() : null)
                .remorqueType(remorque != null ? remorque.getTypeRemorque() : null)
                .status(camion.isStatus())
                .mileage(camion.getMileage())
                .truckModel(camion.getTruckModel())
                .capacityLiters(camion.getCapacityLiters())
                .fuelType(camion.getFuelType())
                .purchaseDate(camion.getPurchaseDate())
                .adminId(camion.getAdmin().getId())
                .revenueBreakdown(statsService.buildRevenueBreakdown(camionId))
                .fuelCostBreakdown(statsService.buildFuelCostBreakdown(camionId, adminId))
                .repairCostBreakdown(statsService.buildRepairCostBreakdown(camionId, adminId))
                .chargeCostBreakdown(statsService.buildChargeCostBreakdown(camionId, adminId)) // ← nouveau
                .lastMaintenanceDate(statsService.getLastMaintenanceDate(camionId, adminId))
                .fuelConsumption(statsService.getFuelConsumption(camionId, adminId))
                .build();
    }
}