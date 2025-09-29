package com.example.demo.controller;

import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.services.AccountService;
import com.example.demo.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PasswordResetController {
    @Autowired
    private  AccountService accountService;
    @Autowired
    private  JavaMailSender mailSender;
    private final Map<String, String> resetCodes = new HashMap<>();
    @Autowired
    private EmailService emailService;

    @PostMapping("/send-reset-code/{username}")
    public ResponseEntity<?> sendResetCode(@PathVariable String username) {
        AppUser user = accountService.loadUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // Code à 6 chiffres
        resetCodes.put(username, code);
        emailService.sendEmail(user.getEmail(),"Password Reset Code","Your password reset code is: " + code);
        return ResponseEntity.ok(user.getEmail());


    }
    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, String> payload) {
        System.out.println("=== Début de la vérification du code ===");
        System.out.println("Payload reçu: " + payload);
        System.out.println("Codes stockés: " + resetCodes);

        String username = payload.get("username");
        String code = payload.get("code");

        System.out.println("Username: " + username);
        System.out.println("Code reçu: " + code);

        if (username == null || code == null) {
            System.out.println("Username ou code manquant");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and code are required");
        }

        String savedCode = resetCodes.get(username);
        System.out.println("Code sauvegardé pour " + username + ": " + savedCode);

        if (savedCode != null && savedCode.equals(code)) {
            System.out.println("Code vérifié avec succès pour " + username);
            // Optionnel: Supprimer le code après vérification pour éviter la réutilisation
            // resetCodes.remove(username);
            return ResponseEntity.ok("Code verified successfully");
        } else {
            System.out.println("Code invalide pour " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired code");
        }
    }

}
