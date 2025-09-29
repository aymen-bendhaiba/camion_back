package com.example.demo.repository;

import com.example.demo.entities.Proposal;
import com.example.demo.entities.Reservation;
import com.example.demo.sec.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByReservationOrderByCreatedAtDesc(Reservation reservation);
    List<Proposal> findByTransporteur(AppUser transporteur);

    long countByTransporteurAndValidated(AppUser transporteur, Boolean validated);

    @Query("SELECT COALESCE(SUM(p.prixPropose), 0) FROM Proposal p WHERE p.transporteur = :transporteur AND p.validated = true")
    Double sumValidatedPrixByTransporteur(AppUser transporteur);
}


