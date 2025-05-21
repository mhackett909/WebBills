package com.projects.bills.Controllers;

import com.projects.bills.DTOs.AuthDTO;
import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Services.JwtService;
import com.projects.bills.Services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * AuthController handles authentication-related requests.
 * Endpoints under /auth are open in my Spring security configuration.
 */
@RestController
public class AuthController {
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/api/v1/auth/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        UserDTO newUserDTO = userService.registerUser(userDTO);

        return new ResponseEntity<>(newUserDTO, HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<AuthDTO> login(@RequestBody UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        AuthDTO authDTO = userService.login(userDTO);

        return new ResponseEntity<>(authDTO, HttpStatus.OK);
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<AuthDTO> refresh(@RequestParam String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }
        try {
            Claims claims = jwtService.validateJwt(refreshToken);
            String username = claims.getSubject();
            @SuppressWarnings("unchecked")
            java.util.List<String> roles = (java.util.List<String>) claims.get("roles");
            String newAccessToken = jwtService.generateAccessToken(username, roles);
            String newRefreshToken = jwtService.generateRefreshToken(username, roles);
            return ResponseEntity.ok(new AuthDTO(username, newAccessToken, newRefreshToken));
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token expired");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
    }
}
