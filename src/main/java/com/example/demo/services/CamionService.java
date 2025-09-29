package com.example.demo.services;

import com.example.demo.entities.Camion;
import com.example.demo.sec.entity.AppUser;
import java.util.List;
import java.util.Optional;

public interface CamionService {
    Camion saveCamion(Camion camion);
    List<Camion> getAllCamions();
    List<Camion> getCamionsProches(double latitude, double longitude, double rayonKm);
    Camion getCamionById(long id);
    Camion updateCamion(long id, Camion camion);
    Optional<Camion> getCamionByTransporteur(AppUser transporteur);
} 