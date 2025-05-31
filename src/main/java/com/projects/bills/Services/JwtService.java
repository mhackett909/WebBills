package com.projects.bills.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    public String generateAccessToken(String username, List<String> roles) {
        logger.info("Generating access token for user: {}", username);
        return generateJwt(username, roles, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String username, List<String> roles) {
        logger.info("Generating refresh token for user: {}", username);
        return generateJwt(username, roles, refreshTokenExpirationMs);
    }

    private String generateJwt(String username, List<String> roles, long expirationMs) {
        logger.debug("Generating JWT for user: {} with roles: {} and expiration: {}ms", username, roles, expirationMs);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims validateJwt(String token) {
        try {
            logger.info("Validating JWT token");
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.info("JWT validated successfully for user: {}", claims.getSubject());
            return claims;
        } catch (Exception e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }
}

