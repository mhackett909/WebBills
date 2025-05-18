package com.projects.bills.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column
    private String roles = "ROLE_USER";

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "recycle_date")
    private LocalDateTime recycleDate;

    @OneToMany(mappedBy = "user")
    @OrderBy("name DESC")
    private List<Bill> bills;

    // Constructors
    public User() {
    }

    public User(String username, String password, String email, boolean enabled, String roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
    }
}