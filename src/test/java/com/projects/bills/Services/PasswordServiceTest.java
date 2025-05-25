package com.projects.bills.Services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {
    @Test
    void testHashPassword_NotNullAndNotEqualToPlain() {
        PasswordService passwordService = new PasswordService();
        String plainPassword = "mySecret123";

        String hashed = passwordService.hashPassword(plainPassword);

        assertNotNull(hashed);
        assertNotEquals(plainPassword, hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$"));
    }

    @Test
    void testVerifyPassword_CorrectPassword_ReturnsTrue() {
        PasswordService passwordService = new PasswordService();
        String plainPassword = "mySecret123";
        String hashed = passwordService.hashPassword(plainPassword);

        boolean matches = passwordService.verifyPassword(plainPassword, hashed);

        assertTrue(matches);
    }

    @Test
    void testVerifyPassword_WrongPassword_ReturnsFalse() {
        PasswordService passwordService = new PasswordService();
        String plainPassword = "mySecret123";
        String wrongPassword = "wrongPassword";
        String hashed = passwordService.hashPassword(plainPassword);

        boolean matches = passwordService.verifyPassword(wrongPassword, hashed);

        assertFalse(matches);
    }
}