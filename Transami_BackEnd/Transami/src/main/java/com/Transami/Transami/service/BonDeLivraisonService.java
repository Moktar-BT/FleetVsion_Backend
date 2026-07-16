package com.Transami.Transami.service;

import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.BonDeLivraisonDao;
import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.ClientDao;
import com.Transami.Transami.dao.CodeProduitDao;
import com.Transami.Transami.dao.FactureDao;
import com.Transami.Transami.dao.FournisseurDao;
import com.Transami.Transami.dto.BonDeLivraisonRequest;
import com.Transami.Transami.dto.BonDeLivraisonResponse;
import com.Transami.Transami.entity.BonDeLivraison;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Client;
import com.Transami.Transami.entity.CodeProduit;
import com.Transami.Transami.entity.Facture;
import com.Transami.Transami.entity.Fournisseur;
import com.Transami.Transami.enums.DeliveryStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BonDeLivraisonService {

    private final BonDeLivraisonDao bonDeLivraisonDao;
    private final CamionDao camionDao;
    private final ClientDao clientDao;
    private final CodeProduitDao codeProduitDao;
    private final FournisseurDao fournisseurDao;
    private final FactureDao factureDao;
    private final CamionService camionService;
    private final ClientService clientService;

    @Transactional
    public BonDeLivraisonResponse create(BonDeLivraisonRequest request, Long adminId) {
        if (bonDeLivraisonDao.existsByNumeroAndAdminId(request.getNumero(), adminId)) {
            throw new RuntimeException("Un bon de livraison avec ce numéro existe déjà : " + request.getNumero());
        }

        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));
        Client client = clientDao.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        CodeProduit codeProduit = codeProduitDao.findById(request.getCodeProduitId())
                .orElseThrow(() -> new RuntimeException("Code produit non trouvé"));

        // Gestion du fournisseur optionnel
        Fournisseur fournisseur = null;
        if (request.getFournisseurId() != null) {
            fournisseur = fournisseurDao.findById(request.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        }

        BonDeLivraison bdl = BonDeLivraison.builder()
                .numero(request.getNumero())
                .adminId(adminId)
                .blNumFournisseur(request.getBlNumFournisseur())
                .date(request.getDate())
                .quantite(request.getQuantite())
                .montantHt(request.getMontantHt())
                .montantTtc(request.getMontantTtc())
                .statut(request.getStatut() != null ? request.getStatut() : DeliveryStatus.NON_FACTURE)
                .camion(camion)
                .client(client)
                .codeProduit(codeProduit)
                .fournisseur(fournisseur)
                .build();

        BonDeLivraison saved = bonDeLivraisonDao.save(bdl);
        clientService.recalculateTurnovers(client.getId());

        return toResponse(saved);
    }

    public List<BonDeLivraisonResponse> getAll(Long adminId) {
        return bonDeLivraisonDao.findAllByCamionAdminId(adminId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BonDeLivraisonResponse> getAllByCamion(Long camionId, Long adminId) {
        camionDao.findByIdAndAdminId(camionId, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));
        return bonDeLivraisonDao.findAllByCamionId(camionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BonDeLivraisonResponse> getAllByStatut(Long adminId, DeliveryStatus statut) {
        return bonDeLivraisonDao.findAllByCamionAdminIdAndStatut(adminId, statut)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BonDeLivraisonResponse> getAllByClient(Long adminId, Long clientId) {
        return bonDeLivraisonDao.findAllByCamionAdminIdAndClientId(adminId, clientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BonDeLivraisonResponse> getAllByDateRange(Long adminId, LocalDate from, LocalDate to) {
        return bonDeLivraisonDao.findAllByCamionAdminIdAndDateBetween(adminId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BonDeLivraisonResponse getById(Long id, Long adminId) {
        BonDeLivraison bdl = bonDeLivraisonDao.findByIdAndCamionAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Bon de livraison non trouvé ou accès refusé"));
        return toResponse(bdl);
    }

    @Transactional
    public BonDeLivraisonResponse update(Long id, BonDeLivraisonRequest request, Long adminId) {
        BonDeLivraison bdl = bonDeLivraisonDao.findByIdAndCamionAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Bon de livraison non trouvé ou accès refusé"));

        if (!bdl.getNumero().equals(request.getNumero())
                && bonDeLivraisonDao.existsByNumeroAndAdminId(request.getNumero(), adminId)) {
            throw new RuntimeException("Un bon de livraison avec ce numéro existe déjà");
        }

        Camion camion = camionDao.findByIdAndAdminId(request.getCamionId(), adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));
        Client client = clientDao.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        CodeProduit codeProduit = codeProduitDao.findById(request.getCodeProduitId())
                .orElseThrow(() -> new RuntimeException("Code produit non trouvé"));

        // Gestion du fournisseur optionnel
        Fournisseur fournisseur = null;
        if (request.getFournisseurId() != null) {
            fournisseur = fournisseurDao.findById(request.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        }

        Long ancienCamionId = bdl.getCamion().getId();
        Long ancienClientId = bdl.getClient().getId();

        // Gestion de la facture existante
        if (bdl.getFacture() != null) {
            Facture facture = bdl.getFacture();
            for (BonDeLivraison autresBdl : facture.getBonsDeLivraison()) {
                if (!autresBdl.getId().equals(bdl.getId())) {
                    autresBdl.setFacture(null);
                    autresBdl.setStatut(DeliveryStatus.NON_FACTURE);
                    bonDeLivraisonDao.save(autresBdl);
                }
            }
            bdl.setFacture(null);
            bonDeLivraisonDao.save(bdl);
            factureDao.delete(facture);
        }

        // Mise à jour des champs
        bdl.setNumero(request.getNumero());
        bdl.setBlNumFournisseur(request.getBlNumFournisseur());
        bdl.setDate(request.getDate());
        bdl.setQuantite(request.getQuantite());
        bdl.setMontantHt(request.getMontantHt());
        bdl.setMontantTtc(request.getMontantTtc());
        if (request.getStatut() != null) bdl.setStatut(request.getStatut());
        bdl.setCamion(camion);
        bdl.setClient(client);
        bdl.setCodeProduit(codeProduit);
        bdl.setFournisseur(fournisseur);

        BonDeLivraison saved = bonDeLivraisonDao.save(bdl);

        // Recalculs des totaux si le camion ou le client a changé
        if (!ancienCamionId.equals(camion.getId())) {

        }

        if (!ancienClientId.equals(client.getId())) {
            clientService.recalculateTurnovers(ancienClientId);
        }
        clientService.recalculateTurnovers(client.getId());

        return toResponse(saved);
    }

    @Transactional
    public BonDeLivraisonResponse updateStatut(Long id, DeliveryStatus statut, Long adminId) {
        BonDeLivraison bdl = bonDeLivraisonDao.findByIdAndCamionAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Bon de livraison non trouvé ou accès refusé"));
        bdl.setStatut(statut);
        BonDeLivraison saved = bonDeLivraisonDao.save(bdl);

        clientService.recalculateTurnovers(bdl.getClient().getId());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        BonDeLivraison bdl = bonDeLivraisonDao.findByIdAndCamionAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Bon de livraison non trouvé ou accès refusé"));

        Long camionId = bdl.getCamion().getId();
        Long clientId = bdl.getClient().getId();

        if (bdl.getFacture() != null) {
            Facture facture = bdl.getFacture();
            for (BonDeLivraison autresBdl : facture.getBonsDeLivraison()) {
                if (!autresBdl.getId().equals(bdl.getId())) {
                    autresBdl.setFacture(null);
                    autresBdl.setStatut(DeliveryStatus.NON_FACTURE);
                    bonDeLivraisonDao.save(autresBdl);
                }
            }
            bdl.setFacture(null);
            bonDeLivraisonDao.save(bdl);
            factureDao.delete(facture);
        }

        bonDeLivraisonDao.delete(bdl);

        clientService.recalculateTurnovers(clientId);
    }

    private BonDeLivraisonResponse toResponse(BonDeLivraison bdl) {
        return BonDeLivraisonResponse.builder()
                .id(bdl.getId())
                .numero(bdl.getNumero())
                .blNumFournisseur(bdl.getBlNumFournisseur())
                .date(bdl.getDate())
                .quantite(bdl.getQuantite())
                .montantHt(bdl.getMontantHt())
                .montantTtc(bdl.getMontantTtc())
                .statut(bdl.getStatut())
                .camionId(bdl.getCamion().getId())
                .camionModele(bdl.getCamion().getTruckModel())
                .codeProduitId(bdl.getCodeProduit().getId())
                .codeProduitCode(bdl.getCodeProduit().getCode())
                .codeProduitUnit(bdl.getCodeProduit().getUnit())
                .clientId(bdl.getClient().getId())
                .clientNom(bdl.getClient().getNom())
                .fournisseurId(bdl.getFournisseur() != null ? bdl.getFournisseur().getId() : null)
                .fournisseurNom(bdl.getFournisseur() != null ? bdl.getFournisseur().getNom() : null)
                .build();
    }
}