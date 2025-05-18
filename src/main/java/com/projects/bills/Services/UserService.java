package com.projects.bills.Services;

import com.projects.bills.DTOs.AuthDTO;
import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.UpdateType;
import com.projects.bills.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordService passwordService;

    private final JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public Optional<UserDTO> findDtoByUsername(String username) {
        return userRepository.findByUsername(username).map(this::mapToDTO);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserDTO registerUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        validatePasswordStrength(userDTO.getPassword());

        validateEmailFormat(userDTO.getEmail());

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordService.hashPassword(userDTO.getPassword()));
        user.setEnabled(true);
        user.setRoles("ROLE_USER");
        user.setMfaEnabled(false);
        user.setCreatedAt(LocalDateTime.now());

        User newUser = userRepository.save(user);

        return mapToDTO(newUser);
    }

    public AuthDTO login(UserDTO userDTO) {
        Optional<User> userOpt = userRepository.findByUsername(userDTO.getUsername());

        if (userOpt.isEmpty()) {
            // Allows the username or email to be used for login
            userOpt = userRepository.findByEmail(userDTO.getUsername());
        }

        User user = userOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed"));

        if (!passwordService.verifyPassword(userDTO.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed");
        }

        user.setLastLogin(LocalDateTime.now());
        user.setRecycleDate(null);
        userRepository.save(user);

        String jwt = jwtService.generateAccessToken(user.getUsername(), List.of(user.getRoles()));
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), List.of(user.getRoles()));

        AuthDTO authDTO = new AuthDTO();
        authDTO.setUsername(user.getUsername());
        authDTO.setAccessToken(jwt);
        authDTO.setRefreshToken(refreshToken);

        return authDTO;
    }

    public UserDTO updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required for update");
        }

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordService.verifyPassword(userDTO.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed");
        }

        switch (getUpdateType(userDTO)) {
            case EMAIL -> updateEmail(userDTO, user);
            case PASSWORD -> updatePassword(userDTO, user);
            case RECYCLE -> updateAccountRecycleDate(userDTO, user);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid update operation specified");
        }

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    private void updatePassword(UserDTO userDTO, User user) {
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank() ||
                userDTO.getNewPassword() == null || userDTO.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current and new password are required");
        }

        if (userDTO.getPassword().equals(userDTO.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as the current password");
        }

        validatePasswordStrength(userDTO.getNewPassword());

        user.setPassword(passwordService.hashPassword(userDTO.getNewPassword()));
    }

    private void updateEmail(UserDTO userDTO, User user) {
        String newEmail = Optional.ofNullable(userDTO.getNewEmail())
                .filter(email -> !email.isBlank())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "New email is required"));

        if (user.getEmail().equals(newEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New email cannot be the same as the current email");
        }

        validateEmailFormat(newEmail);

        if (userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        user.setEmail(newEmail);
    }

    private void updateAccountRecycleDate(UserDTO userDTO, User user) {
        user.setRecycleDate(LocalDateTime.now());
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
                user.getRecycleDate() != null,
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }

    private UpdateType getUpdateType(UserDTO userDTO) {
        if (userDTO.getNewEmail() != null) return UpdateType.EMAIL;
        if (userDTO.getNewPassword() != null) return UpdateType.PASSWORD;
        if (userDTO.getRecycle() != null && userDTO.getRecycle()) return UpdateType.RECYCLE;
        return UpdateType.NONE;
    }

    private void validatePasswordStrength(String password) {
        // At least 8 chars, one upper, one lower, one digit, one special char
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (password == null || !password.matches(pattern)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character"
            );
        }
    }

    private void validateEmailFormat(String email) {
        String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (email == null || !email.matches(pattern)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email format"
            );
        }
    }
}