package org.ecommerce.java.low_signal.service;

import java.util.List;

import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() { 
        return userRepository.findAll();
    }
}
