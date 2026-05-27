package org.ecommerce.java.low_signal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Niente sessioni: ogni richiesta è autenticata tramite il token JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        // Endpoint pubblici: registrazione e login non richiedono token
                        // .requestMatchers("/api/auth/register").permitAll()
                        // .requestMatchers("/api/auth/login").permitAll() 
                        .requestMatchers("/api/auth/**").permitAll()
                        // Endpoint riservati agli admin
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        // Tutto il resto richiede un token valido
                        .anyRequest().authenticated())
                // Il JwtFilter viene eseguito prima del filtro standard di Spring Security
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // @Bean
    // DatabaseUserDetailService userDetailService() {
    //     return new DatabaseUserDetailService();
    // }

    @Bean
     DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder, DatabaseUserDetailService userDetailService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // Necessario per usare AuthenticationManager nel controller
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
