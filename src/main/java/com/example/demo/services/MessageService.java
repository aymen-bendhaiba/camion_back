package com.example.demo.services;

import com.example.demo.entities.Message;
import com.example.demo.entities.Reservation;
import com.example.demo.sec.entity.AppUser;

import java.util.List;

public interface MessageService {
    
    // Envoyer un message
    Message envoyerMessage(String contenu, AppUser expediteur, AppUser destinataire, Reservation reservation);
    
    // Récupérer les messages d'une réservation
    List<Message> getMessagesForReservation(Reservation reservation);
    
    // Récupérer les messages entre deux utilisateurs pour une réservation
    List<Message> getMessagesBetweenUsersForReservation(Reservation reservation, AppUser user1, AppUser user2);
    
    // Marquer les messages comme lus
    void marquerMessagesCommeLus(Reservation reservation, AppUser destinataire);
    
    // Compter les messages non lus
    long compterMessagesNonLus(AppUser destinataire);
} 