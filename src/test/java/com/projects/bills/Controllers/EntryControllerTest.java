package com.projects.bills.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.Services.EntryService;
import com.projects.bills.Services.JwtService;
import com.projects.bills.Services.StatsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EntryController.class)
@AutoConfigureMockMvc(addFilters = false)
class EntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private EntryService entryService;

    @MockBean
    private StatsService statsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "alice")
    void getEntries_success() throws Exception {
        EntryDTOList entryDTOList = new EntryDTOList();
        Mockito.when(entryService.getEntries(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(entryDTOList);

        mockMvc.perform(get("/api/v1/entries"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void getEntryById_success() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(1L);
        Mockito.when(entryService.getEntryDtoById(eq(1L), any(), eq("alice")))
                .thenReturn(Optional.of(entryDTO));

        mockMvc.perform(get("/api/v1/entries/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void getEntryById_notFound_returns404() throws Exception {
        Mockito.when(entryService.getEntryDtoById(eq(99L), any(), eq("alice")))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/entries/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "alice")
    void addEntry_success() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setBillId(1L);
        entryDTO.setDate(LocalDate.now());
        entryDTO.setAmount(BigDecimal.TEN);
        entryDTO.setFlow("Income");
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        Mockito.when(entryService.saveEntry(any(EntryDTO.class), eq(false), isNull(), eq("alice")))
                .thenReturn(entryDTO);

        mockMvc.perform(post("/api/v1/entries/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "alice")
    void addEntry_missingBillId_returns400() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setDate(LocalDate.now());
        entryDTO.setAmount(BigDecimal.TEN);
        entryDTO.setFlow("IN");
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        mockMvc.perform(post("/api/v1/entries/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void addEntry_invalidFlow_returns400() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setBillId(1L);
        entryDTO.setDate(LocalDate.now());
        entryDTO.setAmount(BigDecimal.TEN);
        entryDTO.setFlow("INVALID");
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        mockMvc.perform(post("/api/v1/entries/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void editEntry_success() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(1L);
        entryDTO.setBillId(1L);
        entryDTO.setDate(LocalDate.now());
        entryDTO.setAmount(BigDecimal.TEN);
        entryDTO.setFlow("Income");
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        Mockito.when(entryService.saveEntry(any(EntryDTO.class), eq(true), any(), eq("alice")))
                .thenReturn(entryDTO);

        mockMvc.perform(put("/api/v1/entries/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void editEntry_missingEntryId_returns400() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(0L);
        entryDTO.setBillId(1L);
        entryDTO.setDate(LocalDate.now());
        entryDTO.setAmount(BigDecimal.TEN);
        entryDTO.setFlow("Income");
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        mockMvc.perform(put("/api/v1/entries/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void getStats_success() throws Exception {
        StatsDTO statsDTO = new StatsDTO();
        Mockito.when(statsService.getStats(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(statsDTO);

        mockMvc.perform(get("/api/v1/entries/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void addEntry_missingDate_returns400() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setBillId(1L);
        entryDTO.setAmount(BigDecimal.TEN);
        entryDTO.setFlow("Income");
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        mockMvc.perform(post("/api/v1/entries/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void addEntry_missingAmount_returns400() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setBillId(1L);
        entryDTO.setDate(LocalDate.now());
        entryDTO.setFlow("Income");
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        mockMvc.perform(post("/api/v1/entries/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void addEntry_missingFlow_returns400() throws Exception {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setBillId(1L);
        entryDTO.setDate(LocalDate.now());
        entryDTO.setAmount(BigDecimal.TEN);
        entryDTO.setStatus(true);
        entryDTO.setRecycle(false);

        mockMvc.perform(post("/api/v1/entries/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryDTO)))
                .andExpect(status().isBadRequest());
    }
}