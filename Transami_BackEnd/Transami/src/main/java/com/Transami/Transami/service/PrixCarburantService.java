package com.Transami.Transami.service;

import com.Transami.Transami.dao.PrixCarburantDao;
import com.Transami.Transami.dto.PrixCarburantRequest;
import com.Transami.Transami.dto.PrixCarburantResponse;
import com.Transami.Transami.entity.PrixCarburant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrixCarburantService {

    private final PrixCarburantDao prixCarburantDao;

    @Transactional
    public PrixCarburantResponse createOrUpdate(PrixCarburantRequest request, Long adminId) {
        PrixCarburant prixCarburant = prixCarburantDao.findByAdminId(adminId)
                .orElse(PrixCarburant.builder()
                        .adminId(adminId)
                        .build());

        prixCarburant.setPrixEssence(request.getPrixEssence());
        prixCarburant.setPrixDiesel(request.getPrixDiesel());
        prixCarburant.setPrixDiesel50(request.getPrixDiesel50());

        return toResponse(prixCarburantDao.save(prixCarburant));
    }

    public PrixCarburantResponse getByAdmin(Long adminId) {
        PrixCarburant prixCarburant = prixCarburantDao.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Prix du carburant non configurés pour cet admin"));
        return toResponse(prixCarburant);
    }

    private PrixCarburantResponse toResponse(PrixCarburant prixCarburant) {
        return PrixCarburantResponse.builder()
                .id(prixCarburant.getId())
                .prixEssence(prixCarburant.getPrixEssence())
                .prixDiesel(prixCarburant.getPrixDiesel())
                .prixDiesel50(prixCarburant.getPrixDiesel50())
                .build();
    }
}
