package com.example.demo.services;

import com.example.demo.entities.Reservation;

import java.security.Principal;
import java.util.List;
import java.util.Map;

public interface ReservationService {
    Reservation createReservation(Principal principal, Reservation reservation);
    
    // Récupérer toutes les réservations d'un utilisateur
    List<Reservation> getUserReservations(Principal principal);
    
    // Récupérer les réservations par statut pour un utilisateur
    List<Reservation> getUserReservationsByStatus(Principal principal, String status);
    
    // Mettre à jour une réservation avec un camion
    Reservation updateReservationWithCamion(Long reservationId, Long camionId, Principal principal,Boolean isIgnore);
    

    // Récupérer les réservations disponibles pour les transporteurs
    List<Reservation> getAvailableReservations();
    
    // Récupérer les réservations disponibles par statut pour les transporteurs
    List<Reservation> getAvailableReservationsByStatus(String status);
    
    // Récupérer toutes les réservations par statut (avec ou sans camion assigné)
    List<Reservation> getAllReservationsByStatus(String status);
    
    // Accepter une réservation (assigner le camion du transporteur)
    Reservation acceptReservation(Long reservationId, Principal principal);
    
    // Méthodes pour les réservations du transporteur
    List<Reservation> getMyReservations(Principal principal);
    List<Reservation> getMyReservationsByStatus(Principal principal, String status);
    Reservation updateReservationStatus(Long reservationId, String newStatus, Principal principal);
    
    // Nouvelle méthode pour retourner les réservations avec les informations du chargeur
    List<Map<String, Object>> getMyReservationsWithChargeurInfo(Principal principal, String status);
    
    // Méthode pour les statistiques du transporteur
    Map<String, Object> getMyStatistics(Principal principal);
    
    // Méthode de debug pour vérifier les données
    Map<String, Object> debugMyReservations(Principal principal);
    // Récupérer les récapitulatifs de réservations
    List<Map<String, Object>> getReservationRecapitulatif(Principal principal);
    

    
    // Récupérer une réservation par ID
    Reservation getReservationById(Long reservationId);

    // Compteurs pour le chargeur connecté (réservations lancées/terminées/en attente)
    Map<String, Long> getChargeurCounters(Principal principal);

} 