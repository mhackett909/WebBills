package com.projects.bills.DTOs;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private String roles;
    private boolean mfaEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRoles() { return roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    public boolean isEnabled() { return enabled; }
    public boolean isMfaEnabled() { return mfaEnabled; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setRoles(String roles) { this.roles = roles; }
    public void setMfaEnabled(boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

}
