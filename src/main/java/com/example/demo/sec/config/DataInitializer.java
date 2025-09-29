package com.example.demo.sec.config;

import com.example.demo.sec.entity.AppRole;
import com.example.demo.sec.repo.AppRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialiser les rôles par défaut
        initializeRoles();
    }

    private void initializeRoles() {
        // Vérifier et créer le rôle CHARGEUR
        if (appRoleRepository.findByRolename("CHARGEUR") == null) {
            AppRole chargeurRole = new AppRole();
            chargeurRole.setRolename("CHARGEUR");
            appRoleRepository.save(chargeurRole);
            System.out.println("Rôle CHARGEUR créé");
        }

        // Vérifier et créer le rôle TRANSPORTEUR
        if (appRoleRepository.findByRolename("TRANSPORTEUR") == null) {
            AppRole transporteurRole = new AppRole();
            transporteurRole.setRolename("TRANSPORTEUR");
            appRoleRepository.save(transporteurRole);
            System.out.println("Rôle TRANSPORTEUR créé");
        }

        // Vérifier et créer le rôle ADMIN
        if (appRoleRepository.findByRolename("ADMIN") == null) {
            AppRole adminRole = new AppRole();
            adminRole.setRolename("ADMIN");
            appRoleRepository.save(adminRole);
            System.out.println("Rôle ADMIN créé");
        }

        // Vérifier et créer le rôle USER
        if (appRoleRepository.findByRolename("USER") == null) {
            AppRole userRole = new AppRole();
            userRole.setRolename("USER");
            appRoleRepository.save(userRole);
            System.out.println("Rôle USER créé");
        }
    }
} 