package com.Transami.Transami.service;

import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.ClientDao;
import com.Transami.Transami.dao.BonDeLivraisonDao;
import com.Transami.Transami.dto.ClientRequest;
import com.Transami.Transami.dto.ClientResponse;
import com.Transami.Transami.entity.Client;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientDao clientDao;
    private final BonDeLivraisonDao bonDeLivraisonDao;

    // CREATE
    @Transactional
    public ClientResponse create(ClientRequest request, Long adminId) {
        Client client = Client.builder()
                .nom(request.getNom())
                .localisation(request.getLocalisation())
                .matF(request.getMatF())
                .monthlyTurnover(BigDecimal.ZERO)
                .annualTurnover(BigDecimal.ZERO)
                .adminId(adminId)
                .build();

        return toResponse(clientDao.save(client));
    }

    // READ ALL
    public List<ClientResponse> getAll(Long adminId) {
        return clientDao.findAllByAdminId(adminId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // READ ONE
    public ClientResponse getById(Long id, Long adminId) {
        Client client = clientDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé ou accès refusé"));
        return toResponse(client);
    }

    // READ ONE (internal use, no admin check)
    public Client getEntityById(Long id) {
        return clientDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    // UPDATE name/location
    @Transactional
    public ClientResponse update(Long id, ClientRequest request, Long adminId) {
        Client client = clientDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé ou accès refusé"));

        client.setNom(request.getNom());
        client.setLocalisation(request.getLocalisation());
        client.setMatF(request.getMatF());

        return toResponse(clientDao.save(client));
    }

    // DELETE
    @Transactional
    public void delete(Long id, Long adminId) {
        Client client = clientDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé ou accès refusé"));
        clientDao.delete(client);
    }

    // ============================================
    // RECALCULATE AND STORE TURNOVERS
    // ============================================
    @Transactional
    public void recalculateTurnovers(Long clientId) {
        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now;
        BigDecimal monthly = bonDeLivraisonDao.sumPrixTotalByClientAndDateRange(clientId, startOfMonth, endOfMonth);

        LocalDate startOfYear = now.withDayOfYear(1);
        LocalDate endOfYear = now;
        BigDecimal annual = bonDeLivraisonDao.sumPrixTotalByClientAndDateRange(clientId, startOfYear, endOfYear);

        client.setMonthlyTurnover(monthly);
        client.setAnnualTurnover(annual);
        clientDao.save(client);
    }

    // ============================================
    // MAPPER (just returns stored values)
    // ============================================
    private ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .localisation(client.getLocalisation())
                .matF(client.getMatF())
                .monthlyTurnover(client.getMonthlyTurnover())
                .annualTurnover(client.getAnnualTurnover())
                .build();
    }
}