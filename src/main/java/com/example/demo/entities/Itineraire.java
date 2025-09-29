package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itineraire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String depart;
    private String arrivee;
    private double distance;
    private double dureeEstimee;
    private boolean peage;

    @OneToOne
    private Reservation reservation;

    @ManyToOne
    private Camion camion;
} 