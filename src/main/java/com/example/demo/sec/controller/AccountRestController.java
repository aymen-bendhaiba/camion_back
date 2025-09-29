package com.example.demo.sec.controller;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.repo.UserAppRepository;
import com.example.demo.sec.services.AccountService;
import com.example.demo.sockets.SocketUpdatesHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Month;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
public class AccountRestController {
    private AccountService accountService;
    @Autowired
    private UserAppRepository userAppRepository;

    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }
    @GetMapping("/users")
    public List<AppUser> appUsers(){
        return accountService.getUsers();
    }
    @PostMapping("/user")
    public ResponseEntity<?> addUser(@RequestBody AppUser user){
        try {
            // Vérifier si l'utilisateur existe déjà
            AppUser existingUser = accountService.loadUserByUsername(user.getUsername());
            if(existingUser != null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "username_already_exists", "message", "Ce nom d'utilisateur existe déjà"));
            }
            
            // Vérifier si l'email existe déjà
            AppUser existingEmail = accountService.loadUserByEmail(user.getEmail());
            if(existingEmail != null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "email_already_exists", "message", "Cette adresse email existe déjà"));
            }
            
            // Valider les champs requis
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_data", "message", "Le nom d'utilisateur est requis"));
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_data", "message", "L'email est requis"));
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_data", "message", "Le mot de passe est requis"));
            }
            
            // Créer le nouvel utilisateur
            AppUser savedUser = accountService.addNewAccount(user);
            SocketUpdatesHandler.notifyClients(); // Notifie tous les clients WebSocket
            
            return ResponseEntity.ok(savedUser);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "signup_error", "message", "Erreur lors de la création du compte: " + e.getMessage()));
        }
    }

    @DeleteMapping("user/{id}")
    public void deleteUser(@PathVariable Long id){
        userAppRepository.deleteById(id);
        SocketUpdatesHandler.notifyClients();
    }
    @PutMapping("/userupdate/{id}")
    public void updateUser(@PathVariable Long id,@RequestBody AppUser user){
        accountService.updateUser(id,user);

    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody AppUser user, Principal principal){
        try {
            System.out.println("=== DÉBUT MISE À JOUR PROFIL ===");
            String username = principal.getName();
            System.out.println("Username: " + username);
            System.out.println("Données reçues: " + user);
            
            AppUser currentUser = accountService.loadUserByUsername(username);
            
            if (currentUser == null) {
                System.out.println("Erreur: Utilisateur non trouvé");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "user_not_found", "message", "Utilisateur non trouvé"));
            }
            
            System.out.println("Utilisateur trouvé: " + currentUser.getUsername());
            
            // Mettre à jour seulement les champs autorisés
            if (user.getFirstName() != null) {
                System.out.println("Mise à jour firstName: " + user.getFirstName());
                currentUser.setFirstName(user.getFirstName());
            }
            if (user.getLastName() != null) {
                System.out.println("Mise à jour lastName: " + user.getLastName());
                currentUser.setLastName(user.getLastName());
            }
            if (user.getPhone() != null) {
                System.out.println("Mise à jour phone: " + user.getPhone());
                currentUser.setPhone(user.getPhone());
            }
            if (user.getCompanyName() != null) {
                System.out.println("Mise à jour companyName: " + user.getCompanyName());
                currentUser.setCompanyName(user.getCompanyName());
            }
            
            // Sauvegarder les modifications
            accountService.updateUserObje(currentUser);
            System.out.println("Profil mis à jour avec succès");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profil mis à jour avec succès",
                "user", currentUser
            ));
            
        } catch (Exception e) {
            System.out.println("=== ERREUR MISE À JOUR PROFIL ===");
            System.out.println("Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", "update_error", "message", "Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }
    @PutMapping("/userupdatePassword")
    public AppUser updateUserPass(@RequestBody AppUser user){
        return accountService.updatePassword(user.getUsername(),user.getPassword());

    }

    @GetMapping("/profil")
    public Map<String, Object> profile(Principal principal){
        AppUser user = accountService.loadUserByUsername(principal.getName());
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("email", user.getEmail());
        resp.put("firstName", user.getFirstName());
        resp.put("lastName", user.getLastName());
        resp.put("phone", user.getPhone());
        resp.put("companyName", user.getCompanyName());
        resp.put("userType", user.getUserType());
        resp.put("twoFactorEnabled", user.isTwoFactorEnabled());
        resp.put("twoFactorVerified", user.isTwoFactorVerified());
        // Solde explicitement renvoyé
        resp.put("solde", user.getSolde() == null ? 0.0 : user.getSolde());
        return resp;
    }

}
