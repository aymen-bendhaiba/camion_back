package com.example.demo.sec.services;

import com.example.demo.sec.entity.AppUser;
import com.example.demo.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountActivationService {

    @Autowired
    @Lazy
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    /**
     * Générer un token d'activation et envoyer l'email
     */
    public void sendActivationEmail(AppUser user) {
        // Générer un token unique
        String activationToken = UUID.randomUUID().toString();
        
        // Définir l'expiration (24 heures)
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);
        
        // Sauvegarder le token dans l'utilisateur
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiry(expiry);
        user.setActivated(false);
        
        accountService.updateUserObje(user);
        
        // Construire le lien d'activation
        String activationLink = "http://localhost:8082/activate-account?token=" + activationToken;
        
        // Envoyer l'email
        String subject = "Activation de votre compte TransportPro";
        String body = buildActivationEmailBody(user.getFirstName(), activationLink);
        
        emailService.sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Activer un compte avec le token
     */
    public boolean activateAccount(String token) {
        AppUser user = accountService.findByActivationToken(token);
        
        if (user == null) {
            return false;
        }
        
        // Vérifier si le token n'a pas expiré
        if (user.getActivationTokenExpiry() != null && 
            LocalDateTime.now().isAfter(user.getActivationTokenExpiry())) {
            return false;
        }
        
        // Activer le compte
        user.setActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        
        accountService.updateUserObje(user);
        
        return true;
    }

    /**
     * Vérifier si un compte est activé
     */
    public boolean isAccountActivated(String username) {
        AppUser user = accountService.loadUserByUsername(username);
        return user != null && user.isActivated();
    }

    /**
     * Renvoyer l'email d'activation
     */
    public void resendActivationEmail(String username) {
        AppUser user = accountService.loadUserByUsername(username);
        if (user != null && !user.isActivated()) {
            sendActivationEmail(user);
        }
    }

    /**
     * Construire le contenu de l'email d'activation
     */
    private String buildActivationEmailBody(String firstName, String activationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Activation de votre compte TransportPro</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f7fa;
                    }
                    .container {
                        background-color: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        background: linear-gradient(135deg, #1E3A8A, #3B82F6);
                        color: white;
                        padding: 15px;
                        border-radius: 10px;
                        display: inline-block;
                        margin-bottom: 20px;
                    }
                    .btn {
                        display: inline-block;
                        background: linear-gradient(135deg, #1E3A8A, #3B82F6);
                        color: white;
                        padding: 15px 30px;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: bold;
                        margin: 20px 0;
                        text-align: center;
                    }
                    .btn:hover {
                        background: linear-gradient(135deg, #1E40AF, #2563EB);
                    }
                    .link-box {
                        background-color: #f8f9fa;
                        border: 1px solid #e9ecef;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 20px 0;
                        word-break: break-all;
                        font-family: monospace;
                        font-size: 12px;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e9ecef;
                        text-align: center;
                        color: #6c757d;
                        font-size: 14px;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 20px 0;
                        color: #856404;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">
                            🚛 TransportPro
                        </div>
                        <h1 style="color: #1E3A8A; margin: 0;">Bienvenue sur TransportPro !</h1>
                    </div>
                    
                    <p>Bonjour <strong>%s</strong>,</p>
                    
                    <p>Merci de vous être inscrit sur TransportPro. Pour activer votre compte et commencer à utiliser notre plateforme, veuillez cliquer sur le bouton ci-dessous :</p>
                    
                    <div style="text-align: center;">
                        <a href="%s" class="btn">Activer mon compte</a>
                    </div>
                    
                    <p>Ou copiez ce lien dans votre navigateur :</p>
                    <div class="link-box">
                        %s
                    </div>
                    
                    <div class="warning">
                        <strong>⚠️ Important :</strong> Ce lien expirera dans 24 heures pour des raisons de sécurité.
                    </div>
                    
                    <p>Une fois votre compte activé, vous pourrez :</p>
                    <ul>
                        <li>Accéder à toutes les fonctionnalités de TransportPro</li>
                        <li>Gérer vos transports et expéditions</li>
                        <li>Connecter avec d'autres professionnels du transport</li>
                        <li>Bénéficier de notre support client</li>
                    </ul>
                    
                    <div class="footer">
                        <p>Si vous n'avez pas créé de compte sur TransportPro, vous pouvez ignorer cet email en toute sécurité.</p>
                        <p><strong>Cordialement,<br>L'équipe TransportPro</strong></p>
                        <p style="font-size: 12px; color: #adb5bd;">
                            Cet email a été envoyé automatiquement. Veuillez ne pas y répondre.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName != null ? firstName : "Utilisateur", activationLink, activationLink);
    }
} 