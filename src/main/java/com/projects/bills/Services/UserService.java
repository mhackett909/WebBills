package com.projects.bills.Services;

import com.projects.bills.DTOs.AuthDTO;
import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.UpdateType;
import com.projects.bills.Mappers.UserMapper;
import com.projects.bills.Repositories.UserRepository;
import com.projects.bills.Constants.Exceptions;
import com.projects.bills.Constants.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final UserMapper userMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    public UserDTO registerUser(UserDTO userDTO) {
        logger.info("User registration attempt: {}", userDTO.getUsername());

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            logger.warn("Registration failed: username already exists: {}", userDTO.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, Exceptions.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.warn("Registration failed: email already registered: {}", userDTO.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, Exceptions.EMAIL_ALREADY_REGISTERED);
        }

        try {
            validatePasswordStrength(userDTO.getPassword());
            validateEmailFormat(userDTO.getEmail());
        } catch (ResponseStatusException e) {
            logger.warn("Registration failed for {}: {}", userDTO.getUsername(), e.getReason());
            throw e;
        }

        String passwordHash = passwordService.hashPassword(userDTO.getPassword());
        User user = userMapper.mapToEntity(userDTO, passwordHash);
        User newUser = userRepository.save(user);
        logger.info("User registered successfully: {}", newUser.getUsername());
        return userMapper.mapToDTO(newUser);
    }

    public AuthDTO login(UserDTO userDTO) {
        logger.info("User login attempt: {}", userDTO.getUsername());
        Optional<User> userOpt = userRepository.findByUsername(userDTO.getUsername());

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(userDTO.getUsername());
        }

        User user = userOpt.orElseThrow(() -> {
            logger.warn("Login failed: user not found for {}", userDTO.getUsername());
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, Exceptions.LOGIN_FAILED);
        });

        if (!passwordService.verifyPassword(userDTO.getPassword(), user.getPassword())) {
            logger.warn("Login failed: invalid password for user {}", userDTO.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Exceptions.LOGIN_FAILED);
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
        logger.info("User logged in successfully: {}", user.getUsername());
        return authDTO;
    }

    public UserDTO updateUser(UserDTO userDTO, String userName) {
        logger.info("User update attempt: {}", userName);
        if (userDTO.getId() == null || userDTO.getId() == 0) {
            logger.warn("Update failed: user ID is required");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.USER_ID_REQUIRED_FOR_UPDATE);
        }

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> {
                    logger.warn("Update failed: user not found for ID {}", userDTO.getId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format(Exceptions.USER_NOT_FOUND, userDTO.getId()));
                });

        if (!user.getUsername().equalsIgnoreCase(userName)) {
            logger.warn("Update failed: not authorized for user {}", userName);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_UPDATE_USER);
        }

        if (!passwordService.verifyPassword(userDTO.getPassword(), user.getPassword())) {
            logger.warn("Update failed: invalid password for user {}", userName);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Exceptions.LOGIN_FAILED);
        }

        try {
            switch (getUpdateType(userDTO)) {
                case EMAIL -> {
                    logger.info("Email update attempt for user: {}", userName);
                    updateEmail(userDTO, user);
                }
                case PASSWORD -> {
                    logger.info("Password update attempt for user: {}", userName);
                    updatePassword(userDTO, user);
                }
                case RECYCLE -> {
                    logger.info("Recycle request for user: {}", userName);
                    user.setRecycleDate(LocalDateTime.now());
                }
                default -> {
                    logger.warn("Update failed: no valid update operation specified for user {}", userName);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.NO_VALID_UPDATE_OPERATION);
                }
            }
        } catch (ResponseStatusException e) {
            logger.warn("Update failed for user {}: {}", userName, e.getReason());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during update for user {}: {}", userName, e.getMessage());
            throw e;
        }

        User savedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", userName);
        return userMapper.mapToDTO(savedUser);
    }

    public Optional<UserDTO> findDtoByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::mapToDTO);
    }

    protected Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private void updatePassword(UserDTO userDTO, User user) {
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank() ||
                userDTO.getNewPassword() == null || userDTO.getNewPassword().isBlank()) {
            logger.warn("Password update failed: current and new password required for user {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.CURRENT_AND_NEW_PASSWORD_REQUIRED);
        }
        if (userDTO.getPassword().equals(userDTO.getNewPassword())) {
            logger.warn("Password update failed: new password same as current for user {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.NEW_PASSWORD_SAME_AS_CURRENT);
        }
        try {
            validatePasswordStrength(userDTO.getNewPassword());
        } catch (ResponseStatusException e) {
            logger.warn("Password update failed for user {}: {}", user.getUsername(), e.getReason());
            throw e;
        }
        user.setPassword(passwordService.hashPassword(userDTO.getNewPassword()));
    }

    private void updateEmail(UserDTO userDTO, User user) {
        String newEmail = Optional.ofNullable(userDTO.getNewEmail())
                .filter(email -> !email.isBlank())
                .orElseThrow(() -> {
                    logger.warn("Email update failed: new email required for user {}", user.getUsername());
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.NEW_EMAIL_REQUIRED);
                });
        if (user.getEmail().equals(newEmail)) {
            logger.warn("Email update failed: new email same as current for user {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.NEW_EMAIL_SAME_AS_CURRENT);
        }
        try {
            validateEmailFormat(newEmail);
        } catch (ResponseStatusException e) {
            logger.warn("Email update failed for user {}: {}", user.getUsername(), e.getReason());
            throw e;
        }
        if (userRepository.existsByEmail(newEmail)) {
            logger.warn("Email update failed: email already registered: {}", newEmail);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Exceptions.EMAIL_ALREADY_REGISTERED);
        }
        user.setEmail(newEmail);
    }

    private UpdateType getUpdateType(UserDTO userDTO) {
        if (userDTO.getNewEmail() != null) return UpdateType.EMAIL;
        if (userDTO.getNewPassword() != null) return UpdateType.PASSWORD;
        if (userDTO.getRecycle() != null && userDTO.getRecycle()) return UpdateType.RECYCLE;
        return UpdateType.NONE;
    }

    private void validatePasswordStrength(String password) {
        String pattern = Regex.PASSWORD;
        if (password == null || !password.matches(pattern)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    Exceptions.PASSWORD_STRENGTH
            );
        }
    }

    private void validateEmailFormat(String email) {
        String pattern = Regex.EMAIL;
        if (email == null || !email.matches(pattern)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    Exceptions.INVALID_EMAIL_FORMAT
            );
        }
    }
}

