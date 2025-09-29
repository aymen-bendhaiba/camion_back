package com.example.demo.sec.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.sec.entity.AppUser;
import com.example.demo.sec.services.AccountService;
import com.example.demo.sec.services.TwoFactorService;
import com.example.demo.sec.services.AccountActivationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtAuthenticationFilterWith2FA extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private AccountService accountService;
    private TwoFactorService twoFactorService;
    private AccountActivationService accountActivationService;

    public JwtAuthenticationFilterWith2FA(AuthenticationManager authenticationManager, 
                                        AccountService accountService, 
                                        TwoFactorService twoFactorService,
                                        AccountActivationService accountActivationService) {
        this.authenticationManager = authenticationManager;
        this.accountService = accountService;
        this.twoFactorService = twoFactorService;
        this.accountActivationService = accountActivationService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // Lire le corps de la requête JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(request.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Extraire les valeurs username et password du JSON
        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("Authentication successful");
        User user = (User) authResult.getPrincipal();
        
        // Vérifier si le compte est activé
        if (!accountActivationService.isAccountActivated(user.getUsername())) {
            // Compte non activé
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("requiresActivation", true);
            responseMap.put("username", user.getUsername());
            responseMap.put("message", "Account not activated");
            
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            new ObjectMapper().writeValue(response.getOutputStream(), responseMap);
            return;
        }
        
        // Vérifier si l'utilisateur a la 2FA activée
        AppUser appUser = accountService.loadUserByUsername(user.getUsername());
        
        if (appUser != null && appUser.isTwoFactorEnabled() && appUser.isTwoFactorVerified()) {
            // L'utilisateur a la 2FA activée, retourner une réponse spéciale
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("requires2FA", true);
            responseMap.put("username", user.getUsername());
            responseMap.put("message", "2FA required");
            
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            new ObjectMapper().writeValue(response.getOutputStream(), responseMap);
        } else {
            // L'utilisateur n'a pas la 2FA, générer le token JWT normalement
            generateJwtTokens(request, response, user);
        }
    }

    private void generateJwtTokens(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        Algorithm algorithm = Algorithm.HMAC256("mysecrter123");
        String jwtAccessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3*24*60*60*1000))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().map(ga -> ga.getAuthority()).collect(Collectors.toList()))
                .sign(algorithm);
        
        String jwtRefreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 15*60*1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);
        
        Map<String, String> idToken = new HashMap<>();
        idToken.put("access_token", jwtAccessToken);
        idToken.put("refresh_token", jwtRefreshToken);
        idToken.put("requires2FA", "false");
        
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), idToken);
    }
} 