package com.example.demo.sec.services;





import com.example.demo.sec.entity.AppRole;
import com.example.demo.sec.entity.AppUser;

import java.util.List;

public interface AccountService {
    AppUser addNewAccount(AppUser user);
    AppRole addNewRole(AppRole role);
    void addRoleToUser(String username, String role);
    AppUser loadUserByUsername(String username);
    AppUser loadUserByEmail(String email);
    AppUser loadUserById(Long id);
    AppUser updatePassword(String username, String newPassword);
    List<AppUser> getUsers();
    public void updateUser(Long id,AppUser user);
    public void updateUserObje(AppUser user);
    public AppUser findByActivationToken(String activationToken);

}
