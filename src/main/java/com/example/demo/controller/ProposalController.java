package com.example.demo.controller;

import com.example.demo.entities.Proposal;
import com.example.demo.entities.Reservation;
import com.example.demo.repository.ProposalRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.services.AccountService;
import com.example.demo.services.ProposalService;
import com.example.demo.services.ReservationService;
import com.example.demo.services.CamionService;
import com.example.demo.entities.Camion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.transaction.Transactional;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private CamionService camionService;

    @PostMapping("/{reservationId}")
    public ResponseEntity<?> createProposal(
            @PathVariable Long reservationId,
            @RequestBody Map<String, Object> body,
            Principal principal
    ) {
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            AppUser transporteur = accountService.loadUserByUsername(principal.getName());
            Double prix = null;
            if (body != null && body.get("prix") != null) {
                try { prix = Double.parseDouble(body.get("prix").toString()); } catch (Exception ignored) {}
            }
            String commentaire = body != null ? (String) body.getOrDefault("commentaire", null) : null;

            Proposal p = proposalService.createProposal(reservation, transporteur, prix, commentaire);
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", p.getId());
            resp.put("prix", p.getPrixPropose());
            resp.put("commentaire", p.getCommentaire());
            // Ne pas exposer l'identité du transporteur côté chargeur
            resp.put("displayName", "Transporteur");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<?> listProposals(@PathVariable Long reservationId) {
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            List<Proposal> list = proposalService.listProposals(reservation);
            int[] counter = new int[]{0};
            List<Map<String, Object>> resp = list.stream().map(p -> {
                counter[0]++;
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("prix", p.getPrixPropose());
                if (p.getCommentaire() != null) m.put("commentaire", p.getCommentaire());
                
                // Ajouter les informations du transporteur
                Map<String, Object> transporteur = new HashMap<>();
                transporteur.put("username", p.getTransporteur().getUsername());
                m.put("transporteur", transporteur);
                
                // Récupérer et ajouter les informations du camion
                try {
                    java.util.Optional<Camion> camionOpt = camionService.getCamionByTransporteur(p.getTransporteur());
                    if (camionOpt.isPresent()) {
                        Camion camion = camionOpt.get();
                        Map<String, Object> camionInfo = new HashMap<>();
                        camionInfo.put("id", camion.getId());
                        camionInfo.put("capacite", camion.getCapacite());
                        camionInfo.put("marque", camion.getMarque());
                        camionInfo.put("modele", camion.getModele());
                        camionInfo.put("type", camion.getType());
                        camionInfo.put("immatriculation", camion.getImmatriculation());
                        m.put("camion", camionInfo);
                    }
                } catch (Exception e) {
                    // En cas d'erreur, ne pas inclure les infos du camion
                    System.err.println("Erreur lors de la récupération du camion: " + e.getMessage());
                }
                
                // Anonymiser: exposer uniquement un libellé générique
                m.put("displayName", "Transporteur " + counter[0]);
                return m;
            }).toList();
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{reservationId}/select/{proposalId}")
    @Transactional
    public ResponseEntity<?> selectProposal(
            @PathVariable Long reservationId,
            @PathVariable Long proposalId,
            Principal principal
    ) {
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            AppUser chargeur = accountService.loadUserByUsername(principal.getName());
            // Règles de solde: Chargeur 10%, Transporteur 15%
            Proposal p = proposalRepository.findById(proposalId)
                    .orElseThrow(() -> new RuntimeException("Proposition non trouvée"));
            if (!p.getReservation().getId().equals(reservation.getId())) {
                throw new RuntimeException("La proposition ne correspond pas à la réservation");
            }
            double prix = p.getPrixPropose() != null ? p.getPrixPropose() : 0.0;
            if (prix <= 0) throw new RuntimeException("Prix invalide");

            AppUser transporteur = p.getTransporteur();
            Double soldeChargeur = chargeur.getSolde() == null ? 0.0 : chargeur.getSolde();
            Double soldeTransporteur = transporteur.getSolde() == null ? 0.0 : transporteur.getSolde();
            double besoinChargeur = prix * 0.10;
            double besoinTransporteur = prix * 0.15;

            boolean okChargeur = soldeChargeur >= besoinChargeur;
            boolean okTransporteur = soldeTransporteur >= besoinTransporteur;
            if (!okChargeur && !okTransporteur) {
                return ResponseEntity.badRequest().body("Votre solde ou celui du transporteur est insuffisant.");
            }
            if (!okChargeur) {
                return ResponseEntity.badRequest().body("Votre solde est insuffisant (10%).");
            }
            if (!okTransporteur) {
                return ResponseEntity.badRequest().body("Le solde du transporteur est insuffisant (15%).");
            }

            // Débiter les deux soldes
            chargeur.setSolde(soldeChargeur - besoinChargeur);
            transporteur.setSolde(soldeTransporteur - besoinTransporteur);
            accountService.updateUserObje(chargeur);
            accountService.updateUserObje(transporteur);

            // Marquer la proposition sélectionnée (après succès débit)
            p = proposalService.selectProposal(reservation, proposalId, chargeur);

            // Marquer sélection + champs sur la réservation
            reservation.setSelectedTransporteurId(transporteur.getId());
            reservation.setSelectedPrice(prix);
            // Lier le camion du transporteur pour permettre l'affichage des infos dans /recapitulatif
            try {
                java.util.Optional<Camion> camionOpt = camionService.getCamionByTransporteur(transporteur);
                if (camionOpt.isPresent()) {
                    Camion camion = camionOpt.get();
                    reservation.setCamion(camion);
                    // optionnel: rendre le camion indisponible
                    camion.setDisponible(false);
                    camionService.updateCamion(camion.getId(), camion);
                }
            } catch (Exception ignored) {}
            // Statut final demandé: TERMINEE
            reservation.setStatut("TERMINEE");
            // Marquer validated
            reservation.setValidated(true);
            reservationRepository.save(reservation);

            // Valider la proposition automatiquement
            p.setValidated(true);
            proposalRepository.save(p);

            return ResponseEntity.ok(Map.of(
                    "id", p.getId(),
                    "selected", true,
                    "selectedTransporteurId", transporteur.getId(),
                    "selectedPrice", prix
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


