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
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Reservation reservation;

    @ManyToOne(optional = false)
    private AppUser transporteur;

    @Column(name = "prix_propose")
    private Double prixPropose; // prix proposé (DH/tonne)

    @Column(length = 1000)
    private String commentaire;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Boolean selected = false;

    // renommé: validated (au lieu de validatedByAdmin)
    @Column(name = "validated")
    private Boolean validated = false;
}


