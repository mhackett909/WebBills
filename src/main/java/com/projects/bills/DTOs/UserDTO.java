package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String newPassword;
    private String newEmail;
    private String roles;
    private Boolean enabled = true;
    private Boolean mfaEnabled = false;
    private Boolean recycle = false;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
