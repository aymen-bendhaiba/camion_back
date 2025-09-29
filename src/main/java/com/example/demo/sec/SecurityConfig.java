package com.example.demo.sec;

import com.example.demo.sec.filters.JwtAuthenticationFilter;
import com.example.demo.sec.filters.JwtAuthenticationFilterWith2FA;
import com.example.demo.sec.filters.JwtAuthorisationFilter;
import com.example.demo.sec.services.AccountService;
import com.example.demo.sec.services.TwoFactorService;
import com.example.demo.sec.services.AccountActivationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, 
                                                 AccountService accountService, TwoFactorService twoFactorService,
                                                 AccountActivationService accountActivationService) throws Exception {
        JwtAuthenticationFilterWith2FA jwtAuthenticationFilter = new JwtAuthenticationFilterWith2FA(authenticationManager, accountService, twoFactorService, accountActivationService);
        jwtAuthenticationFilter.setFilterProcessesUrl("/login");

        http

                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Appliquer la source de configuration CORS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/correctCode").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/userupdatePassword").permitAll()
                        .requestMatchers(HttpMethod.POST, "/send-reset-code/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/verify-reset-code").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user").permitAll() // Permettre uniquement POST pour /user
                        .requestMatchers(HttpMethod.PUT, "/userupdate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/camions/*/simulate-movement").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**","/email").permitAll()
                        .requestMatchers("/api/2fa/**").permitAll() // Permettre tous les endpoints 2FA
                        .requestMatchers("/activate-account").permitAll() // Activation de compte
                        .requestMatchers(HttpMethod.POST, "/resend-activation").permitAll() // Renvoyer activation
                        .requestMatchers("/check-activation/**").permitAll() // Vérifier activation
                        .anyRequest().authenticated()
                )

                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(new JwtAuthorisationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://192.168.32.1:3000", "http://localhost:8080", "http://192.168.32.1:8080")); // Ajouter les origines
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Méthodes autorisées
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With")); // En-têtes autorisés
        configuration.setAllowCredentials(true); // Si vous avez besoin de gérer les cookies
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Appliquer à toutes les URL
        return source;
    }
}