package org.ecommerce.java.low_signal.security;

import java.util.Optional;

import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class DatabaseUserDetailService implements UserDetailsService{

    private final UserRepository userRepository;

    public DatabaseUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
         Optional<User> userAttempt = userRepository.findByEmail(email);

        if (userAttempt.isEmpty()) {
            throw new UsernameNotFoundException("username not found");
        }
        return new DatabaseUserDetails(userAttempt.get());
    }
    
}