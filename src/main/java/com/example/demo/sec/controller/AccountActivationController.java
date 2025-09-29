package com.example.demo.sec.controller;

import com.example.demo.sec.services.AccountActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AccountActivationController {

    @Autowired
    private AccountActivationService accountActivationService;

    /**
     * Activer un compte avec le token
     */
    @GetMapping("/activate-account")
    public ResponseEntity<Map<String, Object>> activateAccount(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean activated = accountActivationService.activateAccount(token);
            
            if (activated) {
                response.put("success", true);
                response.put("message", "Compte activé avec succès ! Vous pouvez maintenant vous connecter.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Token invalide ou expiré. Veuillez demander un nouveau lien d'activation.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'activation du compte: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Renvoyer l'email d'activation
     */
    @PostMapping("/resend-activation")
    public ResponseEntity<Map<String, Object>> resendActivationEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = request.get("username");
            
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Nom d'utilisateur requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            accountActivationService.resendActivationEmail(username);
            
            response.put("success", true);
            response.put("message", "Email d'activation renvoyé avec succès");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi de l'email: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vérifier si un compte est activé
     */
    @GetMapping("/check-activation/{username}")
    public ResponseEntity<Map<String, Object>> checkAccountActivation(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isActivated = accountActivationService.isAccountActivated(username);
            
            response.put("success", true);
            response.put("isActivated", isActivated);
            response.put("message", isActivated ? "Compte activé" : "Compte non activé");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la vérification: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 