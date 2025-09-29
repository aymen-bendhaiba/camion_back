package com.example.demo.services;

import com.example.demo.entities.Proposal;
import com.example.demo.entities.Reservation;
import com.example.demo.repository.ProposalRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.sec.entity.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProposalServiceImpl implements ProposalService {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public Proposal createProposal(Reservation reservation, AppUser transporteur, Double prix, String commentaire) {
        Proposal p = Proposal.builder()
                .reservation(reservation)
                .transporteur(transporteur)
                .prixPropose(prix)
                .commentaire(commentaire)
                .createdAt(LocalDateTime.now())
                .selected(false)
                .validated(false)
                .build();
        return proposalRepository.save(p);
    }

    @Override
    public List<Proposal> listProposals(Reservation reservation) {
        return proposalRepository.findByReservationOrderByCreatedAtDesc(reservation);
    }

    @Override
    public Proposal selectProposal(Reservation reservation, Long proposalId, AppUser chargeur) {
        if (reservation.getChargeur() == null || !reservation.getChargeur().getId().equals(chargeur.getId())) {
            throw new RuntimeException("Accès interdit: vous n'êtes pas le propriétaire de la réservation");
        }
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposition non trouvée"));
        if (!proposal.getReservation().getId().equals(reservation.getId())) {
            throw new RuntimeException("La proposition ne correspond pas à la réservation");
        }
        proposal.setSelected(true);
        proposal = proposalRepository.save(proposal);

        // Optionnel: Mettre à jour la réservation (statut / tarif)
        reservation.setTarif(proposal.getPrixPropose() != null ? proposal.getPrixPropose() : reservation.getTarif());
        reservationRepository.save(reservation);

        return proposal;
    }
}


