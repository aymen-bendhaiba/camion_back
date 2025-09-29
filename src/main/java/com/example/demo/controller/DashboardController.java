package com.example.demo.controller;

import com.example.demo.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private ReservationService reservationService;

    // Retourne des statistiques synthétiques pour le dashboard mobile/web
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Principal principal) {
        try {
            Map<String, Object> stats = reservationService.getMyStatistics(principal);

            // Map fields to the frontend expected names (based on attachment)
            Map<String, Object> response = new HashMap<>();
            // "Réservations actives" -> totalReservations
            response.put("reservationsActives", stats.getOrDefault("totalReservations", 0));
            // "En cours" -> reservationsEnCours
            response.put("enCours", stats.getOrDefault("reservationsEnCours", 0));
            // "Terminée" -> missionsTerminees
            response.put("terminee", stats.getOrDefault("missionsTerminees", 0));
            // "Montant total" -> gainsTotaux
            response.put("montantTotal", stats.getOrDefault("gainsTotaux", 0.0));

            // Include raw stats for debugging / additional fields
            response.put("raw", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Compteurs pour le chargeur connecté
    @GetMapping("/chargeur/counters")
    public ResponseEntity<Map<String, Long>> getChargeurCounters(Principal principal) {
        Map<String, Long> counters = reservationService.getChargeurCounters(principal);
        return ResponseEntity.ok(counters);
    }
}
