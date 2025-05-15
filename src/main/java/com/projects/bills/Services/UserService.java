package com.projects.bills.Services;

import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.UpdateType;
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

    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::mapToDTO);
    }

    public UserDTO registerUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordService.hashPassword(userDTO.getPassword()));
        user.setEnabled(true);
        user.setRoles("ROLE_USER");
        user.setMfaEnabled(false);
        user.setCreatedAt(java.time.LocalDateTime.now());

        User newUser = userRepository.save(user);

        return mapToDTO(newUser);
    }

    public UserDTO login(UserDTO userDTO) {
        Optional<User> userOpt = userRepository.findByUsername(userDTO.getUsername());

        if (userOpt.isEmpty()) {
            // Allows the username or email to be used for login
            userOpt = userRepository.findByEmail(userDTO.getUsername());
        }

        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("Login failed"));

        if (!passwordService.verifyPassword(userDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Login failed");
        }

        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        return mapToDTO(user);
    }

    public UserDTO updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new IllegalArgumentException("User ID is required for update");
        }

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordService.verifyPassword(userDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Login failed");
        }

        switch (getUpdateType(userDTO)) {
            case EMAIL -> updateEmail(userDTO, user);
            case PASSWORD -> updatePassword(userDTO, user);
            case STATUS -> updateAccountStatus(userDTO, user);
            default -> throw new IllegalArgumentException("No valid update operation specified");
        }

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    private void updatePassword(UserDTO userDTO, User user) {
        // TODO Password format validation
        user.setPassword(passwordService.hashPassword(userDTO.getNewPassword()));
    }

    private void updateEmail(UserDTO userDTO, User user) {
        // TODO Email format validation

        String newEmail = Optional.ofNullable(userDTO.getNewEmail())
                .filter(email -> !email.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("New email is required"));

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.setEmail(newEmail);
    }

    private void updateAccountStatus(UserDTO userDTO, User user) {
        user.setEnabled(userDTO.isEnabled());
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                null,
                null,
                user.getRoles(),
                user.isEnabled(),
                user.isMfaEnabled(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }

    private UpdateType getUpdateType(UserDTO userDTO) {
        if (userDTO.getNewEmail() != null) return UpdateType.EMAIL;
        if (userDTO.getNewPassword() != null) return UpdateType.PASSWORD;
        if (userDTO.getId() != null) return UpdateType.STATUS;
        return UpdateType.NONE;
    }
}
