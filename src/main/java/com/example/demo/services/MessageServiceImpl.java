package com.example.demo.services;

import com.example.demo.entities.Message;
import com.example.demo.entities.Reservation;
import com.example.demo.repository.MessageRepository;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sockets.ChatWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public Message envoyerMessage(String contenu, AppUser expediteur, AppUser destinataire, Reservation reservation) {
        Message message = Message.builder()
                .contenu(contenu)
                .expediteur(expediteur)
                .destinataire(destinataire)
                .reservation(reservation)
                .dateEnvoi(LocalDateTime.now())
                .lu(false)
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Notifier via WebSocket
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_MESSAGE");
            notification.put("messageId", savedMessage.getId());
            notification.put("contenu", savedMessage.getContenu());
            notification.put("expediteurId", savedMessage.getExpediteur().getId());
            notification.put("expediteurNom", savedMessage.getExpediteur().getUsername());
            notification.put("reservationId", savedMessage.getReservation().getId());
            notification.put("dateEnvoi", savedMessage.getDateEnvoi().toString());
            
            String jsonNotification = mapper.writeValueAsString(notification);
            chatWebSocketHandler.notifyNewMessage(jsonNotification, destinataire.getId(), reservation.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return savedMessage;
    }

    @Override
    public List<Message> getMessagesForReservation(Reservation reservation) {
        return messageRepository.findByReservationOrderByDateEnvoiAsc(reservation);
    }

    @Override
    public List<Message> getMessagesBetweenUsersForReservation(Reservation reservation, AppUser user1, AppUser user2) {
        return messageRepository.findMessagesBetweenUsersForReservation(reservation, user1, user2);
    }

    @Override
    @Transactional
    public void marquerMessagesCommeLus(Reservation reservation, AppUser destinataire) {
        messageRepository.markMessagesAsRead(reservation, destinataire);
    }

    @Override
    public long compterMessagesNonLus(AppUser destinataire) {
        return messageRepository.countUnreadMessages(destinataire);
    }
} 