// src/main/java/com/ems/service/UserService.java
package com.ems.service;

import com.ems.entity.User;
import com.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElse(null);
    }

    // Remove the findByEmail method for now to fix compilation
    // public User findByEmail(String email) {
    //     Optional<User> userOptional = userRepository.findByEmail(email);
    //     return userOptional.orElse(null);
    // }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}