package com.Transami.Transami.service;

import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.ReparationDao;
import com.Transami.Transami.dto.ReparationRequest;
import com.Transami.Transami.dto.ReparationResponse;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Reparation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReparationService {

    private final ReparationDao reparationDao;
    private final CamionDao     camionDao;

    @Transactional
    public ReparationResponse create(ReparationRequest request, Long adminId) {
        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        Reparation r = Reparation.builder()
                .adminId(adminId)
                .date(request.getDate())
                .camion(camion)
                .typeReparation(request.getTypeReparation())
                .cout(request.getCout())
                .notes(request.getNotes())
                .build();

        return toResponse(reparationDao.save(r));
    }

    public List<ReparationResponse> getAll(Long adminId, Long camionId) {
        List<Reparation> list = camionId != null
                ? reparationDao.findAllByCamionIdAndAdminId(camionId, adminId)
                : reparationDao.findAllByAdminId(adminId);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ReparationResponse getById(Long id, Long adminId) {
        Reparation r = reparationDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Réparation non trouvée"));
        if (!r.getAdminId().equals(adminId)) throw new RuntimeException("Accès refusé");
        return toResponse(r);
    }

    @Transactional
    public ReparationResponse update(Long id, ReparationRequest request, Long adminId) {
        Reparation r = reparationDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Réparation non trouvée"));
        if (!r.getAdminId().equals(adminId)) throw new RuntimeException("Accès refusé");

        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        r.setDate(request.getDate());
        r.setCamion(camion);
        r.setTypeReparation(request.getTypeReparation());
        r.setCout(request.getCout());
        r.setNotes(request.getNotes());

        return toResponse(reparationDao.save(r));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        Reparation r = reparationDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Réparation non trouvée"));
        if (!r.getAdminId().equals(adminId)) throw new RuntimeException("Accès refusé");
        reparationDao.delete(r);
    }

    public List<ReparationResponse> getByPeriode(
            Long adminId, LocalDate dateFrom, LocalDate dateTo, Long camionId) {
        List<Reparation> list = camionId != null
                ? reparationDao.findAllByCamionIdAndAdminIdAndDateBetween(camionId, adminId, dateFrom, dateTo)
                : reparationDao.findAllByAdminIdAndDateBetween(adminId, dateFrom, dateTo);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReparationResponse toResponse(Reparation r) {
        return ReparationResponse.builder()
                .id(r.getId())
                .date(r.getDate())
                .camionId(r.getCamion().getId())
                .camionMatricule(r.getCamion().getMatricule())
                .typeReparation(r.getTypeReparation())
                .cout(r.getCout())
                .notes(r.getNotes())
                .build();
    }
}