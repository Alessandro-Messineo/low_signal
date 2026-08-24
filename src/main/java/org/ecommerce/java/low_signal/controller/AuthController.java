package org.ecommerce.java.low_signal.controller;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.ecommerce.java.low_signal.dto.LoginDto;
import org.ecommerce.java.low_signal.dto.RegisterDto;
import org.ecommerce.java.low_signal.model.RefreshToken;
import org.ecommerce.java.low_signal.model.Role;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.RoleRepository;
import org.ecommerce.java.low_signal.repository.UserRepository;
import org.ecommerce.java.low_signal.security.DatabaseUserDetails;
import org.ecommerce.java.low_signal.security.DatabaseUserDetailService;
import org.ecommerce.java.low_signal.security.JwtUtil;
import org.ecommerce.java.low_signal.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthenticationManager authManager;

        private final JwtUtil jwtUtil;

        private final RefreshTokenService refreshTokenService;

        private final DatabaseUserDetailService userDetailService;

        private final UserRepository userRepository;

        private final RoleRepository roleRepository;
        
        private final PasswordEncoder passwordEncoder;

        @Value("${app.cookie.secure}")
        private boolean cookieSecure;

        @Value("${app.cookie.same-site}")
        private String cookieSameSite;

        public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, RefreshTokenService refreshTokenService, DatabaseUserDetailService userDetailService, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
                this.authManager = authManager;
                this.jwtUtil = jwtUtil;
                this.refreshTokenService = refreshTokenService;
                this.userDetailService = userDetailService;
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @PostMapping("/register")
        public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto,
                        BindingResult result) {
                if (result.hasErrors()) {
                        Map<String, String> errors = new java.util.LinkedHashMap<>();
                        result.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
                        return ResponseEntity.badRequest().body(errors);
                }
                if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("error", "Email already in use"));
                }
                Role userRole = roleRepository.findByName("USER")
                                .orElseThrow(() -> new RuntimeException("Role USER not found in DB"));

                User newUser = new User();
                newUser.setName(dto.getName());
                newUser.setSurname(dto.getSurname());
                newUser.setEmail(dto.getEmail());
                newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
                newUser.setPhone(dto.getPhone());
                newUser.setRoles(Set.of(userRole));
                userRepository.save(newUser);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(Map.of("message", "User registered successfully"));
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto,
                        BindingResult result) {
                if (result.hasErrors()) {
                        Map<String, String> errors = new java.util.LinkedHashMap<>();
                        result.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
                        return ResponseEntity.badRequest().body(errors);
                }
                try {
                        Authentication auth = authManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

                        DatabaseUserDetails userDetails = (DatabaseUserDetails) auth.getPrincipal();
                        String accessToken = jwtUtil.generateToken(userDetails);

                        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow();
                        RefreshToken saved = refreshTokenService.createRefreshToken(user);

                        ResponseCookie cookie = buildRefreshCookie(saved.getToken());

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                        .body(Map.of(
                                                        "accessToken", accessToken,
                                                        "id", userDetails.getId(),
                                                        "email", userDetails.getUsername(),
                                                        "roles", userDetails.getAuthorities().stream()
                                                                        .map(a -> a.getAuthority()).toList()));

                } catch (AuthenticationException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", "Invalid email or password"));
                }
        }

        @PostMapping("/refresh")
        public ResponseEntity<?> refresh(
                        @CookieValue(name = "refreshToken", required = false) String tokenValue) {

                if (tokenValue == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", "Refresh token missing"));
                }
                try {
                        RefreshToken validated = refreshTokenService.validateRefreshToken(tokenValue);

                        DatabaseUserDetails userDetails = (DatabaseUserDetails) userDetailService
                                        .loadUserByUsername(validated.getUser().getEmail());

                        String newAccessToken = jwtUtil.generateToken(userDetails);

                        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

                } catch (RuntimeException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout() {
                DatabaseUserDetails userDetails = (DatabaseUserDetails) SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getPrincipal();

                User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
                refreshTokenService.revokeAllUserTokens(user);

                ResponseCookie expired = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .path("/api/auth")
                                .maxAge(0)
                                .sameSite(cookieSameSite)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, expired.toString())
                                .body(Map.of("message", "Logged out successfully"));
        }

        private ResponseCookie buildRefreshCookie(String token) {
                return ResponseCookie.from("refreshToken", token)
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .path("/api/auth")
                                .maxAge(Duration.ofDays(7))
                                .sameSite(cookieSameSite)
                                .build();
        }
}