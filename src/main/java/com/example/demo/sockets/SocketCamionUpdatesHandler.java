package com.example.demo.sockets;

import com.example.demo.entities.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketCamionUpdatesHandler extends TextWebSocketHandler {

    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    // Notifier tous les clients avec la nouvelle position du camion (JSON)
    public static void notifyClients(String camionJson) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(camionJson));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyReservationAccepted(Reservation reservation) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> message = new HashMap<>();
            message.put("type", "RESERVATION_ACCEPTED");
            message.put("reservationId", reservation.getId());
            message.put("camion", reservation.getCamion());
            
            String jsonMessage = mapper.writeValueAsString(message);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 