package com.projects.bills.Controllers;

import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/v1/user")
    public ResponseEntity<UserDTO> getUser(@RequestParam String userName) {
        if (userName == null || userName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!userName.equalsIgnoreCase(requestingUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this user");
        }

        Optional<UserDTO> userDTO = userService.findDtoByUsername(userName);
        if (userDTO.isPresent()) {
            return new ResponseEntity<>(userDTO.get(), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @PutMapping("/api/v1/user")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO) {
        UserDTO responseDTO = userService.updateUser(userDTO);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
