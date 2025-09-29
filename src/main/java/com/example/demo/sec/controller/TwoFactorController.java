package com.example.demo.sec.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.sec.dto.TwoFactorResponse;
import com.example.demo.sec.dto.TwoFactorVerificationRequest;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.services.AccountService;
import com.example.demo.sec.services.TwoFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorController {

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private AccountService accountService;

    /**
     * Activer la 2FA pour un utilisateur
     */
    @PostMapping("/enable")
    public ResponseEntity<TwoFactorResponse> enableTwoFactor(@RequestParam String username) {
        try {
            AppUser user = accountService.loadUserByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Utilisateur non trouvé", false));
            }

            if (user.isTwoFactorEnabled()) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "2FA déjà activée", false));
            }

            // Générer une nouvelle clé secrète
            String secretKey = twoFactorService.generateSecretKey();
            user.setSecretKey(secretKey);
            user.setTwoFactorEnabled(true);
            user.setTwoFactorVerified(false);
            
            accountService.updateUserObje(user);

            // Générer l'URL QR code
            String qrCodeUrl = twoFactorService.generateQRCodeUrl(username, secretKey);

            return ResponseEntity.ok(new TwoFactorResponse(qrCodeUrl, secretKey, 
                "2FA activée. Scannez le QR code avec Google Authenticator", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TwoFactorResponse(null, null, "Erreur lors de l'activation: " + e.getMessage(), false));
        }
    }

    /**
     * Vérifier le code TOTP pour finaliser l'activation
     */
    @PostMapping("/verify-activation")
    public ResponseEntity<TwoFactorResponse> verifyActivation(@RequestBody TwoFactorVerificationRequest request) {
        try {
            AppUser user = accountService.loadUserByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Utilisateur non trouvé", false));
            }

            if (!user.isTwoFactorEnabled()) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "2FA non activée", false));
            }

            if (user.isTwoFactorVerified()) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "2FA déjà vérifiée", false));
            }

            // Vérifier le code TOTP
            boolean isValid = twoFactorService.verifyCodeWithTolerance(user.getSecretKey(), request.getCode());
            
            if (isValid) {
                user.setTwoFactorVerified(true);
                accountService.updateUserObje(user);
                
                return ResponseEntity.ok(new TwoFactorResponse(null, null, 
                    "2FA activée avec succès", true));
            } else {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Code invalide", false));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TwoFactorResponse(null, null, "Erreur lors de la vérification: " + e.getMessage(), false));
        }
    }

    /**
     * Désactiver la 2FA avec vérification du code
     */
    @PostMapping("/disable-with-verification")
    public ResponseEntity<TwoFactorResponse> disableTwoFactorWithVerification(@RequestBody TwoFactorVerificationRequest request) {
        try {
            System.out.println("=== DÉBUT DÉSACTIVATION 2FA AVEC VÉRIFICATION ===");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Code: " + request.getCode());
            
            AppUser user = accountService.loadUserByUsername(request.getUsername());
            if (user == null) {
                System.out.println("Erreur: Utilisateur non trouvé");
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Utilisateur non trouvé", false));
            }

            if (!user.isTwoFactorEnabled()) {
                System.out.println("Erreur: 2FA déjà désactivée");
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "2FA déjà désactivée", false));
            }

            System.out.println("Vérification du code TOTP...");
            // Vérifier le code TOTP avant la désactivation
            boolean isValid = twoFactorService.verifyCodeWithTolerance(user.getSecretKey(), request.getCode());
            System.out.println("Code valide: " + isValid);
            
            if (!isValid) {
                System.out.println("Erreur: Code invalide");
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Code invalide", false));
            }

            System.out.println("Désactivation de la 2FA...");
            // Désactiver la 2FA
            user.setTwoFactorEnabled(false);
            user.setTwoFactorVerified(false);
            user.setSecretKey(null);
            
            accountService.updateUserObje(user);
            System.out.println("2FA désactivée avec succès");

            return ResponseEntity.ok(new TwoFactorResponse(null, null, "2FA désactivée avec succès", true));

        } catch (Exception e) {
            System.out.println("=== ERREUR DÉSACTIVATION 2FA ===");
            System.out.println("Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new TwoFactorResponse(null, null, "Erreur lors de la désactivation: " + e.getMessage(), false));
        }
    }

    /**
     * Désactiver la 2FA (ancienne méthode sans vérification)
     */
    @PostMapping("/disable")
    public ResponseEntity<TwoFactorResponse> disableTwoFactor(@RequestParam String username) {
        try {
            AppUser user = accountService.loadUserByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Utilisateur non trouvé", false));
            }

            user.setTwoFactorEnabled(false);
            user.setTwoFactorVerified(false);
            user.setSecretKey(null);
            
            accountService.updateUserObje(user);

            return ResponseEntity.ok(new TwoFactorResponse(null, null, "2FA désactivée", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TwoFactorResponse(null, null, "Erreur lors de la désactivation: " + e.getMessage(), false));
        }
    }

    /**
     * Vérifier le code TOTP pour la connexion
     */
    @PostMapping("/verify-login")
    public ResponseEntity<TwoFactorResponse> verifyLoginCode(@RequestBody TwoFactorVerificationRequest request) {
        try {
            AppUser user = accountService.loadUserByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Utilisateur non trouvé", false));
            }

            if (!user.isTwoFactorEnabled() || !user.isTwoFactorVerified()) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "2FA non activée", false));
            }

            // Vérifier le code TOTP
            boolean isValid = twoFactorService.verifyCodeWithTolerance(user.getSecretKey(), request.getCode());
            
            if (isValid) {
                return ResponseEntity.ok(new TwoFactorResponse(null, null, "Code valide", true));
            } else {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Code invalide", false));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TwoFactorResponse(null, null, "Erreur lors de la vérification: " + e.getMessage(), false));
        }
    }

    /**
     * Finaliser la connexion avec 2FA et générer le token JWT
     */
    @PostMapping("/finalize-login")
    public ResponseEntity<Map<String, String>> finalizeLogin(@RequestBody TwoFactorVerificationRequest request) {
        try {
            AppUser appUser = accountService.loadUserByUsername(request.getUsername());
            if (appUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur non trouvé"));
            }

            if (!appUser.isTwoFactorEnabled() || !appUser.isTwoFactorVerified()) {
                return ResponseEntity.badRequest().body(Map.of("error", "2FA non activée"));
            }

            // Vérifier le code TOTP
            boolean isValid = twoFactorService.verifyCodeWithTolerance(appUser.getSecretKey(), request.getCode());
            
            if (!isValid) {
                return ResponseEntity.badRequest().body(Map.of("error", "Code invalide"));
            }

            // Générer le token JWT
            Algorithm algorithm = Algorithm.HMAC256("mysecrter123");
            String jwtAccessToken = JWT.create()
                    .withSubject(appUser.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 3*24*60*60*1000))
                    .withIssuer("TransportPro")
                    .withClaim("roles", appUser.getRoles().stream().map(role -> role.getRolename()).collect(Collectors.toList()))
                    .sign(algorithm);
            
            String jwtRefreshToken = JWT.create()
                    .withSubject(appUser.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 15*60*1000))
                    .withIssuer("TransportPro")
                    .sign(algorithm);
            
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", jwtAccessToken);
            tokens.put("refresh_token", jwtRefreshToken);
            tokens.put("requires2FA", "false");
            tokens.put("userType", appUser.getUserType() != null ? appUser.getUserType() : "chargeur");
            
            return ResponseEntity.ok(tokens);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * Obtenir le statut 2FA de l'utilisateur connecté
     */
    @GetMapping("/status")
    public ResponseEntity<TwoFactorResponse> getTwoFactorStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            AppUser user = accountService.loadUserByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(null, null, "Utilisateur non trouvé", false));
            }

            String message = user.isTwoFactorEnabled() && user.isTwoFactorVerified() 
                ? "2FA activée" : "2FA désactivée";
            
            return ResponseEntity.ok(new TwoFactorResponse(null, null, message, 
                user.isTwoFactorEnabled() && user.isTwoFactorVerified()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TwoFactorResponse(null, null, "Erreur: " + e.getMessage(), false));
        }
    }
    
    /**
     * Endpoint de debug pour générer le code TOTP actuel
     */
    @GetMapping("/debug-totp")
    public ResponseEntity<Map<String, Object>> debugTOTP(@RequestParam String username) {
        try {
            AppUser user = accountService.loadUserByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            if (!user.isTwoFactorEnabled()) {
                return ResponseEntity.badRequest().body(Map.of("error", "2FA non activée"));
            }
            
            int currentCode = twoFactorService.getCurrentTOTP(user.getSecretKey());
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("secretKey", user.getSecretKey());
            response.put("currentTOTP", currentCode);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }
} 