package com.Transami.Transami.service;

import com.Transami.Transami.dao.BonDeLivraisonDao;
import com.Transami.Transami.dao.ClientDao;
import com.Transami.Transami.dao.FactureDao;
import com.Transami.Transami.dto.FactureRequest;
import com.Transami.Transami.dto.FactureResponse;
import com.Transami.Transami.entity.BonDeLivraison;
import com.Transami.Transami.entity.Client;
import com.Transami.Transami.entity.Facture;
import com.Transami.Transami.enums.DeliveryStatus;
import com.Transami.Transami.enums.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureDao factureDao;
    private final ClientDao clientDao;
    private final BonDeLivraisonDao bonDeLivraisonDao;
    private final InvoiceNumberService invoiceNumberService;

    // ========== GET ALL ==========
    public List<FactureResponse> getAll(Long adminId) {
        return factureDao.findAllByAdminId(adminId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ========== GET BY ID ==========
    public FactureResponse getById(Long id, Long adminId) {
        Facture facture = factureDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée ou accès refusé"));
        return toResponse(facture);
    }

    // ========== CREATE ==========
    @Transactional
    public FactureResponse create(FactureRequest request, Long adminId) {
        Client client = clientDao.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        List<BonDeLivraison> bdls = bonDeLivraisonDao.findAllById(request.getBonDeLivraisonIds());
        if (bdls.size() != request.getBonDeLivraisonIds().size()) {
            throw new RuntimeException("Certains bons de livraison n'existent pas");
        }

        boolean allSameClient = bdls.stream()
                .allMatch(bdl -> bdl.getClient().getId().equals(client.getId()));
        if (!allSameClient) {
            throw new RuntimeException(
                    "Tous les bons de livraison doivent appartenir au même client que la facture");
        }

        boolean alreadyFactured = bdls.stream()
                .anyMatch(bdl -> bdl.getFacture() != null
                        || DeliveryStatus.FACTURE.equals(bdl.getStatut()));
        if (alreadyFactured) {
            throw new RuntimeException(
                    "Un ou plusieurs bons de livraison sont déjà attachés à une facture");
        }

        // Compute totals from BDLs
        BigDecimal montantHTVA = bdls.stream()
                .map(BonDeLivraison::getMontantHt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantTVA = bdls.stream()
                .map(b -> b.getMontantTtc().subtract(b.getMontantHt()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantTTC = montantHTVA.add(montantTVA).add(request.getDroitsTimbre());

        // Generate invoice number based on the invoice date (to allow back‑dating)
        String numero = invoiceNumberService.generateNextNumber(request.getDate(), adminId);

        Facture facture = Facture.builder()
                .numero(numero)
                .adminId(adminId)
                .date(request.getDate())
                .client(client)
                .montantHTVA(montantHTVA)
                .montantTVA(montantTVA)
                .montantTTC(montantTTC)
                .statut(InvoiceStatus.Enattente)
                .droitsTimbre(request.getDroitsTimbre())
                .bonsDeLivraison(bdls)
                .build();

        for (BonDeLivraison bdl : bdls) {
            bdl.setFacture(facture);
            bdl.setStatut(DeliveryStatus.FACTURE);
        }

        Facture saved = factureDao.save(facture);
        return toResponse(saved);
    }

    // ========== DELETE ==========
    @Transactional
    public void deleteById(Long id, Long adminId) {
        Facture facture = factureDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée ou accès refusé"));
        
        // Sauvegarder le numéro avant suppression
        String numeroFacture = facture.getNumero();
        
        for (BonDeLivraison bdl : facture.getBonsDeLivraison()) {
            bdl.setFacture(null);
            bdl.setStatut(DeliveryStatus.NON_FACTURE);
        }
        
        factureDao.delete(facture);
        
        // Décrémenter le compteur si c'était la dernière facture générée
        invoiceNumberService.decrementIfLastNumber(numeroFacture, adminId);
    }

    // ========== UPDATE STATUT ==========
    @Transactional
    public FactureResponse updateStatut(Long id, InvoiceStatus statut, Long adminId) {
        Facture facture = factureDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée ou accès refusé"));
        facture.setStatut(statut);
        return toResponse(factureDao.save(facture));
    }

    // ========== MAPPING ==========
    private FactureResponse toResponse(Facture facture) {
        List<FactureResponse.BonDeLivraisonSummary> bdlSummaries = facture.getBonsDeLivraison()
                .stream()
                .map(bdl -> FactureResponse.BonDeLivraisonSummary.builder()
                        .id(bdl.getId())
                        .numero(bdl.getNumero())
                        .montantHt(bdl.getMontantHt())
                        .build())
                .collect(Collectors.toList());

        return FactureResponse.builder()
                .id(facture.getId())
                .date(facture.getDate())
                .numero(facture.getNumero())
                .clientId(facture.getClient().getId())
                .clientNom(facture.getClient().getNom())
                .montantHTVA(facture.getMontantHTVA())
                .montantTVA(facture.getMontantTVA())
                .montantTTC(facture.getMontantTTC())
                .statut(facture.getStatut())
                .droitsTimbre(facture.getDroitsTimbre())
                .bonsDeLivraison(bdlSummaries)
                .build();
    }
}