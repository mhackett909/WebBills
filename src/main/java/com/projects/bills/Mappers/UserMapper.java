package com.projects.bills.Mappers;

import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Entities.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    public User mapToEntity(UserDTO userDTO, String passwordHash) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordHash);
        user.setEnabled(true);
        user.setRoles("ROLE_USER");
        user.setMfaEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public UserDTO mapToDTO(User user) {
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
}
