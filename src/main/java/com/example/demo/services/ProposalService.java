package com.example.demo.services;

import com.example.demo.entities.Proposal;
import com.example.demo.entities.Reservation;
import com.example.demo.sec.entity.AppUser;

import java.util.List;

public interface ProposalService {
    Proposal createProposal(Reservation reservation, AppUser transporteur, Double prix, String commentaire);
    List<Proposal> listProposals(Reservation reservation);
    Proposal selectProposal(Reservation reservation, Long proposalId, AppUser chargeur);
}


