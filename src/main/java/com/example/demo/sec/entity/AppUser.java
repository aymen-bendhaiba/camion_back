package com.example.demo.sec.entity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<AppRole> roles = new ArrayList<>();
    
    // 2FA fields
    private String secretKey;
    private boolean twoFactorEnabled = false;
    private boolean twoFactorVerified = false;
    
    // Champs supplémentaires pour l'inscription
    private String firstName;
    private String lastName;
    private String phone;
    private String companyName;
    private String userType; // 'chargeur' ou 'transporteur'
    private boolean isActive = true;
    private boolean isActivated = false; // Compte activé par email
    private String activationToken; // Token pour l'activation
    private LocalDateTime activationTokenExpiry; // Expiration du token

    // Solde du compte (utilisé par l'application mobile)
    // Si la colonne existe déjà en base, elle sera mappée automatiquement
    private Double solde = 0.0;
}
