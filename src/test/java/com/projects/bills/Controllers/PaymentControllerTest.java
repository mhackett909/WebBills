package com.projects.bills.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.DTOs.PaymentDTOList;
import com.projects.bills.Services.JwtService;
import com.projects.bills.Services.PaymentService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "alice")
    void getPayments_success() throws Exception {
        PaymentDTOList list = new PaymentDTOList();
        Mockito.when(paymentService.getPayments(eq(1L), eq("alice"))).thenReturn(list);

        mockMvc.perform(get("/api/v1/payments")
                        .param("entryId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void getPayments_missingEntryId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/payments"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void getPaymentById_success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(1L);
        Mockito.when(paymentService.getPaymentById(eq(1L), eq("alice"))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void getPaymentById_missingId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/payments/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setType("CASH");
        dto.setMedium("BANK");
        dto.setRecycle(false);

        Mockito.when(paymentService.createPayment(any(PaymentDTO.class), eq("alice"))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_missingEntryId_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setType("CASH");
        dto.setMedium("BANK");
        dto.setRecycle(false);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_missingAmount_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setEntryId(1L);
        dto.setDate(LocalDate.now());
        dto.setType("CASH");
        dto.setMedium("BANK");
        dto.setRecycle(false);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_missingDate_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setType("CASH");
        dto.setMedium("BANK");
        dto.setRecycle(false);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_missingType_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setMedium("BANK");
        dto.setRecycle(false);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_blankType_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setType("   ");
        dto.setMedium("BANK");
        dto.setRecycle(false);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_missingMedium_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setType("CASH");
        dto.setRecycle(false);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void createPayment_blankMedium_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setType("CASH");
        dto.setMedium("   ");
        dto.setRecycle(false);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void updatePayment_success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(1L);
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setType("CASH");
        dto.setMedium("BANK");
        dto.setRecycle(false);

        Mockito.when(paymentService.updatePayment(any(PaymentDTO.class), any(), eq("alice"))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice")
    void updatePayment_missingPaymentId_returns400() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(0L);
        dto.setEntryId(1L);
        dto.setAmount(BigDecimal.TEN);
        dto.setDate(LocalDate.now());
        dto.setType("CASH");
        dto.setMedium("BANK");
        dto.setRecycle(false);

        mockMvc.perform(put("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void getPayments_entryIdNull_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/payments"))
                .andExpect(status().isBadRequest());
    }
}