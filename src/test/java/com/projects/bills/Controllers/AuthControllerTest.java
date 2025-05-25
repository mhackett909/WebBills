package com.projects.bills.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.bills.DTOs.AuthDTO;
import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Services.JwtService;
import com.projects.bills.Services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bob");
        userDTO.setEmail("bob@example.com");
        userDTO.setPassword("password");

        Mockito.when(userService.registerUser(any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/v1/auth/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_missingUsername_returns400() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("bob@example.com");
        userDTO.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_missingEmail_returns400() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bob");
        userDTO.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_missingPassword_returns400() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bob");
        userDTO.setEmail("bob@example.com");

        mockMvc.perform(post("/api/v1/auth/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bob");
        userDTO.setPassword("password");

        AuthDTO authDTO = new AuthDTO("bob", "access", "refresh");
        Mockito.when(userService.login(any(UserDTO.class))).thenReturn(authDTO);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void login_missingUsername_returns400() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingPassword_returns400() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("bob");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_success() throws Exception {
        String refreshToken = "valid-refresh";
        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(claims.getSubject()).thenReturn("bob");
        Mockito.when(claims.get("roles")).thenReturn(java.util.List.of("USER"));

        Mockito.when(jwtService.validateJwt(eq(refreshToken))).thenReturn(claims);
        Mockito.when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn("new-access");
        Mockito.when(jwtService.generateRefreshToken(anyString(), anyList())).thenReturn("new-refresh");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", refreshToken))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_missingToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_expiredToken_returns403() throws Exception {
        String refreshToken = "expired";
        Mockito.when(jwtService.validateJwt(eq(refreshToken))).thenThrow(new ExpiredJwtException(null, null, "expired"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", refreshToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        String refreshToken = "invalid";
        Mockito.when(jwtService.validateJwt(eq(refreshToken))).thenThrow(new RuntimeException("invalid"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", refreshToken))
                .andExpect(status().isUnauthorized());
    }
}