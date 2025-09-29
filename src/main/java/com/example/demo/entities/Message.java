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
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private LocalDateTime dateEnvoi;

    @ManyToOne
    private AppUser expediteur;

    @ManyToOne
    private AppUser destinataire;

    @ManyToOne
    private Reservation reservation;

    private boolean lu = false;
} 