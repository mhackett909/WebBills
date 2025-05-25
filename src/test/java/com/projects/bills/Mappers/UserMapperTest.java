package com.projects.bills.Mappers;

import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Entities.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private final UserMapper mapper = new UserMapper();

    @Test
    void testMapToEntity() {
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedPassword";
        String roles = "ROLE_USER";

        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);

        User user = mapper.mapToEntity(dto, passwordHash);

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPassword());
        assertTrue(user.isEnabled());
        assertEquals(roles, user.getRoles());
        assertFalse(user.isMfaEnabled());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testMapToDTO() {
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String roles = "ROLE_USER";
        boolean enabled = true;
        boolean mfaEnabled = true;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime lastLogin = LocalDateTime.now().minusHours(1);
        LocalDateTime recycleDate = LocalDateTime.now().minusDays(1);

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setRoles(roles);
        user.setEnabled(enabled);
        user.setMfaEnabled(mfaEnabled);
        user.setCreatedAt(createdAt);
        user.setLastLogin(lastLogin);
        user.setRecycleDate(recycleDate);

        UserDTO dto = mapper.mapToDTO(user);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(roles, dto.getRoles());
        assertEquals(enabled, dto.getEnabled());
        assertEquals(mfaEnabled, dto.getMfaEnabled());
        assertTrue(dto.getRecycle());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(lastLogin, dto.getLastLogin());
    }
}