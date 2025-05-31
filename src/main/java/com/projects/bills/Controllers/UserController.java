package com.projects.bills.Controllers;

import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Services.UserService;
import com.projects.bills.Constants.Exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/v1/user")
    public ResponseEntity<UserDTO> getUser(@RequestParam String userName, @AuthenticationPrincipal UserDetails user) {
        logger.info("User '{}' requested user info for '{}'.", user.getUsername(), userName);
        if (userName == null || userName.isBlank()) {
            logger.warn("Username parameter is missing or blank.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.USERNAME_REQUIRED);
        }

        if (!userName.equalsIgnoreCase(user.getUsername())) {
            logger.warn("User '{}' is not authorized to access user '{}'.", user.getUsername(), userName);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_ACCESS_USER);
        }

        Optional<UserDTO> userDTO = userService.findDtoByUsername(userName);
        if (userDTO.isPresent()) {
            logger.info("User '{}' found for '{}'.", userName, user.getUsername());
            return new ResponseEntity<>(userDTO.get(), HttpStatus.OK);
        } else {
            logger.warn("User '{}' not found.", userName);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format(Exceptions.USER_NOT_FOUND, userName)
            );
        }
    }

    @PutMapping("/api/v1/user")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO, @AuthenticationPrincipal UserDetails user) {
        logger.info("User '{}' is updating their info.", user.getUsername());
        UserDTO responseDTO = userService.updateUser(userDTO, user.getUsername());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
