package com.projects.bills.Controllers;

import com.projects.bills.DTOs.AuthDTO;
import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Services.JwtService;
import com.projects.bills.Services.UserService;
import com.projects.bills.Constants.Exceptions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * AuthController handles authentication-related requests.
 * Endpoints under /auth are open in my Spring security configuration.
 */
@RestController
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/api/v1/auth/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        logger.info("User creation attempt: {}", userDTO.getUsername());
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            logger.warn("User creation failed: username is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.USERNAME_REQUIRED);
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            logger.warn("User creation failed: email is missing for username: {}", userDTO.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.EMAIL_REQUIRED);
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            logger.warn("User creation failed: password is missing for username: {}", userDTO.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PASSWORD_REQUIRED);
        }

        UserDTO newUserDTO = userService.registerUser(userDTO);
        return new ResponseEntity<>(newUserDTO, HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<AuthDTO> login(@RequestBody UserDTO userDTO) {
        logger.info("User login attempt: {}", userDTO.getUsername());
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            logger.warn("Login failed: username is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.USERNAME_REQUIRED);
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            logger.warn("Login failed: password is missing for username: {}", userDTO.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PASSWORD_REQUIRED);
        }

        AuthDTO authDTO = userService.login(userDTO);
        return new ResponseEntity<>(authDTO, HttpStatus.OK);
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<AuthDTO> refresh(@RequestParam String refreshToken) {
        logger.info("Token refresh attempt");
        if (refreshToken == null || refreshToken.isBlank()) {
            logger.warn("Token refresh failed: refresh token is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.REFRESH_TOKEN_REQUIRED);
        }
        try {
            Claims claims = jwtService.validateJwt(refreshToken);
            String username = claims.getSubject();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            String newAccessToken = jwtService.generateAccessToken(username, roles);
            String newRefreshToken = jwtService.generateRefreshToken(username, roles);
            logger.info("Token refreshed successfully for user: {}", username);
            return ResponseEntity.ok(new AuthDTO(username, newAccessToken, newRefreshToken));
        } catch (ExpiredJwtException e) {
            logger.warn("Token refresh failed: refresh token expired");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.REFRESH_TOKEN_EXPIRED);
        } catch (Exception e) {
            logger.error("Token refresh failed: invalid refresh token. Reason: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Exceptions.INVALID_REFRESH_TOKEN);
        }
    }
}
