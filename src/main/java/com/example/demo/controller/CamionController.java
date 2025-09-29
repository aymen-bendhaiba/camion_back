package com.example.demo.controller;

import com.example.demo.entities.Camion;
import com.example.demo.services.CamionService;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.repo.UserAppRepository;
import com.example.demo.sockets.SocketCamionUpdatesHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/camions")
public class CamionController {
    @Autowired
    private CamionService camionService;
    
    @Autowired
    private UserAppRepository appUserRepository;
    
    @Autowired
    private SocketCamionUpdatesHandler socketCamionUpdatesHandler;

    @PostMapping
    public Camion saveCamion(@RequestBody Camion camion) {
        return camionService.saveCamion(camion);
    }

    @GetMapping
    public List<Camion> getAllCamions() {
        return camionService.getAllCamions();
    }

    @GetMapping("/proches")
    public List<Camion> getCamionsProches(@RequestParam double latitude, @RequestParam double longitude, @RequestParam(defaultValue = "10") double rayonKm) {
        return camionService.getCamionsProches(latitude, longitude, rayonKm);
    }
    
    @GetMapping("/transporteur/{transporteurId}")
    public ResponseEntity<Camion> getCamionByTransporteurId(@PathVariable Long transporteurId) {
        try {
            Optional<AppUser> transporteur = appUserRepository.findById(transporteurId);
            if (transporteur.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Optional<Camion> camion = camionService.getCamionByTransporteur(transporteur.get());
            if (camion.isPresent()) {
                return ResponseEntity.ok(camion.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/my")
    public ResponseEntity<Camion> getMyCamion(Principal principal) {
        try {
            AppUser transporteur = appUserRepository.findByUsername(principal.getName());
            if (transporteur == null) {
                return ResponseEntity.notFound().build();
            }
            Optional<Camion> camion = camionService.getCamionByTransporteur(transporteur);
            if (camion.isPresent()) {
                return ResponseEntity.ok(camion.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Camion> getCamionById(@PathVariable long id) {
        try {
            Camion camion = camionService.getCamionById(id);
            if (camion != null) {
                return ResponseEntity.ok(camion);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Camion> updateCamion(@PathVariable long id, @RequestBody Camion camion) {
        Camion updatedCamion = camionService.updateCamion(id, camion);
        return ResponseEntity.ok(updatedCamion);
    }
    
    @PutMapping("/{id}/position")
    public ResponseEntity<Camion> updateCamionPosition(@PathVariable long id, @RequestBody Map<String, Double> position) {
        try {
            System.out.println("=== Mise à jour position camion ===");
            System.out.println("ID du camion: " + id);
            System.out.println("Position reçue: " + position);
            
            Double latitude = position.get("latitude");
            Double longitude = position.get("longitude");
            
            System.out.println("Latitude: " + latitude);
            System.out.println("Longitude: " + longitude);
            
            if (latitude == null || longitude == null) {
                System.out.println("Erreur: latitude ou longitude null");
                return ResponseEntity.badRequest().build();
            }
            
            Camion camion = camionService.getCamionById(id);
            if (camion == null) {
                System.out.println("Erreur: camion non trouvé avec l'ID " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Camion trouvé: " + camion);
            System.out.println("Ancienne position: lat=" + camion.getLatitude() + ", lng=" + camion.getLongitude());
            
            camion.setLatitude(latitude);
            camion.setLongitude(longitude);
            Camion updatedCamion = camionService.updateCamion(id, camion);
            
            System.out.println("Nouvelle position: lat=" + updatedCamion.getLatitude() + ", lng=" + updatedCamion.getLongitude());
            
            // Notifier les clients via WebSocket
            socketCamionUpdatesHandler.notifyClients(updatedCamion.toString());
            
            System.out.println("Position mise à jour avec succès");
            return ResponseEntity.ok(updatedCamion);
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint de test pour simuler le mouvement du camion
    @PostMapping("/{id}/simulate-movement")
    public ResponseEntity<String> simulateCamionMovement(@PathVariable long id) {
        try {
            // Récupérer le camion actuel
            Camion camion = camionService.getCamionById(id);
            if (camion == null) {
                return ResponseEntity.notFound().build();
            }

            // Simuler un mouvement (exemple: de Casablanca vers Rabat)
            double startLat = 33.243454;
            double startLng = -8.498744;
            double endLat = 33.599281;
            double endLng = -7.613490;
            // Créer un thread pour envoyer des updates toutes les 2 secondes
            new Thread(() -> {
                try {
                    for (int i = 0; i <= 10; i++) {
                        // Interpolation linéaire entre start et end
                        double progress = i / 10.0;
                        double currentLat = startLat + (endLat - startLat) * progress;
                        double currentLng = startLng + (endLng - startLng) * progress;

                        // Mettre à jour la position du camion
                        camion.setLatitude(currentLat);
                        camion.setLongitude(currentLng);
                        camionService.updateCamion(id, camion);

                        // Attendre 2 secondes
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            return ResponseEntity.ok("Simulation de mouvement démarrée pour le camion " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
} 