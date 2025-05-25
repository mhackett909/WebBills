package com.projects.bills.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.bills.DTOs.UserDTO;
import com.projects.bills.Services.JwtService;
import com.projects.bills.Services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetails userDetails(String username) {
        return new User(username, "password", Collections.emptyList());
    }

    @Test
    @WithMockUser(username = "alice")
    void getUser_success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("alice");
        Mockito.when(userService.findDtoByUsername("alice")).thenReturn(Optional.of(userDTO));

        mockMvc.perform(get("/api/v1/user")
                        .param("userName", "alice")
                        .principal(() -> "alice"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void getUser_missingUsername_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void getUser_forbidden_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/user")
                        .param("userName", "bob")
                        .principal(() -> "alice"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice")
    void getUser_notFound_returns404() throws Exception {
        Mockito.when(userService.findDtoByUsername("alice")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/user")
                        .param("userName", "alice")
                        .principal(() -> "alice"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "alice")
    void updateUser_success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("alice");
        Mockito.when(userService.updateUser(any(UserDTO.class), eq("alice"))).thenReturn(userDTO);

        mockMvc.perform(put("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void getUser_blankUsername_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/user")
                        .param("userName", "   "))
                .andExpect(status().isBadRequest());
    }
}