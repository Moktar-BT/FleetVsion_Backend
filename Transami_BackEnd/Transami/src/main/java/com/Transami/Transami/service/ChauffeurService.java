package com.Transami.Transami.service;

import com.Transami.Transami.dao.CamionDao;
import com.Transami.Transami.dao.ChauffeurDao;
import com.Transami.Transami.dto.ChauffeurRequest;
import com.Transami.Transami.dto.ChauffeurResponse;
import com.Transami.Transami.entity.Camion;
import com.Transami.Transami.entity.Chauffeur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChauffeurService {

    private final ChauffeurDao chauffeurDao;
    private final CamionDao camionDao;
    private final ChargeTemplateService chargeTemplateService;


    @Transactional
    public ChauffeurResponse create(ChauffeurRequest request, Long adminId) {
        if (chauffeurDao.existsByCinAndAdminId(request.getCin(), adminId)) {
            throw new RuntimeException("Un chauffeur avec ce CIN existe déjà");
        }

        Chauffeur chauffeur = Chauffeur.builder()
                .adminId(adminId)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .cin(request.getCin())
                .telephone(request.getTelephone())
                .dateEmbauche(request.getDateEmbauche())
                .salaire(request.getSalaire())
                .build();

        Chauffeur saved = chauffeurDao.save(chauffeur);

        // Assignation du camion choisi dans le formulaire (si fourni)
        assignerCamion(saved, request.getCamionId(), adminId);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ChauffeurResponse> getAll(Long adminId, Boolean active) {
        List<Chauffeur> chauffeurs = active != null
                ? chauffeurDao.findAllByAdminIdAndActive(adminId, active)
                : chauffeurDao.findAllByAdminId(adminId);
        return chauffeurs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChauffeurResponse getById(Long id, Long adminId) {
        Chauffeur chauffeur = chauffeurDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé ou accès refusé"));
        return toResponse(chauffeur);
    }

    @Transactional
    public ChauffeurResponse update(Long id, ChauffeurRequest request, Long adminId) {
        Chauffeur chauffeur = chauffeurDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé ou accès refusé"));

        if (!chauffeur.getCin().equals(request.getCin()) &&
                chauffeurDao.existsByCinAndAdminId(request.getCin(), adminId)) {
            throw new RuntimeException("Un chauffeur avec ce CIN existe déjà");
        }

        // IMPORTANT : on ne touche jamais à chauffeur.setActive(...) ici.
        // Le statut actif/inactif est géré exclusivement par toggleActive().
        chauffeur.setNom(request.getNom());
        chauffeur.setPrenom(request.getPrenom());
        chauffeur.setCin(request.getCin());
        chauffeur.setTelephone(request.getTelephone());
        chauffeur.setDateEmbauche(request.getDateEmbauche());
        chauffeur.setSalaire(request.getSalaire());

        Chauffeur saved = chauffeurDao.save(chauffeur);

        // Met à jour l'assignation du camion (assigne, réassigne, ou détache selon le cas)
        assignerCamion(saved, request.getCamionId(), adminId);

        return toResponse(saved);
    }

    @Transactional
    public ChauffeurResponse toggleActive(Long id, boolean active, Long adminId) {
        Chauffeur chauffeur = chauffeurDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé ou accès refusé"));

        chauffeur.setActive(active);
        return toResponse(chauffeurDao.save(chauffeur));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        Chauffeur chauffeur = chauffeurDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé ou accès refusé"));

        // Détacher le chauffeur du camion qui lui est assigné (au lieu de bloquer la suppression)
        camionDao.findByChauffeurIdAndAdminId(id, adminId).ifPresent(camion -> {
            camion.setChauffeur(null);
            camionDao.save(camion);
        });

        // Supprimer les templates de charge liés à ce chauffeur + leurs rappels
        // (les charges déjà générées sont conservées avec leur snapshot pour la traçabilité)
        chargeTemplateService.deleteAllByChauffeur(id, adminId);

        chauffeurDao.delete(chauffeur);
    }

    /**
     * Synchronise la relation Camion ↔ Chauffeur d'après le camionId choisi dans le formulaire.
     *
     * - Si le chauffeur était déjà assigné à un autre camion, ce camion est détaché
     *   (chauffeur = null, nomChauffeur remis à "Aucun chauffeur disponible").
     * - Si un nouveau camionId est fourni, ce camion est assigné au chauffeur.
     *   Si ce camion était déjà assigné à un AUTRE chauffeur, cet autre chauffeur en est détaché.
     * - Le statut actif/inactif du chauffeur n'est jamais modifié par cette méthode.
     */
    private void assignerCamion(Chauffeur chauffeur, Long newCamionId, Long adminId) {
        Camion currentlyAssigned = camionDao.findByChauffeurIdAndAdminId(chauffeur.getId(), adminId).orElse(null);

        // Aucun changement demandé
        if (currentlyAssigned != null && newCamionId != null && currentlyAssigned.getId().equals(newCamionId)) {
            return;
        }

        // Détacher l'ancien camion si le chauffeur en avait un différent
        if (currentlyAssigned != null) {
            currentlyAssigned.setChauffeur(null);
            currentlyAssigned.setNomChauffeur("Aucun chauffeur disponible");
            camionDao.save(currentlyAssigned);
        }

        if (newCamionId == null) {
            return; // Le chauffeur reste sans camion assigné
        }

        Camion nouveauCamion = camionDao.findByIdAndAdminId(newCamionId, adminId)
                .orElseThrow(() -> new RuntimeException("Camion non trouvé ou accès refusé"));

        // Si ce camion était assigné à un autre chauffeur, on le détache de cet autre chauffeur
        // (le nouveau chauffeur prend sa place)
        nouveauCamion.setChauffeur(chauffeur);
        nouveauCamion.setNomChauffeur(chauffeur.getPrenom() + " " + chauffeur.getNom());
        camionDao.save(nouveauCamion);
    }

    private ChauffeurResponse toResponse(Chauffeur c) {
        var camion = camionDao.findByChauffeurIdAndAdminId(c.getId(), c.getAdminId()).orElse(null);

        return ChauffeurResponse.builder()
                .id(c.getId())
                .adminId(c.getAdminId())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .cin(c.getCin())
                .telephone(c.getTelephone())
                .dateEmbauche(c.getDateEmbauche())
                .salaire(c.getSalaire())
                .active(c.isActive())
                .nomComplet(c.getPrenom() + " " + c.getNom())
                .camionId(camion != null ? camion.getId() : null)
                .camionMatricule(camion != null ? camion.getMatricule() : null)
                .build();
    }
}