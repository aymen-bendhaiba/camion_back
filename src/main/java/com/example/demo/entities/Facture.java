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
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private double montant;
    private LocalDateTime dateEmission;
    private boolean payee;

    @ManyToOne
    private Reservation reservation;

    @ManyToOne
    private AppUser chargeur;

    @ManyToOne
    private AppUser transporteur;
} 