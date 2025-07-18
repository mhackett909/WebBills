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
    private long id;

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

    @Column(columnDefinition = "TINYINT", nullable = false)
    private boolean enabled = true;

    @Column
    private String roles = "ROLE_USER";

    @Column(name = "mfa_enabled", columnDefinition = "TINYINT", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "recycle_date")
    private LocalDateTime recycleDate;

    @OneToMany(mappedBy = "user")
    @OrderBy("name DESC")
    private List<Bill> bills;
}