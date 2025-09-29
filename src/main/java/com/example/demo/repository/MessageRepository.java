package com.example.demo.repository;

import com.example.demo.entities.Message;
import com.example.demo.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Trouver tous les messages d'une réservation
    List<Message> findByReservationOrderByDateEnvoiAsc(Reservation reservation);
    
    // Trouver les messages entre deux utilisateurs pour une réservation
    @Query("SELECT m FROM Message m WHERE m.reservation = :reservation " +
           "AND ((m.expediteur = :user1 AND m.destinataire = :user2) " +
           "OR (m.expediteur = :user2 AND m.destinataire = :user1)) " +
           "ORDER BY m.dateEnvoi ASC")
    List<Message> findMessagesBetweenUsersForReservation(
        @Param("reservation") Reservation reservation,
        @Param("user1") com.example.demo.sec.entity.AppUser user1,
        @Param("user2") com.example.demo.sec.entity.AppUser user2
    );
    
    // Compter les messages non lus pour un utilisateur
    @Query("SELECT COUNT(m) FROM Message m WHERE m.destinataire = :destinataire AND m.lu = false")
    long countUnreadMessages(@Param("destinataire") com.example.demo.sec.entity.AppUser destinataire);
    
    // Marquer tous les messages d'une réservation comme lus pour un utilisateur
    @Modifying
    @Query("UPDATE Message m SET m.lu = true WHERE m.reservation = :reservation AND m.destinataire = :destinataire")
    void markMessagesAsRead(@Param("reservation") Reservation reservation, 
                           @Param("destinataire") com.example.demo.sec.entity.AppUser destinataire);
} 