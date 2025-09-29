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
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AppUser chargeur;

    @ManyToOne
    private Camion camion;

    private String typeMarchandise; // normale, frigorifique, etc.
    private double volume;
    private double poids;
    @Column(name = "lieuDepart", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
    private String lieuDepart;
    @Column(name = "lieuArrive", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
    private String lieuArrivee;
    private LocalDateTime dateReservation;
    private LocalDateTime dateLivraison;
    private String statut; // EN_ATTENTE, EN_COURS, TERMINEE, etc.
    private double tarif;
    private String modePaiement;
    private boolean factureGeneree;

    // Champs pour la sélection anonyme d'un transporteur
    @Column(name = "selected_transporteur_id")
    private Long selectedTransporteurId;

    @Column(name = "selected_price")
    private Double selectedPrice;

    // Validation de la réservation après choix du chargeur
    @Column(name = "validated")
    private Boolean validated = false;
} 