package com.projects.bills.Services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PasswordService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String hashPassword(String plainPassword) {
        logger.info("Hashing password");
        String hashed = passwordEncoder.encode(plainPassword);
        logger.debug("Password hashed successfully");
        return hashed;
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        logger.info("Verifying password");
        boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);
        logger.debug("Password verification result: {}", matches);
        return matches;
    }
}
