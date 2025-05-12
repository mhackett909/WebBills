package com.projects.bills.Services;

import com.projects.bills.Entities.User;
import com.projects.bills.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordService passwordService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Registers a new user with a hashed password.
     * @param user The user object containing registration details.
     * @return The registered user.
     */
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Hash password before saving
        user.setPassword(passwordService.hashPassword(user.getPassword()));

        // Set default roles if not provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles("ROLE_USER");
        }

        return userRepository.save(user);
    }

    /**
     * Finds a user by username.
     * @param username The username to search for.
     * @return Optional containing the user if found.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by email.
     * @param email The email to search for.
     * @return Optional containing the user if found.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Updates a user's information (excluding password).
     * @param id The user ID.
     * @param updatedUser The updated user details.
     * @return The updated user.
     */
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setEnabled(updatedUser.isEnabled());
        user.setRoles(updatedUser.getRoles());

        return userRepository.save(user);
    }

    /**
     * Changes the user's password with secure hashing.
     * @param id The user ID.
     * @param newPassword The new password.
     */
    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordService.hashPassword(newPassword));
        userRepository.save(user);
    }

    /**
     * Deletes a user by ID.
     * @param id The user ID to delete.
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }

        userRepository.deleteById(id);
    }
}
