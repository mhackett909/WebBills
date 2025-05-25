package com.projects.bills.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        String secretKey = Base64.getEncoder().encodeToString(keyBytes);
        long accessTokenExpirationMs = 1000 * 60 * 10; // 10 minutes
        long refreshTokenExpirationMs = 1000 * 60 * 60 * 24; // 1 day

        setField(jwtService, "secretKey", secretKey);
        setField(jwtService, "accessTokenExpirationMs", accessTokenExpirationMs);
        setField(jwtService, "refreshTokenExpirationMs", refreshTokenExpirationMs);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        String username = "alice";
        List<String> roles = Arrays.asList("USER", "ADMIN");

        String token = jwtService.generateAccessToken(username, roles);

        assertNotNull(token);

        Claims claims = jwtService.validateJwt(token);

        assertEquals(username, claims.getSubject());
        assertEquals(roles, claims.get("roles"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        String username = "bob";
        List<String> roles = List.of("USER");

        String token = jwtService.generateRefreshToken(username, roles);

        assertNotNull(token);

        Claims claims = jwtService.validateJwt(token);

        assertEquals(username, claims.getSubject());
        assertEquals(roles, claims.get("roles"));
    }

    @Test
    void testValidateJwt_InvalidToken_Throws() {
        String invalidToken = "invalid.jwt.token";

        Exception ex = assertThrows(Exception.class, () -> jwtService.validateJwt(invalidToken));
        assertNotNull(ex.getMessage());
    }
}