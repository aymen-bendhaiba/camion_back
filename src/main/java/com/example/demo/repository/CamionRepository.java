package com.example.demo.repository;

import com.example.demo.entities.Camion;
import com.example.demo.sec.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
 
@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    Optional<Camion> findByTransporteur(AppUser transporteur);
} 