package org.ecommerce.java.low_signal.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtFilter jwtFilter;

    public SecurityConfiguration(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        .ignoringRequestMatchers("/api/auth/**")
                        // Stripe non puo' mandare il token XSRF: la sicurezza
                        // del webhook sta nella verifica della firma
                        .ignoringRequestMatchers("/payments/webhook"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                                .requestMatcher(request -> true)
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self' https:; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'none'"
                                )
                        )
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/collections").permitAll()
                        .requestMatchers(HttpMethod.POST, "/collections").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/collections/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/orders").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/orders").authenticated()
                        .requestMatchers("/orders/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/newsletter").permitAll()
                        // ── Pagamenti Stripe ─────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/payments/intent").authenticated()
                        .requestMatchers(HttpMethod.GET, "/payments/status/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/payments/release-expired").hasAuthority("ADMIN")
                        // Il webhook e' pubblico: la protezione e' la firma HMAC
                        .requestMatchers(HttpMethod.POST, "/payments/webhook").permitAll()
                        .anyRequest().denyAll())
                        // 401 con body JSON per le richieste senza JWT valido: cosi'
                        // il frontend puo' rinnovare il token e riprovare.
                        // 403 con body JSON: senza il body il rifiuto CSRF e'
                        // indistinguibile da un 403 di autorizzazione.
                        // CsrfConfigurer non espone accessDeniedHandler(...):
                        // il CsrfFilter eredita proprio questo handler
                        .exceptionHandling(exception -> exception
                                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                                .accessDeniedHandler(new JsonCsrfAccessDeniedHandler()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:4200",
            "${FRONTEND_URL}"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // @Bean
    // DatabaseUserDetailService userDetailService() {
    // return new DatabaseUserDetailService();
    // }

    @Bean
    DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder,
            DatabaseUserDetailService userDetailService) {
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
