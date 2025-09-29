package com.example.demo.controller;

import com.example.demo.entities.Reservation;
import com.example.demo.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import com.example.demo.sockets.SocketCamionUpdatesHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private SocketCamionUpdatesHandler socketCamionUpdatesHandler;

    @PostMapping
    public ResponseEntity<Reservation> createReservation(Principal principal, @RequestBody Reservation reservation) {
        Reservation created = reservationService.createReservation(principal,reservation);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserReservations(Principal principal) {
        List<Reservation> reservations = reservationService.getUserReservations(principal);
        List<Map<String, Object>> payload = reservations.stream().map(this::mapReservationForFrontend).toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Map<String, Object>>> getUserReservationsByStatus(
            Principal principal,
            @PathVariable String status) {
        List<Reservation> reservations = reservationService.getUserReservationsByStatus(principal, status);
        List<Map<String, Object>> payload = reservations.stream().map(this::mapReservationForFrontend).toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/recapitulatif")
    public ResponseEntity<List<Map<String, Object>>> getReservationRecapitulatif(Principal principal) {
        List<Map<String, Object>> recapitulatifs = reservationService.getReservationRecapitulatif(principal);
        return ResponseEntity.ok(recapitulatifs);
    }
    


    @PutMapping("/{reservationId}/camion/{camionId}/{isIgnore}")
    public ResponseEntity<?> updateReservationWithCamion(
            @PathVariable Long reservationId,
            @PathVariable Long camionId,@PathVariable Boolean isIgnore,
            Principal principal) {
        try {
            Reservation updated = reservationService.updateReservationWithCamion(reservationId, camionId, principal,isIgnore);
            // Notifier les clients via WebSocket
            socketCamionUpdatesHandler.notifyReservationAccepted(updated);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Endpoints pour les transporteurs
    
    @GetMapping("/available")
    public ResponseEntity<List<Reservation>> getAvailableReservations() {
        List<Reservation> reservations = reservationService.getAvailableReservations();
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/available/status/{status}")
    public ResponseEntity<List<Reservation>> getAvailableReservationsByStatus(@PathVariable String status) {
        List<Reservation> reservations = reservationService.getAvailableReservationsByStatus(status);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/all/status/{status}")
    public ResponseEntity<List<Reservation>> getAllReservationsByStatus(@PathVariable String status) {
        List<Reservation> reservations = reservationService.getAllReservationsByStatus(status);
        return ResponseEntity.ok(reservations);
    }
    
    @PostMapping("/{reservationId}/accept")
    public ResponseEntity<?> acceptReservation(@PathVariable Long reservationId, Principal principal) {
        try {
            Reservation accepted = reservationService.acceptReservation(reservationId, principal);
            // Notifier les clients via WebSocket
            socketCamionUpdatesHandler.notifyReservationAccepted(accepted);
            return ResponseEntity.ok(accepted);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Endpoints pour les réservations du transporteur connecté
    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> getMyReservations(Principal principal) {
        try {
            List<Reservation> reservations = reservationService.getMyReservations(principal);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/my/status/{status}")
    public ResponseEntity<List<Reservation>> getMyReservationsByStatus(
        @PathVariable String status, 
        Principal principal
    ) {
        try {
            List<Reservation> reservations = reservationService.getMyReservationsByStatus(principal, status);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/my/{status}/with-chargeur")
    public ResponseEntity<List<Map<String, Object>>> getMyReservationsWithChargeurInfo(
            @PathVariable String status,
            Principal principal) {
        try {
            List<Map<String, Object>> reservations = reservationService.getMyReservationsWithChargeurInfo(principal, status);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{reservationId}/status")
    public ResponseEntity<?> updateReservationStatus(
        @PathVariable Long reservationId,
        @RequestParam String status,
        Principal principal
    ) {
        try {
            Reservation updatedReservation = reservationService.updateReservationStatus(reservationId, status, principal);
            return ResponseEntity.ok(updatedReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint pour les statistiques du transporteur
    @GetMapping("/my/statistics")
    public ResponseEntity<Map<String, Object>> getMyStatistics(Principal principal) {
        try {
            Map<String, Object> statistics = reservationService.getMyStatistics(principal);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint de test pour vérifier les données
    @GetMapping("/my/debug")
    public ResponseEntity<Map<String, Object>> debugMyReservations(Principal principal) {
        try {
            Map<String, Object> debugInfo = reservationService.debugMyReservations(principal);
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // === Helpers ===
    private  Map<String, Object> mapReservationForFrontend(Reservation r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("lieuDepart", r.getLieuDepart());
        m.put("lieuArrivee", r.getLieuArrivee());
        m.put("dateReservation", r.getDateReservation());
        m.put("dateLivraison", r.getDateLivraison());
        m.put("statut", r.getStatut());
        m.put("typeMarchandise", r.getTypeMarchandise());
        m.put("poids", r.getPoids());
        m.put("volume", r.getVolume());
        m.put("tarif", r.getTarif());
        m.put("modePaiement", r.getModePaiement());
        // Flag de validation pour piloter l'UI (Rechercher vs Détails)
        try {
            java.lang.reflect.Field f = Reservation.class.getDeclaredField("validated");
            f.setAccessible(true);
            Object val = f.get(r);
            if (val instanceof Boolean) {
                m.put("validated", (Boolean) val);
            }
        } catch (Exception ignored) {}

        if (r.getCamion() != null) {
            Map<String, Object> camion = new HashMap<>();
            camion.put("id", r.getCamion().getId());
            camion.put("marque", r.getCamion().getMarque());
            camion.put("modele", r.getCamion().getModele());
            camion.put("capacite", r.getCamion().getCapacite());
            camion.put("type", r.getCamion().getType());
            camion.put("immatriculation", r.getCamion().getImmatriculation());
            if (r.getCamion().getTransporteur() != null) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", r.getCamion().getTransporteur().getId());
                t.put("username", r.getCamion().getTransporteur().getUsername());
                t.put("firstName", r.getCamion().getTransporteur().getFirstName());
                t.put("lastName", r.getCamion().getTransporteur().getLastName());
                t.put("email", r.getCamion().getTransporteur().getEmail());
                t.put("phone", r.getCamion().getTransporteur().getPhone());
                camion.put("transporteur", t);
            }
            m.put("camion", camion);
        }

        return m;
    }
}