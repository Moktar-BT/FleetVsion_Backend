package com.Transami.Transami.service;

import com.Transami.Transami.dao.BonCarburantDao;
import com.Transami.Transami.dao.StationDao;
import com.Transami.Transami.dto.StationRequest;
import com.Transami.Transami.dto.StationResponse;
import com.Transami.Transami.entity.BonCarburant;
import com.Transami.Transami.entity.Station;
import com.Transami.Transami.enums.FuelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationDao stationDao;
    private final BonCarburantDao bonCarburantDao;

    @Transactional
    public StationResponse create(StationRequest request, Long adminId) {
        if (stationDao.existsByNomAndAdminId(request.getNom(), adminId)) {
            throw new RuntimeException("Une station avec ce nom existe déjà : " + request.getNom());
        }

        Station station = Station.builder()
                .nom(request.getNom())
                .localisation(request.getLocalisation())
                .adminId(adminId)
                .totalAnnuelle(BigDecimal.ZERO)
                .totalMensuelle(BigDecimal.ZERO)
                .totalDieselMois(BigDecimal.ZERO)
                .totalDiesel50Mois(BigDecimal.ZERO)
                .totalEssenceMois(BigDecimal.ZERO)
                .build();

        return toResponse(stationDao.save(station));
    }

    public List<StationResponse> getAll(Long adminId) {
        return stationDao.findAllByAdminId(adminId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public StationResponse getById(Long id, Long adminId) {
        Station station = stationDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée ou accès refusé"));
        return toResponse(station);
    }

    @Transactional
    public StationResponse update(Long id, StationRequest request, Long adminId) {
        Station station = stationDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée ou accès refusé"));

        if (!station.getNom().equals(request.getNom()) 
                && stationDao.existsByNomAndAdminId(request.getNom(), adminId)) {
            throw new RuntimeException("Une station avec ce nom existe déjà");
        }

        station.setNom(request.getNom());
        station.setLocalisation(request.getLocalisation());

        return toResponse(stationDao.save(station));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        Station station = stationDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée ou accès refusé"));
        stationDao.delete(station);
    }

    @Transactional
    public void recalculerTotaux(Long stationId) {
        Station station = stationDao.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));

        List<BonCarburant> allBons = bonCarburantDao.findAllByStationId(stationId);

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now;

        // Total annuel
        BigDecimal totalAnnuelle = allBons.stream()
                .map(BonCarburant::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total mensuel
        BigDecimal totalMensuelle = allBons.stream()
                .filter(b -> !b.getDate().isBefore(startOfMonth) && !b.getDate().isAfter(endOfMonth))
                .map(BonCarburant::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total diesel mois
        BigDecimal totalDieselMois = allBons.stream()
                .filter(b -> !b.getDate().isBefore(startOfMonth) && !b.getDate().isAfter(endOfMonth))
                .filter(b -> b.getTypCarburant() == FuelType.DIESEL)
                .map(BonCarburant::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total diesel 50 mois
        BigDecimal totalDiesel50Mois = allBons.stream()
                .filter(b -> !b.getDate().isBefore(startOfMonth) && !b.getDate().isAfter(endOfMonth))
                .filter(b -> b.getTypCarburant() == FuelType.DIESEL_50)
                .map(BonCarburant::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total essence mois
        BigDecimal totalEssenceMois = allBons.stream()
                .filter(b -> !b.getDate().isBefore(startOfMonth) && !b.getDate().isAfter(endOfMonth))
                .filter(b -> b.getTypCarburant() == FuelType.ESSENCE)
                .map(BonCarburant::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        station.setTotalAnnuelle(totalAnnuelle);
        station.setTotalMensuelle(totalMensuelle);
        station.setTotalDieselMois(totalDieselMois);
        station.setTotalDiesel50Mois(totalDiesel50Mois);
        station.setTotalEssenceMois(totalEssenceMois);

        stationDao.save(station);
    }

    private StationResponse toResponse(Station station) {
        return StationResponse.builder()
                .id(station.getId())
                .nom(station.getNom())
                .localisation(station.getLocalisation())
                .totalAnnuelle(station.getTotalAnnuelle())
                .totalMensuelle(station.getTotalMensuelle())
                .totalDieselMois(station.getTotalDieselMois())
                .totalDiesel50Mois(station.getTotalDiesel50Mois())
                .totalEssenceMois(station.getTotalEssenceMois())
                .build();
    }
}
