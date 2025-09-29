package com.example.demo.entities;

import com.example.demo.sec.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String immatriculation;
    private String type; // FTL, LTL, frigorifique, etc.
    private Double capacite;
    private String marque;
    private String modele;
    private Boolean disponible;

    private Double latitude;
    private Double longitude;

    @ManyToOne
    private AppUser transporteur;
} 