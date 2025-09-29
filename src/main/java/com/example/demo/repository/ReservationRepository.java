package com.example.demo.repository;

import com.example.demo.entities.Reservation;
import com.example.demo.sec.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
 
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Récupérer toutes les réservations d'un chargeur
    List<Reservation> findByChargeurOrderByDateReservationDesc(AppUser chargeur);
    List<Reservation> findByChargeur(AppUser chargeur);
    
    // Récupérer les réservations par statut pour un chargeur
    List<Reservation> findByChargeurAndStatutOrderByDateReservationDesc(AppUser chargeur, String statut);
    List<Reservation> findByStatut(String statut);
    
    // Récupérer les réservations disponibles pour les transporteurs (sans camion assigné)
    List<Reservation> findByCamionIsNullAndStatutOrderByDateReservationDesc(String statut);
    
    // Récupérer toutes les réservations disponibles (sans camion assigné)
    List<Reservation> findByCamionIsNullOrderByDateReservationDesc();
    
    // Récupérer toutes les réservations par statut (avec ou sans camion assigné)
    List<Reservation> findByStatutOrderByDateReservationDesc(String statut);
    
    // Réservations du transporteur connecté
    List<Reservation> findByCamionTransporteurOrderByDateReservationDesc(AppUser transporteur);
    
    @Query("SELECT r FROM Reservation r WHERE r.camion.transporteur = :transporteur AND r.statut = :statut ORDER BY r.dateReservation DESC")
    List<Reservation> findByCamionTransporteurAndStatutOrderByDateReservationDesc(AppUser transporteur, String statut);
    
    // Méthode alternative pour récupérer toutes les réservations du transporteur
    @Query("SELECT r FROM Reservation r WHERE r.camion.transporteur = :transporteur ORDER BY r.dateReservation DESC")
    List<Reservation> findAllByTransporteur(AppUser transporteur);
    
    // Méthode pour récupérer les réservations terminées du transporteur
    @Query("SELECT r FROM Reservation r WHERE r.camion.transporteur = :transporteur AND r.statut = 'TERMINEE' ORDER BY r.dateReservation DESC")
    List<Reservation> findTerminatedByTransporteur(AppUser transporteur);

    // Compteurs pour le chargeur connecté
    long countByChargeur(AppUser chargeur);
    long countByChargeurAndValidated(AppUser chargeur, Boolean validated);
} 