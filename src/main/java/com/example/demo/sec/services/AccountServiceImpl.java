package com.example.demo.sec.services;

import com.example.demo.sec.entity.AppRole;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.repo.AppRoleRepository;
import com.example.demo.sec.repo.UserAppRepository;
import com.example.demo.sec.services.AccountActivationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {
    private final UserAppRepository userAppRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    @Lazy
    private AccountActivationService accountActivationService;
    
    public AccountServiceImpl(UserAppRepository userAppRepository,
                              AppRoleRepository appRoleRepository,
                              PasswordEncoder passwordEncoder) {
        this.userAppRepository = userAppRepository;
        this.appRoleRepository = appRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void updateUser(Long id,AppUser user) {
        AppUser user1=userAppRepository.findById(id).get();
        user1.setUsername(user.getUsername());
        if(user.getPassword()!=null)
        user1.setPassword(passwordEncoder.encode(user.getPassword()));
        user1.setEmail(user.getEmail());
        userAppRepository.save(user1);

    }


    @Override
    public AppUser addNewAccount(AppUser user) {
        String password = user.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        

        
        // Définir le rôle par défaut selon le type d'utilisateur
        if (user.getUserType() != null) {
            String roleName = user.getUserType().equals("transporteur") ? "TRANSPORTEUR" : "CHARGEUR";
            AppRole role = appRoleRepository.findByRolename(roleName);
            if (role != null) {
                user.getRoles().add(role);
            }
        }
        user.setActive(true);
        AppUser savedUser = userAppRepository.save(user);


        // Envoyer l'email d'activation
        //accountActivationService.sendActivationEmail(savedUser);
        
        return savedUser;
    }

    @Override
    public AppRole addNewRole(AppRole role) {
        return appRoleRepository.save(role);
    }

    @Override
    public void addRoleToUser(String username, String role) {
        AppUser appuser = userAppRepository.findByUsername(username);
        AppRole approle = appRoleRepository.findByRolename(role);
        appuser.getRoles().add(approle);
    }

    @Override
    public AppUser loadUserByUsername(String username) {
        return userAppRepository.findByUsername(username);
    }

    @Override
    public AppUser loadUserByEmail(String email) {
        return userAppRepository.findByEmail(email);    }

    @Override
    public AppUser loadUserById(Long id) {
        return userAppRepository.findById(id).orElse(null);
    }

    @Override
    public AppUser updatePassword(String username, String newPassword) {
        AppUser appuser = userAppRepository.findByUsername(username);
        appuser.setPassword(passwordEncoder.encode(newPassword));
        return userAppRepository.save(appuser);
    }

    @Override
    public List<AppUser> getUsers() {
        return userAppRepository.findAll();
    }
    @Override
    public void updateUserObje(AppUser user) {
        userAppRepository.save(user);
    }
    
    @Override
    public AppUser findByActivationToken(String activationToken) {
        return userAppRepository.findByActivationToken(activationToken);
    }
}