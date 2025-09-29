package com.example.demo.entities;

import com.example.demo.sec.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // bon de livraison, douane, assurance, etc.
    private String url;
    private String nom;

    @ManyToOne
    private Reservation reservation;

    @ManyToOne
    private AppUser proprietaire;
} 