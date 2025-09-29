package com.example.demo.entities;

import com.example.demo.sec.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mode; // carte, virement, cash, etc.
    private double montant;
    private LocalDateTime datePaiement;
    private String statut; // EN_ATTENTE, EFFECTUE, ECHEC

    @ManyToOne
    private Facture facture;

    @ManyToOne
    private AppUser payeur;
} 