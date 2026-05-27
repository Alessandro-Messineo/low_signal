package org.ecommerce.java.low_signal.controller;

import java.util.Map;
import java.util.Set;

import org.ecommerce.java.low_signal.dto.LoginDto;
import org.ecommerce.java.low_signal.dto.RegisterDto;
import org.ecommerce.java.low_signal.model.Role;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.RoleRepository;
import org.ecommerce.java.low_signal.repository.UserRepository;
import org.ecommerce.java.low_signal.security.DatabaseUserDetails;
import org.ecommerce.java.low_signal.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto,
            BindingResult result) {

        // Errori di validazione (es. email malformata, password troppo corta)
        if (result.hasErrors()) {
            Map<String, String> errors = new java.util.LinkedHashMap<>();
            result.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        // Controlla che l'email non sia già registrata
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already in use"));
        }

        // Recupera il ruolo USER dal DB (deve esistere nella tabella roles)
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found in DB"));

        // Costruisce e salva il nuovo utente
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
            // Spring verifica email + password contro il DB
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

            DatabaseUserDetails userDetails = (DatabaseUserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "id", userDetails.getId(),
                    "email", userDetails.getUsername(),
                    "roles", userDetails.getAuthorities().stream()
                            .map(a -> a.getAuthority()).toList()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }
}
