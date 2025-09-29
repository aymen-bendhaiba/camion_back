package com.example.demo.controller;

import com.example.demo.entities.Message;
import com.example.demo.entities.Reservation;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.services.AccountService;
import com.example.demo.services.MessageService;
import com.example.demo.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin("*")
public class MessageController {

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private ReservationService reservationService;

    // Envoyer un message
    @PostMapping("/send")
    public ResponseEntity<Message> envoyerMessage(
            @RequestBody Map<String, String> request,
            Principal principal) {
        try {
            String contenu = request.get("contenu");
            Long destinataireId = Long.parseLong(request.get("destinataireId"));
            Long reservationId = Long.parseLong(request.get("reservationId"));
            
            AppUser expediteur = accountService.loadUserByUsername(principal.getName());
            AppUser destinataire = accountService.loadUserById(destinataireId);
            Reservation reservation = reservationService.getReservationById(reservationId);
            
            Message message = messageService.envoyerMessage(contenu, expediteur, destinataire, reservation);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Récupérer les messages d'une réservation
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<Message>> getMessagesForReservation(
            @PathVariable Long reservationId,
            Principal principal) {
        try {
            AppUser currentUser = accountService.loadUserByUsername(principal.getName());
            Reservation reservation = reservationService.getReservationById(reservationId);
            
            // Vérifier que l'utilisateur a accès à cette réservation
            if (!reservation.getChargeur().getId().equals(currentUser.getId()) && 
                !reservation.getCamion().getTransporteur().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Message> messages = messageService.getMessagesForReservation(reservation);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Marquer les messages comme lus
    @PutMapping("/reservation/{reservationId}/read")
    public ResponseEntity<?> marquerMessagesCommeLus(
            @PathVariable Long reservationId,
            Principal principal) {
        try {
            AppUser currentUser = accountService.loadUserByUsername(principal.getName());
            Reservation reservation = reservationService.getReservationById(reservationId);
            
            messageService.marquerMessagesCommeLus(reservation, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Compter les messages non lus
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> compterMessagesNonLus(Principal principal) {
        try {
            AppUser currentUser = accountService.loadUserByUsername(principal.getName());
            long count = messageService.compterMessagesNonLus(currentUser);
            
            Map<String, Long> response = Map.of("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 