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
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // app, email, sms
    private String message;
    private boolean lue;
    private LocalDateTime dateEnvoi;

    @ManyToOne
    private AppUser destinataire;

    @ManyToOne
    private Reservation reservation;
} 