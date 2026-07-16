package com.Transami.Transami.service;

import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.FournisseurDao;
import com.Transami.Transami.dto.FournisseurRequest;
import com.Transami.Transami.dto.FournisseurResponse;
import com.Transami.Transami.entity.Fournisseur;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurDao fournisseurDao;

    @Transactional
    public FournisseurResponse create(FournisseurRequest request, Long adminId) {
        Fournisseur fournisseur = Fournisseur.builder()
                .nom(request.getNom())
                .localisation(request.getLocalisation())
                .adminId(adminId)
                .build();

        return toResponse(fournisseurDao.save(fournisseur));
    }

    public List<FournisseurResponse> getAll(Long adminId) {
        return fournisseurDao.findAllByAdminId(adminId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FournisseurResponse getById(Long id, Long adminId) {
        Fournisseur f = fournisseurDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé ou accès refusé"));
        return toResponse(f);
    }

    public Fournisseur getEntityById(Long id) {
        return fournisseurDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
    }

    @Transactional
    public FournisseurResponse update(Long id, FournisseurRequest request, Long adminId) {
        Fournisseur f = fournisseurDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé ou accès refusé"));

        f.setNom(request.getNom());
        f.setLocalisation(request.getLocalisation());

        return toResponse(fournisseurDao.save(f));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        Fournisseur f = fournisseurDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé ou accès refusé"));
        fournisseurDao.delete(f);
    }

    private FournisseurResponse toResponse(Fournisseur f) {
        return FournisseurResponse.builder()
                .id(f.getId())
                .nom(f.getNom())
                .localisation(f.getLocalisation())
                .build();
    }
}